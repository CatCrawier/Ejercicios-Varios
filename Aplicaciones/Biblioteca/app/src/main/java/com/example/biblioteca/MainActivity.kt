package com.example.biblioteca

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.core.database.sqlite.transaction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.biblioteca.ui.theme.BibliotecaTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class Libro(val identificador: String, val titulo: String, val autor: String, val isbn: String)
private data class Lector(val identificador: String, val nombre: String, val contacto: String)
private data class Prestamo(
    val identificador: String,
    val identificadorLibro: String,
    val identificadorLector: String,
    val fechaDevolucion: String,
    val devuelto: Boolean = false
)

private data class DatosBiblioteca(
    val libros: List<Libro> = emptyList(),
    val lectores: List<Lector> = emptyList(),
    val prestamos: List<Prestamo> = emptyList()
)

/** Base de datos local SQLite para el catálogo, los lectores y los préstamos. */
private class BaseDatosBiblioteca(private val contexto: Context) :
    SQLiteOpenHelper(contexto, "biblioteca.db", null, VERSION_BASE_DATOS) {

    override fun onConfigure(baseDatos: SQLiteDatabase) {
        super.onConfigure(baseDatos)
        baseDatos.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(baseDatos: SQLiteDatabase) {
        baseDatos.execSQL(
            "CREATE TABLE libros (" +
                "identificador TEXT PRIMARY KEY NOT NULL, " +
                "titulo TEXT NOT NULL, autor TEXT NOT NULL, isbn TEXT NOT NULL)"
        )
        baseDatos.execSQL(
            "CREATE TABLE lectores (" +
                "identificador TEXT PRIMARY KEY NOT NULL, " +
                "nombre TEXT NOT NULL, contacto TEXT NOT NULL)"
        )
        baseDatos.execSQL(
            "CREATE TABLE prestamos (" +
                "identificador TEXT PRIMARY KEY NOT NULL, " +
                "identificador_libro TEXT NOT NULL REFERENCES libros(identificador), " +
                "identificador_lector TEXT NOT NULL REFERENCES lectores(identificador), " +
                "fecha_devolucion TEXT NOT NULL, devuelto INTEGER NOT NULL DEFAULT 0)"
        )
        migrarDatosAnteriores(baseDatos)
    }

    private fun migrarDatosAnteriores(baseDatos: SQLiteDatabase) {
        val preferencias = contexto.getSharedPreferences("biblioteca", Context.MODE_PRIVATE)
        val datosGuardados = preferencias.getString("data", null) ?: return
        try {
            val raiz = JSONObject(datosGuardados)
            baseDatos.transaction {
                val librosAntiguos = raiz.optJSONArray("books")
                for (indice in 0 until (librosAntiguos?.length() ?: 0)) {
                    val libro = librosAntiguos?.optJSONObject(indice) ?: continue
                    baseDatos.insertWithOnConflict("libros", null, ContentValues().apply {
                        put("identificador", libro.getString("id"))
                        put("titulo", libro.getString("title"))
                        put("autor", libro.getString("author"))
                        put("isbn", libro.optString("isbn"))
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
                val lectoresAntiguos = raiz.optJSONArray("readers")
                for (indice in 0 until (lectoresAntiguos?.length() ?: 0)) {
                    val lector = lectoresAntiguos?.optJSONObject(indice) ?: continue
                    baseDatos.insertWithOnConflict("lectores", null, ContentValues().apply {
                        put("identificador", lector.getString("id"))
                        put("nombre", lector.getString("name"))
                        put("contacto", lector.optString("contact"))
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
                val prestamosAntiguos = raiz.optJSONArray("loans")
                for (indice in 0 until (prestamosAntiguos?.length() ?: 0)) {
                    val prestamo = prestamosAntiguos?.optJSONObject(indice) ?: continue
                    baseDatos.insertWithOnConflict("prestamos", null, ContentValues().apply {
                        put("identificador", prestamo.getString("id"))
                        put("identificador_libro", prestamo.getString("bookId"))
                        put("identificador_lector", prestamo.getString("readerId"))
                        put("fecha_devolucion", prestamo.getString("dueDate"))
                        put("devuelto", if (prestamo.optBoolean("returned")) 1 else 0)
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            preferencias.edit { remove("data") }
        } catch (_: Exception) {
            // Si no se pueden importar los datos, se conservan las preferencias originales.
        }
    }

    override fun onUpgrade(baseDatos: SQLiteDatabase, versionAnterior: Int, versionNueva: Int) {
        // Las migraciones de esquema se agregan aquí cuando cambie la versión.
    }

    fun cargarDatos(): DatosBiblioteca = DatosBiblioteca(
        libros = consultar("libros") { Libro(it.getString(0), it.getString(1), it.getString(2), it.getString(3)) },
        lectores = consultar("lectores") { Lector(it.getString(0), it.getString(1), it.getString(2)) },
        prestamos = consultar("prestamos") {
            Prestamo(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getInt(4) == 1)
        }
    )

    private inline fun <T> consultar(tabla: String, convertir: (android.database.Cursor) -> T): List<T> {
        val resultados = mutableListOf<T>()
        readableDatabase.query(tabla, null, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) resultados.add(convertir(cursor))
        }
        return resultados
    }

    fun insertarLibro(libro: Libro) {
        writableDatabase.insertOrThrow("libros", null, ContentValues().apply {
            put("identificador", libro.identificador)
            put("titulo", libro.titulo)
            put("autor", libro.autor)
            put("isbn", libro.isbn)
        })
    }

    fun insertarLector(lector: Lector) {
        writableDatabase.insertOrThrow("lectores", null, ContentValues().apply {
            put("identificador", lector.identificador)
            put("nombre", lector.nombre)
            put("contacto", lector.contacto)
        })
    }

    fun insertarPrestamo(prestamo: Prestamo) {
        writableDatabase.insertOrThrow("prestamos", null, ContentValues().apply {
            put("identificador", prestamo.identificador)
            put("identificador_libro", prestamo.identificadorLibro)
            put("identificador_lector", prestamo.identificadorLector)
            put("fecha_devolucion", prestamo.fechaDevolucion)
            put("devuelto", if (prestamo.devuelto) 1 else 0)
        })
    }

    fun registrarDevolucion(identificadorPrestamo: String) {
        writableDatabase.update(
            "prestamos",
            ContentValues().apply { put("devuelto", 1) },
            "identificador = ?",
            arrayOf(identificadorPrestamo)
        )
    }

    private companion object {
        const val VERSION_BASE_DATOS = 1
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var baseDatos: BaseDatosBiblioteca

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        baseDatos = BaseDatosBiblioteca(this)
        val datosIniciales = baseDatos.cargarDatos()
        setContent {
            BibliotecaTheme {
                AplicacionBiblioteca(datosIniciales, baseDatos)
            }
        }
    }

    override fun onDestroy() {
        baseDatos.close()
        super.onDestroy()
    }
}

@Composable
private fun AplicacionBiblioteca(datosIniciales: DatosBiblioteca, baseDatos: BaseDatosBiblioteca) {
    val libros = remember { mutableStateListOf<Libro>().apply { addAll(datosIniciales.libros) } }
    val lectores = remember { mutableStateListOf<Lector>().apply { addAll(datosIniciales.lectores) } }
    val prestamos = remember { mutableStateListOf<Prestamo>().apply { addAll(datosIniciales.prestamos) } }
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { relleno ->
        Column(Modifier.padding(relleno).fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Biblioteca", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Administra libros, lectores y préstamos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PrimaryTabRow(selectedTabIndex = pestanaSeleccionada) {
                listOf("Libros", "Lectores", "Préstamos").forEachIndexed { indice, etiqueta ->
                    Tab(selected = pestanaSeleccionada == indice, onClick = { pestanaSeleccionada = indice }, text = { Text(etiqueta) })
                }
            }
            mensaje?.let {
                Text(it, Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            }
            when (pestanaSeleccionada) {
                0 -> PantallaLibros(libros, prestamos, alAgregar = { libro ->
                    baseDatos.insertarLibro(libro)
                    libros.add(libro)
                    mensaje = "Libro registrado."
                })
                1 -> PantallaLectores(lectores, alAgregar = { lector ->
                    baseDatos.insertarLector(lector)
                    lectores.add(lector)
                    mensaje = "Lector registrado."
                })
                else -> PantallaPrestamos(
                    libros = libros,
                    lectores = lectores,
                    prestamos = prestamos,
                    alPrestar = { identificadorLibro, identificadorLector, fecha ->
                        val prestamo = Prestamo(UUID.randomUUID().toString(), identificadorLibro, identificadorLector, fecha)
                        baseDatos.insertarPrestamo(prestamo)
                        prestamos.add(prestamo)
                        mensaje = "Préstamo registrado."
                    },
                    alDevolver = { prestamo ->
                        baseDatos.registrarDevolucion(prestamo.identificador)
                        val indice = prestamos.indexOfFirst { it.identificador == prestamo.identificador }
                        if (indice >= 0) prestamos[indice] = prestamo.copy(devuelto = true)
                        mensaje = "Libro devuelto."
                    }
                )
            }
        }
    }
}

@Composable
private fun PantallaLibros(libros: List<Libro>, prestamos: List<Prestamo>, alAgregar: (Libro) -> Unit) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Button(onClick = { mostrarFormulario = !mostrarFormulario }, modifier = Modifier.fillMaxWidth()) {
            Text(if (mostrarFormulario) "Cerrar formulario" else "Registrar libro")
        }
        if (mostrarFormulario) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(autor, { autor = it }, label = { Text("Autor") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(isbn, { isbn = it }, label = { Text("ISBN (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                if (titulo.isNotBlank() && autor.isNotBlank()) {
                    alAgregar(Libro(UUID.randomUUID().toString(), titulo.trim(), autor.trim(), isbn.trim()))
                    titulo = ""; autor = ""; isbn = ""; mostrarFormulario = false
                }
            }, enabled = titulo.isNotBlank() && autor.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Guardar libro") }
        }
        Spacer(Modifier.height(20.dp))
        Text("Catálogo · ${libros.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (libros.isEmpty()) EstadoVacio("Aún no hay libros registrados.")
        libros.forEach { libro ->
            val disponible = prestamos.none { it.identificadorLibro == libro.identificador && !it.devuelto }
            TarjetaElemento(libro.titulo, "${libro.autor}${if (libro.isbn.isNotBlank()) " · ISBN ${libro.isbn}" else ""}", if (disponible) "Disponible" else "Prestado", disponible)
        }
    }
}

@Composable
private fun PantallaLectores(lectores: List<Lector>, alAgregar: (Lector) -> Unit) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var contacto by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Button(onClick = { mostrarFormulario = !mostrarFormulario }, modifier = Modifier.fillMaxWidth()) {
            Text(if (mostrarFormulario) "Cerrar formulario" else "Registrar lector")
        }
        if (mostrarFormulario) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(contacto, { contacto = it }, label = { Text("Correo o teléfono (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = {
                if (nombre.isNotBlank()) {
                    alAgregar(Lector(UUID.randomUUID().toString(), nombre.trim(), contacto.trim()))
                    nombre = ""; contacto = ""; mostrarFormulario = false
                }
            }, enabled = nombre.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Guardar lector") }
        }
        Spacer(Modifier.height(20.dp))
        Text("Lectores · ${lectores.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (lectores.isEmpty()) EstadoVacio("Aún no hay lectores registrados.")
        lectores.forEach { lector -> TarjetaElemento(lector.nombre, lector.contacto.ifBlank { "Sin datos de contacto" }) }
    }
}

@Composable
private fun PantallaPrestamos(
    libros: List<Libro>, lectores: List<Lector>, prestamos: List<Prestamo>,
    alPrestar: (String, String, String) -> Unit, alDevolver: (Prestamo) -> Unit
) {
    val contexto = LocalContext.current
    val librosDisponibles = libros.filter { libro -> prestamos.none { it.identificadorLibro == libro.identificador && !it.devuelto } }
    var libroSeleccionado by remember { mutableStateOf<Libro?>(null) }
    var lectorSeleccionado by remember { mutableStateOf<Lector?>(null) }
    var menuLibrosAbierto by remember { mutableStateOf(false) }
    var menuLectoresAbierto by remember { mutableStateOf(false) }
    var fechaDevolucion by remember { mutableStateOf(fechaPredeterminadaDevolucion()) }
    val prestamosActivos = prestamos.filterNot { it.devuelto }
    val prestamosDevueltos = prestamos.filter { it.devuelto }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Nuevo préstamo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        if (libros.isEmpty() || lectores.isEmpty()) {
            EstadoVacio("Registra al menos un libro y un lector para crear préstamos.")
        } else {
            OutlinedButton(onClick = { menuLibrosAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                Text(libroSeleccionado?.titulo ?: "Seleccionar libro disponible")
            }
            DropdownMenu(expanded = menuLibrosAbierto, onDismissRequest = { menuLibrosAbierto = false }) {
                if (librosDisponibles.isEmpty()) DropdownMenuItem(text = { Text("No hay libros disponibles") }, onClick = { menuLibrosAbierto = false })
                librosDisponibles.forEach { libro -> DropdownMenuItem(text = { Text(libro.titulo) }, onClick = { libroSeleccionado = libro; menuLibrosAbierto = false }) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { menuLectoresAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                Text(lectorSeleccionado?.nombre ?: "Seleccionar lector")
            }
            DropdownMenu(expanded = menuLectoresAbierto, onDismissRequest = { menuLectoresAbierto = false }) {
                lectores.forEach { lector -> DropdownMenuItem(text = { Text(lector.nombre) }, onClick = { lectorSeleccionado = lector; menuLectoresAbierto = false }) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                val calendario = Calendar.getInstance()
                calendario.add(Calendar.DAY_OF_YEAR, 1)
                DatePickerDialog(contexto, { _, anio, mes, dia -> fechaDevolucion = formarFecha(anio, mes, dia) }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
            }, modifier = Modifier.fillMaxWidth()) { Text("Fecha de devolución: ${mostrarFecha(fechaDevolucion)}") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val libro = libroSeleccionado
                val lector = lectorSeleccionado
                if (libro != null && lector != null) {
                    alPrestar(libro.identificador, lector.identificador, fechaDevolucion)
                    libroSeleccionado = null
                    lectorSeleccionado = null
                }
            }, enabled = libroSeleccionado != null && lectorSeleccionado != null && librosDisponibles.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text("Registrar préstamo")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Préstamos activos · ${prestamosActivos.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (prestamosActivos.isEmpty()) EstadoVacio("No hay préstamos activos.")
        prestamosActivos.forEach { prestamo ->
            val libro = libros.find { it.identificador == prestamo.identificadorLibro }
            val lector = lectores.find { it.identificador == prestamo.identificadorLector }
            val vencido = prestamo.fechaDevolucion < fechaActual()
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text(libro?.titulo ?: "Libro eliminado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Lector: ${lector?.nombre ?: "Lector eliminado"}")
                    Text("Devolución: ${mostrarFecha(prestamo.fechaDevolucion)}", color = if (vencido) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    if (vencido) Text("Préstamo vencido", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { alDevolver(prestamo) }) { Text("Registrar devolución") }
                }
            }
        }
        if (prestamosDevueltos.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Text("Devoluciones registradas · ${prestamosDevueltos.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            prestamosDevueltos.asReversed().forEach { prestamo ->
                TarjetaElemento(
                    libros.find { it.identificador == prestamo.identificadorLibro }?.titulo ?: "Libro",
                    "Devuelto por ${lectores.find { it.identificador == prestamo.identificadorLector }?.nombre ?: "lector"} · vencía ${mostrarFecha(prestamo.fechaDevolucion)}",
                    "Devuelto", true
                )
            }
        }
    }
}

@Composable
private fun TarjetaElemento(titulo: String, subtitulo: String, detalle: String? = null, positivo: Boolean = false) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (detalle != null) {
                Spacer(Modifier.width(8.dp))
                Text(detalle, color = if (positivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EstadoVacio(texto: String) {
    Text(texto, Modifier.fillMaxWidth().padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun fechaActual(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun mostrarFecha(fecha: String): String = try {
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CO")).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)!!)
} catch (_: Exception) { fecha }
private fun fechaPredeterminadaDevolucion(): String {
    val calendario = Calendar.getInstance()
    calendario.add(Calendar.DAY_OF_YEAR, 14)
    return formarFecha(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
}
private fun formarFecha(anio: Int, mes: Int, dia: Int): String = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
