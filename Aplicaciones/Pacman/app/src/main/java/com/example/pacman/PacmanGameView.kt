package com.example.pacman

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.get
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

private const val COLUMNAS_LABERINTO = 28
private const val FILAS_LABERINTO = 31
private const val TAMANO_CELDA_ORIGEN = 8

private data class Casilla(val columna: Int, val fila: Int)

private enum class Direccion(
    val deltaColumna: Int,
    val deltaFila: Int,
    val nombreArchivo: String,
) {
    IZQUIERDA(-1, 0, "left"),
    DERECHA(1, 0, "right"),
    ARRIBA(0, -1, "up"),
    ABAJO(0, 1, "down"),
}

private data class Orbe(
    val casilla: Casilla,
    val limites: RectF,
    val esPoderosa: Boolean,
)

private enum class EstadoJuego { JUGANDO, MURIENDO, GANADO, PERDIDO }

private data class Fantasma(
    val prefijoSprite: String,
    val casa: Casilla,
    val intervaloMovimiento: Int,
    var casilla: Casilla = casa,
    var direccion: Direccion = Direccion.IZQUIERDA,
    var ojosRegresando: Boolean = false,
)

/** Vista del juego Pac-Man bajada con los sprites extractors del atlas. */
class VistaJuegoPacman @JvmOverloads constructor(
    contexto: Context,
    atributos: AttributeSet? = null,
) : View(contexto, atributos) {
    private val escalaDensidad = resources.displayMetrics.density
    private val bitmapMapa = BitmapFactory.decodeResource(resources, R.drawable.map_left)
    private val mapaSprites = HashMap<String, Bitmap>()
    private val recursosSprites = mapOf(
            "pacman_right_1" to R.drawable.pacman_right_1,
            "pacman_right_2" to R.drawable.pacman_right_2,
            "pacman_right_3" to R.drawable.pacman_right_3,
            "pacman_left_1" to R.drawable.pacman_left_1,
            "pacman_left_2" to R.drawable.pacman_left_2,
            "pacman_left_3" to R.drawable.pacman_left_3,
            "pacman_up_1" to R.drawable.pacman_up_1,
            "pacman_up_2" to R.drawable.pacman_up_2,
            "pacman_up_3" to R.drawable.pacman_up_3,
            "pacman_down_1" to R.drawable.pacman_down_1,
            "pacman_down_2" to R.drawable.pacman_down_2,
            "pacman_down_3" to R.drawable.pacman_down_3,
            "pacman_death_1" to R.drawable.pacman_death_1,
            "pacman_death_2" to R.drawable.pacman_death_2,
            "pacman_death_3" to R.drawable.pacman_death_3,
            "pacman_death_4" to R.drawable.pacman_death_4,
            "pacman_death_5" to R.drawable.pacman_death_5,
            "pacman_death_6" to R.drawable.pacman_death_6,
            "pacman_death_7" to R.drawable.pacman_death_7,
            "pacman_death_8" to R.drawable.pacman_death_8,
            "pacman_death_9" to R.drawable.pacman_death_9,
            "pacman_death_10" to R.drawable.pacman_death_10,
            "pacman_death_11" to R.drawable.pacman_death_11,
            "ghost_blinky_right" to R.drawable.ghost_blinky_right,
            "ghost_blinky_right_2" to R.drawable.ghost_blinky_right_2,
            "ghost_blinky_left" to R.drawable.ghost_blinky_left,
            "ghost_blinky_left_2" to R.drawable.ghost_blinky_left_2,
            "ghost_blinky_up" to R.drawable.ghost_blinky_up,
            "ghost_blinky_up_2" to R.drawable.ghost_blinky_up_2,
            "ghost_blinky_down" to R.drawable.ghost_blinky_down,
            "ghost_blinky_down_2" to R.drawable.ghost_blinky_down_2,
            "ghost_pinky_right" to R.drawable.ghost_pinky_right,
            "ghost_pinky_right_2" to R.drawable.ghost_pinky_right_2,
            "ghost_pinky_left" to R.drawable.ghost_pinky_left,
            "ghost_pinky_left_2" to R.drawable.ghost_pinky_left_2,
            "ghost_pinky_up" to R.drawable.ghost_pinky_up,
            "ghost_pinky_up_2" to R.drawable.ghost_pinky_up_2,
            "ghost_pinky_down" to R.drawable.ghost_pinky_down,
            "ghost_pinky_down_2" to R.drawable.ghost_pinky_down_2,
            "ghost_inky_right" to R.drawable.ghost_inky_right,
            "ghost_inky_right_2" to R.drawable.ghost_inky_right_2,
            "ghost_inky_left" to R.drawable.ghost_inky_left,
            "ghost_inky_left_2" to R.drawable.ghost_inky_left_2,
            "ghost_inky_up" to R.drawable.ghost_inky_up,
            "ghost_inky_up_2" to R.drawable.ghost_inky_up_2,
            "ghost_inky_down" to R.drawable.ghost_inky_down,
            "ghost_inky_down_2" to R.drawable.ghost_inky_down_2,
            "ghost_clyde_right" to R.drawable.ghost_clyde_right,
            "ghost_clyde_right_2" to R.drawable.ghost_clyde_right_2,
            "ghost_clyde_left" to R.drawable.ghost_clyde_left,
            "ghost_clyde_left_2" to R.drawable.ghost_clyde_left_2,
            "ghost_clyde_up" to R.drawable.ghost_clyde_up,
            "ghost_clyde_up_2" to R.drawable.ghost_clyde_up_2,
            "ghost_clyde_down" to R.drawable.ghost_clyde_down,
            "ghost_clyde_down_2" to R.drawable.ghost_clyde_down_2,
            "ghost_scared_blue_1" to R.drawable.ghost_scared_blue_1,
            "ghost_scared_blue_2" to R.drawable.ghost_scared_blue_2,
            "ghost_scared_white_1" to R.drawable.ghost_scared_white_1,
            "ghost_scared_white_2" to R.drawable.ghost_scared_white_2,
            "ghost_eyes_right" to R.drawable.ghost_eyes_right,
            "ghost_eyes_left" to R.drawable.ghost_eyes_left,
            "ghost_eyes_up" to R.drawable.ghost_eyes_up,
            "ghost_eyes_down" to R.drawable.ghost_eyes_down,
            "door_key" to R.drawable.door_key,
    )
    private val pincel = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gestorTiempo = Handler(Looper.getMainLooper())
    private val laberinto = Laberinto(bitmapMapa)
    private val orbesIniciales = laberinto.orbes
    private val orbesRestantes = LinkedHashMap<Casilla, Orbe>()
    private val azar = Random.Default

    private val inicioJugador = Casilla(13, 23)
    private val casillaLlave = Casilla(12, 20)
    private var casillaJugador = inicioJugador
    private var direccionJugador = Direccion.IZQUIERDA
    private var direccionSolicitada = Direccion.IZQUIERDA
    private var puntuacion = 0
    private var vidas = 3
    private var ticksTranscurridos = 0
    private var tickAnimacion = 0
    private var ticksAsustados = 0
    private var estado = EstadoJuego.JUGANDO
    private var rectanguloTablero = RectF()
    private var pixelesCasilla = 1f
    private var inicioDeslizamientoX = 0f
    private var inicioDeslizamientoY = 0f

    private val fantasmas = mutableListOf(
        Fantasma("blinky", Casilla(11, 13), 2),
        Fantasma("pinky", Casilla(14, 13), 3),
        Fantasma("inky", Casilla(11, 15), 3),
        Fantasma("clyde", Casilla(14, 15), 4),
    )

    private val bucleJuego = object : Runnable {
        override fun run() {
            when (estado) {
                EstadoJuego.JUGANDO -> {
                    actualizarJuego()
                    invalidate()
                }
                EstadoJuego.MURIENDO -> {
                    tickAnimacion++
                    if (tickAnimacion >= DURACION_ANIMACION_MUERTE) {
                        if (vidas <= 0) {
                            estado = EstadoJuego.PERDIDO
                        } else {
                            reiniciarPosiciones()
                        }
                    }
                    invalidate()
                }
                EstadoJuego.GANADO -> Unit
                EstadoJuego.PERDIDO -> {
                    tickAnimacion++
                    invalidate()
                }
            }
            gestorTiempo.postDelayed(this, INTERVALO_TICK_MS)
        }
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        mapaSprites["map_left"] = bitmapMapa
        recursosSprites.forEach { (nombreArchivo, identificador) ->
            mapaSprites[nombreArchivo] = decodificarSprite(identificador)
        }
        reiniciarJuego()
        gestorTiempo.postDelayed(bucleJuego, INTERVALO_TICK_MS)
    }

    override fun onDetachedFromWindow() {
        gestorTiempo.removeCallbacks(bucleJuego)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(0, 0, 0))
        dibujarMarcador(canvas)
        calcularTablero()
        canvas.drawBitmap(bitmapMapa, null, rectanguloTablero, pincelPixelado())
        ocultarOrbesRecogidas(canvas)
        dibujarPuerta(canvas)
        if (estado == EstadoJuego.JUGANDO && orbesRestantes.isEmpty()) dibujarLlave(canvas)
        dibujarFantasmas(canvas)
        dibujarJugador(canvas)
        dibujarControles(canvas)
        if (estado == EstadoJuego.GANADO || estado == EstadoJuego.PERDIDO) dibujarPantallaFinal(canvas)
    }

    private fun dibujarMarcador(canvas: Canvas) {
        val margen = convertirDp(18f)
        pincel.color = AMARILLO_PACMAN
        pincel.textSize = convertirDp(25f)
        pincel.typeface = Typeface.DEFAULT_BOLD
        pincel.textAlign = Paint.Align.CENTER
        canvas.drawText("PAC-MAN", width / 2f, convertirDp(32f), pincel)

        pincel.textAlign = Paint.Align.LEFT
        pincel.textSize = convertirDp(14f)
        pincel.color = Color.WHITE
        canvas.drawText("PUNTOS  $puntuacion", margen, convertirDp(58f), pincel)
        canvas.drawText("ORBES  ${orbesRestantes.size}", margen, convertirDp(79f), pincel)
        pincel.textAlign = Paint.Align.RIGHT
        canvas.drawText("TIEMPO  ${formatearTiempo(ticksTranscurridos / TICKS_POR_SEGUNDO)}", width - margen, convertirDp(58f), pincel)
        pincel.color = AMARILLO_PACMAN
        canvas.drawText("VIDAS  ${"● ".repeat(vidas).trim()}", width - margen, convertirDp(79f), pincel)
        pincel.textAlign = Paint.Align.LEFT
    }

    /** Have transparent el fond negro opcode que vine en los rectorates del atlas. */
    private fun decodificarSprite(identificador: Int): Bitmap {
        val bitmapOriginal = BitmapFactory.decodeResource(resources, identificador)
        val bitmapConTransparencia = bitmapOriginal.copy(Bitmap.Config.ARGB_8888, true)
        val pixeles = IntArray(bitmapConTransparencia.width * bitmapConTransparencia.height)
        bitmapConTransparencia.getPixels(
            pixeles,
            0,
            bitmapConTransparencia.width,
            0,
            0,
            bitmapConTransparencia.width,
            bitmapConTransparencia.height,
        )
        for (indice in pixeles.indices) {
            if ((pixeles[indice] and 0x00FFFFFF) == 0) {
                pixeles[indice] = Color.TRANSPARENT
            }
        }
        bitmapConTransparencia.setPixels(
            pixeles,
            0,
            bitmapConTransparencia.width,
            0,
            0,
            bitmapConTransparencia.width,
            bitmapConTransparencia.height,
        )
        bitmapOriginal.recycle()
        return bitmapConTransparencia
    }

    private fun calcularTablero() {
        val parteSuperior = convertirDp(92f)
        val espacioControles = convertirDp(178f)
        val anchoDisponible = width - convertirDp(24f)
        val altoDisponible = (height - parteSuperior - espacioControles).coerceAtLeast(convertirDp(120f))
        pixelesCasilla = min(anchoDisponible / COLUMNAS_LABERINTO, altoDisponible / FILAS_LABERINTO)
        val anchoTablero = pixelesCasilla * COLUMNAS_LABERINTO
        val altoTablero = pixelesCasilla * FILAS_LABERINTO
        val izquierda = (width - anchoTablero) / 2f
        rectanguloTablero = RectF(izquierda, parteSuperior, izquierda + anchoTablero, parteSuperior + altoTablero)
    }

    private fun ocultarOrbesRecogidas(canvas: Canvas) {
        pincel.color = Color.BLACK
        pincel.style = Paint.Style.FILL
        for ((casilla, limites) in orbesIniciales) {
            if (casilla !in orbesRestantes) {
                canvas.drawRect(
                    rectanguloTablero.left + limites.left * pixelesCasilla / TAMANO_CELDA_ORIGEN,
                    rectanguloTablero.top + limites.top * pixelesCasilla / TAMANO_CELDA_ORIGEN,
                    rectanguloTablero.left + limites.right * pixelesCasilla / TAMANO_CELDA_ORIGEN,
                    rectanguloTablero.top + limites.bottom * pixelesCasilla / TAMANO_CELDA_ORIGEN,
                    pincel,
                )
            }
        }
    }

    private fun dibujarPuerta(canvas: Canvas) {
        val izquierda = rectanguloTablero.left + 13 * pixelesCasilla
        val parteSuperior = rectanguloTablero.top + 12 * pixelesCasilla
        val rectanguloPuerta = RectF(izquierda, parteSuperior + pixelesCasilla * 0.25f, izquierda + 2 * pixelesCasilla, parteSuperior + pixelesCasilla * 0.75f)
        pincel.style = Paint.Style.FILL
        pincel.color = if (orbesRestantes.isEmpty()) Color.rgb(55, 220, 125) else Color.rgb(255, 187, 45)
        canvas.drawRoundRect(rectanguloPuerta, pixelesCasilla * 0.12f, pixelesCasilla * 0.12f, pincel)
        pincel.color = Color.rgb(55, 35, 15)
        val anchoBarra = pixelesCasilla * 0.11f
        for (indice in 1..5) {
            val coordenadaX = rectanguloPuerta.left + rectanguloPuerta.width() * indice / 6f
            canvas.drawRect(coordenadaX - anchoBarra / 2f, rectanguloPuerta.top, coordenadaX + anchoBarra / 2f, rectanguloPuerta.bottom, pincel)
        }
    }

    private fun dibujarLlave(canvas: Canvas) {
        val imagenLlave = mapaSprites["door_key"] ?: return
        dibujarSprite(canvas, imagenLlave, casillaLlave, 1.8f)
        pincel.color = AMARILLO_PACMAN
        pincel.textAlign = Paint.Align.CENTER
        pincel.textSize = convertirDp(11f)
        canvas.drawText("¡LLAVE!", centroX(casillaLlave), rectanguloTablero.top + (casillaLlave.fila - 0.35f) * pixelesCasilla, pincel)
        pincel.textAlign = Paint.Align.LEFT
    }

    private fun dibujarFantasmas(canvas: Canvas) {
        fantasmas.forEach { fantasma ->
            val nombreSprite = when {
                fantasma.ojosRegresando -> "ghost_eyes_${fantasma.direccion.nombreArchivo}"
                ticksAsustados in 1 until 20 && tickAnimacion % 4 < 2 ->
                    "ghost_scared_white_${tickAnimacion % 2 + 1}"
                ticksAsustados > 0 -> "ghost_scared_blue_${tickAnimacion % 2 + 1}"
                else -> "ghost_${fantasma.prefijoSprite}_${fantasma.direccion.nombreArchivo}_${tickAnimacion % 4 / 2 + 1}"
            }
            val bitmapFantasma = mapaSprites[nombreSprite]
                ?: mapaSprites["ghost_${fantasma.prefijoSprite}_${fantasma.direccion.nombreArchivo}"]
                ?: return@forEach
            val tamano = if (fantasma.ojosRegresando) 1.25f else 1.9f
            dibujarSprite(canvas, bitmapFantasma, fantasma.casilla, tamano)
        }
    }

    private fun dibujarJugador(canvas: Canvas) {
        val nombreSprite = if (estado == EstadoJuego.MURIENDO || estado == EstadoJuego.PERDIDO) {
            "pacman_death_${(tickAnimacion / 2 + 1).coerceIn(1, 11)}"
        } else {
            "pacman_${direccionJugador.nombreArchivo}_${tickAnimacion % 6 / 2 + 1}"
        }
        mapaSprites[nombreSprite]?.let { dibujarSpriteEscalaOrigen(canvas, it, casillaJugador) }
    }

    private fun dibujarSpriteEscalaOrigen(canvas: Canvas, imagenSprite: Bitmap, casilla: Casilla) {
        // Los rectorates del atlas include largeness variables; manner un tamale visible uniform.
        val escalaPixel = (pixelesCasilla * TAMANO_ACTOR_EN_CASILLAS) /
            maxOf(imagenSprite.width, imagenSprite.height)
        val anchoDestino = imagenSprite.width * escalaPixel
        val altoDestino = imagenSprite.height * escalaPixel
        val centroX = centroX(casilla)
        val centroY = centroY(casilla)
        val destino = RectF(
            centroX - anchoDestino / 2f,
            centroY - altoDestino / 2f,
            centroX + anchoDestino / 2f,
            centroY + altoDestino / 2f,
        )
        canvas.drawBitmap(imagenSprite, null, destino, pincelPixelado())
    }

    private fun dibujarSprite(canvas: Canvas, imagenSprite: Bitmap, casilla: Casilla, tamanoEnCeldas: Float) {
        val tamano = pixelesCasilla * tamanoEnCeldas
        val centroX = centroX(casilla)
        val centroY = centroY(casilla)
        val destino = RectF(centroX - tamano / 2f, centroY - tamano / 2f, centroX + tamano / 2f, centroY + tamano / 2f)
        canvas.drawBitmap(imagenSprite, null, destino, pincelPixelado())
    }

    private fun dibujarControles(canvas: Canvas) {
        val centroX = width / 2f
        val centroY = height - convertirDp(100f)
        val separacion = convertirDp(54f)
        val radio = convertirDp(22f)
        val botones = listOf(
            Triple(Direccion.ARRIBA, centroX, centroY - separacion),
            Triple(Direccion.ABAJO, centroX, centroY + separacion),
            Triple(Direccion.IZQUIERDA, centroX - separacion, centroY),
            Triple(Direccion.DERECHA, centroX + separacion, centroY),
        )
        botones.forEach { (direccion, x, y) ->
            pincel.color = Color.rgb(24, 34, 70)
            pincel.style = Paint.Style.FILL
            canvas.drawCircle(x, y, radio, pincel)
            pincel.color = AMARILLO_PACMAN
            pincel.style = Paint.Style.STROKE
            pincel.strokeWidth = convertirDp(2f)
            canvas.drawCircle(x, y, radio, pincel)
            pincel.style = Paint.Style.FILL
            dibujarFlecha(canvas, direccion, x, y)
        }
        pincel.color = Color.LTGRAY
        pincel.textSize = convertirDp(11f)
        pincel.textAlign = Paint.Align.CENTER
        canvas.drawText("DESLIZA O USA LAS FLECHES", centroX, height - convertirDp(12f), pincel)
        pincel.textAlign = Paint.Align.LEFT
    }

    private fun dibujarFlecha(canvas: Canvas, direccion: Direccion, centroX: Float, centroY: Float) {
        val trayectoria = android.graphics.Path()
        val mitad = convertirDp(9f)
        when (direccion) {
            Direccion.ARRIBA -> {
                trayectoria.moveTo(centroX, centroY - mitad)
                trayectoria.lineTo(centroX - mitad, centroY + mitad / 2)
                trayectoria.lineTo(centroX + mitad, centroY + mitad / 2)
            }
            Direccion.ABAJO -> {
                trayectoria.moveTo(centroX, centroY + mitad)
                trayectoria.lineTo(centroX - mitad, centroY - mitad / 2)
                trayectoria.lineTo(centroX + mitad, centroY - mitad / 2)
            }
            Direccion.IZQUIERDA -> {
                trayectoria.moveTo(centroX - mitad, centroY)
                trayectoria.lineTo(centroX + mitad / 2, centroY - mitad)
                trayectoria.lineTo(centroX + mitad / 2, centroY + mitad)
            }
            Direccion.DERECHA -> {
                trayectoria.moveTo(centroX + mitad, centroY)
                trayectoria.lineTo(centroX - mitad / 2, centroY - mitad)
                trayectoria.lineTo(centroX - mitad / 2, centroY + mitad)
            }
        }
        trayectoria.close()
        pincel.color = AMARILLO_PACMAN
        canvas.drawPath(trayectoria, pincel)
    }

    private fun dibujarPantallaFinal(canvas: Canvas) {
        pincel.color = Color.argb(210, 0, 0, 0)
        canvas.drawRect(rectanguloTablero, pincel)
        pincel.textAlign = Paint.Align.CENTER
        pincel.typeface = Typeface.DEFAULT_BOLD
        pincel.textSize = convertirDp(26f)
        pincel.color = if (estado == EstadoJuego.GANADO) AMARILLO_PACMAN else Color.RED
        canvas.drawText(if (estado == EstadoJuego.GANADO) "¡LLAVE OBTENIDA!" else "FIN DEL JUEGO", width / 2f, rectanguloTablero.centerY(), pincel)
        pincel.textSize = convertirDp(16f)
        pincel.color = Color.WHITE
        canvas.drawText("Puntuación: $puntuacion  ·  Tiempo: ${formatearTiempo(ticksTranscurridos / TICKS_POR_SEGUNDO)}", width / 2f, rectanguloTablero.centerY() + convertirDp(30f), pincel)
        canvas.drawText("Toca para jugar de Nuevo", width / 2f, rectanguloTablero.centerY() + convertirDp(58f), pincel)
        pincel.typeface = Typeface.DEFAULT
        pincel.textAlign = Paint.Align.LEFT
    }

    private fun actualizarJuego() {
        tickAnimacion++
        ticksTranscurridos++
        if (ticksAsustados > 0) ticksAsustados--

        val solicitaGiro = direccionSolicitada != direccionJugador
        val casillaSolicitada = laberinto.siguienteCasilla(
            casillaJugador,
            direccionSolicitada,
            tolerarEsquina = solicitaGiro,
            radioColision = RADIO_COLISION_PACMAN,
        )
        val giroAplicado = casillaSolicitada != null &&
            puedeEntrarJugador(casillaSolicitada, tolerarEsquina = solicitaGiro)
        if (giroAplicado) direccionJugador = direccionSolicitada
        val siguienteCasillaJugador = laberinto.siguienteCasilla(
            casillaJugador,
            direccionJugador,
            tolerarEsquina = giroAplicado && solicitaGiro,
            radioColision = RADIO_COLISION_PACMAN,
        )
        if (siguienteCasillaJugador != null &&
            puedeEntrarJugador(siguienteCasillaJugador, tolerarEsquina = giroAplicado && solicitaGiro)
        ) casillaJugador = siguienteCasillaJugador
        recogerOrbeJugador()

        actualizarFantasmas()
        comprobarColisionFantasma()

        if (orbesRestantes.isEmpty() && casillaJugador == casillaLlave) estado = EstadoJuego.GANADO
    }

    private fun actualizarFantasmas() {
        fantasmas.forEach { fantasma ->
            if (fantasma.ojosRegresando) {
                val pasoDeRegreso = laberinto.siguientePaso(
                    fantasma.casilla,
                    fantasma.casa,
                    permitirPuerta = true,
                )
                if (pasoDeRegreso == null) {
                    fantasma.ojosRegresando = false
                    fantasma.direccion = Direccion.IZQUIERDA
                } else {
                    fantasma.casilla = pasoDeRegreso.first
                    fantasma.direccion = pasoDeRegreso.second
                    if (fantasma.casilla == fantasma.casa) fantasma.ojosRegresando = false
                }
                return@forEach
            }
            if (tickAnimacion % fantasma.intervaloMovimiento != 0) return@forEach
            val siguiente = if (ticksAsustados > 0) {
                laberinto.vecinos(fantasma.casilla, permitirPuerta = true).elegirAlAzarONulo(azar)
            } else {
                laberinto.siguientePaso(fantasma.casilla, casillaJugador, permitirPuerta = true)
                    ?: if (fantasma.casilla != casillaJugador) {
                        laberinto.vecinos(fantasma.casilla, permitirPuerta = true).elegirAlAzarONulo(azar)
                    } else {
                        null
                    }
            }
            siguiente?.let { (casillaSiguiente, direccionSiguiente) ->
                fantasma.casilla = casillaSiguiente
                fantasma.direccion = direccionSiguiente
            }
        }
    }

    private fun recogerOrbeJugador() {
        val orbe = orbesRestantes.remove(casillaJugador) ?: return
        puntuacion += if (orbe.esPoderosa) 50 else 10
        if (orbe.esPoderosa) ticksAsustados = 8 * TICKS_POR_SEGUNDO
    }

    private fun comprobarColisionFantasma() {
        val fantasmaGolpeado = fantasmas.firstOrNull { !it.ojosRegresando && it.casilla == casillaJugador } ?: return
        if (ticksAsustados > 0) {
            puntuacion += 200
            fantasmaGolpeado.ojosRegresando = true
            return
        }

        vidas--
        estado = EstadoJuego.MURIENDO
        tickAnimacion = 0
    }

    private fun puedeEntrarJugador(casilla: Casilla, tolerarEsquina: Boolean = false): Boolean {
        val casillaAbierta = if (tolerarEsquina) {
            laberinto.casillaAbiertaSinMargen(casilla)
        } else {
            laberinto.casillaAbierta(casilla)
        }
        if (!casillaAbierta) return false
        val dentroCasaFantasmas = casilla.columna in 11..16 && casilla.fila in 13..18
        val enPuerta = casilla.fila == 12 && casilla.columna in 13..14
        return orbesRestantes.isEmpty() || (!dentroCasaFantasmas && !enPuerta)
    }

    private fun reiniciarJuego() {
        orbesRestantes.clear()
        orbesIniciales.forEach { orbesRestantes[it.casilla] = it }
        puntuacion = 0
        vidas = 3
        ticksTranscurridos = 0
        tickAnimacion = 0
        ticksAsustados = 0
        estado = EstadoJuego.JUGANDO
        reiniciarPosiciones()
        invalidate()
    }

    private fun reiniciarPosiciones() {
        casillaJugador = inicioJugador
        direccionJugador = Direccion.IZQUIERDA
        direccionSolicitada = Direccion.IZQUIERDA
        fantasmas.forEach {
            it.casilla = it.casa
            it.direccion = Direccion.IZQUIERDA
            it.ojosRegresando = false
        }
        tickAnimacion = 0
        estado = EstadoJuego.JUGANDO
    }

    private fun establecerDireccion(direccion: Direccion) {
        when (estado) {
            EstadoJuego.JUGANDO -> direccionSolicitada = direccion
            EstadoJuego.MURIENDO -> return
            EstadoJuego.GANADO, EstadoJuego.PERDIDO -> reiniciarJuego()
        }
        invalidate()
    }

    override fun onTouchEvent(evento: MotionEvent): Boolean {
        when (evento.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                inicioDeslizamientoX = evento.x
                inicioDeslizamientoY = evento.y
                if (estado == EstadoJuego.MURIENDO) return true
                if (estado == EstadoJuego.GANADO || estado == EstadoJuego.PERDIDO) {
                    reiniciarJuego()
                    return true
                }
                val botonPulsado = direccionControlEn(evento.x, evento.y)
                if (botonPulsado != null) establecerDireccion(botonPulsado)
                return true
            }
            MotionEvent.ACTION_UP -> {
                val desplazamientoX = evento.x - inicioDeslizamientoX
                val desplazamientoY = evento.y - inicioDeslizamientoY
                if (abs(desplazamientoX) > convertirDp(24f) || abs(desplazamientoY) > convertirDp(24f)) {
                    establecerDireccion(if (abs(desplazamientoX) > abs(desplazamientoY)) {
                        if (desplazamientoX < 0) Direccion.IZQUIERDA else Direccion.DERECHA
                    } else {
                        if (desplazamientoY < 0) Direccion.ARRIBA else Direccion.ABAJO
                    })
                }
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onKeyDown(keyCode: Int, evento: KeyEvent?): Boolean {
        val direccion = when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A -> Direccion.IZQUIERDA
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D -> Direccion.DERECHA
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W -> Direccion.ARRIBA
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S -> Direccion.ABAJO
            else -> null
        }
        if (direccion != null) {
            establecerDireccion(direccion)
            return true
        }
        return super.onKeyDown(keyCode, evento)
    }

    private fun direccionControlEn(coordenadaX: Float, coordenadaY: Float): Direccion? {
        val centroX = width / 2f
        val centroY = height - convertirDp(100f)
        val separacion = convertirDp(54f)
        val radio = convertirDp(27f)
        return listOf(
            Triple(Direccion.ARRIBA, centroX, centroY - separacion),
            Triple(Direccion.ABAJO, centroX, centroY + separacion),
            Triple(Direccion.IZQUIERDA, centroX - separacion, centroY),
            Triple(Direccion.DERECHA, centroX + separacion, centroY),
        ).firstOrNull { (_, botonX, botonY) ->
            val desplazamientoX = coordenadaX - botonX
            val desplazamientoY = coordenadaY - botonY
            desplazamientoX * desplazamientoX + desplazamientoY * desplazamientoY <= radio * radio
        }?.let { (direccion, _, _) -> direccion }
    }

    private fun centroX(casilla: Casilla): Float = rectanguloTablero.left + (casilla.columna + 0.5f) * pixelesCasilla
    private fun centroY(casilla: Casilla): Float = rectanguloTablero.top + (casilla.fila + 0.5f) * pixelesCasilla
    private fun convertirDp(valor: Float): Float = valor * escalaDensidad

    private fun pincelPixelado(): Paint = Paint().apply { isFilterBitmap = false }

    private fun formatearTiempo(segundosTotales: Int): String =
        "%02d:%02d".format(segundosTotales / 60, segundosTotales % 60)

    private inner class Laberinto(private val mapaBitmap: Bitmap) {
        val orbes: List<Orbe> = buscarOrbes()
        private val transiciones = HashMap<Triple<Casilla, Direccion, Pair<Boolean, Int>>, Casilla?>()
        private val mascaraParedes = crearMascaraParedes()

        fun casillaAbierta(casilla: Casilla): Boolean {
            return casillaValida(casilla) &&
                !hayParedPixel(casilla.columna * TAMANO_CELDA_ORIGEN + 4, casilla.fila * TAMANO_CELDA_ORIGEN + 4)
        }

        fun casillaAbiertaSinMargen(casilla: Casilla): Boolean =
            casillaValida(casilla) &&
                !esPared(mapaBitmap[casilla.columna * TAMANO_CELDA_ORIGEN + 4, casilla.fila * TAMANO_CELDA_ORIGEN + 4])

        private fun casillaValida(casilla: Casilla): Boolean =
            casilla.columna in 0 until COLUMNAS_LABERINTO && casilla.fila in 0 until FILAS_LABERINTO

        fun siguienteCasilla(
            casilla: Casilla,
            direccion: Direccion,
            tolerarEsquina: Boolean = false,
            radioColision: Int = RADIO_COLISION_FANTASMA,
        ): Casilla? {
            val clave = Triple(casilla, direccion, tolerarEsquina to radioColision)
            if (transiciones.containsKey(clave)) return transiciones[clave]

            val siguiente = calcularSiguienteCasilla(casilla, direccion, tolerarEsquina, radioColision)
            transiciones[clave] = siguiente
            return siguiente
        }

        private fun calcularSiguienteCasilla(
            casilla: Casilla,
            direccion: Direccion,
            tolerarEsquina: Boolean,
            radioColision: Int,
        ): Casilla? {
            if (casilla.fila == FILA_TUNEL && direccion == Direccion.IZQUIERDA && casilla.columna == 0) {
                return Casilla(COLUMNAS_LABERINTO - 1, FILA_TUNEL).takeIf(::casillaAbierta)
            }
            if (casilla.fila == FILA_TUNEL && direccion == Direccion.DERECHA && casilla.columna == COLUMNAS_LABERINTO - 1) {
                return Casilla(0, FILA_TUNEL).takeIf(::casillaAbierta)
            }
            val siguiente = Casilla(casilla.columna + direccion.deltaColumna, casilla.fila + direccion.deltaFila)
            val destinoAbierto = if (tolerarEsquina) casillaAbiertaSinMargen(siguiente) else casillaAbierta(siguiente)
            if (!destinoAbierto) return null
            for (paso in -radioColision..TAMANO_CELDA_ORIGEN + radioColision) {
                val coordenadaX = casilla.columna * TAMANO_CELDA_ORIGEN + 4 + direccion.deltaColumna * paso
                val coordenadaY = casilla.fila * TAMANO_CELDA_ORIGEN + 4 + direccion.deltaFila * paso
                for (desplazamiento in -radioColision..radioColision) {
                    val pixelX = coordenadaX + if (direccion.deltaFila != 0) desplazamiento else 0
                    val pixelY = coordenadaY + if (direccion.deltaColumna != 0) desplazamiento else 0
                    val dentroDelTunel = casilla.fila == FILA_TUNEL && direccion.deltaFila == 0
                    val fueraDelMapa = pixelX !in 0 until mapaBitmap.width || pixelY !in 0 until mapaBitmap.height
                    if (fueraDelMapa) {
                        if (!dentroDelTunel || pixelY !in 0 until mapaBitmap.height) return null
                    } else if (esPared(mapaBitmap[pixelX, pixelY])) {
                        return null
                    }
                }
            }
            return siguiente
        }

        /** Engross un píxel las unease azures y sierra hue cos de rasterize en las parades. */
        private fun crearMascaraParedes(): BooleanArray {
            val ancho = mapaBitmap.width
            val alto = mapaBitmap.height
            val paredesOriginales = BooleanArray(ancho * alto)
            val paredesAmpliadas = BooleanArray(ancho * alto)
            for (y in 0 until alto) {
                for (x in 0 until ancho) {
                    if (esPared(mapaBitmap[x, y])) paredesOriginales[y * ancho + x] = true
                }
            }
            for (y in 0 until alto) {
                for (x in 0 until ancho) {
                    if (!paredesOriginales[y * ancho + x]) continue
                    for (desplazamientoY in -1..1) {
                        for (desplazamientoX in -1..1) {
                            val vecinoX = x + desplazamientoX
                            val vecinoY = y + desplazamientoY
                            if (vecinoX in 0 until ancho && vecinoY in 0 until alto) {
                                paredesAmpliadas[vecinoY * ancho + vecinoX] = true
                            }
                        }
                    }
                }
            }
            return paredesAmpliadas
        }

        private fun hayParedPixel(coordenadaX: Int, coordenadaY: Int): Boolean =
            coordenadaX !in 0 until mapaBitmap.width || coordenadaY !in 0 until mapaBitmap.height ||
                mascaraParedes[coordenadaY * mapaBitmap.width + coordenadaX]

        fun vecinos(casilla: Casilla, permitirPuerta: Boolean): List<Pair<Casilla, Direccion>> =
            Direccion.entries.mapNotNull { direccion ->
                val siguiente = siguienteCasilla(casilla, direccion) ?: return@mapNotNull null
                if (!permitirPuerta && !puedeEntrarJugador(siguiente)) return@mapNotNull null
                siguiente to direccion
            }

        fun siguientePaso(inicio: Casilla, objetivo: Casilla, permitirPuerta: Boolean): Pair<Casilla, Direccion>? {
            if (inicio == objetivo) return null
            val cola = ArrayDeque<Triple<Casilla, Casilla?, Direccion?>>()
            val visitadas = HashSet<Casilla>()
            cola.add(Triple(inicio, null, null))
            visitadas.add(inicio)
            while (cola.isNotEmpty()) {
                val (actual, primeraCasilla, primeraDireccion) = cola.removeFirst()
                for ((siguiente, direccion) in vecinos(actual, permitirPuerta)) {
                    if (!visitadas.add(siguiente)) continue
                    val primera = primeraCasilla ?: siguiente
                    val direccionPrimera = primeraDireccion ?: direccion
                    if (siguiente == objetivo) return primera to direccionPrimera
                    cola.add(Triple(siguiente, primera, direccionPrimera))
                }
            }
            return null
        }

        private fun buscarOrbes(): List<Orbe> {
            val colorOrbe = Color.rgb(255, 183, 174)
            val visitadas = BooleanArray(mapaBitmap.width * mapaBitmap.height)
            val encontrados = ArrayList<Orbe>()
            val cola = ArrayDeque<Int>()
            for (coordenadaY in 0 until mapaBitmap.height) {
                for (coordenadaX in 0 until mapaBitmap.width) {
                    val indice = coordenadaY * mapaBitmap.width + coordenadaX
                    if (visitadas[indice] || mapaBitmap[coordenadaX, coordenadaY] != colorOrbe) continue
                    visitadas[indice] = true
                    cola.add(indice)
                    var xMinima = coordenadaX
                    var xMaxima = coordenadaX
                    var yMinima = coordenadaY
                    var yMaxima = coordenadaY
                    var cantidadPixeles = 0
                    while (cola.isNotEmpty()) {
                        val actual = cola.removeFirst()
                        val pixelX = actual % mapaBitmap.width
                        val pixelY = actual / mapaBitmap.width
                        xMinima = min(xMinima, pixelX)
                        xMaxima = maxOf(xMaxima, pixelX)
                        yMinima = min(yMinima, pixelY)
                        yMaxima = maxOf(yMaxima, pixelY)
                        cantidadPixeles++
                        val pixelesVecinos = intArrayOf(
                            actual - 1,
                            actual + 1,
                            actual - mapaBitmap.width,
                            actual + mapaBitmap.width,
                        )
                        pixelesVecinos.forEach { candidato ->
                            if (candidato !in visitadas.indices || visitadas[candidato]) return@forEach
                            val coordenadaX = candidato % mapaBitmap.width
                            val coordenadaY = candidato / mapaBitmap.width
                            if (abs(coordenadaX - pixelX) + abs(coordenadaY - pixelY) != 1) return@forEach
                            if (mapaBitmap[coordenadaX, coordenadaY] == colorOrbe) {
                                visitadas[candidato] = true
                                cola.add(candidato)
                            }
                        }
                    }
                    val centroX = (xMinima + xMaxima + 1) / 2f
                    val centroY = (yMinima + yMaxima + 1) / 2f
                    val casilla = Casilla((centroX / TAMANO_CELDA_ORIGEN).toInt(), (centroY / TAMANO_CELDA_ORIGEN).toInt())
                    encontrados.add(
                        Orbe(
                            casilla = casilla,
                            limites = RectF(xMinima.toFloat(), yMinima.toFloat(), (xMaxima + 1).toFloat(), (yMaxima + 1).toFloat()),
                            esPoderosa = cantidadPixeles > 4,
                        ),
                    )
                }
            }
            return encontrados.distinctBy { it.casilla }
        }

        private fun esPared(color: Int): Boolean =
            Color.blue(color) > 180 && Color.red(color) < 80 && Color.green(color) < 80
    }

    private fun List<Pair<Casilla, Direccion>>.elegirAlAzarONulo(azar: Random): Pair<Casilla, Direccion>? =
        if (isEmpty()) null else this[azar.nextInt(size)]

    companion object {
        private const val INTERVALO_TICK_MS = 125L
        private const val TICKS_POR_SEGUNDO = 8
        private const val DURACION_ANIMACION_MUERTE = 22
        private const val TAMANO_ACTOR_EN_CASILLAS = 2f
        private const val RADIO_COLISION_PACMAN = 6
        private const val RADIO_COLISION_FANTASMA = 2
        private const val FILA_TUNEL = 14
        private val AMARILLO_PACMAN = Color.rgb(255, 235, 0)
    }
}
