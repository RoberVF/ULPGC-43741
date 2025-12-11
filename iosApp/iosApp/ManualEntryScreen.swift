import SwiftUI
import Shared
import PhotosUI // Necesario para el selector de fotos

struct ManualEntryScreen: View {
    @ObservedObject var viewModel: SearchViewModel
    @Environment(\.presentationMode) var presentationMode
    
    // Estados del formulario
    @State private var title = ""
    @State private var author = ""
    @State private var pages = ""
    @State private var isbn = ""
    @State private var description = ""
    
    // Estados para la imagen
    @State private var selectedItem: PhotosPickerItem? = nil
    @State private var selectedImage: UIImage? = nil
    
    var body: some View {
        Form {
            // SECCIÓN 1: FOTO
            Section {
                HStack {
                    Spacer()
                    VStack {
                        if let image = selectedImage {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFit()
                                .frame(height: 200)
                                .cornerRadius(12)
                        } else {
                            Image(systemName: "photo.badge.plus")
                                .font(.system(size: 60))
                                .foregroundColor(.gray)
                                .padding()
                        }
                        
                        // Selector de Fotos
                        PhotosPicker(
                            selection: $selectedItem,
                            matching: .images,
                            photoLibrary: .shared()
                        ) {
                            Text(selectedImage == nil ? "Añadir Portada" : "Cambiar Foto")
                                .fontWeight(.bold)
                                .foregroundColor(.blue)
                        }
                        .onChange(of: selectedItem) { newItem in
                            Task {
                                // Convertimos la selección a UIImage
                                if let data = try? await newItem?.loadTransferable(type: Data.self),
                                   let uiImage = UIImage(data: data) {
                                    selectedImage = uiImage
                                }
                            }
                        }
                    }
                    Spacer()
                }
                .listRowBackground(Color.clear) // Fondo transparente para que destaque
            }
            
            // SECCIÓN 2: DATOS OBLIGATORIOS
            Section(header: Text("Datos Obligatorios")) {
                TextField("Título del libro *", text: $title)
            }
            
            // SECCIÓN 3: DATOS OPCIONALES
            Section(header: Text("Datos Opcionales")) {
                TextField("Autor", text: $author)
                TextField("Número de páginas", text: $pages)
                    .keyboardType(.numberPad)
                TextField("ISBN", text: $isbn)
                    .keyboardType(.numberPad)
                
                // Campo de texto grande para descripción
                ZStack(alignment: .topLeading) {
                    if description.isEmpty {
                        Text("Descripción / Sinopsis")
                            .foregroundColor(.gray.opacity(0.5))
                            .padding(.top, 8)
                    }
                    TextEditor(text: $description)
                        .frame(minHeight: 100)
                }
            }
        }
        .navigationTitle("Registro Manual")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button("Guardar") {
                    viewModel.saveManualBook(
                        title: title,
                        author: author,
                        pages: pages,
                        isbn: isbn,
                        description: description,
                        image: selectedImage
                    )
                    // Volver atrás
                    presentationMode.wrappedValue.dismiss()
                }
                .disabled(title.isEmpty) // Desactivado si no hay título
            }
        }
    }
}
