package com.example.biblioteca

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.biblioteca.ui.theme.BibliotecaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

private data class Libro(
    val identificador: String, val titulo: String, val autor: String, val isbn: String,
    val editorial: String = "", val anio: String = "", val categoria: String = "",
    val prestamoDomicilio: Boolean = true
)
private data class Lector(
    val identificador: String, val nombre: String, val correo: String,
    val telefono: String = "", val documento: String = "", val direccion: String = ""
)
private data class Prestamo(
    val identificador: String, val identificadorLibro: String, val identificadorLector: String,
    val fechaDevolucion: String, val devuelto: Boolean = false,
    val modalidad: String = "Sala", val direccion: String = "", val ciudad: String = "", val telefono: String = "",
    val fechaEntregaReal: String = "", val horaDevolucion: String = "", val horaEntregaReal: String = ""
)
private data class ResultadoOpenLibrary(
    val titulo: String, val autor: String, val isbn: String, val editorial: String,
    val anio: String, val categoria: String
)
private data class DatosBiblioteca(
    val libros: List<Libro> = emptyList(), val lectores: List<Lector> = emptyList(),
    val prestamos: List<Prestamo> = emptyList()
)

private data class PerfilLector(val entregasPuntuales: Int, val faltas: Int) {
    val puntos: Int get() = (entregasPuntuales * 10 - faltas * 15).coerceAtLeast(0)
    val nivelRecompensa: String get() = when {
        entregasPuntuales >= 10 -> "Oro"
        entregasPuntuales >= 6 -> "Plata"
        entregasPuntuales >= 3 -> "Bronce"
        entregasPuntuales > 0 -> "Lector cumplido"
        else -> "Inicial"
    }
    val nivelPenalizacion: String get() = when {
        faltas >= 5 -> "Suspensión de préstamos"
        faltas >= 3 -> "Solo consulta en sala"
        faltas >= 2 -> "Advertencia formal"
        faltas == 1 -> "Aviso"
        else -> "Sin faltas"
    }
    val diasPrestamo: Int get() = when (nivelRecompensa) {
        "Oro" -> 30
        "Plata" -> 21
        "Bronce" -> 17
        "Lector cumplido" -> 15
        else -> 14
    }
    val suspendido: Boolean get() = faltas >= 5
    val soloSala: Boolean get() = faltas >= 3
}

private fun perfilLector(lector: Lector, prestamos: List<Prestamo>): PerfilLector {
    val devueltos = prestamos.filter { it.identificadorLector == lector.identificador && it.devuelto && it.fechaEntregaReal.isNotBlank() }
    return PerfilLector(
        entregasPuntuales = devueltos.count { devolucionFuePuntual(it) },
        faltas = devueltos.count { !devolucionFuePuntual(it) }
    )
}

private fun devolucionFuePuntual(prestamo: Prestamo): Boolean {
    if (prestamo.modalidad != "Sala" || prestamo.horaDevolucion.isBlank() || prestamo.horaEntregaReal.isBlank()) {
        return prestamo.fechaEntregaReal <= prestamo.fechaDevolucion
    }
    return "${prestamo.fechaEntregaReal} ${prestamo.horaEntregaReal}" <= "${prestamo.fechaDevolucion} ${prestamo.horaDevolucion}"
}

private fun prestamoVencido(prestamo: Prestamo): Boolean {
    if (prestamo.modalidad != "Sala" || prestamo.horaDevolucion.isBlank()) return prestamo.fechaDevolucion < fechaActual()
    return "${prestamo.fechaDevolucion} ${prestamo.horaDevolucion}" < "${fechaActual()} ${horaActual()}"
}

private class BaseDatosBiblioteca(private val contexto: Context) :
    SQLiteOpenHelper(contexto, "biblioteca.db", null, 4) {
    override fun onConfigure(db: SQLiteDatabase) { super.onConfigure(db); db.setForeignKeyConstraintsEnabled(true) }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE libros (identificador TEXT PRIMARY KEY NOT NULL, titulo TEXT NOT NULL, autor TEXT NOT NULL, isbn TEXT NOT NULL, editorial TEXT NOT NULL DEFAULT '', anio TEXT NOT NULL DEFAULT '', categoria TEXT NOT NULL DEFAULT '', prestamo_domicilio INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE lectores (identificador TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, contacto TEXT NOT NULL, telefono TEXT NOT NULL DEFAULT '', documento TEXT NOT NULL DEFAULT '', direccion TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE prestamos (identificador TEXT PRIMARY KEY NOT NULL, identificador_libro TEXT NOT NULL REFERENCES libros(identificador), identificador_lector TEXT NOT NULL REFERENCES lectores(identificador), fecha_devolucion TEXT NOT NULL, devuelto INTEGER NOT NULL DEFAULT 0, modalidad TEXT NOT NULL DEFAULT 'Sala', direccion TEXT NOT NULL DEFAULT '', ciudad TEXT NOT NULL DEFAULT '', telefono TEXT NOT NULL DEFAULT '', fecha_entrega_real TEXT NOT NULL DEFAULT '', hora_devolucion TEXT NOT NULL DEFAULT '', hora_entrega_real TEXT NOT NULL DEFAULT '')")
        importarPreferencias(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE libros ADD COLUMN editorial TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE libros ADD COLUMN anio TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE libros ADD COLUMN categoria TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE libros ADD COLUMN prestamo_domicilio INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE lectores ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE lectores ADD COLUMN documento TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE lectores ADD COLUMN direccion TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE prestamos ADD COLUMN modalidad TEXT NOT NULL DEFAULT 'Sala'")
            db.execSQL("ALTER TABLE prestamos ADD COLUMN direccion TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE prestamos ADD COLUMN ciudad TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE prestamos ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
        }
        if (oldVersion < 3) db.execSQL("ALTER TABLE prestamos ADD COLUMN fecha_entrega_real TEXT NOT NULL DEFAULT ''")
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE prestamos ADD COLUMN hora_devolucion TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE prestamos ADD COLUMN hora_entrega_real TEXT NOT NULL DEFAULT ''")
        }
    }

    private fun importarPreferencias(db: SQLiteDatabase) {
        val prefs = contexto.getSharedPreferences("biblioteca", Context.MODE_PRIVATE)
        val saved = prefs.getString("data", null) ?: return
        try {
            val root = JSONObject(saved)
            db.transaction {
                val books = root.optJSONArray("books")
                for (i in 0 until (books?.length() ?: 0)) {
                    val row = books?.optJSONObject(i) ?: continue
                    db.insertWithOnConflict("libros", null, ContentValues().apply {
                        put("identificador", row.getString("id")); put("titulo", row.getString("title")); put("autor", row.getString("author")); put("isbn", row.optString("isbn"))
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
                val readers = root.optJSONArray("readers")
                for (i in 0 until (readers?.length() ?: 0)) {
                    val row = readers?.optJSONObject(i) ?: continue
                    db.insertWithOnConflict("lectores", null, ContentValues().apply {
                        put("identificador", row.getString("id")); put("nombre", row.getString("name")); put("contacto", row.optString("contact"))
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
                val loans = root.optJSONArray("loans")
                for (i in 0 until (loans?.length() ?: 0)) {
                    val row = loans?.optJSONObject(i) ?: continue
                    db.insertWithOnConflict("prestamos", null, ContentValues().apply {
                        put("identificador", row.getString("id")); put("identificador_libro", row.getString("bookId")); put("identificador_lector", row.getString("readerId")); put("fecha_devolucion", row.getString("dueDate")); put("devuelto", if (row.optBoolean("returned")) 1 else 0)
                    }, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
            prefs.edit { remove("data") }
        } catch (_: Exception) { /* Se conservan los datos antiguos si no se pudieron importar. */ }
    }

    fun cargarDatos(): DatosBiblioteca = DatosBiblioteca(
        libros = query("libros") { c -> Libro(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getInt(7) == 1) },
        lectores = query("lectores") { c -> Lector(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5)) },
        prestamos = query("prestamos") { c -> Prestamo(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getInt(4) == 1, c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9), c.getString(10), c.getString(11)) }
    )
    private inline fun <T> query(table: String, map: (android.database.Cursor) -> T): List<T> = buildList {
        readableDatabase.query(table, null, null, null, null, null, null).use { c -> while (c.moveToNext()) add(map(c)) }
    }
    fun insertarLibro(x: Libro) { writableDatabase.insertOrThrow("libros", null, ContentValues().apply {
        put("identificador", x.identificador); put("titulo", x.titulo); put("autor", x.autor); put("isbn", x.isbn); put("editorial", x.editorial); put("anio", x.anio); put("categoria", x.categoria); put("prestamo_domicilio", if (x.prestamoDomicilio) 1 else 0)
    }) }
    fun insertarLector(x: Lector) { writableDatabase.insertOrThrow("lectores", null, ContentValues().apply {
        put("identificador", x.identificador); put("nombre", x.nombre); put("contacto", x.correo); put("telefono", x.telefono); put("documento", x.documento); put("direccion", x.direccion)
    }) }
    fun insertarPrestamo(x: Prestamo) { writableDatabase.insertOrThrow("prestamos", null, ContentValues().apply {
        put("identificador", x.identificador); put("identificador_libro", x.identificadorLibro); put("identificador_lector", x.identificadorLector); put("fecha_devolucion", x.fechaDevolucion); put("devuelto", if (x.devuelto) 1 else 0); put("modalidad", x.modalidad); put("direccion", x.direccion); put("ciudad", x.ciudad); put("telefono", x.telefono); put("fecha_entrega_real", x.fechaEntregaReal); put("hora_devolucion", x.horaDevolucion); put("hora_entrega_real", x.horaEntregaReal)
    }) }
    fun registrarDevolucion(id: String) { writableDatabase.update("prestamos", ContentValues().apply { put("devuelto", 1); put("fecha_entrega_real", fechaActual()); put("hora_entrega_real", horaActual()) }, "identificador = ?", arrayOf(id)) }
}

class MainActivity : ComponentActivity() {
    private lateinit var db: BaseDatosBiblioteca
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge(); db = BaseDatosBiblioteca(this)
        val initial = db.cargarDatos()
        setContent { BibliotecaTheme { AplicacionBiblioteca(initial, db) } }
    }
    override fun onDestroy() { db.close(); super.onDestroy() }
}

@Composable
private fun AplicacionBiblioteca(inicial: DatosBiblioteca, db: BaseDatosBiblioteca) {
    val libros = remember { mutableStateListOf<Libro>().apply { addAll(inicial.libros) } }
    val lectores = remember { mutableStateListOf<Lector>().apply { addAll(inicial.lectores) } }
    val prestamos = remember { mutableStateListOf<Prestamo>().apply { addAll(inicial.prestamos) } }
    var tab by remember { mutableIntStateOf(0) }; var mensaje by remember { mutableStateOf<String?>(null) }
    Scaffold(Modifier.fillMaxSize()) { inset ->
        Column(Modifier.padding(inset).fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp)) {
                Text("✦  BIBLIOTECA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Tu espacio de lectura", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Catálogo, lectores y préstamos en un solo lugar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Resumen("${libros.size}", "libros", Modifier.weight(1f)); Resumen("${lectores.size}", "lectores", Modifier.weight(1f)); Resumen("${prestamos.count { !it.devuelto }}", "activos", Modifier.weight(1f))
                }
            }
            PrimaryTabRow(selectedTabIndex = tab) {
                listOf("Catálogo", "Lectores", "Préstamos").forEachIndexed { i, label -> Tab(tab == i, { tab = i }, text = { Text(label) }) }
            }
            mensaje?.let { Text(it, Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary) }
            when (tab) {
                0 -> PantallaLibros(libros, prestamos) { x -> db.insertarLibro(x); libros.add(x); mensaje = "Libro agregado al catálogo." }
                1 -> PantallaLectores(lectores, prestamos) { x -> db.insertarLector(x); lectores.add(x); mensaje = "Lector registrado." }
                else -> PantallaPrestamos(
                    libros = libros,
                    lectores = lectores,
                    prestamos = prestamos,
                    alPrestar = { book, reader, date, time, mode, address, city, phone ->
                        val x = Prestamo(UUID.randomUUID().toString(), book, reader, date, modalidad = mode, direccion = address, ciudad = city, telefono = phone, horaDevolucion = if (mode == "Sala") time else "")
                        db.insertarPrestamo(x); prestamos.add(x); mensaje = "Préstamo registrado."
                    }
                ) { x ->
                    db.registrarDevolucion(x.identificador)
                    val i = prestamos.indexOfFirst { it.identificador == x.identificador }
                    if (i >= 0) prestamos[i] = x.copy(devuelto = true, fechaEntregaReal = fechaActual(), horaEntregaReal = horaActual())
                    mensaje = "Devolución registrada. ${perfilLector(lectores.first { it.identificador == x.identificadorLector }, prestamos).let { "Nivel ${it.nivelRecompensa} · ${it.puntos} puntos · ${it.nivelPenalizacion}" }}"
                }
            }
        }
    }
}

@Composable private fun Resumen(numero: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) { Text(numero, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(etiqueta, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun PantallaLibros(libros: List<Libro>, prestamos: List<Prestamo>, alAgregar: (Libro) -> Unit) {
    var open by remember { mutableStateOf(false) }; var query by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }; var author by remember { mutableStateOf("") }; var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }; var year by remember { mutableStateOf("") }; var category by remember { mutableStateOf("") }
    var homeAllowed by remember { mutableStateOf(true) }; var results by remember { mutableStateOf<List<ResultadoOpenLibrary>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }; var searchError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(query, open) {
        if (!open || query.trim().length < 2) { results = emptyList(); loading = false; return@LaunchedEffect }
        delay(450.milliseconds); loading = true; searchError = null
        try { results = buscarOpenLibrary(query.trim()); if (results.isEmpty()) searchError = "No encontramos coincidencias. Puedes completar los datos manualmente." }
        catch (_: Exception) { results = emptyList(); searchError = "No fue posible consultar Open Library. Revisa tu conexión o ingresa los datos manualmente." }
        loading = false
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Button(onClick = { open = !open }, Modifier.fillMaxWidth()) { Text(if (open) "Cerrar registro" else "＋  Registrar libro") }
        if (open) {
            Spacer(Modifier.height(12.dp)); EncabezadoSeccion("Buscar en Open Library", "Busca por título, autor, ISBN o tema")
            OutlinedTextField(query, { query = it }, label = { Text("¿Qué libro buscas?") }, placeholder = { Text("Ej. Cien años de soledad, García Márquez, ISBN…") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            if (loading) Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp); Text("  Buscando libros…") }
            searchError?.let { Text(it, Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            results.take(6).forEach { item ->
                Card(onClick = {
                    title = item.titulo; author = item.autor; isbn = item.isbn; publisher = item.editorial; year = item.anio; category = item.categoria
                    results = emptyList(); query = ""
                }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Column(Modifier.padding(12.dp)) { Text(item.titulo, fontWeight = FontWeight.SemiBold); Text(item.autor + if (item.anio.isNotBlank()) " · ${item.anio}" else "", color = MaterialTheme.colorScheme.onSurfaceVariant); if (item.isbn.isNotBlank()) Text("ISBN ${item.isbn}", style = MaterialTheme.typography.labelMedium) } }
            }
            Spacer(Modifier.height(12.dp)); EncabezadoSeccion("Detalles del ejemplar", "Revisa los datos sugeridos y completa lo que falte")
            Campo(title, { title = it }, "Título *"); Campo(author, { author = it }, "Autor *"); Campo(isbn, { isbn = it }, "ISBN")
            Campo(publisher, { publisher = it }, "Editorial"); Campo(year, { year = it }, "Año de publicación", KeyboardType.Number); Campo(category, { category = it }, "Género o categoría")
            Text("Modalidad permitida", Modifier.padding(top = 10.dp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OpcionRadio("Préstamo a domicilio y consulta en sala", homeAllowed) { homeAllowed = true }
            OpcionRadio("Solo consulta dentro de la biblioteca", !homeAllowed) { homeAllowed = false }
            Button(onClick = {
                alAgregar(Libro(UUID.randomUUID().toString(), title.trim(), author.trim(), isbn.trim(), publisher.trim(), year.trim(), category.trim(), homeAllowed))
                title = ""; author = ""; isbn = ""; publisher = ""; year = ""; category = ""; open = false
            }, enabled = title.isNotBlank() && author.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Guardar en catálogo") }
        }
        Spacer(Modifier.height(22.dp)); EncabezadoSeccion("Estantería", "${libros.size} ${if (libros.size == 1) "ejemplar" else "ejemplares"}")
        if (libros.isEmpty()) EstadoVacio("Tu catálogo está listo para recibir sus primeros libros.")
        libros.forEach { libro ->
            val available = prestamos.none { it.identificadorLibro == libro.identificador && !it.devuelto }
            TarjetaElemento(libro.titulo, listOf(libro.autor, libro.editorial, libro.anio, libro.isbn.takeIf { it.isNotBlank() }?.let { "ISBN $it" }).filter { !it.isNullOrBlank() }.joinToString(" · "), if (available) "Disponible" else "Prestado", available,
                if (libro.prestamoDomicilio) "Sala y domicilio" else "Solo sala")
        }
    }
}

@Composable
private fun PantallaLectores(lectores: List<Lector>, prestamos: List<Prestamo>, alAgregar: (Lector) -> Unit) {
    var open by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }; var document by remember { mutableStateOf("") }; var address by remember { mutableStateOf("") }
    val emailOk = email.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        Button(onClick = { open = !open }, Modifier.fillMaxWidth()) { Text(if (open) "Cerrar registro" else "＋  Registrar lector") }
        if (open) {
            Spacer(Modifier.height(12.dp)); EncabezadoSeccion("Ficha del lector", "Los datos de contacto ayudan a gestionar sus préstamos")
            Campo(name, { name = it }, "Nombre completo *"); Campo(email, { email = it }, "Correo electrónico *", KeyboardType.Email)
            if (email.isNotBlank()) Text(if (emailOk) "Formato de correo válido" else "Ingresa un correo válido, por ejemplo nombre@dominio.com", Modifier.padding(start = 4.dp, top = 3.dp), color = if (emailOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Campo(phone, { phone = it }, "Teléfono / WhatsApp", KeyboardType.Phone); Campo(document, { document = it }, "Documento de identidad"); Campo(address, { address = it }, "Dirección de residencia")
            Button(onClick = { alAgregar(Lector(UUID.randomUUID().toString(), name.trim(), email.trim(), phone.trim(), document.trim(), address.trim())); name = ""; email = ""; phone = ""; document = ""; address = ""; open = false }, enabled = name.isNotBlank() && emailOk, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Guardar lector") }
        }
        Spacer(Modifier.height(22.dp)); EncabezadoSeccion("Comunidad lectora", "${lectores.size} registrados")
        Text("Cada devolución puntual suma 10 puntos. Las entregas tarde restan 15 puntos y agregan una falta.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        if (lectores.isEmpty()) EstadoVacio("Los lectores registrados aparecerán aquí.")
        lectores.forEach { lector ->
            val perfil = perfilLector(lector, prestamos)
            TarjetaElemento(
                lector.nombre,
                listOf(lector.correo, lector.telefono, lector.documento).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Sin datos adicionales" },
                "${perfil.puntos} pts",
                perfil.faltas == 0,
                "Recompensa: ${perfil.nivelRecompensa} · ${perfil.diasPrestamo} días por préstamo\n${perfil.puntos} puntos · ${perfil.entregasPuntuales} devoluciones puntuales\nPenalización: ${perfil.nivelPenalizacion} · ${perfil.faltas} faltas\n${lector.direccion}"
            )
        }
    }
}

@Composable
private fun PantallaPrestamos(libros: List<Libro>, lectores: List<Lector>, prestamos: List<Prestamo>,
    alPrestar: (String, String, String, String, String, String, String, String) -> Unit, alDevolver: (Prestamo) -> Unit) {
    val context = LocalContext.current
    val disponibles = libros.filter { libro -> prestamos.none { it.identificadorLibro == libro.identificador && !it.devuelto } }
    var book by remember { mutableStateOf<Libro?>(null) }; var reader by remember { mutableStateOf<Lector?>(null) }
    var menuBooks by remember { mutableStateOf(false) }; var menuReaders by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(fechaPredeterminadaDevolucion()) }; var mode by remember { mutableStateOf("Sala") }
    var time by remember { mutableStateOf(horaPredeterminadaDevolucion()) }
    var address by remember { mutableStateOf("") }; var city by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("") }
    val active = prestamos.filterNot { it.devuelto }; val returned = prestamos.filter { it.devuelto }
    val perfilSeleccionado = reader?.let { perfilLector(it, prestamos) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        EncabezadoSeccion("Nuevo préstamo", "Entrega y devolución de ejemplares")
        if (libros.isEmpty() || lectores.isEmpty()) EstadoVacio("Registra primero al menos un libro y un lector.") else {
            OutlinedButton(onClick = { menuBooks = true }, Modifier.fillMaxWidth()) { Text(book?.titulo ?: "Seleccionar ejemplar disponible") }
            DropdownMenu(menuBooks, { menuBooks = false }) { disponibles.forEach { DropdownMenuItem(text = { Text(it.titulo) }, onClick = { book = it; menuBooks = false }) } }
            OutlinedButton(onClick = { menuReaders = true }, Modifier.fillMaxWidth().padding(top = 6.dp)) { Text(reader?.nombre ?: "Seleccionar lector") }
            DropdownMenu(menuReaders, { menuReaders = false }) { lectores.forEach { lector -> DropdownMenuItem(text = { Text(lector.nombre) }, onClick = { reader = lector; date = fechaPredeterminadaDevolucion(perfilLector(lector, prestamos).diasPrestamo); menuReaders = false }) } }
            perfilSeleccionado?.let { perfil ->
                Text("${perfil.nivelRecompensa} · ${perfil.puntos} puntos · hasta ${perfil.diasPrestamo} días por préstamo · ${perfil.nivelPenalizacion}", Modifier.padding(top = 6.dp), color = if (perfil.faltas >= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                if (perfil.suspendido) Text("Lector suspendido: no puede registrar nuevos préstamos hasta revisión.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                else if (perfil.soloSala) Text("Por sus faltas, solo puede consultar libros dentro de la biblioteca.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Text("Tipo de préstamo", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OpcionRadio("Consulta en sala", mode == "Sala") { mode = "Sala" }
            OpcionRadio("Préstamo para llevar a casa", mode == "Domicilio") { mode = "Domicilio" }
            if (mode == "Domicilio") {
                if (book?.prestamoDomicilio == false) Text("Este ejemplar está habilitado solo para consulta en sala.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Campo(address, { address = it }, "Dirección de entrega *"); Campo(city, { city = it }, "Ciudad / municipio *"); Campo(phone, { phone = it }, "Teléfono de contacto *", KeyboardType.Phone)
            }
            if (mode == "Sala") {
                OutlinedButton(onClick = {
                    val ahora = Calendar.getInstance()
                    TimePickerDialog(context, { _, hour, minute -> time = formarHora(hour, minute) }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true).show()
                }, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Hora de devolución en sala: $time") }
            } else {
                OutlinedButton(onClick = {
                    val max = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, perfilSeleccionado?.diasPrestamo ?: 14) }
                    DatePickerDialog(context, { _, y, m, d -> date = formarFecha(y, m, d) }, max.get(Calendar.YEAR), max.get(Calendar.MONTH), max.get(Calendar.DAY_OF_MONTH)).apply { datePicker.maxDate = max.timeInMillis }.show()
                }, Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Fecha de devolución: ${mostrarFecha(date)} · máximo ${perfilSeleccionado?.diasPrestamo ?: 14} días") }
            }
            val domicilioValido = mode == "Sala" || (address.isNotBlank() && city.isNotBlank() && phone.isNotBlank() && book?.prestamoDomicilio == true && perfilSeleccionado?.soloSala != true)
            Button(onClick = { val b = book; val r = reader; if (b != null && r != null) { alPrestar(b.identificador, r.identificador, if (mode == "Sala") fechaActual() else date, time, mode, address.trim(), city.trim(), phone.trim()); book = null; reader = null; address = ""; city = ""; phone = ""; mode = "Sala" } }, enabled = book != null && reader != null && disponibles.isNotEmpty() && domicilioValido && perfilSeleccionado?.suspendido != true, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Confirmar préstamo") }
        }
        Spacer(Modifier.height(24.dp)); EncabezadoSeccion("En circulación", "${active.size} préstamos activos")
        if (active.isEmpty()) EstadoVacio("No hay préstamos activos ahora.")
        active.forEach { loan ->
            val b = libros.find { it.identificador == loan.identificadorLibro }; val r = lectores.find { it.identificador == loan.identificadorLector }; val overdue = prestamoVencido(loan)
            val fechaLimite = if (loan.modalidad == "Sala" && loan.horaDevolucion.isNotBlank()) "hoy a las ${loan.horaDevolucion}" else mostrarFecha(loan.fechaDevolucion)
            TarjetaElemento(b?.titulo ?: "Libro", "${r?.nombre ?: "Lector"} · ${loan.modalidad} · vence $fechaLimite", if (overdue) "Vencido" else "Activo", !overdue, if (loan.modalidad == "Domicilio") "${loan.direccion}, ${loan.ciudad}" else "")
            TextButton(onClick = { alDevolver(loan) }) { Text("Registrar devolución") }
        }
        if (returned.isNotEmpty()) { Spacer(Modifier.height(16.dp)); EncabezadoSeccion("Historial de devoluciones", "${returned.size} completadas"); returned.asReversed().forEach { loan ->
            val entregaReal = if (loan.fechaEntregaReal.isBlank()) "sin fecha" else if (loan.modalidad == "Sala") "${mostrarFecha(loan.fechaEntregaReal)} a las ${loan.horaEntregaReal}" else mostrarFecha(loan.fechaEntregaReal)
            val limite = if (loan.modalidad == "Sala" && loan.horaDevolucion.isNotBlank()) "hoy a las ${loan.horaDevolucion}" else mostrarFecha(loan.fechaDevolucion)
            TarjetaElemento(libros.find { it.identificador == loan.identificadorLibro }?.titulo ?: "Libro", "Devuelto por ${lectores.find { it.identificador == loan.identificadorLector }?.nombre ?: "lector"} · entrega $entregaReal · vencía $limite", "Devuelto", true)
        } }
    }
}

@Composable private fun Campo(value: String, onValueChange: (String) -> Unit, label: String, keyboard: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value, onValueChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = keyboard))
}
@Composable private fun OpcionRadio(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onClick); TextButton(onClick = onClick) { Text(text) } }
}
@Composable private fun EncabezadoSeccion(titulo: String, subtitulo: String) {
    Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
}
@Composable private fun TarjetaElemento(titulo: String, subtitulo: String, detalle: String? = null, positivo: Boolean = false, extra: String = "") {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) { Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); if (subtitulo.isNotBlank()) Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant); if (extra.isNotBlank()) Text(extra, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (!detalle.isNullOrBlank()) { Spacer(Modifier.width(8.dp)); Text(detalle, color = if (positivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge) }
        }
    }
}
@Composable private fun EstadoVacio(texto: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text(texto, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSecondaryContainer) }
}

private suspend fun buscarOpenLibrary(q: String): List<ResultadoOpenLibrary> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(q, "UTF-8")
    val connection = (URL("https://openlibrary.org/search.json?q=$encoded&limit=8&fields=title,author_name,isbn,publisher,first_publish_year,subject").openConnection() as HttpURLConnection).apply { connectTimeout = 8000; readTimeout = 8000; requestMethod = "GET"; setRequestProperty("User-Agent", "BibliotecaApp/1.0 (catalog search)") }
    try {
        val root = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }); val docs = root.optJSONArray("docs") ?: return@withContext emptyList()
        (0 until docs.length()).mapNotNull { i ->
            val item = docs.optJSONObject(i) ?: return@mapNotNull null
            fun first(name: String): String = item.optJSONArray(name)?.optString(0)?.takeIf { it != "null" }.orEmpty()
            val title = item.optString("title").takeIf { it != "null" }.orEmpty()
            if (title.isBlank()) null else ResultadoOpenLibrary(title, item.optJSONArray("author_name")?.let { a -> (0 until minOf(a.length(), 2)).joinToString(", ") { a.optString(it) } }.orEmpty(), first("isbn"), first("publisher"), item.optInt("first_publish_year").takeIf { it > 0 }?.toString().orEmpty(), first("subject"))
        }
    } finally { connection.disconnect() }
}

private fun fechaActual(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun horaActual(): String = SimpleDateFormat("HH:mm", Locale.US).format(Date())
private fun horaPredeterminadaDevolucion(): String {
    val c = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) }
    return if (SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time) == fechaActual()) formarHora(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)) else "23:59"
}
private fun mostrarFecha(fecha: String): String = try { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CO")).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha)!!) } catch (_: Exception) { fecha }
private fun fechaPredeterminadaDevolucion(dias: Int = 14): String { val c = Calendar.getInstance(); c.add(Calendar.DAY_OF_YEAR, dias); return formarFecha(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)) }
private fun formarFecha(anio: Int, mes: Int, dia: Int): String = String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
private fun formarHora(hora: Int, minuto: Int): String = String.format(Locale.US, "%02d:%02d", hora, minuto)
