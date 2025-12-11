import SwiftUI
import Shared

struct ShelfDetailScreen: View {
    let repository: BookRepository
    @StateObject private var viewModel: ShelfDetailViewModel
    
    @State private var showAddBookSheet = false
    
    init(repository: BookRepository, shelfId: Int64, shelfName: String) {
        self.repository = repository
        _viewModel = StateObject(wrappedValue: ShelfDetailViewModel(repository: repository, shelfId: shelfId, shelfName: shelfName))
    }
    
    var body: some View {
        VStack {
            if viewModel.booksInShelf.isEmpty {
                VStack(spacing: 16) {
                    Spacer()
                    Image(systemName: "books.vertical.fill")
                        .font(.system(size: 60))
                        .foregroundColor(.gray.opacity(0.5))
                    Text("Esta estantería está vacía")
                        .font(.title3)
                        .foregroundColor(.gray)
                    Button("Añadir libros") {
                        viewModel.loadAvailableBooks()
                        showAddBookSheet = true
                    }
                    .buttonStyle(.borderedProminent)
                    Spacer()
                }
            } else {
                List {
                    ForEach(viewModel.booksInShelf, id: \.gid) { book in
                        // Reutilizamos la fila que ya creamos antes
                        NavigationLink(destination: BookDetailScreen(repository: repository, book: book.toBookItem(), isLibraryMode: true)) {
                            BookRow(book: book)
                        }
                    }
                    .onDelete(perform: viewModel.removeBook)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle(viewModel.shelfName)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewModel.loadAvailableBooks()
                    showAddBookSheet = true
                }) {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showAddBookSheet) {
            AddBookToShelfView(availableBooks: viewModel.availableBooks) { book in
                viewModel.addBook(book)
                // Opcional: Cerrar sheet al añadir uno, o dejarlo abierto para añadir más.
                // showAddBookSheet = false
            }
        }
    }
}

// --- SUBVISTA: LISTA PARA ELEGIR LIBROS ---
struct AddBookToShelfView: View {
    let availableBooks: [Book]
    let onSelect: (Book) -> Void
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            Group {
                if availableBooks.isEmpty {
                    Text("No tienes más libros para añadir.")
                        .foregroundColor(.secondary)
                } else {
                    List(availableBooks, id: \.gid) { book in
                        Button(action: { onSelect(book) }) {
                            HStack {
                                BookRow(book: book) // Reutilizamos visualización
                                Spacer()
                                Image(systemName: "plus.circle")
                                    .foregroundColor(.blue)
                            }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Añadir Libro")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cerrar") { presentationMode.wrappedValue.dismiss() }
                }
            }
        }
    }
}
