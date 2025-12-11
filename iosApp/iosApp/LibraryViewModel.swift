import Foundation
import Shared
import SwiftUI

// Estructura auxiliar para usar en la lista de SwiftUI
struct ShelfUIItem: Identifiable {
    var id: Int64 { category.id }
    let category: Shared.Category
    let count: Int64
}

@MainActor
class LibraryViewModel: ObservableObject {
    
    private let repository: BookRepository
    
    // Guardamos TODOS los libros en memoria
    private var allBooks: [Book] = []
    
    // --- ESTADO DE LA UI ---
    // Esta es la lista que verá la pantalla (filtrada)
    @Published var books: [Book] = []
    @Published var shelves: [ShelfUIItem] = []
    @Published var isLoading = false
    
    // --- FILTROS ---
    @Published var searchQuery = "" {
        didSet { applyFilters() } // Al escribir, se recalcula solo
    }
    @Published var statusFilter: String? = nil {
        didSet { applyFilters() } // Al cambiar filtro, se recalcula
    }
    
    init(repository: BookRepository) {
        self.repository = repository
        loadData()
    }
    
    func loadData() {
        isLoading = true
        
        // 1. Cargar TODO de la base de datos
        self.allBooks = repository.getAllBooks()
        
        // 2. Cargar Estanterías
        let categories = repository.getAllShelves()
        var tempShelves: [ShelfUIItem] = []
        for cat in categories {
            let count = repository.countBooksInShelf(shelfId: cat.id)
            tempShelves.append(ShelfUIItem(category: cat, count: count))
        }
        self.shelves = tempShelves
        
        // 3. Aplicar filtros iniciales
        applyFilters()
        
        isLoading = false
    }
    
    private func applyFilters() {
        var result = allBooks
        
        // Filtro 1: Estado (Si hay uno seleccionado)
        if let status = statusFilter {
            result = result.filter { $0.status == status }
        }
        
        // Filtro 2: Texto (Título o Autor)
        if !searchQuery.isEmpty {
            result = result.filter { book in
                let titleMatch = book.title.localizedCaseInsensitiveContains(searchQuery)
                let authorMatch = book.authors?.localizedCaseInsensitiveContains(searchQuery) ?? false
                return titleMatch || authorMatch
            }
        }
        
        // Actualizamos la lista visible
        self.books = result
    }
    
    func deleteBook(at offsets: IndexSet) {
        // Cuidado: offsets se refiere a la lista FILTRADA 'books', no a 'allBooks'
        for index in offsets {
            let book = books[index]
            repository.deleteBookById(id: book.gid)
        }
        loadData() // Recargamos todo
    }
    
    // --- ACCIONES DE ESTANTERÍA ---
    func createShelf(name: String, description: String, colorHex: Int64) {
        repository.createShelf(name: name, description: description, color: colorHex)
        loadData()
    }
    
    func deleteShelf(id: Int64) {
        repository.deleteShelf(id: id)
        loadData()
    }
}
