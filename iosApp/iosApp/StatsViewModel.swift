import Foundation
import Shared

@MainActor
class StatsViewModel: ObservableObject {
    private let repository: BookRepository
    
    // --- VARIABLES PUBLICADAS (Estado de la UI) ---
    @Published var booksRead = 0
    @Published var totalPages = 0
    @Published var averageRating: Double = 0.0
    @Published var topAuthor: String = "-"
    @Published var topAuthorCount = 0
    
    @Published var longestBookTitle = "-"
    @Published var longestBookPages = 0
    
    @Published var readingSpeed = 0 // Días por libro
    @Published var yearlyGoal = 12  // Meta de libros al año
    @Published var currentYearProgress = 0
    
    init(repository: BookRepository) {
        self.repository = repository
        loadStats()
    }
    
    func loadStats() {
        let allBooks = repository.getAllBooks()
        
        // Filtrar libros terminados
        let completedBooks = allBooks.filter { $0.status == "COMPLETED" }
        
        // 1. Básicos
        booksRead = completedBooks.count
        
        // Sumar páginas
        // CORRECCIÓN: Convertimos KotlinLong? a Int de Swift antes de sumar
        totalPages = completedBooks.reduce(0) { sum, book in
            sum + (book.pageCount?.intValue ?? 0)
        }
        
        // 2. Nota Media
        let ratedBooks = completedBooks.filter { ($0.rating?.intValue ?? 0) > 0 }
        
        if !ratedBooks.isEmpty {
            let sumRating = ratedBooks.reduce(0.0) { $0 + Double($1.rating?.intValue ?? 0) }
            let avg = sumRating / Double(ratedBooks.count)
            // Redondear a 1 decimal
            averageRating = (avg * 10).rounded() / 10
        } else {
            averageRating = 0.0
        }
        
        // 3. Autor Favorito
        calculateTopAuthor(books: completedBooks)
        
        // 4. Libro más largo
        if let longest = completedBooks.max(by: { ($0.pageCount?.intValue ?? 0) < ($1.pageCount?.intValue ?? 0) }) {
            longestBookTitle = longest.title
            longestBookPages = Int(longest.pageCount?.intValue ?? 0)
        }
        
        // 5. Velocidad de Lectura
        calculateReadingSpeed(books: completedBooks)
        
        // 6. Progreso del Año
        calculateYearlyProgress(books: completedBooks)
    }
    
    private func calculateTopAuthor(books: [Book]) {
        var counts: [String: Int] = [:]
        
        for book in books {
            guard let authors = book.authors else { continue }
            // Separamos por comas si hay varios autores
            let list = authors.components(separatedBy: ", ")
            for author in list {
                counts[author, default: 0] += 1
            }
        }
        
        // Buscamos el mayor
        if let max = counts.max(by: { $0.value < $1.value }) {
            topAuthor = max.key
            topAuthorCount = max.value
        } else {
            topAuthor = "-"
            topAuthorCount = 0
        }
    }
    
    private func calculateReadingSpeed(books: [Book]) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        
        var totalDays = 0
        var count = 0
        
        for book in books {
            if let startStr = book.startDate, let endStr = book.endDate,
               let startDate = formatter.date(from: startStr),
               let endDate = formatter.date(from: endStr) {
                
                // Calculamos diferencia en días
                let diff = Calendar.current.dateComponents([.day], from: startDate, to: endDate).day ?? 0
                if diff >= 0 {
                    totalDays += diff
                    count += 1
                }
            }
        }
        
        if count > 0 {
            readingSpeed = max(1, totalDays / count)
        } else {
            readingSpeed = 0
        }
    }
    
    private func calculateYearlyProgress(books: [Book]) {
        let currentYear = Calendar.current.component(.year, from: Date())
        let yearString = String(currentYear)
        
        // Contamos cuántos libros tienen fecha de fin este año
        currentYearProgress = books.filter { $0.endDate?.starts(with: yearString) == true }.count
    }
}
