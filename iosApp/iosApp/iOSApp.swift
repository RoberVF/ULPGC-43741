import SwiftUI
import Shared // <- ¡Aquí está tu magia de Kotlin!

@main
struct iOSApp: App {
    
    // 1. Instanciamos el Repositorio UNA sola vez (Singleton)
    // Kotlin: val repository = BookRepository(DatabaseDriverFactory(context))
    // Swift:  let repository = BookRepository(driverFactory: DatabaseDriverFactory())
    let repository: BookRepository
    
    init() {
        let driverFactory = DatabaseDriverFactory()
        self.repository = BookRepository(driverFactory: driverFactory)
    }
    
    var body: some Scene {
        WindowGroup {
            // Pasamos el repositorio a la pantalla principal
            ContentView(repository: repository)
        }
    }
}
