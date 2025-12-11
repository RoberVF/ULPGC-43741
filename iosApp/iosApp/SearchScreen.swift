import SwiftUI
import Shared

struct SearchScreen: View {
    let repository: BookRepository
    @StateObject private var viewModel: SearchViewModel
    
    // Inyección del repositorio
    init(repository: BookRepository) {
        self.repository = repository
        _viewModel = StateObject(wrappedValue: SearchViewModel(repository: repository))
    }
    
    var body: some View {
        NavigationView {
            VStack {
                // --- BARRA DE BÚSQUEDA ---
                HStack {
                    TextField("Título, autor, ISBN...", text: $viewModel.query)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .onSubmit {
                            viewModel.search() // Buscar al pulsar Intro
                        }
                    
                    Button(action: {
                        viewModel.search() // Buscar al pulsar la lupa
                    }) {
                        if viewModel.isLoading {
                            ProgressView()
                        } else {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.blue)
                        }
                    }
                }
                .padding()
                
                // --- MENSAJE DE ERROR ---
                if let error = viewModel.errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                        .padding(.horizontal)
                }
                
                // --- LISTA DE RESULTADOS ---
                List(viewModel.results, id: \.id) { item in
                    ZStack {
                        NavigationLink(destination: BookDetailScreen(repository: repository, book: item, isLibraryMode: false)) {
                            EmptyView()
                        }
                        .opacity(0)
                        
                        SearchResultRow(item: item, onSave: {
                            viewModel.saveBook(item)
                        })
                    }
                }
                .listStyle(.plain)
            }
            .navigationTitle("Buscar")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink(destination: ManualEntryScreen(viewModel: viewModel)) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
    }
}

// Fila individual de resultado
struct SearchResultRow: View {
    let item: BookItem
    let onSave: () -> Void
    @State private var isSaved = false // Para cambiar el icono al guardar
    
    var body: some View {
        HStack {
            // Portada
            let urlString = item.volumeInfo.imageLinks?.thumbnail?.replacingOccurrences(of: "http:", with: "https:")
            
            if let urlString = urlString, let url = URL(string: urlString) {
                AsyncImage(url: url) { image in
                    image.resizable()
                } placeholder: {
                    Color.gray.opacity(0.3)
                }
                .frame(width: 50, height: 75)
                .cornerRadius(4)
            } else {
                Rectangle()
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 50, height: 75)
                    .cornerRadius(4)
            }
            
            // Textos
            VStack(alignment: .leading, spacing: 4) {
                Text(item.volumeInfo.title)
                    .font(.headline)
                    .lineLimit(2)
                
                if let authors = item.volumeInfo.authors {
                    Text(authors.joined(separator: ", "))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
            }
            
            Spacer()
            
            // Botón Guardar
            Button(action: {
                onSave()
                withAnimation { isSaved = true }
            }) {
                Image(systemName: isSaved ? "checkmark.circle.fill" : "plus.circle")
                    .font(.title2)
                    .foregroundColor(isSaved ? .green : .blue)
            }
            .buttonStyle(BorderlessButtonStyle()) // Importante para que el click no seleccione toda la fila
        }
        .padding(.vertical, 4)
    }
}
