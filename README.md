# GoodBooks: Informe de Progreso Técnico (KMP)

Este documento detalla la arquitectura, configuración y desarrollo del proyecto GoodBooks.

## Arquitectura del Proyecto

El proyecto sigue una arquitectura Local-First. Toda la lógica de negocio, almacenamiento de datos y conexión a redes reside en el módulo shared, el cual se compila en una librería Android (.aar) y un Framework de iOS (.framework).

### Tecnologías Clave Utilizadas:

Lenguaje: Kotlin (versión 1.9.x / 2.0).

Base de Datos: SQLDelight (2.0.2).

Red (Networking): Ktor Client + Kotlinx.Serialization.

Gestión de Dependencias: Gradle Version Catalogs (libs.versions.toml).

### Configuración de Entorno (Gradle)

Uno de los mayores retos fue la configuración del sistema de construcción (Build System) para soportar las nuevas versiones de SQLDelight en un entorno multiplataforma.

Problema Resuelto: La configuración de los directorios fuente en build.gradle.kts para SQLDelight 2.0 requería una sintaxis específica (`srcDirs.setFrom`) para evitar errores de inmutabilidad en Gradle.

**Archivo: shared/build.gradle.kts**
```
sqldelight {
    databases {
        create("GoodBooksDatabase") {
        packageName.set("com.roberto.goodbooks.db")
        srcDirs.setFrom("sqldelight")
        }
    }
}
```


## Capa de Datos (Persistencia Local)

He utilizado SQLDelight porque genera código Kotlin seguro a partir de sentencias SQL puras. Esto evita errores en tiempo de ejecución al validar las consultas durante la compilación.

### Definición del Esquema

Definimos las tablas y las operaciones en un archivo .sq.

**Archivo: shared/src/commonMain/sqldelight/.../db/GoodBooks.sq**

**Tabla principal de Libros**
```
CREATE TABLE Book (
    gid TEXT NOT NULL PRIMARY KEY, -- ID de Google Books
    title TEXT NOT NULL,
    authors TEXT,
    status TEXT NOT NULL CHECK(status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED')),
    rating INTEGER
);
```

**Query generadora de función Kotlin**
```
getAllBooks:
SELECT * FROM Book;
```

**Query con parámetros**
```
insertBook:
INSERT OR REPLACE INTO Book(gid, title, status, ...)
VALUES (?, ?, ?, ...);
```


### Abstracción Multiplataforma (Drivers)

Para que la base de datos funcione en ambos sistemas operativos, utilizamos el patrón expect / actual.

**Common (La Promesa)**: Definimos una interfaz que esperamos que exista.

**Archivo: shared/src/commonMain/.../DatabaseDriverFactory.kt**
```
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
```


**Android (La Realidad):** Implementación usando el Contexto de Android.

**Archivo: shared/src/androidMain/.../DatabaseDriverFactory.kt**
```
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(GoodBooksDatabase.Schema, context, "GoodBooks.db")
    }
}
```


**iOS (La Realidad):** Implementación usando drivers nativos de Apple.

**Archivo: shared/src/iosMain/.../DatabaseDriverFactory.kt**
```
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(GoodBooksDatabase.Schema, "GoodBooks.db")
    }
}
```


### El Repositorio (Gestión de Datos)

He implementado el patrón Repository. Esta clase actúa como la única fuente de verdad para la Interfaz de Usuario (UI). La UI nunca habla directamente con la base de datos SQL, habla con el Repositorio.

**Archivo: shared/src/commonMain/.../repository/BookRepository.kt**
```
class BookRepository(driverFactory: DatabaseDriverFactory) {
    // Inicialización de la DB generada por SQLDelight
    private val database = GoodBooksDatabase(driverFactory.createDriver())
    private val dbQueries = database.goodBooksQueries
    
    // La UI llamará a esta función simple, sin saber nada de SQL
    fun getAllBooks(): List<Book> {
        return dbQueries.getAllBooks().executeAsList()
    }
    
    fun insertBook(book: Book) {
        dbQueries.insertBook(
            gid = book.gid,
            title = book.title,
            // ... mapeo de datos
        )
    }
}
```

## Integración de Red y Patrón Repositorio

Para mantener una arquitectura limpia, he evolucionado la clase BookRepository. Inicialmente solo gestionaba la base de datos local (SQLDelight), pero ahora integra también el cliente de red (GoogleBooksApiClient).

El objetivo es que el Repositorio actúe como una "fuente única de verdad" para la Interfaz de Usuario (UI).

- La UI pide buscar libros -> El Repositorio consulta a la API de Google.

- La UI pide ver la biblioteca -> El Repositorio consulta a la Base de Datos Local.

De esta forma, la capa visual (Android/iOS) no necesita conocer los detalles complejos de cómo funcionan las peticiones HTTP o las consultas SQL; solo interactúa con funciones simples del Repositorio.

Flujo de Datos:
UI (Pantalla) <--> BookRepository <--> [Base de Datos Local / API Google Books]

## Capa de Presentación Android

Una vez completado el módulo compartido, comenzamos la implementación de la aplicación nativa Android.

**Arquitectura UI:**
Utilizamos **Jetpack Compose**, el kit de herramientas moderno de Google para construir interfaces de usuario nativas de forma declarativa (similar a SwiftUI).

**Inyección de Dependencias Manual:**
Para conectar la capa de UI (Android) con la capa de Datos (Shared), implementamos una clase `Application` personalizada.
* **Objetivo:** Instanciar el `BookRepository` una única vez al iniciar la aplicación (Singleton).
* **Ciclo de Vida:** El repositorio sobrevive mientras la app esté en memoria, evitando reconexiones innecesarias a la base de datos cada vez que se cambia de pantalla.

Ademas, como vamos a buscar libros en Google, Android nos exige pedir permiso explícito. Este permiso lo declaramos en el archivo `composeApp/src/androidMain/AndroidManifest.xml`

### Implementación del Patrón MVVM en Android

Para la gestión del estado de la interfaz (UI), utilizamos el patrón arquitectónico **Model-View-ViewModel (MVVM)**.

**Componentes:**
1.  **ViewModel (`SearchViewModel`):** Actúa como intermediario entre la UI y la capa de datos. Hereda de `AndroidViewModel` para tener acceso al contexto de la aplicación y así recuperar la instancia única del `BookRepository`.
2.  **State Management:** Utilizamos `StateFlow` o `MutableState` de Jetpack Compose para que la interfaz reaccione automáticamente a los cambios de datos (Reactividad). Por ejemplo, cuando la búsqueda en la API termina, la lista se actualiza sola.
3.  **Corrutinas:** Las llamadas a la API se ejecutan en segundo plano (`viewModelScope.launch`) para no bloquear la interfaz principal mientras se descargan los datos.

## Interfaz de Usuario Declarativa (Jetpack Compose)

La pantalla de búsqueda (`SearchScreen`) se ha construido utilizando **Jetpack Compose**. A diferencia del sistema clásico de Views (XML), Compose permite describir la interfaz mediante funciones de Kotlin.

**Elementos Clave de la UI:**
* **`LazyColumn`:** Componente equivalente a un `RecyclerView`. Renderiza de forma eficiente listas que pueden ser infinitas, reciclando los elementos que salen de la pantalla para ahorrar memoria.
* **`OutlinedTextField`:** Campo de entrada de texto siguiendo las guías de Material Design.
* **`State Hoisting`:** La pantalla no "guarda" datos. Recibe el estado (`query`, `results`, `isLoading`) desde el `ViewModel` y le "avisa" de eventos (`onSearchClick`). Esto hace que la UI sea puramente reactiva.

##  Aplicación Android Funcional

Se ha completado la integración vertical de la aplicación en Android.
**Flujo comprobado:**
1.  El usuario introduce un término ("Harry Potter") en la `SearchScreen`.
2.  El `SearchViewModel` recibe el evento y llama al `BookRepository` en un hilo secundario (`Coroutines`).
3.  El `BookRepository` utiliza el `GoogleBooksApiClient` (Ktor) para realizar la petición HTTP GET.
4.  La respuesta JSON se deserializa automáticamente a objetos `BookItem` de Kotlin.
5.  La lista de libros viaja de vuelta al ViewModel y actualiza el `State` de la UI.
6.  Jetpack Compose detecta el cambio de estado y repinta la `LazyColumn`, mostrando los libros con título, autor y páginas.

**Resultado:** La aplicación es capaz de buscar y mostrar información real de internet, validando que el módulo `shared` funciona correctamente en el ecosistema Android.
También se ha integrado una creación de libro manual por si el libro no aparece en Google Books se pueda registrar manualmente, agregandole los mismos campos (nombre, autor, número de páginas, portada, descripción, isbn).

## Carga de Imágenes y Pantalla de Detalle

Para visualizar las portadas de los libros, integramos la librería **Coil**.
* **Coil (Coroutines Image Loading):** Librería ligera respaldada por Kotlin Coroutines. Se encarga de descargar la imagen de la URL, cachearla en memoria/disco y mostrarla en un componente `AsyncImage` de Compose.

**Navegación con Argumentos:**
Implementamos la navegación desde la lista de búsqueda hacia una pantalla de detalle (`BookDetailScreen`).
* Al hacer clic en un libro, pasamos su ID único (Google Book ID) a la nueva pantalla.
* La pantalla de detalle recupera la información completa y permite la acción de **Persistencia**: transformar el objeto de la API (transitorio) en una entidad de la Base de Datos (permanente) mediante el Repositorio.

## Ciclo de Lectura y Valoración

Este hito se centra en la funcionalidad "Core" de seguimiento (tracking):
* **Transiciones de Estado:** Implementación de la máquina de estados `PENDING` -> `IN_PROGRESS` -> `COMPLETED`.
* **Timestamps Automáticos:** Registro transparente de `startDate` al iniciar y `endDate` al finalizar.
* **Sistema de Rating:** Interfaz para puntuar el libro al completarlo, cerrando el ciclo de lectura.
* **Persistencia de Cambios:** Uso de la query SQL `UPDATE` para modificar registros existentes sin duplicarlos.

## Sistema de Filtros

Se han establecido unos filtros en el apartado "Mi Biblioteca" para poder filtrar los libros por:
* **Estado:** Tres filtros con estado "Pendiente", "Leyendo", "Terminado"
* **Número de páginas:** Permitiendo filtrar por rango de páginas, por ejemplo, los libros que tengan entre 100 y 200 páginas. También permite filtrar solo por una cantidad, por ejemplo, como mínimo 100 páginas o como máximo 100 páginas.
* **Fecha:** Permitiendo solamente a los libros terminados (que son los que tendrán una fecha de inicio y fin) filtrarlos también por rango de fechas o bien por fecha mínima de inicio o fecha máxima de fin.

## La estanteria
El concepto de la estanteria es bastante interesante, ya que permite a los usuarios poder agrupar los libros en las carpetas en las que les resulte mas comodo. 
El flujo de trabajo que debemos seguir sera el siguiente:
* Actualizaremos en la base de datos la tabla Category ya que aunque Category ya este creado quiero que tenga mas columnas como color, descripcion, etc.
* Hare la funcionalidad para poder crear estanterias y meter/sacar libros de ella.
* Habra que modificar el UI de la seccion "Biblioteca" para poder/crear ver las estanterias.
* UI para la creacion de las estanterias.
* UI para el detalle de la estanteria (ver los libros que hay dentro y meter mas).

## Estadisticas
Las estadisticas son uno de los pilares mas importantes de la aplicacion ya que le permitiran al lector saber cosas que solamente podra saberlas si utiliza correcta y normalmente la aplicacion.
He establecido diferentes estadisticas para que el lector pueda tener conocimiento de la cantidad de libros que ha leido anualmente, la nota media que le ha dado a sus libros o el numero de paginas totales que hay leido entre otros.

## Informacion Interesante

#### ¿Dónde está el servidor backend?
La aplicación sigue una arquitectura Serverless/Local-First. La base de datos principal es SQLite dentro del dispositivo del usuario. Usamos la API pública de Google Books solo para consultar información, pero los datos del usuario (progreso, notas) se guardan localmente, garantizando privacidad y funcionamiento offline.

#### ¿Qué es ese archivo .sq?
Es un archivo de SQLDelight. A diferencia de Room (Android) o CoreData (iOS) que usan anotaciones sobre clases, aquí escribimos SQL puro y la librería genera las clases Kotlin por nosotros. Es más eficiente y multiplataforma.

#### ¿Cómo se comunica Android con el módulo compartido?
Android ve el módulo shared como una librería normal de Kotlin. Simplemente instancia la clase BookRepository y llama a sus funciones.