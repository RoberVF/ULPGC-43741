import SwiftUI
import Shared

struct ContentView: View {
    // Recibimos el repositorio desde iOSApp
    let repository: BookRepository
    
    var body: some View {
        // En Android: NavigationBar
        // En iOS: TabView
        TabView {
            // Pestaña 1: Biblioteca
            LibraryScreen(repository: repository)
                .tabItem {
                    Label("Biblioteca", systemImage: "books.vertical")
                }
            
            // Pestaña 2: Buscar
            SearchScreen(repository: repository)
                .tabItem {
                    Label("Buscar", systemImage: "magnifyingglass")
                }
            
            // Pestaña 3: Estadísticas
            StatsScreen(repository: repository)
                .tabItem {
                    Label("Estadísticas", systemImage: "chart.bar.xaxis")
                }
            
            // Pestaña 4: Recomendaciones
            RecommendationsScreen()
                .tabItem {
                    Label("Para ti", systemImage: "star")
                }
        }
    }
}
