package com.example.pacantsdowngrade

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.LruCache
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.ArrayDeque
import java.util.concurrent.Executors
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class ActividadPrincipal : ComponentActivity() {
    private lateinit var vistaColonia: VistaColonia

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        vistaColonia = VistaColonia(this)
        setContentView(vistaColonia)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = vistaColonia.manejarRetroceso()
        })
    }

    override fun onDestroy() {
        if (::vistaColonia.isInitialized) vistaColonia.liberarRecursos()
        super.onDestroy()
    }
}

private enum class PanelColonia { NINGUNO, MERCADO, MEJORAS }
private enum class PantallaPacAnts { TITULO, PARTIDAS, JUEGO, PAUSA, GUARDAR_SALIR }
private data class ZonaTactil(val limites: RectF, val accion: () -> Unit)
private class FragmentoMapa(val bitmap: Bitmap, val celdasAbiertas: BooleanArray) {
    val lienzo = Canvas(bitmap)
}
private data class ResumenPartida(val nivelBase: Int, val puntos: Int, val poblacion: Int, val frutas: Int)

private class VistaColonia(contexto: Context) : View(contexto) {
    private companion object {
        const val CELDAS_POR_FRAGMENTO = 16
        const val PIXELES_POR_CELDA = 32
        const val PIXELES_POR_FRAGMENTO = CELDAS_POR_FRAGMENTO * PIXELES_POR_CELDA
    }

    private val modelo = ModeloColonia()
    private val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pincelMapa = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val sprites = HashMap<String, Bitmap>()
    private val zonasTactiles = mutableListOf<ZonaTactil>()
    private val baseDatos = BaseDatosColonia.obtener(contexto)
    private val alcanceBaseDatos = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bloqueoBaseDatos = Mutex()
    private val gestor = Handler(Looper.getMainLooper())
    private val fuente = Typeface.create("casual", Typeface.BOLD)
    private val fondoMapa = RectF()
    private val destinoSprite = RectF()
    private val destinoFragmentoMapa = RectF()
    private val marcoPanel = RectF()
    private val filaPanel = RectF()
    private val botonPanel = RectF()
    private val tarjetaMejora = RectF()
    private val botonMejora = RectF()
    private val limitesMensaje = RectF()
    private val zonaComenzar = ZonaTactil(RectF()) { pantalla = PantallaPacAnts.PARTIDAS; invalidate() }
    private val zonaVolverTitulo = ZonaTactil(RectF()) { pantalla = PantallaPacAnts.TITULO; invalidate() }
    private val zonaCancelarGuardar = ZonaTactil(RectF()) { pantalla = PantallaPacAnts.PAUSA; invalidate() }
    private val zonasSlots = Array(3) { indice -> ZonaTactil(RectF()) { pulsarSlot(indice + 1) } }
    private val zonasBorrarSlots = Array(3) { indice -> ZonaTactil(RectF()) { confirmarBorrarSlot(indice + 1) } }
    private val zonasPausa = Array(2) { indice -> ZonaTactil(RectF()) { pulsarPausa(indice) } }
    private val etiquetasBarra = arrayOf("Todos", "Exploradores", "Obreros", "Defensores", "Frutas", "Mejoras", "−", "+", "Reiniciar", "Pausa")
    private val zonasBarra = Array(etiquetasBarra.size) { indice ->
        ZonaTactil(RectF()) { pulsarBarra(indice) }
    }
    private val zonasMercado = Array(modelo.tiposFruta.size) { indice ->
        ZonaTactil(RectF()) { venderFruta(indice) }
    }
    private val zonasMejoras = Array(MejoraColonia.entries.size) { indice ->
        ZonaTactil(RectF()) { comprarMejora(MejoraColonia.entries[indice]) }
    }
    private val zonaCerrarPanel = ZonaTactil(RectF()) {
        panel = PanelColonia.NINGUNO
        invalidate()
    }
    private val spritesFruta = HashMap<String, Bitmap>()
    private val cacheFragmentosMapa = object : LruCache<Long, FragmentoMapa>(24 * 1024) {
        override fun sizeOf(key: Long, value: FragmentoMapa): Int = value.bitmap.byteCount / 1024

        override fun entryRemoved(evicted: Boolean, key: Long, oldValue: FragmentoMapa, newValue: FragmentoMapa?) {
            if (oldValue.bitmap !== newValue?.bitmap) oldValue.bitmap.recycle()
        }
    }
    private val ejecutorFragmentosMapa = Executors.newSingleThreadExecutor { tarea ->
        Thread(tarea, "PacAnts-Mapa").apply { isDaemon = true }
    }
    private val fragmentosEnCreacion = HashSet<Long>()
    private val celdasMapaModificadas = ArrayList<Int>(32)
    private var revisionMapaEnPantalla = modelo.revisionEstructuraMapa
    private val spritesPacman = Array(RolPacman.entries.size) {
        Array(DireccionPacman.entries.size) { arrayOfNulls<Bitmap>(3) }
    }
    private val spritesMuerte = Array(RolPacman.entries.size) { arrayOfNulls<Bitmap>(11) }
    private val spritesFantasma = Array(modelo.tiposFantasma.size) {
        Array(DireccionPacman.entries.size) { arrayOfNulls<Bitmap>(2) }
    }
    private val spritesAsustado = arrayOfNulls<Bitmap>(2)
    private val indiceFantasmaPorTipo = modelo.tiposFantasma.withIndex().associate { it.value to it.index }
    private var valorPuntosEnCabecera = Int.MIN_VALUE
    private var textoPuntosEnCabecera = ""
    private var nivelBaseEnCabecera = Int.MIN_VALUE
    private var textoBaseEnCabecera = ""
    private var nivelBaseDentroColonia = Int.MIN_VALUE
    private var textoNivelBaseDentroColonia = ""
    private var poblacionEnCabecera = Int.MIN_VALUE
    private var activosEnCabecera = Int.MIN_VALUE
    private var cicloPoblacionEnCabecera = Int.MIN_VALUE
    private var textoPoblacionEnCabecera = ""
    private var existenciasEnCabecera = Int.MIN_VALUE
    private var capacidadEnCabecera = Int.MIN_VALUE
    private var textoAlmacenEnCabecera = ""
    private var anchoCelda = 16f
    private var zoom = 1f
    private var zoomObjetivo = 1f
    private var detalleReducido = false
    private var columnaVisibleMinima = 0
    private var columnaVisibleMaxima = 0
    private var filaVisibleMinima = 0
    private var filaVisibleMaxima = 0
    private var camaraColumna = modelo.columnaBase.toFloat()
    private var camaraFila = modelo.filaBase.toFloat()
    private var objetivoColumna = camaraColumna
    private var objetivoFila = camaraFila
    private var desplazamientoMapaX = 0
    private var desplazamientoMapaY = 0
    private var rolEnVista: RolPacman? = null
    private var pacmanSeguido: Int? = null
    private var pacmanEnSeguimiento: PacmanColono? = null
    private val indicesVistaPorRol = mutableMapOf<RolPacman, Int>()
    private var panel = PanelColonia.NINGUNO
    private var partidaLista = false
    private var pantalla = PantallaPacAnts.TITULO
    private var slotsListos = false
    private var slotActual = 1
    private val partidasGuardadas = arrayOfNulls<PartidaColonia>(3)
    private val resumenesPartida = arrayOfNulls<ResumenPartida>(3)
    private var arrastrando = false
    private var inicioX = 0f
    private var inicioY = 0f
    private var distanciaPinza = 0f
    private var ultimoAvance = 0L
    private var ultimoFotograma = 0L
    private var ultimoGuardado = 0
    @Volatile private var generacionPartida = 0
    private var mensaje = "La colonia despierta"
    private var mensajeHasta = 0L

    fun liberarRecursos() {
        ejecutorFragmentosMapa.shutdownNow()
        fragmentosEnCreacion.clear()
    }

    private val bucle = object : Runnable {
        override fun run() {
            val inicioFotograma = SystemClock.uptimeMillis()
            val ahora = System.currentTimeMillis()
            val deltaFotograma = if (ultimoFotograma == 0L) 32L else (ahora - ultimoFotograma).coerceIn(1L, 100L)
            ultimoFotograma = ahora
            if (partidaLista && pantalla == PantallaPacAnts.JUEGO) {
                val deltaActualizacion = (ahora - ultimoAvance).coerceIn(1L, 100L)
                val capacidadAnterior = modelo.capacidadAlmacen
                val inventarioAnterior = modelo.frutasAlmacenadas
                var indicePacman = 0
                while (indicePacman < modelo.pacmanes.size) {
                    val pacman = modelo.pacmanes[indicePacman++]
                    pacman.estabaAgotadoAntesDelAvance = pacman.agotado
                }
                modelo.avanzar(deltaActualizacion)
                val inventarioActual = modelo.frutasAlmacenadas
                if (inventarioAnterior < capacidadAnterior && inventarioActual >= modelo.capacidadAlmacen) {
                    mensaje = "Almacen lleno: los exploradores regresan a la base"
                    mensajeHasta = ahora + 3000L
                }
                var nuevoAgotado: PacmanColono? = null
                indicePacman = 0
                while (indicePacman < modelo.pacmanes.size && nuevoAgotado == null) {
                    val pacman = modelo.pacmanes[indicePacman++]
                    if (pacman.agotado && !pacman.estabaAgotadoAntesDelAvance) nuevoAgotado = pacman
                }
                if (nuevoAgotado != null) {
                    val especie = if (nuevoAgotado.rol == RolPacman.OBRERO) "Un obrero" else "Un defensor"
                    mensaje = "$especie agotado: regresa a la base a descansar"
                    mensajeHasta = ahora + 2600L
                }
                val deltaX = modelo.desplazamientoCoordenadasX - desplazamientoMapaX
                val deltaY = modelo.desplazamientoCoordenadasY - desplazamientoMapaY
                if (deltaX != 0 || deltaY != 0) {
                    camaraColumna += deltaX
                    objetivoColumna += deltaX
                    camaraFila += deltaY
                    objetivoFila += deltaY
                    desplazamientoMapaX = modelo.desplazamientoCoordenadasX
                    desplazamientoMapaY = modelo.desplazamientoCoordenadasY
                }
                ultimoAvance = ahora
                if (modelo.ciclo - ultimoGuardado >= 10) guardarPartida()
            }
            if (pantalla == PantallaPacAnts.JUEGO) {
                seguirColonia(deltaFotograma)
            }
            invalidate()
            val intervaloFotograma = if (zoom < 1f) 34L else 32L
            val tiempoRestante = intervaloFotograma - (SystemClock.uptimeMillis() - inicioFotograma)
            gestor.postDelayed(this, tiempoRestante.coerceAtLeast(1L))
        }
    }

    init {
        gestor.post {
            cargarSprites()
            invalidate()
        }
        isFocusable = true
        alcanceBaseDatos.launch {
            val partidas = runCatching { baseDatos.partidaColoniaDao().cargarPartidas() }.getOrDefault(emptyList())
            gestor.post {
                partidasGuardadas.fill(null)
                resumenesPartida.fill(null)
                partidas.forEach { partida ->
                    if (partida.identificador in 1..3) {
                        partidasGuardadas[partida.identificador - 1] = partida
                        resumenesPartida[partida.identificador - 1] = crearResumen(partida)
                    }
                }
                slotsListos = true
                invalidate()
            }
        }
        gestor.post(bucle)
    }

    private fun cargarSprites() {
        val nombres = buildList {
            RolPacman.entries.forEach { rol ->
                DireccionPacman.entries.forEach { direccion ->
                    for (fotograma in 1..3) add("${rol.prefijoSprite}_${direccion.clave}_$fotograma")
                }
                for (fotograma in 1..11) add("${rol.prefijoSprite}_death_$fotograma")
            }
            modelo.tiposFruta.forEach { add("fruit_${it.clave}") }
            modelo.tiposFantasma.forEach { tipo ->
                DireccionPacman.entries.forEach { direccion -> add("ghost_${tipo}_${direccion.clave}"); add("ghost_${tipo}_${direccion.clave}_2") }
            }
            add("ghost_scared_blue_1"); add("ghost_scared_blue_2")
            add("ghost_scared_white_1"); add("ghost_scared_white_2")
        }
        nombres.distinct().forEach { nombre ->
            val identificador = identificadoresSprites[nombre] ?: return@forEach
            runCatching {
                BitmapFactory.decodeResource(resources, identificador)?.let { bitmap ->
                    sprites[nombre] = quitarFondoNegro(bitmap)
                    bitmap.recycle()
                }
            }
        }
        for (rol in RolPacman.entries) {
            for (direccion in DireccionPacman.entries) {
                for (fotograma in 1..3) {
                    spritesPacman[rol.ordinal][direccion.ordinal][fotograma - 1] =
                        sprites["${rol.prefijoSprite}_${direccion.clave}_$fotograma"]
                }
            }
            for (fotograma in 1..11) {
                spritesMuerte[rol.ordinal][fotograma - 1] = sprites["${rol.prefijoSprite}_death_$fotograma"]
            }
        }
        modelo.tiposFruta.forEach { tipo ->
            spritesFruta[tipo.clave] = sprites["fruit_${tipo.clave}"] ?: return@forEach
        }
        for ((indiceFantasma, tipo) in modelo.tiposFantasma.withIndex()) {
            for (direccion in DireccionPacman.entries) {
                spritesFantasma[indiceFantasma][direccion.ordinal][0] =
                    sprites["ghost_${tipo}_${direccion.clave}"]
                spritesFantasma[indiceFantasma][direccion.ordinal][1] =
                    sprites["ghost_${tipo}_${direccion.clave}_2"]
            }
        }
        spritesAsustado[0] = sprites["ghost_scared_blue_1"]
        spritesAsustado[1] = sprites["ghost_scared_blue_2"]
    }

    /** Los recursos originales tienen el negro opaco como fondo; lo vuelve transparente sin tocar colores oscuros internos. */
    private fun quitarFondoNegro(bitmap: Bitmap): Bitmap {
        val ancho = bitmap.width
        val alto = bitmap.height
        val pixeles = IntArray(ancho * alto)
        bitmap.getPixels(pixeles, 0, ancho, 0, 0, ancho, alto)
        val visitados = BooleanArray(pixeles.size)
        val pendientes = ArrayDeque<Int>()
        fun esFondo(indice: Int): Boolean {
            val color = pixeles[indice]
            return Color.alpha(color) > 0 && Color.red(color) < 20 && Color.green(color) < 20 && Color.blue(color) < 20
        }
        fun agregar(indice: Int) {
            if (!visitados[indice] && esFondo(indice)) {
                visitados[indice] = true
                pendientes.addLast(indice)
            }
        }
        for (x in 0 until ancho) {
            agregar(x)
            agregar((alto - 1) * ancho + x)
        }
        for (y in 0 until alto) {
            agregar(y * ancho)
            agregar(y * ancho + ancho - 1)
        }
        while (pendientes.isNotEmpty()) {
            val indice = pendientes.removeFirst()
            pixeles[indice] = Color.TRANSPARENT
            val x = indice % ancho
            val y = indice / ancho
            if (x > 0) agregar(indice - 1)
            if (x + 1 < ancho) agregar(indice + 1)
            if (y > 0) agregar(indice - ancho)
            if (y + 1 < alto) agregar(indice + ancho)
        }
        return createBitmap(ancho, alto, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixeles, 0, ancho, 0, 0, ancho, alto)
        }
    }

    private fun guardarPartida(identificador: Int = slotActual, alCompletar: (() -> Unit)? = null) {
        if (identificador !in 1..3 || !partidaLista) return
        if (identificador == slotActual) ultimoGuardado = modelo.ciclo
        val generacionGuardado = generacionPartida
        val partida = runCatching {
            PartidaColonia(identificador = identificador, estadoJson = modelo.serializar())
        }.getOrNull() ?: return
        alcanceBaseDatos.launch {
            val guardada = bloqueoBaseDatos.withLock {
                generacionGuardado == generacionPartida &&
                    runCatching { baseDatos.partidaColoniaDao().guardarPartida(partida) }.isSuccess
            }
            if (guardada) gestor.post {
                partidasGuardadas[identificador - 1] = partida
                resumenesPartida[identificador - 1] = crearResumen(partida)
                alCompletar?.invoke()
                invalidate()
            }
        }
    }

    private fun pulsarSlot(identificador: Int) {
        if (!slotsListos || identificador !in 1..3) return
        if (pantalla == PantallaPacAnts.GUARDAR_SALIR) {
            confirmarDestinoGuardado(identificador)
            return
        }
        val partida = partidasGuardadas[identificador - 1]
        if (partida == null) {
            modelo.nuevaPartida()
        } else if (!modelo.restaurar(partida.estadoJson)) {
            AlertDialog.Builder(context)
                .setTitle("No se pudo abrir el slot")
                .setMessage("El guardado está dañado y no se ha modificado.")
                .setPositiveButton("Aceptar", null)
                .show()
            return
        }
        slotActual = identificador
        partidaLista = true
        pantalla = PantallaPacAnts.JUEGO
        panel = PanelColonia.NINGUNO
        rolEnVista = null
        pacmanSeguido = null
        pacmanEnSeguimiento = null
        indicesVistaPorRol.clear()
        desplazamientoMapaX = modelo.desplazamientoCoordenadasX
        desplazamientoMapaY = modelo.desplazamientoCoordenadasY
        camaraColumna = modelo.columnaBase.toFloat()
        camaraFila = modelo.filaBase.toFloat()
        objetivoColumna = camaraColumna
        objetivoFila = camaraFila
        zoom = 1f
        zoomObjetivo = 1.18f
        ultimoAvance = System.currentTimeMillis()
        ultimoGuardado = modelo.ciclo
        if (partida == null) guardarPartida(identificador)
        invalidate()
    }

    private fun confirmarDestinoGuardado(identificador: Int) {
        val destinoOcupado = partidasGuardadas[identificador - 1] != null
        if (destinoOcupado && identificador != slotActual) {
            AlertDialog.Builder(context)
                .setTitle("Sobrescribir partida")
                .setMessage("El slot $identificador ya tiene una colonia. Si continúas, su guardado será reemplazado por la partida actual.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sobrescribir") { _, _ -> guardarYSalir(identificador) }
                .show()
        } else {
            guardarYSalir(identificador)
        }
    }

    private fun guardarYSalir(identificador: Int) {
        guardarPartida(identificador) {
            slotActual = identificador
            partidaLista = false
            panel = PanelColonia.NINGUNO
            pantalla = PantallaPacAnts.PARTIDAS
            invalidate()
        }
    }

    private fun confirmarBorrarSlot(identificador: Int) {
        if (partidasGuardadas[identificador - 1] == null) return
        AlertDialog.Builder(context)
            .setTitle("Borrar slot $identificador")
            .setMessage("Se eliminará esta partida guardada. Esta acción no se puede deshacer.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Borrar") { _, _ -> borrarSlot(identificador) }
            .show()
    }

    private fun borrarSlot(identificador: Int) {
        alcanceBaseDatos.launch {
            val borrada = runCatching {
                baseDatos.partidaColoniaDao().borrarPartida(identificador)
            }.isSuccess
            gestor.post {
                if (borrada) {
                    partidasGuardadas[identificador - 1] = null
                    resumenesPartida[identificador - 1] = null
                }
                invalidate()
            }
        }
    }

    private fun pulsarPausa(indice: Int) {
        when (indice) {
            0 -> pantalla = PantallaPacAnts.JUEGO
            1 -> pantalla = PantallaPacAnts.GUARDAR_SALIR
        }
        invalidate()
    }

    fun manejarRetroceso() {
        pantalla = when (pantalla) {
            PantallaPacAnts.TITULO -> {
                (context as? Activity)?.finish()
                return
            }
            PantallaPacAnts.PARTIDAS -> PantallaPacAnts.TITULO
            PantallaPacAnts.JUEGO -> PantallaPacAnts.PAUSA
            PantallaPacAnts.PAUSA -> PantallaPacAnts.JUEGO
            PantallaPacAnts.GUARDAR_SALIR -> PantallaPacAnts.PAUSA
        }
        invalidate()
    }

    private fun seguirColonia(deltaMs: Long) {
        pacmanEnSeguimiento?.let { pacman ->
            objetivoColumna = pacman.columna.toFloat()
            objetivoFila = pacman.fila.toFloat()
        }
        val factorCamara = 1f - 0.86f.pow(deltaMs / 32f)
        val factorZoom = 1f - 0.88f.pow(deltaMs / 32f)
        camaraColumna += (objetivoColumna - camaraColumna) * factorCamara
        camaraFila += (objetivoFila - camaraFila) * factorCamara
        zoom += (zoomObjetivo - zoom) * factorZoom
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val densidad = resources.displayMetrics.density
        zonasTactiles.clear()
        when (pantalla) {
            PantallaPacAnts.TITULO -> {
                dibujarFondoMenu(canvas, densidad)
                dibujarTitulo(canvas, densidad)
                return
            }
            PantallaPacAnts.PARTIDAS, PantallaPacAnts.GUARDAR_SALIR -> {
                dibujarFondoMenu(canvas, densidad)
                dibujarSelectorPartidas(canvas, densidad)
                return
            }
            else -> Unit
        }
        val altoCabecera = 49f * densidad
        val altoBarra = 68f * densidad
        val altoEscena = height - altoCabecera - altoBarra
        fondoMapa.set(0f, altoCabecera, width.toFloat(), altoCabecera + altoEscena)
        canvas.drawColor(Color.rgb(13, 16, 32))
        anchoCelda = min(fondoMapa.width() / 34f, fondoMapa.height() / 18f) * zoom
        detalleReducido = zoom < 1f
        val medioAnchoMapa = fondoMapa.width() / (2f * anchoCelda)
        val medioAltoMapa = fondoMapa.height() / (2f * anchoCelda)
        columnaVisibleMinima = (camaraColumna - medioAnchoMapa).toInt() - 1
        columnaVisibleMaxima = (camaraColumna + medioAnchoMapa).toInt() + 1
        filaVisibleMinima = (camaraFila - medioAltoMapa).toInt() - 1
        filaVisibleMaxima = (camaraFila + medioAltoMapa).toInt() + 1
        dibujarMapa(canvas)
        canvas.withClip(fondoMapa.left, fondoMapa.top, fondoMapa.right, fondoMapa.bottom) {
            dibujarColonia(this)
            dibujarFrutas(this)
            dibujarFantasmas(this)
            dibujarPacmanes(this)
        }
        dibujarCabecera(canvas, altoCabecera, densidad)
        dibujarBarra(canvas, altoCabecera + altoEscena, altoBarra, densidad)
        if (panel != PanelColonia.NINGUNO) dibujarPanel(canvas, densidad)
        if (pantalla == PantallaPacAnts.PAUSA) dibujarMenuPausa(canvas, densidad)
        dibujarMensaje(canvas, densidad)
    }

    private fun dibujarFondoMenu(canvas: Canvas, densidad: Float) {
        canvas.drawColor(Color.rgb(17, 88, 179))
        pincel.color = Color.rgb(26, 124, 216)
        canvas.drawCircle(width * .12f, height * .12f, height * .31f, pincel)
        pincel.color = Color.rgb(24, 109, 197)
        canvas.drawCircle(width * .89f, height * .78f, height * .43f, pincel)
        pincel.color = Color.rgb(43, 154, 222)
        canvas.drawOval(width * .03f, height * .69f, width * .97f, height * 1.18f, pincel)
        pincel.color = Color.rgb(32, 127, 107)
        canvas.drawRect(0f, height * .87f, width.toFloat(), height.toFloat(), pincel)
        pincel.color = Color.rgb(49, 170, 113)
        canvas.drawRect(0f, height * .91f, width.toFloat(), height * .94f, pincel)
        for (indice in 0..10) {
            val x = width * (indice + .5f) / 11f
            val y = height * (.89f + (indice % 3) * .018f)
            pincel.color = if (indice % 2 == 0) Color.rgb(246, 202, 75) else Color.rgb(130, 219, 122)
            canvas.drawCircle(x, y, 2.2f * densidad, pincel)
        }
        pincel.color = Color.argb(170, 225, 249, 255)
        for (indice in 0..17) {
            val x = width * ((indice * 37 % 101) / 100f)
            val y = height * ((indice * 23 % 73) / 100f)
            canvas.drawCircle(x, y, (1.2f + indice % 3) * densidad, pincel)
        }
        pincel.color = Color.argb(75, 255, 255, 255)
        for (indice in 0..5) {
            val x = width * (indice + 1) / 7f
            val y = height * .83f + (indice % 2) * 5f * densidad
            canvas.drawOval(x, y, x + 90f * densidad, y + 15f * densidad, pincel)
        }
    }

    private fun dibujarTitulo(canvas: Canvas, densidad: Float) {
        zonasTactiles.clear()
        val centroX = width / 2f
        val logo = sprites["pacman_right_2"]
        logo?.let {
            val lado = min(height * .27f, 112f * densidad)
            destinoSprite.set(centroX - lado / 2f, height * .08f, centroX + lado / 2f, height * .08f + lado)
            canvas.drawBitmap(it, null, destinoSprite, pincel)
        }
        texto(canvas, "PACANTS", centroX, height * .53f, 39f * densidad, Color.WHITE, Paint.Align.CENTER)
        texto(canvas, "LA COLONIA DESPIERTA", centroX, height * .60f, 15f * densidad, Color.rgb(255, 225, 94), Paint.Align.CENTER)
        val boton = RectF(width * .34f, height * .68f, width * .66f, height * .81f)
        dibujarBotonMenu(canvas, boton, Color.rgb(255, 208, 55), "PARTIDAS", 19f * densidad)
        zonaComenzar.limites.set(boton)
        zonasTactiles.add(zonaComenzar)
        dibujarPacmanesDecorativos(canvas, densidad, height * .865f)
        texto(canvas, "Gestiona exploradores, obreros y defensores", centroX, height * .97f, 10f * densidad, Color.rgb(222, 245, 255), Paint.Align.CENTER)
    }

    private fun dibujarPacmanesDecorativos(canvas: Canvas, densidad: Float, y: Float) {
        val nombres = arrayOf("pacman_right_2", "orange_pacman_right_2", "red_pacman_right_2")
        nombres.forEachIndexed { indice, nombre ->
            val bitmap = sprites[nombre] ?: return@forEachIndexed
            val lado = 31f * densidad
            val x = width * (.42f + indice * .08f)
            destinoSprite.set(x - lado / 2, y - lado / 2, x + lado / 2, y + lado / 2)
            canvas.drawBitmap(bitmap, null, destinoSprite, pincel)
        }
    }

    private fun dibujarSelectorPartidas(canvas: Canvas, densidad: Float) {
        zonasTactiles.clear()
        val guardarSalida = pantalla == PantallaPacAnts.GUARDAR_SALIR
        texto(canvas, if (guardarSalida) "GUARDAR Y SALIR" else "TUS COLONIAS", width / 2f, height * .14f, 28f * densidad, Color.WHITE, Paint.Align.CENTER)
        texto(
            canvas,
            if (guardarSalida) "Elige el slot donde guardar esta colonia" else "Elige una partida o comienza una colonia nueva",
            width / 2f, height * .20f, 12f * densidad, Color.rgb(223, 246, 255), Paint.Align.CENTER,
        )
        val margen = 22f * densidad
        val separacion = 12f * densidad
        val anchoTarjeta = (width - margen * 2f - separacion * 2f) / 3f
        val arriba = height * .29f
        val abajo = height * .78f
        for (indice in 0..2) {
            val partida = partidasGuardadas[indice]
            val izquierda = margen + indice * (anchoTarjeta + separacion)
            val tarjeta = zonasSlots[indice].limites
            tarjeta.set(izquierda, arriba, izquierda + anchoTarjeta, abajo)
            if (slotsListos) zonasTactiles.add(zonasSlots[indice])
            pincel.color = when (indice) {
                0 -> Color.rgb(255, 247, 224)
                1 -> Color.rgb(239, 252, 255)
                else -> Color.rgb(250, 241, 255)
            }
            canvas.drawRoundRect(tarjeta, 20f * densidad, 20f * densidad, pincel)
            pincel.style = Paint.Style.STROKE
            pincel.strokeWidth = 3f * densidad
            pincel.color = arrayOf(Color.rgb(255, 202, 59), Color.rgb(63, 208, 205), Color.rgb(180, 127, 236))[indice]
            canvas.drawRoundRect(tarjeta, 20f * densidad, 20f * densidad, pincel)
            pincel.style = Paint.Style.FILL
            pincel.color = Color.rgb(230, 69, 72)
            canvas.drawCircle(tarjeta.left + 24f * densidad, tarjeta.top + 25f * densidad, 15f * densidad, pincel)
            texto(canvas, "${indice + 1}", tarjeta.left + 24f * densidad, tarjeta.top + 31f * densidad, 15f * densidad, Color.WHITE, Paint.Align.CENTER)
            texto(canvas, "SLOT ${indice + 1}", tarjeta.left + 48f * densidad, tarjeta.top + 30f * densidad, 12f * densidad, Color.rgb(47, 48, 78))
            if (partida == null) {
                texto(canvas, "New", tarjeta.centerX(), tarjeta.centerY() + 8f * densidad, 31f * densidad, Color.rgb(218, 65, 69), Paint.Align.CENTER)
                texto(canvas, "Pulsa para empezar", tarjeta.centerX(), tarjeta.centerY() + 34f * densidad, 10f * densidad, Color.rgb(79, 78, 103), Paint.Align.CENTER)
            } else {
                dibujarPacmanSlot(canvas, tarjeta.centerX(), tarjeta.top + tarjeta.height() * .34f, 38f * densidad)
                val resumen = resumenesPartida[indice] ?: crearResumen(partida).also { resumenesPartida[indice] = it }
                texto(canvas, "Base ${resumen.nivelBase}  ·  ${resumen.puntos} pts", tarjeta.centerX(), tarjeta.top + tarjeta.height() * .60f, 12f * densidad, Color.rgb(42, 63, 100), Paint.Align.CENTER)
                texto(canvas, "${resumen.poblacion} Pac-Man  ·  ${resumen.frutas} frutas", tarjeta.centerX(), tarjeta.top + tarjeta.height() * .69f, 10f * densidad, Color.rgb(73, 89, 112), Paint.Align.CENTER)
                if (!guardarSalida) {
                    val borrar = RectF(tarjeta.right - 40f * densidad, tarjeta.top + 9f * densidad, tarjeta.right - 8f * densidad, tarjeta.top + 41f * densidad)
                    pincel.color = Color.rgb(184, 54, 68)
                    canvas.drawRoundRect(borrar, 10f * densidad, 10f * densidad, pincel)
                    texto(canvas, "×", borrar.centerX(), borrar.centerY() + 7f * densidad, 22f * densidad, Color.WHITE, Paint.Align.CENTER)
                    zonasBorrarSlots[indice].limites.set(borrar)
                    zonasTactiles.add(zonasBorrarSlots[indice])
                }
            }
        }
        if (!slotsListos) texto(canvas, "Cargando partidas…", width / 2f, height * .84f, 13f * densidad, Color.WHITE, Paint.Align.CENTER)
        val botonTexto = if (guardarSalida) "CANCELAR" else "VOLVER"
        val limitesBoton = RectF(width * .42f, height * .85f, width * .58f, height * .95f)
        dibujarBotonMenu(canvas, limitesBoton, Color.rgb(55, 72, 113), botonTexto, 13f * densidad)
        val zonaVolver = if (guardarSalida) zonaCancelarGuardar else zonaVolverTitulo
        zonaVolver.limites.set(limitesBoton)
        zonasTactiles.add(zonaVolver)
    }

    private fun dibujarPacmanSlot(canvas: Canvas, centroX: Float, centroY: Float, lado: Float) {
        val bitmap = sprites["pacman_right_2"] ?: return
        destinoSprite.set(centroX - lado / 2, centroY - lado / 2, centroX + lado / 2, centroY + lado / 2)
        canvas.drawBitmap(bitmap, null, destinoSprite, pincel)
    }

    private fun crearResumen(partida: PartidaColonia): ResumenPartida {
        val estado = runCatching { JSONObject(partida.estadoJson) }.getOrNull()
        val inventario = estado?.optJSONObject("inventario")
        var frutas = 0
        if (inventario != null) {
            val claves = inventario.keys()
            while (claves.hasNext()) frutas += inventario.optInt(claves.next(), 0)
        }
        return ResumenPartida(
            nivelBase = estado?.optInt("nivelBase", 1) ?: 1,
            puntos = estado?.optInt("puntos", 0) ?: 0,
            poblacion = estado?.optJSONArray("pacmanes")?.length() ?: 0,
            frutas = frutas,
        )
    }

    private fun dibujarMenuPausa(canvas: Canvas, densidad: Float) {
        zonasTactiles.clear()
        pincel.color = Color.argb(205, 6, 11, 29)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pincel)
        val marco = RectF(width * .31f, height * .18f, width * .69f, height * .83f)
        pincel.color = Color.rgb(38, 45, 89)
        canvas.drawRoundRect(marco, 24f * densidad, 24f * densidad, pincel)
        pincel.style = Paint.Style.STROKE
        pincel.strokeWidth = 3f * densidad
        pincel.color = Color.rgb(82, 229, 201)
        canvas.drawRoundRect(marco, 24f * densidad, 24f * densidad, pincel)
        pincel.style = Paint.Style.FILL
        texto(canvas, "COLONIA EN PAUSA", marco.centerX(), marco.top + 47f * densidad, 22f * densidad, Color.WHITE, Paint.Align.CENTER)
        texto(canvas, "Slot $slotActual  ·  Base ${modelo.nivelBase}  ·  ${modelo.puntos} puntos", marco.centerX(), marco.top + 75f * densidad, 12f * densidad, Color.rgb(196, 228, 255), Paint.Align.CENTER)
        val botones = arrayOf(
            RectF(marco.left + 25f * densidad, marco.top + 112f * densidad, marco.right - 25f * densidad, marco.top + 174f * densidad),
            RectF(marco.left + 25f * densidad, marco.top + 190f * densidad, marco.right - 25f * densidad, marco.top + 252f * densidad),
        )
        dibujarBotonMenu(canvas, botones[0], Color.rgb(50, 177, 124), "REANUDAR", 16f * densidad)
        dibujarBotonMenu(canvas, botones[1], Color.rgb(255, 172, 56), "GUARDAR Y SALIR", 14f * densidad)
        botones.forEachIndexed { indice, limites ->
            zonasPausa[indice].limites.set(limites)
            zonasTactiles.add(zonasPausa[indice])
        }
    }

    private fun dibujarBotonMenu(canvas: Canvas, limites: RectF, color: Int, etiqueta: String, tamano: Float) {
        pincel.color = Color.argb(100, 7, 24, 59)
        canvas.drawRoundRect(limites.left + 2f, limites.top + 4f, limites.right + 2f, limites.bottom + 4f, 16f, 16f, pincel)
        pincel.color = color
        canvas.drawRoundRect(limites, 16f, 16f, pincel)
        texto(canvas, etiqueta, limites.centerX(), limites.centerY() + tamano * .34f, tamano, Color.rgb(35, 38, 64), Paint.Align.CENTER)
    }

    private fun izquierdaMundo() = fondoMapa.centerX() - (camaraColumna + .5f) * anchoCelda
    private fun arribaMundo() = fondoMapa.centerY() - (camaraFila + .5f) * anchoCelda
    private fun izquierdaCelda(columna: Int) = izquierdaMundo() + columna * anchoCelda
    private fun arribaCelda(fila: Int) = arribaMundo() + fila * anchoCelda
    private fun centroCeldaX(columna: Int): Float {
        val izquierda = izquierdaCelda(columna)
        return (izquierda + izquierda + anchoCelda) / 2f
    }
    private fun centroCeldaY(fila: Int): Float {
        val arriba = arribaCelda(fila)
        return (arriba + arriba + anchoCelda) / 2f
    }

    private fun celdaVisible(columna: Int, fila: Int): Boolean =
        columna in columnaVisibleMinima..columnaVisibleMaxima && fila in filaVisibleMinima..filaVisibleMaxima

    private fun dibujarMapa(canvas: Canvas) {
        actualizarFragmentosMapaModificados()
        canvas.withClip(fondoMapa.left, fondoMapa.top, fondoMapa.right, fondoMapa.bottom) {
            val celdasVisiblesX = (fondoMapa.width() / anchoCelda / 2f).toInt() + 2
            val celdasVisiblesY = (fondoMapa.height() / anchoCelda / 2f).toInt() + 2
            val columnaInicial = camaraColumna.toInt() - celdasVisiblesX
            val filaInicial = camaraFila.toInt() - celdasVisiblesY
            val origenX = izquierdaMundo()
            val origenY = arribaMundo()
            val ultimaColumna = columnaInicial + celdasVisiblesX * 2
            val ultimaFila = filaInicial + celdasVisiblesY * 2
            val primerFragmentoX = Math.floorDiv(columnaInicial, CELDAS_POR_FRAGMENTO)
            val primerFragmentoY = Math.floorDiv(filaInicial, CELDAS_POR_FRAGMENTO)
            val ultimoFragmentoX = Math.floorDiv(ultimaColumna, CELDAS_POR_FRAGMENTO)
            val ultimoFragmentoY = Math.floorDiv(ultimaFila, CELDAS_POR_FRAGMENTO)
            for (fragmentoY in primerFragmentoY..ultimoFragmentoY) {
                for (fragmentoX in primerFragmentoX..ultimoFragmentoX) {
                    val clave = (fragmentoY.toLong() shl 32) xor (fragmentoX.toLong() and 0xffffffffL)
                    val columnaMundo = fragmentoX * CELDAS_POR_FRAGMENTO
                    val filaMundo = fragmentoY * CELDAS_POR_FRAGMENTO
                    destinoFragmentoMapa.set(
                        origenX + columnaMundo * anchoCelda,
                        origenY + filaMundo * anchoCelda,
                        origenX + (columnaMundo + CELDAS_POR_FRAGMENTO) * anchoCelda,
                        origenY + (filaMundo + CELDAS_POR_FRAGMENTO) * anchoCelda,
                    )
                    val fragmento = cacheFragmentosMapa.get(clave)
                    if (fragmento == null) {
                        solicitarFragmentoMapa(clave, fragmentoX, fragmentoY)
                        pincelMapa.color = Color.rgb(19, 49, 44)
                        canvas.drawRect(destinoFragmentoMapa, pincelMapa)
                    } else {
                        pincelMapa.color = Color.WHITE
                        canvas.drawBitmap(fragmento.bitmap, null, destinoFragmentoMapa, pincelMapa)
                    }
                }
            }
        }
    }

    private fun solicitarFragmentoMapa(clave: Long, fragmentoX: Int, fragmentoY: Int) {
        if (fragmentosEnCreacion.size >= 48 || !fragmentosEnCreacion.add(clave)) return
        val revision = modelo.revisionEstructuraMapa
        val inicioColumna = fragmentoX * CELDAS_POR_FRAGMENTO
        val inicioFila = fragmentoY * CELDAS_POR_FRAGMENTO
        val celdasAbiertas = BooleanArray(CELDAS_POR_FRAGMENTO * CELDAS_POR_FRAGMENTO)
        for (y in 0 until CELDAS_POR_FRAGMENTO) {
            val fila = inicioFila + y
            if (fila !in 0 until modelo.filas) continue
            for (x in 0 until CELDAS_POR_FRAGMENTO) {
                val columna = inicioColumna + x
                if (columna in 0 until modelo.columnas && modelo.tunel[fila][columna]) {
                    celdasAbiertas[y * CELDAS_POR_FRAGMENTO + x] = true
                }
            }
        }
        ejecutorFragmentosMapa.execute {
            val fragmento = runCatching {
                crearFragmentoMapa(fragmentoX, fragmentoY, celdasAbiertas)
            }.getOrNull()
            gestor.post {
                fragmentosEnCreacion.remove(clave)
                if (fragmento == null) {
                    invalidate()
                    return@post
                }
                if (revision != modelo.revisionEstructuraMapa) {
                    fragmento.bitmap.recycle()
                    invalidate()
                    return@post
                }
                sincronizarFragmentoMapa(fragmento, fragmentoX, fragmentoY)
                cacheFragmentosMapa.put(clave, fragmento)
                invalidate()
            }
        }
    }

    private fun sincronizarFragmentoMapa(fragmento: FragmentoMapa, fragmentoX: Int, fragmentoY: Int) {
        val inicioColumna = fragmentoX * CELDAS_POR_FRAGMENTO
        val inicioFila = fragmentoY * CELDAS_POR_FRAGMENTO
        for (y in 0 until CELDAS_POR_FRAGMENTO) {
            val fila = inicioFila + y
            for (x in 0 until CELDAS_POR_FRAGMENTO) {
                val columna = inicioColumna + x
                val abierta = columna in 0 until modelo.columnas && fila in 0 until modelo.filas && modelo.tunel[fila][columna]
                val indiceLocal = y * CELDAS_POR_FRAGMENTO + x
                if (fragmento.celdasAbiertas[indiceLocal] == abierta) continue
                dibujarCeldaFragmento(fragmento.lienzo, x, y, columna, fila, abierta)
                fragmento.celdasAbiertas[indiceLocal] = abierta
            }
        }
    }

    private fun actualizarFragmentosMapaModificados() {
        if (revisionMapaEnPantalla != modelo.revisionEstructuraMapa) {
            cacheFragmentosMapa.evictAll()
            revisionMapaEnPantalla = modelo.revisionEstructuraMapa
        }
        modelo.copiarCeldasTunelModificadas(celdasMapaModificadas)
        for (indiceCelda in celdasMapaModificadas) {
            val columna = indiceCelda % modelo.columnas
            val fila = indiceCelda / modelo.columnas
            val fragmentoX = Math.floorDiv(columna, CELDAS_POR_FRAGMENTO)
            val fragmentoY = Math.floorDiv(fila, CELDAS_POR_FRAGMENTO)
            val clave = (fragmentoY.toLong() shl 32) xor (fragmentoX.toLong() and 0xffffffffL)
            val fragmento = cacheFragmentosMapa.get(clave) ?: continue
            val localX = Math.floorMod(columna, CELDAS_POR_FRAGMENTO)
            val localY = Math.floorMod(fila, CELDAS_POR_FRAGMENTO)
            val indiceLocal = localY * CELDAS_POR_FRAGMENTO + localX
            if (fragmento.celdasAbiertas[indiceLocal]) continue
            dibujarCeldaFragmento(fragmento.lienzo, localX, localY, columna, fila, abierta = true)
            fragmento.celdasAbiertas[indiceLocal] = true
        }
        celdasMapaModificadas.clear()
    }

    private fun crearFragmentoMapa(fragmentoX: Int, fragmentoY: Int, celdasAbiertas: BooleanArray): FragmentoMapa {
        val bitmap = createBitmap(PIXELES_POR_FRAGMENTO, PIXELES_POR_FRAGMENTO, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(bitmap)
        val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
        val inicioColumna = fragmentoX * CELDAS_POR_FRAGMENTO
        val inicioFila = fragmentoY * CELDAS_POR_FRAGMENTO
        for (y in 0 until CELDAS_POR_FRAGMENTO) {
            for (x in 0 until CELDAS_POR_FRAGMENTO) {
                val columna = inicioColumna + x
                val fila = inicioFila + y
                val abierta = celdasAbiertas[y * CELDAS_POR_FRAGMENTO + x]
                dibujarCeldaFragmento(lienzo, x, y, columna, fila, abierta, limpiar = false, pincel = pincel)
            }
        }
        return FragmentoMapa(bitmap, celdasAbiertas)
    }

    private fun dibujarCeldaFragmento(
        lienzo: Canvas,
        x: Int,
        y: Int,
        columna: Int,
        fila: Int,
        abierta: Boolean,
        limpiar: Boolean = true,
        pincel: Paint = pincelMapa,
    ) {
        val tamanoCelda = PIXELES_POR_CELDA.toFloat()
        val izquierda = x * tamanoCelda
        val arriba = y * tamanoCelda
        val derecha = izquierda + tamanoCelda
        val abajo = arriba + tamanoCelda
        val semilla = ((columna * 73856093) xor (fila * 19349663)) and 0x7fffffff
        if (limpiar) {
            lienzo.withClip(izquierda, arriba, derecha, abajo) {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
        }
        if (abierta) {
            pincel.color = when (semilla % 4) {
                0 -> Color.rgb(65, 46, 68)
                1 -> Color.rgb(71, 49, 65)
                2 -> Color.rgb(59, 47, 68)
                else -> Color.rgb(69, 52, 70)
            }
            lienzo.drawRoundRect(izquierda, arriba, derecha, abajo, tamanoCelda * .22f, tamanoCelda * .22f, pincel)
            pincel.color = Color.argb(65, 255, 208, 136)
            val guijarroX = izquierda + (semilla % 7 + 2) * tamanoCelda / 10f
            val guijarroY = arriba + ((semilla / 9) % 7 + 2) * tamanoCelda / 10f
            lienzo.drawCircle(guijarroX, guijarroY, max(1f, tamanoCelda * .035f), pincel)
        } else {
            pincel.color = if (semilla % 3 == 0) Color.rgb(22, 57, 48) else Color.rgb(19, 49, 44)
            lienzo.drawRect(izquierda, arriba, derecha, abajo, pincel)
            if (semilla % 11 == 0) {
                pincel.color = Color.rgb(39, 104, 66)
                lienzo.drawCircle(izquierda + tamanoCelda * .5f, arriba + tamanoCelda * .5f, tamanoCelda * .18f, pincel)
                pincel.color = Color.rgb(79, 146, 77)
                lienzo.drawCircle(izquierda + tamanoCelda * .437f, arriba + tamanoCelda * .455f, tamanoCelda * .099f, pincel)
            }
            if (semilla % 17 == 0) {
                pincel.color = Color.rgb(74, 123, 77)
                lienzo.drawCircle(izquierda + tamanoCelda * .25f, arriba + tamanoCelda * .65f, max(1f, tamanoCelda * .06f), pincel)
            }
        }
    }

    private fun dibujarColonia(canvas: Canvas) {
        val izquierda = izquierdaCelda(modelo.columnaBase - modelo.anchoBase)
        val arriba = arribaCelda(modelo.filaBase - modelo.altoBase)
        val derecha = izquierda + (modelo.anchoBase * 2 + 1) * anchoCelda
        val abajo = arriba + (modelo.altoBase * 2 + 1) * anchoCelda
        val centroX = (izquierda + derecha) / 2f
        val centroY = (arriba + abajo) / 2f
        pincel.color = Color.argb(110, 10, 9, 28); canvas.drawRoundRect(izquierda - 5, arriba - 5, derecha + 5, abajo + 5, 22f, 22f, pincel)
        pincel.color = Color.rgb(27, 145, 137); canvas.drawRoundRect(izquierda, arriba, derecha, abajo, 18f, 18f, pincel)
        pincel.style = Paint.Style.STROKE; pincel.strokeWidth = max(2f, anchoCelda * .12f)
        pincel.color = if (modelo.nivelBase > 1) Color.rgb(255, 218, 80) else Color.rgb(96, 240, 207)
        canvas.drawRoundRect(izquierda + 3, arriba + 3, derecha - 3, abajo - 3, 15f, 15f, pincel)
        pincel.style = Paint.Style.FILL
        // Entrada occidental abierta y corredor de acceso.
        val puertaIzquierda = izquierdaCelda(modelo.columnaBase - modelo.anchoBase)
        val puertaArriba = arribaCelda(modelo.filaBase)
        pincel.color = Color.rgb(37, 54, 71); canvas.drawRect(puertaIzquierda - 2, puertaArriba, puertaIzquierda + 3, puertaArriba + anchoCelda, pincel)
        pincel.color = Color.rgb(255, 207, 77)
        canvas.drawCircle(izquierda + anchoCelda * .7f, centroY, max(2f, anchoCelda * .12f), pincel)
        pincel.color = Color.rgb(91, 238, 197)
        for (ventana in 0..min(5, modelo.nivelBase + 1)) {
            val x = izquierda + anchoCelda * (1.3f + ventana * .9f)
            pincel.color = if (ventana % 2 == 0) Color.rgb(255, 234, 107) else Color.rgb(153, 248, 221)
            canvas.drawRoundRect(x, arriba + anchoCelda * .45f, x + anchoCelda * .28f, arriba + anchoCelda * .73f, 4f, 4f, pincel)
        }
        if (nivelBaseDentroColonia != modelo.nivelBase) {
            nivelBaseDentroColonia = modelo.nivelBase
            textoNivelBaseDentroColonia = "COLONIA ${modelo.nivelBase}"
        }
        texto(canvas, textoNivelBaseDentroColonia, centroCeldaX(modelo.columnaBase), puertaArriba + anchoCelda * .48f, max(9f, anchoCelda * .28f), Color.WHITE, Paint.Align.CENTER)
        if ((modelo.nivelesMejora[MejoraColonia.DEFENSA] ?: 0) > 0) {
            pincel.style = Paint.Style.STROKE; pincel.strokeWidth = max(1f, anchoCelda * .06f)
            pincel.color = Color.argb(90, 85, 221, 255)
            canvas.drawCircle(centroX, centroY, anchoCelda * (2f + modelo.nivelesMejora.getValue(MejoraColonia.DEFENSA)), pincel)
            pincel.style = Paint.Style.FILL
        }
    }

    private fun dibujarFrutas(canvas: Canvas) {
        var indice = 0
        while (indice < modelo.frutas.size) {
            val fruta = modelo.frutas[indice++]
            if (!celdaVisible(fruta.columna, fruta.fila)) continue
            dibujarSprite(canvas, spritesFruta[fruta.tipo.clave], fruta.columna, fruta.fila, .63f)
        }
    }

    private fun dibujarFantasmas(canvas: Canvas) {
        val animacion = if (modelo.ciclo % 2 == 0) 1 else 0
        var indice = 0
        while (indice < modelo.fantasmas.size) {
            val fantasma = modelo.fantasmas[indice++]
            if (!celdaVisible(fantasma.columna, fantasma.fila)) continue
            val sprite = if (fantasma.asustado) spritesAsustado[1 - animacion] else {
                val indiceFantasma = indiceFantasmaPorTipo[fantasma.tipo]
                if (indiceFantasma == null) null else spritesFantasma[indiceFantasma][fantasma.direccion.ordinal][animacion]
            }
            dibujarSprite(canvas, sprite, fantasma.columna, fantasma.fila, .78f)
            if (fantasma.asustado && !detalleReducido) {
                pincel.color = Color.rgb(111, 211, 255)
                canvas.drawCircle(centroCeldaX(fantasma.columna), arribaCelda(fantasma.fila), max(1.5f, anchoCelda * .055f), pincel)
            }
        }
    }

    private fun dibujarPacmanes(canvas: Canvas) {
        val fotogramaMovimiento = modelo.ciclo % 3
        var indice = 0
        while (indice < modelo.pacmanes.size) {
            val pacman = modelo.pacmanes[indice++]
            if (!celdaVisible(pacman.columna, pacman.fila)) continue
            val sprite = if (pacman.animacionMuerte > 0) {
                spritesMuerte[pacman.rol.ordinal][pacman.animacionMuerte.coerceIn(1, 11) - 1]
            } else {
                spritesPacman[pacman.rol.ordinal][pacman.direccion.ordinal][fotogramaMovimiento]
            }
            if (!detalleReducido && pacman.animacionMuerte == 0 && pacman.identificador == pacmanSeguido) {
                pincel.style = Paint.Style.STROKE; pincel.strokeWidth = max(1.5f, anchoCelda * .07f)
                pincel.color = Color.argb(220, 255, 255, 255)
                canvas.drawCircle(centroCeldaX(pacman.columna), centroCeldaY(pacman.fila), anchoCelda * .46f, pincel)
                pincel.style = Paint.Style.FILL
            }
            dibujarSprite(canvas, sprite, pacman.columna, pacman.fila, .88f)
            if ((pacman.rol == RolPacman.OBRERO || pacman.rol == RolPacman.DEFENSOR) &&
                (!detalleReducido || pacman.agotado)
            ) {
                dibujarBarraResistencia(canvas, pacman)
            }
        }
    }

    private fun dibujarBarraResistencia(canvas: Canvas, pacman: PacmanColono) {
        val centroX = centroCeldaX(pacman.columna)
        val centroY = centroCeldaY(pacman.fila)
        if (!fondoMapa.contains(centroX, centroY)) return
        val capacidad = modelo.capacidadResistencia(pacman.rol).coerceAtLeast(1f)
        val progreso = (pacman.resistenciaActual / capacidad).coerceIn(0f, 1f)
        val ancho = anchoCelda * .72f
        val alto = max(2f, anchoCelda * .11f)
        val izquierda = centroX - ancho / 2f
        val arriba = (arribaCelda(pacman.fila) - alto - 1f).coerceAtLeast(fondoMapa.top + 1f)
        val derecha = izquierda + ancho
        val abajo = arriba + alto
        pincel.color = Color.rgb(25, 24, 36)
        canvas.drawRoundRect(izquierda, arriba, derecha, abajo, alto / 2f, alto / 2f, pincel)
        if (progreso > 0f) {
            pincel.color = when {
                progreso > .6f -> Color.rgb(74, 224, 128)
                progreso > .3f -> Color.rgb(255, 202, 65)
                else -> Color.rgb(255, 74, 83)
            }
            canvas.drawRoundRect(izquierda, arriba, izquierda + ancho * progreso, abajo, alto / 2f, alto / 2f, pincel)
        }
        pincel.style = Paint.Style.STROKE
        pincel.strokeWidth = max(1f, anchoCelda * .025f)
        pincel.color = Color.argb(220, 255, 255, 255)
        canvas.drawRoundRect(izquierda, arriba, derecha, abajo, alto / 2f, alto / 2f, pincel)
        pincel.style = Paint.Style.FILL
    }

    private fun dibujarSprite(canvas: Canvas, bitmap: Bitmap?, columna: Int, fila: Int, escala: Float) {
        bitmap ?: return
        val centroX = centroCeldaX(columna)
        val centroY = centroCeldaY(fila)
        val mitadLado = anchoCelda * escala / 2f
        destinoSprite.set(centroX - mitadLado, centroY - mitadLado, centroX + mitadLado, centroY + mitadLado)
        pincel.alpha = 255; canvas.drawBitmap(bitmap, null, destinoSprite, pincel)
    }

    private fun dibujarIcono(canvas: Canvas, nombre: String, centroX: Float, centroY: Float, lado: Float) {
        val bitmap = sprites[nombre] ?: return
        destinoSprite.set(centroX - lado / 2, centroY - lado / 2, centroX + lado / 2, centroY + lado / 2)
        canvas.drawBitmap(bitmap, null, destinoSprite, pincel)
    }

    private fun dibujarCabecera(canvas: Canvas, alto: Float, densidad: Float) {
        pincel.color = Color.rgb(29, 28, 55); canvas.drawRect(0f, 0f, width.toFloat(), alto, pincel)
        pincel.color = Color.rgb(255, 218, 75); canvas.drawCircle(21f * densidad, alto / 2, 12f * densidad, pincel)
        texto(canvas, "P", 21f * densidad, alto / 2 + 5f * densidad, 18f * densidad, Color.rgb(37, 31, 66), Paint.Align.CENTER)
        texto(canvas, "PACANTS", 42f * densidad, alto * .42f, 17f * densidad, Color.WHITE)
        if (valorPuntosEnCabecera != modelo.puntos) {
            valorPuntosEnCabecera = modelo.puntos
            textoPuntosEnCabecera = "Puntos ${modelo.puntos}"
        }
        if (nivelBaseEnCabecera != modelo.nivelBase) {
            nivelBaseEnCabecera = modelo.nivelBase
            textoBaseEnCabecera = "Base ${modelo.nivelBase}"
        }
        if (poblacionEnCabecera != modelo.pacmanes.size || cicloPoblacionEnCabecera != modelo.ciclo) {
            var activos = 0
            var indicePacman = 0
            while (indicePacman < modelo.pacmanes.size) {
                val pacman = modelo.pacmanes[indicePacman++]
                if (pacman.animacionMuerte == 0 && !pacman.agotado) activos++
            }
            poblacionEnCabecera = modelo.pacmanes.size
            cicloPoblacionEnCabecera = modelo.ciclo
            activosEnCabecera = activos
            textoPoblacionEnCabecera = "Pac-Man $poblacionEnCabecera • activos $activosEnCabecera"
        }
        val existencias = modelo.frutasAlmacenadas
        val capacidad = modelo.capacidadAlmacen
        if (existenciasEnCabecera != existencias || capacidadEnCabecera != capacidad) {
            existenciasEnCabecera = existencias
            capacidadEnCabecera = capacidad
            textoAlmacenEnCabecera = "Almacén $existenciasEnCabecera/$capacidadEnCabecera"
        }
        texto(canvas, textoPuntosEnCabecera, width * .35f, alto * .42f, 14f * densidad, Color.rgb(255, 226, 91))
        texto(canvas, textoBaseEnCabecera, width * .57f, alto * .42f, 14f * densidad, Color.rgb(119, 240, 208))
        texto(canvas, textoPoblacionEnCabecera, width * .75f, alto * .42f, 12f * densidad, Color.WHITE)
        texto(canvas, textoAlmacenEnCabecera, width * .985f, alto * .42f, 11f * densidad, Color.rgb(191, 225, 255), Paint.Align.RIGHT)
    }

    private fun dibujarBarra(canvas: Canvas, arriba: Float, alto: Float, densidad: Float) {
        pincel.color = Color.rgb(26, 24, 48); canvas.drawRect(0f, arriba, width.toFloat(), arriba + alto, pincel)
        zonasTactiles.clear()
        val ancho = width.toFloat() / etiquetasBarra.size
        for (indice in etiquetasBarra.indices) {
            val etiqueta = etiquetasBarra[indice]
            val zona = zonasBarra[indice]
            val limites = zona.limites
            limites.set(indice * ancho + 3f * densidad, arriba + 7f * densidad, (indice + 1) * ancho - 3f * densidad, arriba + alto - 6f * densidad)
            val rol = when (indice) { 1 -> RolPacman.EXPLORADOR; 2 -> RolPacman.OBRERO; 3 -> RolPacman.DEFENSOR; else -> null }
            pincel.color = when (indice) {
                0 -> Color.rgb(61, 70, 102); 1 -> RolPacman.EXPLORADOR.color
                2 -> RolPacman.OBRERO.color; 3 -> RolPacman.DEFENSOR.color
                4 -> Color.rgb(50, 137, 103); 5 -> Color.rgb(94, 85, 169)
                8 -> Color.rgb(180, 57, 72); 9 -> Color.rgb(38, 123, 130)
                else -> Color.rgb(61, 70, 102)
            }
            canvas.drawRoundRect(limites, 13f * densidad, 13f * densidad, pincel)
            if (rol != null && rolEnVista == rol) {
                pincel.style = Paint.Style.STROKE; pincel.strokeWidth = 2.5f * densidad; pincel.color = Color.WHITE
                canvas.drawRoundRect(limites, 13f * densidad, 13f * densidad, pincel); pincel.style = Paint.Style.FILL
            }
            val colorTexto = if (indice == 1) Color.rgb(40, 31, 20) else Color.WHITE
            texto(canvas, rol?.nombre ?: etiqueta, limites.centerX(), limites.centerY() + 4f * densidad, 9f * densidad, colorTexto, Paint.Align.CENTER)
            zonasTactiles.add(zona)
        }
    }

    private fun pulsarBarra(indice: Int) {
        if (!partidaLista || pantalla != PantallaPacAnts.JUEGO) return
        when (indice) {
            0 -> { rolEnVista = null; pacmanSeguido = null; pacmanEnSeguimiento = null; objetivoColumna = modelo.columnaBase.toFloat(); objetivoFila = modelo.filaBase.toFloat(); zoomObjetivo = 1.18f }
            1 -> enfocar(RolPacman.EXPLORADOR)
            2 -> enfocar(RolPacman.OBRERO)
            3 -> enfocar(RolPacman.DEFENSOR)
            4 -> panel = if (panel == PanelColonia.MERCADO) PanelColonia.NINGUNO else PanelColonia.MERCADO
            5 -> panel = if (panel == PanelColonia.MEJORAS) PanelColonia.NINGUNO else PanelColonia.MEJORAS
            6 -> zoomObjetivo = (zoomObjetivo - .16f).coerceAtLeast(.72f)
            7 -> zoomObjetivo = (zoomObjetivo + .16f).coerceAtMost(2.8f)
            8 -> confirmarReinicio()
            9 -> pantalla = PantallaPacAnts.PAUSA
        }
        invalidate()
    }

    private fun venderFruta(indice: Int) {
        val fruta = modelo.tiposFruta[indice]
        if (modelo.venderFruta(fruta.clave)) {
            mensaje = "${fruta.nombre} vendida por ${fruta.precio} puntos"
            mensajeHasta = System.currentTimeMillis() + 1800
            guardarPartida()
        }
        invalidate()
    }

    private fun comprarMejora(mejora: MejoraColonia) {
        if (modelo.mejorar(mejora)) {
            mensaje = when (mejora) {
                MejoraColonia.BASE -> "Llegó 1 explorador, 1 obrero y 1 defensor"
                MejoraColonia.TUNELES -> "Herramientas mejoradas: túneles más rápidos"
                MejoraColonia.ALMACEN -> "Almacén ampliado a ${modelo.capacidadAlmacen} frutas"
                MejoraColonia.DEFENSA -> "La entrada ahuyenta fantasmas desde más lejos"
                MejoraColonia.RESISTENCIA_DEFENSORES -> "Resistencia mejorada: ${modelo.capacidadResistencia(RolPacman.DEFENSOR).toInt()} puntos"
            }
            mensajeHasta = System.currentTimeMillis() + 2300
            guardarPartida()
        } else {
            mensaje = "Vende frutas para reunir más puntos"
            mensajeHasta = System.currentTimeMillis() + 1800
        }
        invalidate()
    }

    private fun confirmarReinicio() {
        AlertDialog.Builder(context)
            .setTitle("Reiniciar partida")
            .setMessage("Se borrarán los puntos, las frutas, las mejoras y el mapa actual. ¿Quieres empezar desde cero?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Reiniciar") { _, _ -> reiniciarPartida() }
            .show()
    }

    private fun reiniciarPartida() {
        if (!partidaLista) return
        val nuevaGeneracion = ++generacionPartida
        partidaLista = false
        panel = PanelColonia.NINGUNO
        rolEnVista = null
        pacmanSeguido = null
        pacmanEnSeguimiento = null
        indicesVistaPorRol.clear()
        contextoPreferencias().edit { clear() }
        mensaje = "Reiniciando la colonia..."
        mensajeHasta = System.currentTimeMillis() + 2500L
        alcanceBaseDatos.launch {
            bloqueoBaseDatos.withLock {
                runCatching { baseDatos.partidaColoniaDao().borrarPartida(slotActual) }
            }
            gestor.post {
                if (nuevaGeneracion != generacionPartida) return@post
                partidasGuardadas[slotActual - 1] = null
                resumenesPartida[slotActual - 1] = null
                modelo.nuevaPartida()
                desplazamientoMapaX = modelo.desplazamientoCoordenadasX
                desplazamientoMapaY = modelo.desplazamientoCoordenadasY
                camaraColumna = modelo.columnaBase.toFloat()
                camaraFila = modelo.filaBase.toFloat()
                objetivoColumna = camaraColumna
                objetivoFila = camaraFila
                zoom = 1f
                zoomObjetivo = 1.18f
                ultimoAvance = System.currentTimeMillis()
                ultimoGuardado = modelo.ciclo
                partidaLista = true
                mensaje = "La colonia comienza de nuevo"
                mensajeHasta = System.currentTimeMillis() + 2500L
                guardarPartida()
                invalidate()
            }
        }
    }

    private fun contextoPreferencias() = context.getSharedPreferences("colonia", Context.MODE_PRIVATE)

    private fun enfocar(rol: RolPacman) {
        rolEnVista = rol
        val pacmanesDisponibles = modelo.pacmanes.filter { it.rol == rol }.sortedBy { it.identificador }
        if (pacmanesDisponibles.isEmpty()) return
        val indice = indicesVistaPorRol[rol] ?: -1
        val siguienteIndice = (indice + 1) % pacmanesDisponibles.size
        indicesVistaPorRol[rol] = siguienteIndice
        val pacman = pacmanesDisponibles[siguienteIndice]
        pacmanSeguido = pacman.identificador
        pacmanEnSeguimiento = pacman
        objetivoColumna = pacman.columna.toFloat(); objetivoFila = pacman.fila.toFloat()
        zoomObjetivo = 2.25f
        panel = PanelColonia.NINGUNO
    }

    private fun dibujarPanel(canvas: Canvas, densidad: Float) {
        pincel.color = Color.argb(205, 9, 11, 27); canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), pincel)
        val ancho = min(width * .82f, 760f * densidad)
        val alto = min(height * .94f, if (panel == PanelColonia.MERCADO) 540f * densidad else 510f * densidad)
        val marco = marcoPanel
        marco.set((width - ancho) / 2, (height - alto) / 2, (width + ancho) / 2, (height + alto) / 2)
        pincel.color = Color.rgb(38, 38, 76); canvas.drawRoundRect(marco, 24f * densidad, 24f * densidad, pincel)
        pincel.style = Paint.Style.STROKE; pincel.strokeWidth = 2f * densidad; pincel.color = Color.rgb(118, 229, 199); canvas.drawRoundRect(marco, 24f * densidad, 24f * densidad, pincel); pincel.style = Paint.Style.FILL
        texto(canvas, if (panel == PanelColonia.MERCADO) "Mercado de frutas" else "Taller de mejoras", marco.centerX(), marco.top + 32f * densidad, 23f * densidad, Color.WHITE, Paint.Align.CENTER)
        texto(canvas, "Puntos disponibles: ${modelo.puntos}", marco.centerX(), marco.top + 56f * densidad, 13f * densidad, Color.rgb(255, 222, 88), Paint.Align.CENTER)
        if (panel == PanelColonia.MERCADO) dibujarMercado(canvas, marco, densidad) else dibujarMejoras(canvas, marco, densidad)
        val cerrar = zonaCerrarPanel.limites
        cerrar.set(marco.right - 46f * densidad, marco.top + 8f * densidad, marco.right - 10f * densidad, marco.top + 42f * densidad)
        pincel.color = Color.rgb(191, 78, 100); canvas.drawRoundRect(cerrar, 10f, 10f, pincel)
        texto(canvas, "×", cerrar.centerX(), cerrar.centerY() + 6f * densidad, 20f * densidad, Color.WHITE, Paint.Align.CENTER)
        zonasTactiles.add(zonaCerrarPanel)
    }

    private fun dibujarMercado(canvas: Canvas, marco: RectF, densidad: Float) {
        val altoFila = min(48f * densidad, (marco.height() - 100f * densidad) / modelo.tiposFruta.size)
        modelo.tiposFruta.forEachIndexed { indice, fruta ->
            val arriba = marco.top + 75f * densidad + indice * altoFila
            val fila = filaPanel
            fila.set(marco.left + 18f * densidad, arriba, marco.right - 18f * densidad, arriba + altoFila - 4f * densidad)
            pincel.color = if (indice % 2 == 0) Color.rgb(50, 49, 92) else Color.rgb(45, 44, 83)
            canvas.drawRoundRect(fila, 10f * densidad, 10f * densidad, pincel)
            dibujarIcono(canvas, "fruit_${fruta.clave}", fila.left + 22f * densidad, fila.centerY(), 30f * densidad)
            texto(canvas, fruta.nombre, fila.left + 43f * densidad, fila.centerY() + 5f * densidad, 15f * densidad, Color.WHITE)
            texto(canvas, "En almacén: ${modelo.inventario[fruta.clave] ?: 0}", fila.centerX(), fila.centerY() + 5f * densidad, 13f * densidad, Color.rgb(183, 225, 214), Paint.Align.CENTER)
            val vender = botonPanel
            vender.set(fila.right - 145f * densidad, fila.top + 4f * densidad, fila.right - 5f * densidad, fila.bottom - 4f * densidad)
            val disponible = (modelo.inventario[fruta.clave] ?: 0) > 0
            pincel.color = if (disponible) Color.rgb(53, 164, 114) else Color.rgb(82, 82, 101)
            canvas.drawRoundRect(vender, 8f * densidad, 8f * densidad, pincel)
            texto(canvas, "Vender • ${fruta.precio}", vender.centerX(), vender.centerY() + 4f * densidad, 12f * densidad, Color.WHITE, Paint.Align.CENTER)
            val zona = zonasMercado[indice]
            zona.limites.set(vender)
            zonasTactiles.add(zona)
        }
        val total = modelo.frutasAlmacenadas
        texto(canvas, "Capacidad del almacén: $total / ${modelo.capacidadAlmacen}", marco.centerX(), marco.bottom - 12f * densidad, 12f * densidad, Color.WHITE, Paint.Align.CENTER)
    }

    private fun dibujarMejoras(canvas: Canvas, marco: RectF, densidad: Float) {
        val anchoTarjeta = (marco.width() - 54f * densidad) / 2f
        val cantidadFilas = (MejoraColonia.entries.size + 1) / 2
        val margenSuperior = 70f * densidad
        val margenInferior = 18f * densidad
        val separacionVertical = 8f * densidad
        val altoTarjeta = (marco.height() - margenSuperior - margenInferior - separacionVertical * (cantidadFilas - 1)) / cantidadFilas
        MejoraColonia.entries.forEachIndexed { indice, mejora ->
            val columna = indice % 2; val fila = indice / 2
            val izquierda = marco.left + 18f * densidad + columna * (anchoTarjeta + 18f * densidad)
            val arriba = marco.top + margenSuperior + fila * (altoTarjeta + separacionVertical)
            val tarjeta = tarjetaMejora
            tarjeta.set(izquierda, arriba, izquierda + anchoTarjeta, arriba + altoTarjeta)
            pincel.color = Color.rgb(54, 53, 101); canvas.drawRoundRect(tarjeta, 14f * densidad, 14f * densidad, pincel)
            val nivel = modelo.nivelesMejora[mejora] ?: 0
            texto(canvas, mejora.nombre, tarjeta.left + 12f * densidad, tarjeta.top + 19f * densidad, 12f * densidad, Color.WHITE)
            texto(canvas, "Nivel $nivel", tarjeta.left + 12f * densidad, tarjeta.top + 36f * densidad, 10f * densidad, Color.rgb(173, 230, 217))
            val detalle = when (mejora) {
                MejoraColonia.BASE -> "+3 Pac-Man y una base más amplia"
                MejoraColonia.TUNELES -> "Excava rápido · resistencia ${modelo.capacidadResistencia(RolPacman.OBRERO).toInt()}"
                MejoraColonia.ALMACEN -> "Capacidad: ${modelo.capacidadAlmacen} frutas"
                MejoraColonia.DEFENSA -> "Los fantasmas huyen desde más lejos"
                MejoraColonia.RESISTENCIA_DEFENSORES -> "Resistencia: ${modelo.capacidadResistencia(RolPacman.DEFENSOR).toInt()} puntos"
            }
            val lineaDetalle = min(52f * densidad, tarjeta.height() - 34f * densidad)
            texto(canvas, detalle, tarjeta.left + 12f * densidad, tarjeta.top + lineaDetalle, 9f * densidad, Color.rgb(223, 219, 241))
            val precio = modelo.precioMejora(mejora)
            val comprar = botonMejora
            comprar.set(tarjeta.left + 10f * densidad, tarjeta.bottom - 30f * densidad, tarjeta.right - 10f * densidad, tarjeta.bottom - 5f * densidad)
            pincel.color = if (modelo.puntos >= precio) Color.rgb(48, 162, 116) else Color.rgb(93, 86, 111)
            canvas.drawRoundRect(comprar, 9f * densidad, 9f * densidad, pincel)
            texto(canvas, "Mejorar • $precio", comprar.centerX(), comprar.centerY() + 4f * densidad, 10f * densidad, Color.WHITE, Paint.Align.CENTER)
            val zona = zonasMejoras[indice]
            zona.limites.set(comprar)
            zonasTactiles.add(zona)
        }
    }

    private fun texto(canvas: Canvas, texto: String, x: Float, y: Float, tamano: Float, color: Int, alineacion: Paint.Align = Paint.Align.LEFT) {
        pincel.color = color; pincel.textSize = tamano; pincel.typeface = fuente; pincel.textAlign = alineacion; pincel.style = Paint.Style.FILL
        canvas.drawText(texto, x, y, pincel)
    }

    private fun dibujarMensaje(canvas: Canvas, densidad: Float) {
        if (System.currentTimeMillis() > mensajeHasta) return
        val limites = limitesMensaje
        limites.set(width * .25f, fondoMapa.bottom - 40f * densidad, width * .75f, fondoMapa.bottom - 5f * densidad)
        pincel.color = Color.argb(230, 32, 37, 71); canvas.drawRoundRect(limites, 12f * densidad, 12f * densidad, pincel)
        texto(canvas, mensaje, limites.centerX(), limites.centerY() + 4f * densidad, 12f * densidad, Color.WHITE, Paint.Align.CENTER)
    }

    override fun onTouchEvent(evento: MotionEvent): Boolean {
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN -> { inicioX = evento.x; inicioY = evento.y; arrastrando = false; return true }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (pantalla == PantallaPacAnts.JUEGO && evento.pointerCount >= 2) distanciaPinza = distancia(evento)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (pantalla != PantallaPacAnts.JUEGO) return true
                if (evento.pointerCount >= 2) {
                    val nuevaDistancia = distancia(evento)
                    if (distanciaPinza > 0f) zoomObjetivo = (zoomObjetivo * nuevaDistancia / distanciaPinza).coerceIn(.72f, 2.8f)
                    distanciaPinza = nuevaDistancia; pacmanSeguido = null; pacmanEnSeguimiento = null; return true
                }
                if (panel == PanelColonia.NINGUNO && inicioY in fondoMapa.top..fondoMapa.bottom) {
                    val deltaX = evento.x - inicioX; val deltaY = evento.y - inicioY
                    if (abs(deltaX) + abs(deltaY) > 3f) { arrastrando = true; pacmanSeguido = null; pacmanEnSeguimiento = null; rolEnVista = null }
                    objetivoColumna -= deltaX / anchoCelda; objetivoFila -= deltaY / anchoCelda
                    inicioX = evento.x; inicioY = evento.y
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!arrastrando) {
                    val zona = zonasTactiles.lastOrNull { it.limites.contains(evento.x, evento.y) }
                    if (zona != null) zona.accion()
                    else if (pantalla == PantallaPacAnts.JUEGO && panel == PanelColonia.NINGUNO && fondoMapa.contains(evento.x, evento.y)) {
                        objetivoColumna = ((evento.x - fondoMapa.centerX()) / anchoCelda + camaraColumna).coerceIn(0f, (modelo.columnas - 1).toFloat())
                        objetivoFila = ((evento.y - fondoMapa.centerY()) / anchoCelda + camaraFila).coerceIn(0f, (modelo.filas - 1).toFloat())
                        pacmanSeguido = null; pacmanEnSeguimiento = null; rolEnVista = null
                    }
                    performClick()
                }
                distanciaPinza = 0f; invalidate(); return true
            }
            MotionEvent.ACTION_CANCEL -> { distanciaPinza = 0f; arrastrando = false; return true }
        }
        return true
    }

    private fun distancia(evento: MotionEvent): Float {
        val dx = evento.getX(0) - evento.getX(1); val dy = evento.getY(0) - evento.getY(1)
        return sqrt(dx * dx + dy * dy)
    }

    override fun performClick(): Boolean { super.performClick(); return true }

    override fun onDetachedFromWindow() {
        gestor.removeCallbacks(bucle)
        if (partidaLista && (pantalla == PantallaPacAnts.JUEGO || pantalla == PantallaPacAnts.PAUSA)) guardarPartida()
        super.onDetachedFromWindow()
    }

    private val identificadoresSprites by lazy {
        mapOf(
        "door_key" to R.drawable.door_key,
        "fruit_apple" to R.drawable.fruit_apple,
        "fruit_bell" to R.drawable.fruit_bell,
        "fruit_cherry" to R.drawable.fruit_cherry,
        "fruit_galaxian" to R.drawable.fruit_galaxian,
        "fruit_melon" to R.drawable.fruit_melon,
        "fruit_orange" to R.drawable.fruit_orange,
        "fruit_strawberry" to R.drawable.fruit_strawberry,
        "ghost_blinky_down" to R.drawable.ghost_blinky_down,
        "ghost_blinky_down_2" to R.drawable.ghost_blinky_down_2,
        "ghost_blinky_left" to R.drawable.ghost_blinky_left,
        "ghost_blinky_left_2" to R.drawable.ghost_blinky_left_2,
        "ghost_blinky_right" to R.drawable.ghost_blinky_right,
        "ghost_blinky_right_2" to R.drawable.ghost_blinky_right_2,
        "ghost_blinky_up" to R.drawable.ghost_blinky_up,
        "ghost_blinky_up_2" to R.drawable.ghost_blinky_up_2,
        "ghost_clyde_down" to R.drawable.ghost_clyde_down,
        "ghost_clyde_down_2" to R.drawable.ghost_clyde_down_2,
        "ghost_clyde_left" to R.drawable.ghost_clyde_left,
        "ghost_clyde_left_2" to R.drawable.ghost_clyde_left_2,
        "ghost_clyde_right" to R.drawable.ghost_clyde_right,
        "ghost_clyde_right_2" to R.drawable.ghost_clyde_right_2,
        "ghost_clyde_up" to R.drawable.ghost_clyde_up,
        "ghost_clyde_up_2" to R.drawable.ghost_clyde_up_2,
        "ghost_inky_down" to R.drawable.ghost_inky_down,
        "ghost_inky_down_2" to R.drawable.ghost_inky_down_2,
        "ghost_inky_left" to R.drawable.ghost_inky_left,
        "ghost_inky_left_2" to R.drawable.ghost_inky_left_2,
        "ghost_inky_right" to R.drawable.ghost_inky_right,
        "ghost_inky_right_2" to R.drawable.ghost_inky_right_2,
        "ghost_inky_up" to R.drawable.ghost_inky_up,
        "ghost_inky_up_2" to R.drawable.ghost_inky_up_2,
        "ghost_pinky_down" to R.drawable.ghost_pinky_down,
        "ghost_pinky_down_2" to R.drawable.ghost_pinky_down_2,
        "ghost_pinky_left" to R.drawable.ghost_pinky_left,
        "ghost_pinky_left_2" to R.drawable.ghost_pinky_left_2,
        "ghost_pinky_right" to R.drawable.ghost_pinky_right,
        "ghost_pinky_right_2" to R.drawable.ghost_pinky_right_2,
        "ghost_pinky_up" to R.drawable.ghost_pinky_up,
        "ghost_pinky_up_2" to R.drawable.ghost_pinky_up_2,
        "ghost_scared_blue_1" to R.drawable.ghost_scared_blue_1,
        "ghost_scared_blue_2" to R.drawable.ghost_scared_blue_2,
        "ghost_scared_white_1" to R.drawable.ghost_scared_white_1,
        "ghost_scared_white_2" to R.drawable.ghost_scared_white_2,
        "map_left" to R.drawable.map_left,
        "map_right" to R.drawable.map_right,
        "orange_pacman_death_1" to R.drawable.orange_pacman_death_1,
        "orange_pacman_death_10" to R.drawable.orange_pacman_death_10,
        "orange_pacman_death_11" to R.drawable.orange_pacman_death_11,
        "orange_pacman_death_2" to R.drawable.orange_pacman_death_2,
        "orange_pacman_death_3" to R.drawable.orange_pacman_death_3,
        "orange_pacman_death_4" to R.drawable.orange_pacman_death_4,
        "orange_pacman_death_5" to R.drawable.orange_pacman_death_5,
        "orange_pacman_death_6" to R.drawable.orange_pacman_death_6,
        "orange_pacman_death_7" to R.drawable.orange_pacman_death_7,
        "orange_pacman_death_8" to R.drawable.orange_pacman_death_8,
        "orange_pacman_death_9" to R.drawable.orange_pacman_death_9,
        "orange_pacman_down_1" to R.drawable.orange_pacman_down_1,
        "orange_pacman_down_2" to R.drawable.orange_pacman_down_2,
        "orange_pacman_down_3" to R.drawable.orange_pacman_down_3,
        "orange_pacman_left_1" to R.drawable.orange_pacman_left_1,
        "orange_pacman_left_2" to R.drawable.orange_pacman_left_2,
        "orange_pacman_right_1" to R.drawable.orange_pacman_right_1,
        "orange_pacman_right_2" to R.drawable.orange_pacman_right_2,
        "orange_pacman_up_1" to R.drawable.orange_pacman_up_1,
        "orange_pacman_up_2" to R.drawable.orange_pacman_up_2,
        "pacman_death_1" to R.drawable.pacman_death_1,
        "pacman_death_10" to R.drawable.pacman_death_10,
        "pacman_death_11" to R.drawable.pacman_death_11,
        "pacman_death_2" to R.drawable.pacman_death_2,
        "pacman_death_3" to R.drawable.pacman_death_3,
        "pacman_death_4" to R.drawable.pacman_death_4,
        "pacman_death_5" to R.drawable.pacman_death_5,
        "pacman_death_6" to R.drawable.pacman_death_6,
        "pacman_death_7" to R.drawable.pacman_death_7,
        "pacman_death_8" to R.drawable.pacman_death_8,
        "pacman_death_9" to R.drawable.pacman_death_9,
        "pacman_down_1" to R.drawable.pacman_down_1,
        "pacman_down_2" to R.drawable.pacman_down_2,
        "pacman_down_3" to R.drawable.pacman_down_3,
        "pacman_left_1" to R.drawable.pacman_left_1,
        "pacman_left_2" to R.drawable.pacman_left_2,
        "pacman_left_3" to R.drawable.pacman_left_3,
        "pacman_right_1" to R.drawable.pacman_right_1,
        "pacman_right_2" to R.drawable.pacman_right_2,
        "pacman_right_3" to R.drawable.pacman_right_3,
        "pacman_up_1" to R.drawable.pacman_up_1,
        "pacman_up_2" to R.drawable.pacman_up_2,
        "pacman_up_3" to R.drawable.pacman_up_3,
        "red_pacman_death_1" to R.drawable.red_pacman_death_1,
        "red_pacman_death_10" to R.drawable.red_pacman_death_10,
        "red_pacman_death_11" to R.drawable.red_pacman_death_11,
        "red_pacman_death_2" to R.drawable.red_pacman_death_2,
        "red_pacman_death_3" to R.drawable.red_pacman_death_3,
        "red_pacman_death_4" to R.drawable.red_pacman_death_4,
        "red_pacman_death_5" to R.drawable.red_pacman_death_5,
        "red_pacman_death_6" to R.drawable.red_pacman_death_6,
        "red_pacman_death_7" to R.drawable.red_pacman_death_7,
        "red_pacman_death_8" to R.drawable.red_pacman_death_8,
        "red_pacman_death_9" to R.drawable.red_pacman_death_9,
        "red_pacman_down_1" to R.drawable.red_pacman_down_1,
        "red_pacman_down_2" to R.drawable.red_pacman_down_2,
        "red_pacman_down_3" to R.drawable.red_pacman_down_3,
        "red_pacman_left_1" to R.drawable.red_pacman_left_1,
        "red_pacman_left_2" to R.drawable.red_pacman_left_2,
        "red_pacman_right_1" to R.drawable.red_pacman_right_1,
        "red_pacman_right_2" to R.drawable.red_pacman_right_2,
        "red_pacman_up_1" to R.drawable.red_pacman_up_1,
        "red_pacman_up_2" to R.drawable.red_pacman_up_2,
        "score_100" to R.drawable.score_100,
        "score_1000" to R.drawable.score_1000,
        "score_1600" to R.drawable.score_1600,
        "score_200" to R.drawable.score_200,
        "score_2000" to R.drawable.score_2000,
        "score_300" to R.drawable.score_300,
        "score_3000" to R.drawable.score_3000,
        "score_400" to R.drawable.score_400,
        "score_500" to R.drawable.score_500,
        "score_5000" to R.drawable.score_5000,
        "score_700" to R.drawable.score_700,
        "score_800" to R.drawable.score_800,
        "orange_pacman_left_3" to R.drawable.orange_pacman_down_3,
        "orange_pacman_right_3" to R.drawable.orange_pacman_down_3,
        "orange_pacman_up_3" to R.drawable.orange_pacman_down_3,
        "red_pacman_left_3" to R.drawable.red_pacman_down_3,
        "red_pacman_right_3" to R.drawable.red_pacman_down_3,
        "red_pacman_up_3" to R.drawable.red_pacman_down_3
        )
    }
}
