import SwiftUI
import Shared

struct StatsScreen: View {
    // Inyectamos el repo
    let repository: BookRepository
    @StateObject private var viewModel: StatsViewModel
    
    init(repository: BookRepository) {
        self.repository = repository
        _viewModel = StateObject(wrappedValue: StatsViewModel(repository: repository))
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    
                    // 1. RETO ANUAL
                    ChallengeCard(read: viewModel.currentYearProgress, goal: viewModel.yearlyGoal)
                    
                    // 2. RESUMEN GENERAL (Grid de 2 columnas)
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        StatCard(title: "Libros Leídos", value: "\(viewModel.booksRead)", icon: "book.closed.fill", color: .blue)
                        StatCard(title: "Páginas", value: "\(viewModel.totalPages)", icon: "doc.text.fill", color: .orange)
                        StatCard(title: "Nota Media", value: "\(viewModel.averageRating)", icon: "star.fill", color: .yellow)
                        StatCard(title: "Velocidad", value: "\(viewModel.readingSpeed) días/libro", icon: "timer", color: .green)
                    }
                    
                    // 3. RÉCORDS
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Salón de la Fama")
                            .font(.title3)
                            .bold()
                        
                        HighlightRow(icon: "person.fill", title: "Autor Favorito", value: viewModel.topAuthor, subtitle: "\(viewModel.topAuthorCount) libros")
                        Divider()
                        HighlightRow(icon: "arrow.up.to.line", title: "El más largo", value: viewModel.longestBookTitle, subtitle: "\(viewModel.longestBookPages) págs")
                    }
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(12)
                }
                .padding()
            }
            .navigationTitle("Estadísticas")
            .onAppear {
                viewModel.loadStats() // Recargar al entrar
            }
        }
    }
}

// --- SUBVISTAS (Componentes reutilizables) ---

struct ChallengeCard: View {
    let read: Int
    let goal: Int
    
    var progress: Double {
        return min(Double(read) / Double(goal), 1.0)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Reto Anual")
                    .font(.headline)
                    .foregroundColor(.white)
                Spacer()
                Image(systemName: "trophy.fill")
                    .foregroundColor(.yellow)
                    .font(.title)
            }
            
            Text("Has leído \(read) de \(goal) libros")
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.9))
            
            // Barra de progreso
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .frame(width: geometry.size.width, height: 8)
                        .opacity(0.3)
                        .foregroundColor(.white)
                    
                    Rectangle()
                        .frame(width: min(CGFloat(self.progress)*geometry.size.width, geometry.size.width), height: 8)
                        .foregroundColor(.white)
                }
                .cornerRadius(4.0)
            }
            .frame(height: 8)
        }
        .padding()
        .background(Color.blue)
        .cornerRadius(12)
        .shadow(radius: 3)
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(color)
            
            VStack(alignment: .leading) {
                Text(value)
                    .font(.title2)
                    .bold()
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 2, x: 0, y: 1)
    }
}

struct HighlightRow: View {
    let icon: String
    let title: String
    let value: String
    let subtitle: String
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.purple)
                .frame(width: 40, height: 40)
                .background(Color.purple.opacity(0.1))
                .clipShape(Circle())
            
            VStack(alignment: .leading) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(value)
                    .font(.headline)
                if !subtitle.isEmpty {
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
    }
}
