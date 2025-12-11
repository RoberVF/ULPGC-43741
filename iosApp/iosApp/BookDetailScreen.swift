import SwiftUI
import Shared

struct BookDetailScreen: View {
    @StateObject private var viewModel: BookDetailViewModel
    @Environment(\.presentationMode) var presentationMode // Para poder volver atrás
    
    // Necesitamos saber si venimos de la biblioteca para mostrar el botón de borrar
    let isLibraryMode: Bool

    init(repository: BookRepository, book: BookItem, isLibraryMode: Bool) {
        self.isLibraryMode = isLibraryMode
        _viewModel = StateObject(wrappedValue: BookDetailViewModel(repository: repository, book: book))
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView {
                VStack(spacing: 16) {
                    // 1. Portada Grande
                    if let urlString = viewModel.book.volumeInfo.imageLinks?.thumbnail?.replacingOccurrences(of: "http:", with: "https:"),
                       let url = URL(string: urlString) {
                        AsyncImage(url: url) { image in
                            image.resizable()
                                .aspectRatio(contentMode: .fit)
                        } placeholder: {
                            Color.gray.opacity(0.3)
                        }
                        .frame(height: 250)
                        .cornerRadius(8)
                        .shadow(radius: 5)
                    }
                    
                    // 2. Título y Autor
                    VStack(spacing: 4) {
                        Text(viewModel.book.volumeInfo.title)
                            .font(.title2)
                            .bold()
                            .multilineTextAlignment(.center)
                        
                        if let authors = viewModel.book.volumeInfo.authors {
                            Text(authors.joined(separator: ", "))
                                .font(.headline)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    // 3. Estado (Si es de mi biblioteca)
                    if let status = viewModel.book.volumeInfo.myStatus {
                        StatusInfoView(status: status,
                                       start: viewModel.book.volumeInfo.myStartDate,
                                       end: viewModel.book.volumeInfo.myEndDate,
                                       rating: viewModel.book.volumeInfo.myRating)
                    }
                    
                    Divider()
                    
                    // 4. Sinopsis
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Sinopsis")
                            .font(.headline)
                        Text(viewModel.book.volumeInfo.description_ ?? "Sin descripción disponible.")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    
                    // Espacio para que el botón no tape texto
                    Spacer().frame(height: 80)
                }
                .padding()
            }
            
            // 5. Botón de Acción Flotante (FAB)
            VStack {
                Spacer()
                ActionButton(status: viewModel.book.volumeInfo.myStatus, isLibraryMode: isLibraryMode) {
                    handleAction()
                }
                .padding()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            // Botón de borrar (solo si es mi libro)
            if isLibraryMode {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.deleteBook()
                        presentationMode.wrappedValue.dismiss() // Volver atrás
                    }) {
                        Image(systemName: "trash")
                            .foregroundColor(.red)
                    }
                }
            }
        }
        // Alerta para poner nota
        .sheet(isPresented: $viewModel.showRatingDialog) {
            RatingSheet(
                rating: $viewModel.rating,
                onSave: {
                    viewModel.finishReading()
                    viewModel.showRatingDialog = false // Cerrar al guardar
                },
                onCancel: {
                    viewModel.showRatingDialog = false // Cerrar al cancelar
                }
            )
        }
    }
    
    func handleAction() {
        let status = viewModel.book.volumeInfo.myStatus
        
        if !isLibraryMode && status == nil {
            viewModel.saveBook() // Guardar desde búsqueda
        } else if status == "PENDING" {
            viewModel.startReading()
        } else if status == "IN_PROGRESS" {
            viewModel.showRatingDialog = true
        }
    }
}

// Subcomponente: Botón de Acción
struct ActionButton: View {
    let status: String?
    let isLibraryMode: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                Image(systemName: iconName)
                Text(label)
                    .fontWeight(.bold)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(backgroundColor)
            .foregroundColor(.white)
            .cornerRadius(12)
            .shadow(radius: 3)
        }
    }
    
    var iconName: String {
        if !isLibraryMode && status == nil { return "plus" }
        switch status {
        case "PENDING": return "book"
        case "IN_PROGRESS": return "checkmark"
        default: return ""
        }
    }
    
    var label: String {
        if !isLibraryMode && status == nil { return "GUARDAR EN BIBLIOTECA" }
        switch status {
        case "PENDING": return "EMPEZAR LECTURA"
        case "IN_PROGRESS": return "TERMINAR LIBRO"
        case "COMPLETED": return "LIBRO TERMINADO"
        default: return "GUARDAR"
        }
    }
    
    var backgroundColor: Color {
        if status == "COMPLETED" { return .gray }
        if status == "IN_PROGRESS" { return .orange }
        return .blue
    }
}

// Subcomponente: Información de estado
struct StatusInfoView: View {
    let status: String
    let start: String?
    let end: String?
    let rating: KotlinInt?
    
    var body: some View {
        VStack(spacing: 4) {
            Text(statusLabel)
                .font(.caption)
                .padding(6)
                .background(Color.gray.opacity(0.1))
                .cornerRadius(4)
            
            if let s = start {
                Text("Empezado: \(s)").font(.caption2).foregroundColor(.secondary)
            }
            if let e = end {
                Text("Terminado: \(e)").font(.caption2).foregroundColor(.secondary)
            }
            if let r = rating {
                HStack(spacing: 2) {
                    Text("Nota: \(r.intValue)/10")
                    Image(systemName: "star.fill").foregroundColor(.yellow)
                }
                .font(.headline)
            }
        }
    }
    
    var statusLabel: String {
        switch status {
        case "IN_PROGRESS": return "LEYENDO ACTUALMENTE"
        case "COMPLETED": return "LEÍDO"
        default: return "PENDIENTE"
        }
    }
}

// --- SUBVISTA: HOJA DE PUNTUACIÓN ---
struct RatingSheet: View {
    @Binding var rating: Double
    let onSave: () -> Void
    let onCancel: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            // Título
            Text("¡Libro Terminado!")
                .font(.title3)
                .bold()
                .padding(.top)

            Text("¿Qué nota le pones?")
                .foregroundColor(.secondary)

            // Número Grande (La nota actual)
            Text("\(Int(rating))")
                .font(.system(size: 60, weight: .heavy))
                .foregroundColor(.blue)
                .contentTransition(.numericText()) // Animación chula al cambiar número

            // El Slider (Barra deslizante)
            Slider(value: $rating, in: 0...10, step: 1) {
                Text("Nota")
            } minimumValueLabel: {
                Text("0").font(.caption).foregroundColor(.gray)
            } maximumValueLabel: {
                Text("10").font(.caption).foregroundColor(.gray)
            }
            .padding(.horizontal)

            // Botones
            HStack(spacing: 20) {
                Button("Cancelar") {
                    onCancel()
                }
                .foregroundColor(.red)
                .frame(maxWidth: .infinity)

                Button("GUARDAR") {
                    onSave()
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
            }
            .padding(.bottom)
        }
        .padding()
        // Esto hace que la hoja ocupe solo el espacio necesario (iOS 16+)
        .presentationDetents([.height(350)])
        .presentationDragIndicator(.visible)
    }
}
