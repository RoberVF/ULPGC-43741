import Foundation
import Shared
import UIKit

@MainActor
class SearchViewModel: ObservableObject {
    private let repository: BookRepository
    
    @Published var query = ""
    @Published var results: [BookItem] = []
    @Published var isLoading = false
    @Published var errorMessage: String? = nil
    
    init(repository: BookRepository) {
        self.repository = repository
    }
    
    func search() {
        guard !query.isEmpty else { return }
        
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                let foundBooks = try await repository.searchRemoteBooks(query: query)
                self.results = foundBooks
            } catch {
                self.errorMessage = "Error: \(error.localizedDescription)"
            }
            self.isLoading = false
        }
    }
    
    func saveBook(_ bookItem: BookItem) {
        let vol = bookItem.volumeInfo
        
        // 1. Autores
        let authorsString = vol.authors?.joined(separator: ", ")
        
        // 2. Imagen HTTPS
        var thumb = vol.imageLinks?.thumbnail
        if let t = thumb, t.hasPrefix("http:") {
            thumb = t.replacingOccurrences(of: "http:", with: "https:")
        }
        
        // 3. ISBNs
        let isbn10 = vol.industryIdentifiers?.first(where: { $0.type == "ISBN_10" })?.identifier
        let isbn13 = vol.industryIdentifiers?.first(where: { $0.type == "ISBN_13" })?.identifier
        
        // 4. Crear Libro
        let newBook = Book(
            gid: bookItem.id,
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
    }
    
    // --- FUNCIÓN AUXILIAR MÁGICA ---
    // Convierte un Int de Swift (Int32) a un Long de Kotlin (KotlinLong)
    private func toKotlinLong(_ number: KotlinInt?) -> KotlinLong? {
            guard let n = number else { return nil }
            // Extraemos el valor entero (.intValue) y lo convertimos a Int64
            return KotlinLong(value: Int64(n.intValue))
        }
    

    // --- REGISTRO MANUAL ---
    
    func saveManualBook(title: String, author: String, pages: String, isbn: String, description: String, image: UIImage?) {
        // 1. Generar ID único basado en la fecha actual
        let id = "manual_\(Int(Date().timeIntervalSince1970))"
        
        // 2. Guardar imagen en disco (si hay) y obtener su ruta
        var localImagePath: String? = nil
        if let img = image {
            localImagePath = saveImageToDocuments(img, fileName: id)
        }
        
        // 3. Crear el libro
        // Si el usuario no pone descripción, ponemos una por defecto
        let finalDesc = description.isEmpty ? "Registrado manualmente" : description
        
        let newBook = Book(
            gid: id,
            title: title,
            subtitle: nil,
            authors: author.isEmpty ? nil : author,
            description: finalDesc,
            pageCount: KotlinLong(value: Int64(pages) ?? 0),
            thumbnailUrl: localImagePath, // Guardamos la ruta del archivo local
            isbn10: nil,
            isbn13: isbn.isEmpty ? nil : isbn,
            status: "PENDING",
            startDate: nil,
            endDate: nil,
            rating: nil,
            notes: nil
        )
        
        repository.insertBook(book: newBook)
    }
    
    // Función auxiliar para guardar la foto en la carpeta de Documentos de la app
    private func saveImageToDocuments(_ image: UIImage, fileName: String) -> String? {
        // Convertimos la imagen a JPG
        guard let data = image.jpegData(compressionQuality: 0.8) else { return nil }
        
        // Buscamos la ruta de documentos
        let paths = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let filename = paths[0].appendingPathComponent("\(fileName).jpg")
        
        do {
            try data.write(to: filename)
            return filename.absoluteString // Devolvemos la ruta "file://..."
        } catch {
            print("Error guardando imagen: \(error)")
            return nil
        }
    }
}
