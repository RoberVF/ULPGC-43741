import SwiftUI
import Shared

struct LibraryScreen: View {
    let repository: BookRepository
    @StateObject private var viewModel: LibraryViewModel
    
    // Control de pestañas interno
    @State private var selectedTab = 0
    @State private var showCreateShelfSheet = false
    
    init(repository: BookRepository) {
        self.repository = repository
        _viewModel = StateObject(wrappedValue: LibraryViewModel(repository: repository))
    }
    
    var body: some View {
        NavigationView {
            VStack {
                // 1. Selector Superior (Mis Libros / Estanterías)
                Picker("Sección", selection: $selectedTab) {
                    Text("Mis Libros").tag(0)
                    Text("Estanterías").tag(1)
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                
                // 2. Contenido según la pestaña elegida
                if selectedTab == 0 {
                    BooksListView(viewModel: viewModel, repository: repository)
                } else {
                    ShelvesGridView(viewModel: viewModel, repository: repository)
                }
            }
            .navigationTitle("Mi Biblioteca")
            .toolbar {
                // Botón + solo visible en la pestaña Estanterías
                if selectedTab == 1 {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action: { showCreateShelfSheet = true }) {
                            Image(systemName: "folder.badge.plus")
                        }
                    }
                }
            }
            .sheet(isPresented: $showCreateShelfSheet) {
                CreateShelfView { name, desc, color in
                    viewModel.createShelf(name: name, description: desc, colorHex: color)
                    showCreateShelfSheet = false
                }
            }
            .onAppear {
                viewModel.loadData()
            }
        }
    }
}

// --- SUBVISTA: GRID DE ESTANTERÍAS ---
struct ShelvesGridView: View {
    @ObservedObject var viewModel: LibraryViewModel
    let repository: BookRepository
    
    // Configuración del Grid (2 columnas)
    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
    var body: some View {
        if viewModel.shelves.isEmpty {
            VStack {
                Spacer()
                Text("No tienes estanterías")
                    .font(.headline)
                    .foregroundColor(.gray)
                Text("Dale al + para crear una")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                Spacer()
            }
        } else {
            ScrollView {
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(viewModel.shelves) { item in
                        NavigationLink(destination: ShelfDetailScreen(
                            repository: repository,
                            shelfId: item.category.id,
                            shelfName: item.category.name
                        )) {
                            ShelfCard(item: item)
                                .contextMenu {
                                    Button(role: .destructive) {
                                        viewModel.deleteShelf(id: item.id)
                                    } label: {
                                        Label("Eliminar", systemImage: "trash")
                                    }
                                }
                        }
                        // Importante: Para que el color del texto no se ponga azul por ser un enlace
                        .buttonStyle(PlainButtonStyle())
                    }
                }
                .padding()
            }
        }
    }
}

// --- SUBVISTA: LISTA DE LIBROS CON FILTROS ---
struct BooksListView: View {
    @ObservedObject var viewModel: LibraryViewModel
    let repository: BookRepository
    
    var body: some View {
        VStack {
            // Fila de Filtros (Chips)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 10) {
                    FilterChip(title: "Todos", isSelected: viewModel.statusFilter == nil) {
                        viewModel.statusFilter = nil
                    }
                    FilterChip(title: "Pendientes", isSelected: viewModel.statusFilter == "PENDING") {
                        viewModel.statusFilter = "PENDING"
                    }
                    FilterChip(title: "Leyendo", isSelected: viewModel.statusFilter == "IN_PROGRESS") {
                        viewModel.statusFilter = "IN_PROGRESS"
                    }
                    FilterChip(title: "Terminados", isSelected: viewModel.statusFilter == "COMPLETED") {
                        viewModel.statusFilter = "COMPLETED"
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
            
            // Lista o Mensaje de Vacío
            if viewModel.books.isEmpty {
                if !viewModel.searchQuery.isEmpty || viewModel.statusFilter != nil {
                    // Si hay filtros pero no resultados
                    VStack {
                        Spacer()
                        Image(systemName: "magnifyingglass")
                            .font(.largeTitle)
                            .foregroundColor(.gray)
                        Text("No se encontraron libros")
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    // Si la biblioteca está realmente vacía
                    EmptyLibraryView()
                }
            } else {
                List {
                    ForEach(viewModel.books, id: \.gid) { book in
                        NavigationLink(destination: BookDetailScreen(repository: repository, book: book.toBookItem(), isLibraryMode: true)) {
                            BookRow(book: book)
                        }
                    }
                    .onDelete(perform: viewModel.deleteBook)
                }
                .listStyle(.plain)
            }
        }
        // Buscador Nativo de iOS
        .searchable(text: $viewModel.searchQuery, placement: .navigationBarDrawer(displayMode: .always), prompt: "Buscar título o autor")
    }
}

// Componente visual para el Chip de filtro
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.footnote)
                .fontWeight(.medium)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color.blue : Color.gray.opacity(0.1))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.gray.opacity(0.2), lineWidth: isSelected ? 0 : 1)
                )
        }
    }
}

// Tarjeta de Estantería
struct ShelfCard: View {
    let item: ShelfUIItem
    
    var body: some View {
        VStack {
            Image(systemName: "folder.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 50, height: 50)
                // Convertimos el Int64 a Color de SwiftUI
                .foregroundColor(Color(hex: item.category.colorHex))
            
            Text(item.category.name)
                .font(.headline)
                .lineLimit(1)
            
            Text("\(item.count) libros")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(hex: item.category.colorHex).opacity(0.1))
        .cornerRadius(12)
    }
}

// Pantalla para crear estantería (Sheet)
struct CreateShelfView: View {
    var onSave: (String, String, Int64) -> Void
    
    @State private var name = ""
    @State private var description = ""
    @State private var selectedColor = Color.blue
    @Environment(\.presentationMode) var presentationMode
    
    // Lista de colores para elegir
    let colors: [Color] = [.blue, .red, .green, .orange, .purple, .pink, .teal, .indigo]
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Información")) {
                    TextField("Nombre de la estantería", text: $name)
                    TextField("Descripción (Opcional)", text: $description)
                }
                
                Section(header: Text("Color")) {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 15) {
                            ForEach(colors, id: \.self) { color in
                                Circle()
                                    .fill(color)
                                    .frame(width: 30, height: 30)
                                    .overlay(
                                        Circle()
                                            .stroke(Color.primary, lineWidth: selectedColor == color ? 3 : 0)
                                    )
                                    .onTapGesture {
                                        selectedColor = color
                                    }
                            }
                        }
                        .padding(.vertical, 8)
                    }
                }
            }
            .navigationTitle("Nueva Estantería")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { presentationMode.wrappedValue.dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Crear") {
                        // Convertimos el Color de SwiftUI a Int64 para guardar en la BD
                        onSave(name, description, selectedColor.toInt64())
                    }
                    .disabled(name.isEmpty)
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES DE LIBRO ---

struct BookRow: View {
    let book: Book
    
    var body: some View {
        HStack {
            // Portada
            if let urlString = book.thumbnailUrl, let url = URL(string: urlString) {
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
                Text(book.title)
                    .font(.headline)
                    .lineLimit(2)
                
                if let authors = book.authors {
                    Text(authors)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                
                // Etiqueta de estado
                StatusBadge(status: book.status)
            }
        }
        .padding(.vertical, 4)
    }
}

struct StatusBadge: View {
    let status: String
    
    var body: some View {
        Text(statusLabel)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 2)
            .background(statusColor.opacity(0.2))
            .foregroundColor(statusColor)
            .cornerRadius(4)
    }
    
    var statusLabel: String {
        switch status {
        case "IN_PROGRESS": return "Leyendo"
        case "COMPLETED": return "Terminado"
        default: return "Pendiente"
        }
    }
    
    var statusColor: Color {
        switch status {
        case "IN_PROGRESS": return .orange
        case "COMPLETED": return .green
        default: return .blue
        }
    }
}

// Estado Vacío
struct EmptyLibraryView: View {
    var body: some View {
        VStack {
            Spacer()
            Image(systemName: "books.vertical")
                .font(.system(size: 50))
                .foregroundColor(.gray)
            Text("Tu biblioteca está vacía")
                .font(.headline)
                .padding(.top)
            Spacer()
        }
    }
}

// Extensiones útiles para colores
extension Color {
    // Convierte Color SwiftUI a Int64 (ARGB)
    func toInt64() -> Int64 {
        // Obtenemos los componentes (R, G, B, A)
        // Nota: Esto es una simplificación. En una app real usaríamos UIColor/NSColor.
        // Aquí usamos un hash simple o una conversión manual para los colores predefinidos.
        // Para simplificar al máximo y que funcione con la lista 'colors':
        switch self {
        case .blue: return 0xFF0000FF
        case .red: return 0xFFFF0000
        case .green: return 0xFF00FF00
        case .orange: return 0xFFFFA500
        case .purple: return 0xFF800080
        case .pink: return 0xFFFFC0CB
        case .teal: return 0xFF008080
        case .indigo: return 0xFF4B0082
        default: return 0xFF0000FF // Azul por defecto
        }
    }
    
    // Inicializa Color desde Int64 (Hex)
    init(hex: Int64) {
        let alpha = Double((hex >> 24) & 0xFF) / 255.0
        let red = Double((hex >> 16) & 0xFF) / 255.0
        let green = Double((hex >> 8) & 0xFF) / 255.0
        let blue = Double(hex & 0xFF) / 255.0
        // Como nuestros Int64 a veces pierden el canal Alpha al guardarse como enteros con signo,
        // asumimos opacidad 1 si sale muy baja, o usamos colores fijos.
        self.init(.sRGB, red: red, green: green, blue: blue, opacity: 1.0)
    }
}
