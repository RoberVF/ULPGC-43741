import Foundation
import Shared

@MainActor
class BookDetailViewModel: ObservableObject {
    private let repository: BookRepository
    
    @Published var book: BookItem
    // Para mostrar/ocultar el diálogo de puntuación
    @Published var showRatingDialog = false
    @Published var rating: Double = 5.0
    
    init(repository: BookRepository, book: BookItem) {
        self.repository = repository
        self.book = book
    }
    
    // --- ACCIONES DE ESTADO ---
    
    func startReading() {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let today = formatter.string(from: Date())
        
        repository.updateBookStatus(id: book.id, status: "IN_PROGRESS", startDate: today, endDate: nil)
        
        refreshLocalBook(status: "IN_PROGRESS", startDate: today, endDate: nil, rating: nil)
    }
    
    func finishReading() {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let today = formatter.string(from: Date())
        
        let startDate = book.volumeInfo.myStartDate
        let finalRating = Int64(rating)
        
        repository.updateBookStatus(id: book.id, status: "COMPLETED", startDate: startDate, endDate: today)
        repository.updateBookRating(id: book.id, rating: finalRating)
        
        refreshLocalBook(status: "COMPLETED", startDate: startDate, endDate: today, rating: Int32(finalRating))
    }
    
    func deleteBook() {
        repository.deleteBookById(id: book.id)
    }
    
    // --- LÓGICA DE GUARDAR ---
    func saveBook() {
        let vol = book.volumeInfo
        let authorsString = vol.authors?.joined(separator: ", ")
        
        var thumb = vol.imageLinks?.thumbnail
        if let t = thumb, t.hasPrefix("http:") {
            thumb = t.replacingOccurrences(of: "http:", with: "https:")
        }
        
        let isbn10 = vol.industryIdentifiers?.first(where: { $0.type == "ISBN_10" })?.identifier
        let isbn13 = vol.industryIdentifiers?.first(where: { $0.type == "ISBN_13" })?.identifier
        
        let newBook = Book(
            gid: book.id,
            title: vol.title,
            subtitle: vol.subtitle,
            authors: authorsString,

            description: vol.description_,
            
            pageCount: toKotlinLong(vol.pageCount),
            thumbnailUrl: thumb,
            isbn10: isbn10,
            isbn13: isbn13,
            status: "PENDING",
            startDate: nil,
            endDate: nil,
            rating: nil,
            notes: nil
        )
        repository.insertBook(book: newBook)
        
        refreshLocalBook(status: "PENDING", startDate: nil, endDate: nil, rating: nil)
    }
    
    // Función auxiliar para refrescar UI
    private func refreshLocalBook(status: String, startDate: String?, endDate: String?, rating: Int32?) {
        var newInfo = book.volumeInfo
        newInfo = newInfo.doCopy(
            title: newInfo.title,
            subtitle: newInfo.subtitle,
            authors: newInfo.authors,
            description: newInfo.description_,
            pageCount: newInfo.pageCount,
            imageLinks: newInfo.imageLinks,
            industryIdentifiers: newInfo.industryIdentifiers,
            myStatus: status,
            myRating: rating != nil ? KotlinInt(int: rating!) : newInfo.myRating,
            myStartDate: startDate,
            myEndDate: endDate
        )
        self.book = BookItem(id: book.id, volumeInfo: newInfo)
    }
    
    private func toKotlinLong(_ number: KotlinInt?) -> KotlinLong? {
        guard let n = number else { return nil }
        return KotlinLong(value: Int64(n.intValue))
    }
}
