import Foundation
import Shared

@MainActor
class ShelfDetailViewModel: ObservableObject {
    
    private let repository: BookRepository
    let shelfId: Int64
    let shelfName: String // Guardamos el nombre para el título
    
    @Published var booksInShelf: [Book] = []
    @Published var availableBooks: [Book] = [] // Libros candidatos para añadir
    @Published var isLoading = false
    
    init(repository: BookRepository, shelfId: Int64, shelfName: String) {
        self.repository = repository
        self.shelfId = shelfId
        self.shelfName = shelfName
        loadBooks()
    }
    
    // Carga los libros que ya están dentro
    func loadBooks() {
        isLoading = true
        booksInShelf = repository.getBooksInShelf(shelfId: shelfId)
        isLoading = false
    }
    
    // Carga los libros que PODRÍAS añadir (Todos menos los que ya están)
    func loadAvailableBooks() {
        let allBooks = repository.getAllBooks()
        // Filtramos: Solo queremos los que NO estén ya en la lista booksInShelf
        // Usamos un Set de IDs para que la búsqueda sea rápida
        let existingIds = Set(booksInShelf.map { $0.gid })
        
        availableBooks = allBooks.filter { !existingIds.contains($0.gid) }
    }
    
    func addBook(_ book: Book) {
        repository.addBookToShelf(bookId: book.gid, shelfId: shelfId)
        loadBooks()          // Recargar la lista principal
        loadAvailableBooks() // Recargar la lista de candidatos (para quitar el que acabas de añadir)
    }
    
    func removeBook(at offsets: IndexSet) {
        for index in offsets {
            let book = booksInShelf[index]
            repository.removeBookFromShelf(bookGid: book.gid, shelfId: shelfId)
        }
        loadBooks()
    }
}
