package com.example.pacants

import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random

enum class RolPacman(val nombre: String, val prefijoSprite: String, val color: Int) {
    EXPLORADOR("Exploradores", "pacman", Color.rgb(255, 224, 38)),
    OBRERO("Obreros", "orange_pacman", Color.rgb(255, 142, 38)),
    DEFENSOR("Defensores", "red_pacman", Color.rgb(255, 66, 84)),
}

enum class DireccionPacman(val dx: Int, val dy: Int, val clave: String) {
    IZQUIERDA(-1, 0, "left"), DERECHA(1, 0, "right"), ARRIBA(0, -1, "up"), ABAJO(0, 1, "down"),
}

enum class MejoraColonia(val nombre: String) {
    BASE("Centro de la colonia"),
    TUNELES("Herramientas de túnel"),
    ALMACEN("Almacén de frutas"),
    DEFENSA("Defensa de la entrada"),
    RESISTENCIA_DEFENSORES("Resistencia defensores"),
}

data class PacmanColono(
    val identificador: Int,
    val rol: RolPacman,
    var columna: Int,
    var fila: Int,
    var direccion: DireccionPacman = DireccionPacman.DERECHA,
    var animacionMuerte: Int = 0,
    var objetivoPatrullaColumna: Int = -1,
    var objetivoPatrullaFila: Int = -1,
    var objetivoFantasmaId: Int = -1,
    var objetivoFrutaColumna: Int = -1,
    var objetivoFrutaFila: Int = -1,
    var objetivoTunelColumna: Int = -1,
    var objetivoTunelFila: Int = -1,
    var ultimaColumna: Int = -1,
    var ultimaFila: Int = -1,
    var resistenciaActual: Float = 20f,
    var agotado: Boolean = false,
    var objetivoBaseColumna: Int = -1,
    var objetivoBaseFila: Int = -1,
    var refugiadoPorAmenaza: Boolean = false,
) {
    /** Estado temporal para detectar transiciones de resistencia sin crear colecciones en cada fotograma. */
    var estabaAgotadoAntesDelAvance: Boolean = false
}

data class FantasmaColonia(
    val identificador: Int,
    val tipo: String,
    var columna: Int,
    var fila: Int,
    var direccion: DireccionPacman = DireccionPacman.IZQUIERDA,
    var asustado: Boolean = false,
    var objetivoPacmanId: Int = -1,
    var ultimaColumna: Int = -1,
    var ultimaFila: Int = -1,
)

data class TipoFruta(val clave: String, val nombre: String, val precio: Int)
data class FrutaColonia(val columna: Int, val fila: Int, val tipo: TipoFruta)

/** Lógica autónoma, economía, mapa procedural y estado serializable de la colonia. */
class ModeloColonia {
    private class RutaBaseCacheada(
        val destino: Int,
        val revisionMapa: Int,
        val pasos: IntArray,
        var siguientePaso: Int = 0,
        var posicionEsperada: Int,
    )

    private companion object {
        const val INTERVALO_SIMULACION_MS = 430L
        const val REGENERACION_RESISTENCIA_POR_SEGUNDO = .75f
        const val GASTO_RESISTENCIA_OBRERO_POR_CELDA = 1f
        const val GASTO_RESISTENCIA_DEFENSOR_POR_FANTASMA = 8f
        const val ALFABETO_TUNEL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        const val MAX_MARCA_RUTA = Int.MAX_VALUE shr 2
    }

    var columnas = 58; private set
    var filas = 32; private set
    var tunel = Array(filas) { BooleanArray(columnas) }; private set
    val pacmanes = mutableListOf<PacmanColono>()
    val fantasmas = mutableListOf<FantasmaColonia>()
    val frutas = mutableListOf<FrutaColonia>()
    val inventario = linkedMapOf(
        "cherry" to 0, "strawberry" to 0, "orange" to 0, "apple" to 0,
        "melon" to 0, "bell" to 0, "galaxian" to 0,
    )
    var frutasAlmacenadas: Int = 0; private set
    val tiposFruta = listOf(
        TipoFruta("cherry", "Cereza", 10), TipoFruta("strawberry", "Fresa", 20),
        TipoFruta("orange", "Naranja", 30), TipoFruta("apple", "Manzana", 45),
        TipoFruta("melon", "Melón", 65), TipoFruta("bell", "Campana", 90),
        TipoFruta("galaxian", "Galaxiana", 120),
    )
    val tiposFantasma = listOf("blinky", "pinky", "inky", "clyde")
    val nivelesMejora = MejoraColonia.entries.associateWith { 0 }.toMutableMap()
    var nivelBase = 1; private set
    var puntos = 0; private set
    var ciclo = 0; private set
    var siguienteIdentificador = 1
    val capacidadAlmacen get() = 12 + (nivelesMejora[MejoraColonia.ALMACEN] ?: 0) * 8
    private val limiteFrutas get() = maxOf(34, pacmanes.count { it.rol == RolPacman.EXPLORADOR && it.animacionMuerte == 0 } * 2)
    private var posicionBaseX = columnas / 2
    private var posicionBaseY = filas / 2
    var desplazamientoCoordenadasX = 0; private set
    var desplazamientoCoordenadasY = 0; private set
    val columnaBase get() = posicionBaseX
    val filaBase get() = posicionBaseY
    val anchoBase get() = 3 + (nivelBase - 1) / 2
    val altoBase get() = 2 + (nivelBase - 1) / 3
    private var azar = Random(System.currentTimeMillis())
    private var tiempoCicloMs = 0L
    private val tiempoMovimientoPacman = mutableMapOf<Int, Long>()
    private var cacheTunelesAbiertos: Collection<Pair<Int, Int>>? = null
    private var filasTunelesAbiertos: Array<MutableList<Pair<Int, Int>>>? = null
    private var cantidadTunelesCache = 0
    var revisionEstructuraMapa: Int = 0
        private set
    private val celdasTunelModificadas = ArrayList<Int>()
    private var cacheFronterasAbiertas: java.util.TreeSet<Int>? = null
    private val posicionesFruta = HashSet<Int>()
    private val candidatosFruta = ArrayList<Pair<Int, Int>>()
    private val fantasmasEnAvance = ArrayList<FantasmaColonia>()
    private val pacmanesPorIdEnAvance = HashMap<Int, PacmanColono>()
    private val vecinosFantasmaScratch = IntArray(4)
    private var estadoRuta = IntArray(0)
    private var predecesoresRuta = IntArray(0)
    private var colaRuta = IntArray(0)
    private var costesRuta = IntArray(0)
    private var prioridadesRuta = IntArray(0)
    private var posicionesHeapRuta = IntArray(0)
    private var ocupacionRuta = IntArray(0)
    private var marcaRuta = 0
    private var tamanoHeapRuta = 0
    private var pasosRutaTemporal = IntArray(0)
    private var longitudPasosRutaTemporal = 0
    private val rutasBaseCacheadas = HashMap<Int, RutaBaseCacheada>()

    private inner class VistaTunelesAbiertos : AbstractCollection<Pair<Int, Int>>() {
        override val size: Int get() = cantidadTunelesCache

        override fun iterator(): Iterator<Pair<Int, Int>> {
            val filas = filasTunelesAbiertos ?: return emptyList<Pair<Int, Int>>().iterator()
            return object : Iterator<Pair<Int, Int>> {
                private var fila = 0
                private var indiceColumna = 0

                private fun avanzarHastaElemento() {
                    while (fila < filas.size && indiceColumna >= filas[fila].size) {
                        fila++
                        indiceColumna = 0
                    }
                }

                override fun hasNext(): Boolean {
                    avanzarHastaElemento()
                    return fila < filas.size
                }

                override fun next(): Pair<Int, Int> {
                    if (!hasNext()) throw NoSuchElementException()
                    return filas[fila][indiceColumna++]
                }
            }
        }
    }

    private val vistaTunelesAbiertos = VistaTunelesAbiertos()

    init { nuevaPartida() }

    @Synchronized
    fun nuevaPartida() {
        columnas = 58; filas = 32
        tunel = Array(filas) { BooleanArray(columnas) }
        invalidarCacheTuneles()
        posicionBaseX = columnas / 2; posicionBaseY = filas / 2
        desplazamientoCoordenadasX = 0; desplazamientoCoordenadasY = 0
        pacmanes.clear(); fantasmas.clear(); frutas.clear()
        inventario.keys.forEach { inventario[it] = 0 }
        frutasAlmacenadas = 0
        nivelesMejora.keys.forEach { nivelesMejora[it] = 0 }
        nivelBase = 1; puntos = 0; ciclo = 0; siguienteIdentificador = 1
        tiempoCicloMs = 0L
        tiempoMovimientoPacman.clear()
        azar = Random(System.currentTimeMillis())
        cavarBase()
        repeat(3) { abrirRedDeTuneles() }
        RolPacman.entries.forEach(::crearPacmanEnBase)
        generarFrutasHasta(27)
    }

    private fun crearPacmanEnBase(rol: RolPacman) {
        val centro = columnaBase to filaBase
        val posicion = buscarPosicionLibreBase(referencia = centro) ?: centro
        pacmanes += PacmanColono(
            identificador = siguienteIdentificador++,
            rol = rol,
            columna = posicion.first,
            fila = posicion.second,
            resistenciaActual = capacidadResistencia(rol),
        )
    }

    private fun abrirRedDeTuneles() {
        val salida = columnaBase - anchoBase - 1
        abrirTunel(salida, filaBase)
        val direcciones = DireccionPacman.entries
        repeat(11) {
            var columna = salida
            var fila = filaBase
            var direccion = direcciones.random(azar)
            repeat(azar.nextInt(13, 27)) {
                if (azar.nextFloat() < .19f) direccion = direcciones.random(azar)
                var nuevaColumna = (columna + direccion.dx).coerceIn(1, columnas - 2)
                var nuevaFila = (fila + direccion.dy).coerceIn(1, filas - 2)
                if (esDentroBase(nuevaColumna, nuevaFila)) {
                    nuevaColumna = salida - 1
                    nuevaFila = filaBase
                    direccion = DireccionPacman.IZQUIERDA
                }
                columna = nuevaColumna; fila = nuevaFila
                abrirTunel(columna, fila)
                if (azar.nextFloat() < .10f) {
                    val ramal = direcciones.filter { it.dx != -direccion.dx || it.dy != -direccion.dy }.random(azar)
                    val ramalColumna = (columna + ramal.dx).coerceIn(1, columnas - 2)
                    val ramalFila = (fila + ramal.dy).coerceIn(1, filas - 2)
                    if (!esDentroBase(ramalColumna, ramalFila)) abrirTunel(ramalColumna, ramalFila)
                }
            }
        }
    }

    private fun cavarBase() {
        for (fila in filaBase - altoBase..filaBase + altoBase) {
            for (columna in columnaBase - anchoBase..columnaBase + anchoBase) abrirTunel(columna, fila)
        }
        abrirTunel(columnaBase - anchoBase - 1, filaBase)
    }

    private fun asegurarExpansion(pacman: PacmanColono) {
        val izquierda = pacman.columna < 8
        val derecha = pacman.columna > columnas - 9
        val arriba = pacman.fila < 8
        val abajo = pacman.fila > filas - 9
        if (izquierda || derecha || arriba || abajo) expandirMapa(izquierda, derecha, arriba, abajo)
    }

    private fun asegurarExpansionBase() {
        val izquierda = columnaBase - anchoBase - 1 < 2
        val derecha = columnaBase + anchoBase + 1 > columnas - 3
        val arriba = filaBase - altoBase - 1 < 2
        val abajo = filaBase + altoBase + 1 > filas - 3
        if (izquierda || derecha || arriba || abajo) expandirMapa(izquierda, derecha, arriba, abajo)
    }

    private fun expandirMapa(izquierda: Boolean, derecha: Boolean, arriba: Boolean, abajo: Boolean) {
        val columnasAnteriores = columnas
        val filasAnteriores = filas
        val agregarColumnas = if (izquierda || derecha) 40 else 0
        val agregarFilas = if (arriba || abajo) 24 else 0
        val moverX = if (izquierda) agregarColumnas else 0
        val moverY = if (arriba) agregarFilas else 0
        val nuevoAncho = columnasAnteriores + agregarColumnas
        val nuevoAlto = filasAnteriores + agregarFilas
        val nuevosTuneles = Array(nuevoAlto) { BooleanArray(nuevoAncho) }
        for (fila in 0 until filasAnteriores) {
            tunel[fila].copyInto(nuevosTuneles[fila + moverY], destinationOffset = moverX)
        }
        tunel = nuevosTuneles
        invalidarCacheTuneles()
        columnas = nuevoAncho
        filas = nuevoAlto
        posicionBaseX += moverX
        posicionBaseY += moverY
        desplazamientoCoordenadasX += moverX
        desplazamientoCoordenadasY += moverY
        pacmanes.forEach { pacman ->
            pacman.columna += moverX
            pacman.fila += moverY
            if (pacman.objetivoPatrullaColumna >= 0) pacman.objetivoPatrullaColumna += moverX
            if (pacman.objetivoPatrullaFila >= 0) pacman.objetivoPatrullaFila += moverY
            if (pacman.objetivoFrutaColumna >= 0) pacman.objetivoFrutaColumna += moverX
            if (pacman.objetivoFrutaFila >= 0) pacman.objetivoFrutaFila += moverY
            if (pacman.objetivoTunelColumna >= 0) pacman.objetivoTunelColumna += moverX
            if (pacman.objetivoTunelFila >= 0) pacman.objetivoTunelFila += moverY
            if (pacman.objetivoBaseColumna >= 0) pacman.objetivoBaseColumna += moverX
            if (pacman.objetivoBaseFila >= 0) pacman.objetivoBaseFila += moverY
            if (pacman.ultimaColumna >= 0) pacman.ultimaColumna += moverX
            if (pacman.ultimaFila >= 0) pacman.ultimaFila += moverY
        }
        fantasmas.forEach { fantasma ->
            fantasma.columna += moverX
            fantasma.fila += moverY
            if (fantasma.ultimaColumna >= 0) fantasma.ultimaColumna += moverX
            if (fantasma.ultimaFila >= 0) fantasma.ultimaFila += moverY
        }
        frutas.indices.forEach { indice ->
            val fruta = frutas[indice]
            frutas[indice] = fruta.copy(columna = fruta.columna + moverX, fila = fruta.fila + moverY)
        }
    }

    fun esDentroBase(columna: Int, fila: Int): Boolean =
        columna >= columnaBase - anchoBase && columna <= columnaBase + anchoBase &&
            fila >= filaBase - altoBase && fila <= filaBase + altoBase

    private fun celdasBase(): List<Pair<Int, Int>> = buildList {
        for (fila in filaBase - altoBase..filaBase + altoBase) {
            for (columna in columnaBase - anchoBase..columnaBase + anchoBase) {
                val celda = columna to fila
                if (celda != puertaInteriorBase()) add(celda)
            }
        }
    }

    private fun puertaInteriorBase(): Pair<Int, Int> = (columnaBase - anchoBase) to filaBase

    private fun buscarPosicionLibreBase(
        excluirIdentificador: Int? = null,
        referencia: Pair<Int, Int> = columnaBase to filaBase,
    ): Pair<Int, Int>? = celdasBase()
        .filter { celda ->
            pacmanes.none {
                it.identificador != excluirIdentificador && it.columna == celda.first && it.fila == celda.second
            }
        }
        .minByOrNull { distancia(it.first, it.second, referencia.first, referencia.second) }

    private fun obtenerObjetivoBase(
        pacman: PacmanColono,
        evitarFantasmas: Boolean = false,
    ): Pair<Int, Int> = elegirObjetivoBase(pacman, celdasBaseReservadas(pacman), evitarFantasmas)

    private fun celdasBaseReservadas(pacman: PacmanColono): Set<Pair<Int, Int>> = pacmanes.asSequence()
        .filter { it.identificador != pacman.identificador && regresandoABase(it) }
        .mapNotNull { otro ->
            if (otro.objetivoBaseColumna >= 0 && esDentroBase(otro.objetivoBaseColumna, otro.objetivoBaseFila)) {
                otro.objetivoBaseColumna to otro.objetivoBaseFila
            } else null
        }
        .toSet()

    private fun elegirObjetivoBase(
        pacman: PacmanColono,
        reservadas: Set<Pair<Int, Int>>,
        evitarFantasmas: Boolean,
    ): Pair<Int, Int> {
        fun disponible(celda: Pair<Int, Int>): Boolean = celda !in reservadas &&
            (celda == (pacman.columna to pacman.fila) || pacmanes.none {
                it.identificador != pacman.identificador && it.columna == celda.first && it.fila == celda.second
            })
        fun alcanzable(celda: Pair<Int, Int>): Boolean {
            if (celda == (pacman.columna to pacman.fila)) return true
            val posicionActual = indice(pacman.columna, pacman.fila)
            val destino = indice(celda.first, celda.second)
            if (!evitarFantasmas) {
                val pasos = buscarRutaCompletaBase(pacman, celda.first, celda.second) ?: return false
                rutasBaseCacheadas[pacman.identificador] = RutaBaseCacheada(
                    destino = destino,
                    revisionMapa = revisionEstructuraMapa,
                    pasos = pasos,
                    posicionEsperada = posicionActual,
                )
                return true
            }
            return siguientePaso(
                pacman.columna, pacman.fila, celda.first, celda.second,
                fantasma = false,
                pasoAnterior = pacman.ultimaColumna to pacman.ultimaFila,
                evitarFantasmas = true,
            ) != null
        }

        val actual = pacman.objetivoBaseColumna to pacman.objetivoBaseFila
        val celdas = celdasBase()
        val conservarObjetivo = pacman.objetivoBaseColumna >= 0 && actual in celdas && disponible(actual)
        if (!conservarObjetivo) rutasBaseCacheadas.remove(pacman.identificador)
        val puertaBloqueada = puertaInteriorOcupada(pacman)
        val destino = if (conservarObjetivo) actual else {
            val libres = celdas.filter(::disponible)
            val candidatosLibres = libres.sortedBy { distancia(it.first, it.second, pacman.columna, pacman.fila) }
            val candidatosAlternos = celdas.filter { it !in reservadas }
                .sortedBy { distancia(it.first, it.second, pacman.columna, pacman.fila) }
            if (puertaBloqueada) {
                candidatosLibres.firstOrNull()
                    ?: candidatosAlternos.firstOrNull()
                    ?: (columnaBase to filaBase)
            } else {
                candidatosLibres.firstOrNull(::alcanzable)
                    ?: candidatosAlternos.firstOrNull(::alcanzable)
                    ?: (columnaBase to filaBase)
            }
        }
        pacman.objetivoBaseColumna = destino.first
        pacman.objetivoBaseFila = destino.second
        return destino
    }

    private fun regresandoABase(pacman: PacmanColono): Boolean = pacman.agotado ||
        pacman.refugiadoPorAmenaza ||
        (pacman.rol == RolPacman.EXPLORADOR && frutasAlmacenadas >= capacidadAlmacen)

    private fun puertaInteriorOcupada(pacman: PacmanColono): Boolean {
        if (esDentroBase(pacman.columna, pacman.fila)) return false
        val puerta = puertaInteriorBase()
        return pacmanes.any {
            it.identificador != pacman.identificador && it.columna == puerta.first && it.fila == puerta.second
        }
    }

    private fun despejarPuertaBase(pacman: PacmanColono): Boolean {
        if ((pacman.columna to pacman.fila) != puertaInteriorBase()) return false
        val tieneDestinoExterior = when (pacman.rol) {
            RolPacman.OBRERO -> pacman.objetivoTunelColumna >= 0 &&
                esFrontera(pacman.objetivoTunelColumna, pacman.objetivoTunelFila)
            RolPacman.EXPLORADOR -> frutasAlmacenadas < capacidadAlmacen &&
                pacman.objetivoFrutaColumna >= 0 &&
                frutas.any { it.columna == pacman.objetivoFrutaColumna && it.fila == pacman.objetivoFrutaFila }
            RolPacman.DEFENSOR -> {
                val fantasma = fantasmas.firstOrNull { it.identificador == pacman.objetivoFantasmaId }
                (fantasma != null && !esDentroBase(fantasma.columna, fantasma.fila)) ||
                    (pacman.objetivoPatrullaColumna >= 0 &&
                        !esDentroBase(pacman.objetivoPatrullaColumna, pacman.objetivoPatrullaFila))
            }
        }
        if (tieneDestinoExterior && !pacman.refugiadoPorAmenaza && !pacman.agotado) return false

        val pasoLibre = DireccionPacman.entries
            .map { (pacman.columna + it.dx) to (pacman.fila + it.dy) }
            .filter { esDentroBase(it.first, it.second) && movimientoValido(pacman.columna, pacman.fila, it.first, it.second, fantasma = false) }
            .maxWithOrNull(
                compareBy<Pair<Int, Int>> { distancia(it.first, it.second, entradaBase().first, entradaBase().second) }
                    .thenBy { -distancia(it.first, it.second, columnaBase, filaBase) }
            )
        if (pasoLibre != null) {
            moverA(pacman, pasoLibre)
            return true
        }
        return desplazarPacmanesBaseHastaHueco(pacman)
    }

    /** Despeja la entrada moviendo en cadena a sus ocupantes, siempre una celda por personaje. */
    private fun desplazarPacmanesBaseHastaHueco(pacmanEntrada: PacmanColono): Boolean {
        fun buscarRutaAHueco(inicio: Pair<Int, Int>): List<Pair<Int, Int>>? {
            val padres = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()
            val cola = ArrayDeque<Pair<Int, Int>>()
            padres[inicio] = null
            cola.addLast(inicio)

            while (cola.isNotEmpty()) {
                val actual = cola.removeFirst()
                for (direccion in DireccionPacman.entries) {
                    val siguiente = (actual.first + direccion.dx) to (actual.second + direccion.dy)
                    if (!esDentroBase(siguiente.first, siguiente.second) || !tunel[siguiente.second][siguiente.first] || siguiente in padres) {
                        continue
                    }
                    val ocupante = pacmanes.firstOrNull { it.columna == siguiente.first && it.fila == siguiente.second }
                    if ((ocupante?.animacionMuerte ?: 0) > 0) continue
                    padres[siguiente] = actual
                    if (ocupante != null) {
                        cola.addLast(siguiente)
                        continue
                    }

                    val ruta = mutableListOf(siguiente)
                    var celda = siguiente
                    while (celda != inicio) {
                        celda = padres[celda] ?: return null
                        ruta.add(celda)
                    }
                    return ruta.asReversed()
                }
            }
            return null
        }

        val ruta = buscarRutaAHueco(pacmanEntrada.columna to pacmanEntrada.fila) ?: return false

        for (indice in ruta.lastIndex - 1 downTo 0) {
            val origen = ruta[indice]
            val destino = ruta[indice + 1]
            val ocupante = pacmanes.firstOrNull { it.columna == origen.first && it.fila == origen.second } ?: return false
            if (pacmanes.any { it.identificador != ocupante.identificador && it.columna == destino.first && it.fila == destino.second }) return false
            moverA(ocupante, destino)
            tiempoMovimientoPacman[ocupante.identificador] = 0L
        }
        return true
    }

    private fun corregirSolapamientosBase() {
        val ocupadas = mutableSetOf<Pair<Int, Int>>()
        pacmanes.sortedBy { it.identificador }.forEach { pacman ->
            val posicion = pacman.columna to pacman.fila
            if (!esDentroBase(pacman.columna, pacman.fila)) return@forEach
            if (posicion != puertaInteriorBase() && ocupadas.add(posicion)) return@forEach
            val nuevaPosicion = celdasBase()
                .filter { it !in ocupadas }
                .minByOrNull { distancia(it.first, it.second, pacman.columna, pacman.fila) }
            if (nuevaPosicion != null) {
                pacman.columna = nuevaPosicion.first
                pacman.fila = nuevaPosicion.second
                ocupadas.add(nuevaPosicion)
            }
        }
    }

    private fun celdaValida(columna: Int, fila: Int, fantasma: Boolean = false): Boolean =
        columna > 0 && columna < columnas - 1 && fila > 0 && fila < filas - 1 &&
            tunel[fila][columna] && (!fantasma || !esDentroBase(columna, fila))

    private fun movimientoValido(
        origenColumna: Int, origenFila: Int, destinoColumna: Int, destinoFila: Int, fantasma: Boolean,
        marcaOcupacionPacman: Int = 0,
    ): Boolean {
        if (!celdaValida(destinoColumna, destinoFila, fantasma)) return false
        if (fantasma) return true
        if (esDentroBase(destinoColumna, destinoFila)) {
            val ocupada = if (marcaOcupacionPacman != 0) {
                val ocupacion = ocupacionRuta[indice(destinoColumna, destinoFila)]
                (ocupacion ushr 2) == marcaOcupacionPacman && (ocupacion and 1) != 0
            } else {
                pacmanes.any { it.columna == destinoColumna && it.fila == destinoFila }
            }
            if (ocupada) return false
        }
        val origenEnBase = esDentroBase(origenColumna, origenFila)
        val destinoEnBase = esDentroBase(destinoColumna, destinoFila)
        val puertaInterior = puertaInteriorBase()
        val puertaExterior = entradaBase()
        return when {
            origenEnBase && !destinoEnBase -> (origenColumna to origenFila) == puertaInterior && (destinoColumna to destinoFila) == puertaExterior
            !origenEnBase && destinoEnBase -> (origenColumna to origenFila) == puertaExterior && (destinoColumna to destinoFila) == puertaInterior
            else -> true
        }
    }

    private fun generarFruta() {
        generarFrutasHasta(frutas.size + 1)
    }

    private fun generarFrutasHasta(cantidadMinima: Int) {
        val cantidadObjetivo = minOf(cantidadMinima, limiteFrutas)
        if (frutas.size >= cantidadObjetivo) return
        posicionesFruta.clear()
        frutas.forEach { posicionesFruta.add(indice(it.columna, it.fila)) }
        candidatosFruta.clear()
        tunelesAbiertos().forEach { posicion ->
            if (!esDentroBase(posicion.first, posicion.second) &&
                indice(posicion.first, posicion.second) !in posicionesFruta
            ) candidatosFruta.add(posicion)
        }
        while (frutas.size < cantidadObjetivo && candidatosFruta.isNotEmpty()) {
            val indiceElegido = azar.nextInt(candidatosFruta.size)
            val posicion = candidatosFruta[indiceElegido]
            val ultimoIndice = candidatosFruta.lastIndex
            candidatosFruta[indiceElegido] = candidatosFruta[ultimoIndice]
            candidatosFruta.removeAt(ultimoIndice)
            frutas += FrutaColonia(posicion.first, posicion.second, tiposFruta.random(azar))
        }
    }

    fun precioMejora(mejora: MejoraColonia): Int {
        val nivel = nivelesMejora[mejora] ?: 0
        return when (mejora) {
            MejoraColonia.BASE -> 100 + nivelBase * 100
            MejoraColonia.TUNELES -> 55 + nivel * 55
            MejoraColonia.ALMACEN -> 65 + nivel * 65
            MejoraColonia.DEFENSA -> 75 + nivel * 75
            MejoraColonia.RESISTENCIA_DEFENSORES -> 95 + nivel * 85
        }
    }

    @Synchronized
    fun mejorar(mejora: MejoraColonia): Boolean {
        val precio = precioMejora(mejora)
        if (puntos < precio) return false
        val capacidadesAnteriores = when (mejora) {
            MejoraColonia.TUNELES -> pacmanes.filter { it.rol == RolPacman.OBRERO }
            MejoraColonia.RESISTENCIA_DEFENSORES -> pacmanes.filter { it.rol == RolPacman.DEFENSOR }
            else -> emptyList()
        }.associate { it.identificador to capacidadResistencia(it.rol) }
        puntos -= precio
        nivelesMejora[mejora] = (nivelesMejora[mejora] ?: 0) + 1
        capacidadesAnteriores.forEach { (identificador, capacidadAnterior) ->
            pacmanes.firstOrNull { it.identificador == identificador }?.let { pacman ->
                val aumento = capacidadResistencia(pacman.rol) - capacidadAnterior
                pacman.resistenciaActual = (pacman.resistenciaActual + aumento)
                    .coerceAtMost(capacidadResistencia(pacman.rol))
            }
        }
        if (mejora == MejoraColonia.BASE) {
            nivelBase++
            cacheFronterasAbiertas = null
            rutasBaseCacheadas.clear()
            asegurarExpansionBase()
            cavarBase()
            RolPacman.entries.forEach(::crearPacmanEnBase)
        }
        return true
    }

    fun capacidadResistencia(rol: RolPacman): Float = when (rol) {
        RolPacman.OBRERO -> 20f + (nivelesMejora[MejoraColonia.TUNELES] ?: 0) * 16f
        RolPacman.DEFENSOR -> 28f + (nivelesMejora[MejoraColonia.RESISTENCIA_DEFENSORES] ?: 0) * 18f
        RolPacman.EXPLORADOR -> 0f
    }

    @Synchronized
    fun venderFruta(clave: String): Boolean {
        val fruta = tiposFruta.firstOrNull { it.clave == clave } ?: return false
        if ((inventario[clave] ?: 0) <= 0) return false
        inventario[clave] = (inventario[clave] ?: 0) - 1
        frutasAlmacenadas--
        puntos += fruta.precio
        return true
    }

    @Synchronized
    fun avanzar(deltaMs: Long = INTERVALO_SIMULACION_MS) {
        if (deltaMs <= 0L) return
        tiempoCicloMs += deltaMs
        while (tiempoCicloMs >= INTERVALO_SIMULACION_MS) {
            tiempoCicloMs -= INTERVALO_SIMULACION_MS
            avanzarCicloSimulacion()
        }

        var indicePacman = 0
        while (indicePacman < pacmanes.size) {
            val pacman = pacmanes[indicePacman++]
            if (pacman.animacionMuerte > 0) continue
            actualizarResistencia(pacman, deltaMs)
            val tiempoAcumulado = (tiempoMovimientoPacman[pacman.identificador] ?: 0L) + deltaMs
            val intervaloMovimiento = intervaloMovimiento(pacman)
            if (tiempoAcumulado >= intervaloMovimiento) {
                // Como máximo un paso por actualización: las mejoras aceleran el ritmo,
                // pero cada cambio de posición sigue recorriendo una sola casilla.
                tiempoMovimientoPacman[pacman.identificador] = tiempoAcumulado - intervaloMovimiento
                val puertaDespejada = despejarPuertaBase(pacman)
                if (!puertaDespejada) {
                    if (pacman.agotado) {
                        val destino = obtenerObjetivoBase(pacman)
                        if (pacman.columna != destino.first || pacman.fila != destino.second) {
                            moverHaciaBase(pacman, destino.first, destino.second)
                        }
                    } else {
                        avanzarPacman(pacman)
                    }
                }
            } else {
                tiempoMovimientoPacman[pacman.identificador] = tiempoAcumulado
            }
        }
        resolverCombates()
    }

    private fun avanzarCicloSimulacion() {
        ciclo++
        val intervaloFantasmas = (20 - pacmanes.size).coerceAtLeast(4)
        val limiteFantasmas = (pacmanes.size * 2).coerceIn(6, 60)
        if (ciclo % intervaloFantasmas == 0 && fantasmas.size < limiteFantasmas) {
            val apariciones = (1 + (pacmanes.size - 3) / 9).coerceIn(1, 4)
            repeat(minOf(apariciones, limiteFantasmas - fantasmas.size)) { generarFantasma() }
        }
        if (ciclo % 7 == 0 && frutas.size < limiteFrutas) generarFruta()
        asignarTareasColectivas()
        avanzarFantasmas()
        avanzarAnimacionesMuerte()
        resolverCombates()
    }

    private fun intervaloMovimiento(pacman: PacmanColono): Long = when (pacman.rol) {
        RolPacman.OBRERO -> if (pacman.agotado) {
            INTERVALO_SIMULACION_MS
        } else {
            val mejora = (nivelesMejora[MejoraColonia.TUNELES] ?: 0).coerceIn(0, 5)
            (INTERVALO_SIMULACION_MS / (mejora + 1)).coerceAtLeast(70L)
        }
        RolPacman.DEFENSOR -> {
            val fantasma = fantasmas.firstOrNull { it.identificador == pacman.objetivoFantasmaId }
            if (!pacman.agotado && fantasma != null && distancia(pacman.columna, pacman.fila, fantasma.columna, fantasma.fila) <= 6) {
                INTERVALO_SIMULACION_MS / 2
            } else {
                INTERVALO_SIMULACION_MS
            }
        }
        RolPacman.EXPLORADOR -> INTERVALO_SIMULACION_MS
    }

    private fun actualizarResistencia(pacman: PacmanColono, deltaMs: Long) {
        if (pacman.rol == RolPacman.EXPLORADOR) return
        val capacidad = capacidadResistencia(pacman.rol)
        pacman.resistenciaActual = pacman.resistenciaActual.coerceIn(0f, capacidad)
        if (!esDentroBase(pacman.columna, pacman.fila)) return
        pacman.resistenciaActual = (pacman.resistenciaActual + REGENERACION_RESISTENCIA_POR_SEGUNDO * deltaMs / 1000f)
            .coerceAtMost(capacidad)
        if (pacman.agotado && pacman.resistenciaActual >= capacidad) {
            pacman.agotado = false
            pacman.objetivoFantasmaId = -1
            pacman.objetivoTunelColumna = -1
            pacman.objetivoTunelFila = -1
            pacman.objetivoPatrullaColumna = -1
            pacman.objetivoPatrullaFila = -1
            pacman.ultimaColumna = -1
            pacman.ultimaFila = -1
            pacman.objetivoBaseColumna = -1
            pacman.objetivoBaseFila = -1
        }
    }

    private fun gastarResistencia(pacman: PacmanColono, cantidad: Float) {
        if (pacman.agotado) return
        pacman.resistenciaActual = (pacman.resistenciaActual - cantidad).coerceAtLeast(0f)
        if (pacman.resistenciaActual <= 0f) {
            pacman.agotado = true
            pacman.objetivoFantasmaId = -1
            pacman.objetivoTunelColumna = -1
            pacman.objetivoTunelFila = -1
            pacman.objetivoPatrullaColumna = -1
            pacman.objetivoPatrullaFila = -1
            pacman.ultimaColumna = -1
            pacman.ultimaFila = -1
            pacman.objetivoBaseColumna = -1
            pacman.objetivoBaseFila = -1
        }
    }

    private fun avanzarAnimacionesMuerte() {
        var indicePacman = 0
        while (indicePacman < pacmanes.size) {
            val pacman = pacmanes[indicePacman++]
            if (pacman.animacionMuerte <= 0) continue
            pacman.animacionMuerte++
            if (pacman.animacionMuerte > 11) {
                pacman.animacionMuerte = 0
                val posicionBase = buscarPosicionLibreBase(
                    excluirIdentificador = pacman.identificador,
                    referencia = columnaBase to filaBase,
                ) ?: (columnaBase to filaBase)
                pacman.columna = posicionBase.first
                pacman.fila = posicionBase.second
                pacman.objetivoFantasmaId = -1
                pacman.objetivoFrutaColumna = -1
                pacman.objetivoFrutaFila = -1
                pacman.objetivoTunelColumna = -1
                pacman.objetivoTunelFila = -1
                pacman.ultimaColumna = -1
                pacman.ultimaFila = -1
                pacman.objetivoBaseColumna = -1
                pacman.objetivoBaseFila = -1
                pacman.refugiadoPorAmenaza = false
                tiempoMovimientoPacman[pacman.identificador] = 0L
            }
        }
    }

    /** Conserva objetivos mientras sigan disponibles y reparte los nuevos sin duplicarlos. */
    private fun asignarTareasColectivas() {
        asignarFronterasObreros()
        asignarFrutasExploradores()
        asignarFantasmasDefensores()
        asignarPresasFantasmas()
    }

    private fun asignarFronterasObreros() {
        val obreros = pacmanes.filter {
            it.rol == RolPacman.OBRERO && it.animacionMuerte == 0 && !it.agotado && !it.refugiadoPorAmenaza
        }
            .sortedBy { it.identificador }
        pacmanes.filter { it.rol == RolPacman.OBRERO && it.refugiadoPorAmenaza }.forEach {
            it.objetivoTunelColumna = -1
            it.objetivoTunelFila = -1
        }
        val ocupadas = HashSet<Int>(obreros.size * 2)
        val listaOcupadas = ArrayList<Int>(obreros.size)
        obreros.forEach { obrero ->
            val columna = obrero.objetivoTunelColumna
            val fila = obrero.objetivoTunelFila
            val objetivo = if (columna >= 0 && fila >= 0) indice(columna, fila) else -1
            if (objetivo >= 0 && esFrontera(columna, fila) && ocupadas.add(objetivo)) {
                listaOcupadas.add(objetivo)
                return@forEach
            }
            obrero.objetivoTunelColumna = -1; obrero.objetivoTunelFila = -1
        }
        val fronteras = obtenerFronterasAbiertas()
        if (fronteras.isEmpty() || fronteras.size <= ocupadas.size) return
        obreros.forEach { obrero ->
            if (obrero.objetivoTunelColumna >= 0) return@forEach
            var mejorDestino = -1
            var mejorPuntuacion = Int.MAX_VALUE
            for (distanciaAnillo in 0..(columnas + filas)) {
                val filaInicial = maxOf(1, obrero.fila - distanciaAnillo)
                val filaFinal = minOf(filas - 2, obrero.fila + distanciaAnillo)
                for (fila in filaInicial..filaFinal) {
                    val desplazamientoX = distanciaAnillo - abs(fila - obrero.fila)
                    val columnaIzquierda = obrero.columna - desplazamientoX
                    if (columnaIzquierda in 1 until columnas - 1) {
                        val candidato = indice(columnaIzquierda, fila)
                        if (candidato !in ocupadas && candidato in fronteras) {
                            val puntuacion = puntuarFrontera(candidato, obrero, listaOcupadas)
                            if (puntuacion < mejorPuntuacion ||
                                (puntuacion == mejorPuntuacion && candidato < mejorDestino)
                            ) {
                                mejorPuntuacion = puntuacion
                                mejorDestino = candidato
                            }
                        }
                    }
                    if (desplazamientoX != 0) {
                        val columnaDerecha = obrero.columna + desplazamientoX
                        if (columnaDerecha in 1 until columnas - 1) {
                            val candidato = indice(columnaDerecha, fila)
                            if (candidato !in ocupadas && candidato in fronteras) {
                                val puntuacion = puntuarFrontera(candidato, obrero, listaOcupadas)
                                if (puntuacion < mejorPuntuacion ||
                                    (puntuacion == mejorPuntuacion && candidato < mejorDestino)
                                ) {
                                    mejorPuntuacion = puntuacion
                                    mejorDestino = candidato
                                }
                            }
                        }
                    }
                }
                if (mejorDestino >= 0 && (distanciaAnillo + 1) * 3 - 18 > mejorPuntuacion) break
            }
            if (mejorDestino < 0) return@forEach
            obrero.objetivoTunelColumna = mejorDestino % columnas
            obrero.objetivoTunelFila = mejorDestino / columnas
            ocupadas.add(mejorDestino)
            listaOcupadas.add(mejorDestino)
        }
    }

    private fun puntuarFrontera(candidato: Int, obrero: PacmanColono, fronterasOcupadas: List<Int>): Int {
        val columna = candidato % columnas
        val fila = candidato / columnas
        var separacion = Int.MAX_VALUE
        for (ocupada in fronterasOcupadas) {
            separacion = minOf(
                separacion,
                distancia(columna, fila, ocupada % columnas, ocupada / columnas),
            )
        }
        if (separacion == Int.MAX_VALUE) separacion = 0
        return distancia(obrero.columna, obrero.fila, columna, fila) * 3 - minOf(separacion, 18)
    }

    private fun asignarFrutasExploradores() {
        val exploradores = pacmanes.filter {
            it.rol == RolPacman.EXPLORADOR && it.animacionMuerte == 0 && !it.refugiadoPorAmenaza
        }.sortedBy { it.identificador }
        pacmanes.filter { it.rol == RolPacman.EXPLORADOR && it.refugiadoPorAmenaza }.forEach {
            it.objetivoFrutaColumna = -1
            it.objetivoFrutaFila = -1
        }
        if (frutasAlmacenadas >= capacidadAlmacen) {
            exploradores.forEach { it.objetivoFrutaColumna = -1; it.objetivoFrutaFila = -1 }
            return
        }
        if (frutas.size < exploradores.size) generarFrutasHasta(exploradores.size)
        val disponibles = HashSet<Int>(frutas.size * 2)
        frutas.forEach { disponibles.add(indice(it.columna, it.fila)) }
        val ocupadas = HashSet<Int>(exploradores.size * 2)
        exploradores.forEach { explorador ->
            val columna = explorador.objetivoFrutaColumna
            val fila = explorador.objetivoFrutaFila
            val objetivo = if (columna >= 0 && fila >= 0) indice(columna, fila) else -1
            if (objetivo >= 0 && objetivo in disponibles && ocupadas.add(objetivo)) return@forEach
            explorador.objetivoFrutaColumna = -1; explorador.objetivoFrutaFila = -1
        }
        exploradores.forEach { explorador ->
            if (explorador.objetivoFrutaColumna >= 0) return@forEach
            var mejorDestino = -1
            var mejorDistancia = Int.MAX_VALUE
            for ((columna, fila) in frutas) {
                val destino = indice(columna, fila)
                if (destino in ocupadas) continue
                val distanciaFruta = distancia(columna, fila, explorador.columna, explorador.fila)
                if (distanciaFruta < mejorDistancia) {
                    mejorDistancia = distanciaFruta
                    mejorDestino = destino
                }
            }
            if (mejorDestino >= 0) {
                explorador.objetivoFrutaColumna = mejorDestino % columnas
                explorador.objetivoFrutaFila = mejorDestino / columnas
                ocupadas.add(mejorDestino)
            }
        }
    }

    private fun asignarFantasmasDefensores() {
        val defensores = pacmanes.filter { it.rol == RolPacman.DEFENSOR && it.animacionMuerte == 0 && !it.agotado }
            .sortedBy { it.identificador }
        val fantasmasDisponibles = fantasmas.associateBy { it.identificador }
        val ocupados = mutableSetOf<Int>()
        defensores.forEach { defensor ->
            val objetivo = defensor.objetivoFantasmaId
            if (objetivo in fantasmasDisponibles && ocupados.add(objetivo)) {
                defensor.objetivoPatrullaColumna = -1; defensor.objetivoPatrullaFila = -1
            } else {
                defensor.objetivoFantasmaId = -1
            }
        }
        defensores.filter { it.objetivoFantasmaId < 0 }.forEach { defensor ->
            val fantasma = fantasmas.filter { it.identificador !in ocupados }
                .minByOrNull { distancia(it.columna, it.fila, defensor.columna, defensor.fila) }
            if (fantasma != null) {
                defensor.objetivoFantasmaId = fantasma.identificador
                defensor.objetivoPatrullaColumna = -1; defensor.objetivoPatrullaFila = -1
                ocupados.add(fantasma.identificador)
            }
        }

        val patrullas = HashSet<Int>(defensores.size * 2)
        val listaPatrullas = ArrayList<Int>(defensores.size)
        defensores.filter { it.objetivoFantasmaId < 0 }.forEach { defensor ->
            val columna = defensor.objetivoPatrullaColumna
            val fila = defensor.objetivoPatrullaFila
            val objetivo = if (columna >= 0 && fila >= 0) indice(columna, fila) else -1
            val valido = objetivo >= 0 && celdaValida(columna, fila) && !esDentroBase(columna, fila) &&
                objetivo !in patrullas && (defensor.columna != columna || defensor.fila != fila)
            if (valido) {
                patrullas.add(objetivo)
                listaPatrullas.add(objetivo)
            } else {
                defensor.objetivoPatrullaColumna = -1; defensor.objetivoPatrullaFila = -1
            }
        }
        val celdasPatrulla = tunelesAbiertos()
        defensores.filter { it.objetivoFantasmaId < 0 && it.objetivoPatrullaColumna < 0 }.forEach { defensor ->
            var mejorDestino = -1
            var mejorPuntuacion = Int.MIN_VALUE
            for ((columna, fila) in celdasPatrulla) {
                if (esDentroBase(columna, fila) || distancia(columna, fila, columnaBase, filaBase) < 6) continue
                val indiceCandidato = indice(columna, fila)
                if (indiceCandidato in patrullas) continue
                var separacion = Int.MAX_VALUE
                for (ocupada in listaPatrullas) {
                    separacion = minOf(
                        separacion,
                        distancia(columna, fila, ocupada % columnas, ocupada / columnas),
                    )
                }
                if (separacion == Int.MAX_VALUE) separacion = 18
                val puntuacion = separacion * 3 - distancia(defensor.columna, defensor.fila, columna, fila)
                if (puntuacion > mejorPuntuacion) {
                    mejorPuntuacion = puntuacion
                    mejorDestino = indiceCandidato
                }
            }
            if (mejorDestino < 0) {
                val entrada = entradaBase()
                defensor.objetivoPatrullaColumna = entrada.first
                defensor.objetivoPatrullaFila = entrada.second
                val indiceEntrada = indice(entrada.first, entrada.second)
                patrullas.add(indiceEntrada)
                listaPatrullas.add(indiceEntrada)
            } else {
                defensor.objetivoPatrullaColumna = mejorDestino % columnas
                defensor.objetivoPatrullaFila = mejorDestino / columnas
                patrullas.add(mejorDestino)
                listaPatrullas.add(mejorDestino)
            }
        }
    }

    private fun asignarPresasFantasmas() {
        val presas = pacmanes.filter {
            it.rol != RolPacman.DEFENSOR && it.animacionMuerte == 0 && !it.refugiadoPorAmenaza &&
                !esDentroBase(it.columna, it.fila)
        }
        val porId = presas.associateBy { it.identificador }
        val cargas = mutableMapOf<Int, Int>()
        fantasmas.sortedBy { it.identificador }.forEach { fantasma ->
            if (fantasma.objetivoPacmanId !in porId) fantasma.objetivoPacmanId = -1
            if (fantasma.objetivoPacmanId >= 0) cargas[fantasma.objetivoPacmanId] = (cargas[fantasma.objetivoPacmanId] ?: 0) + 1
        }
        val menorCarga = presas.minOfOrNull { cargas[it.identificador] ?: 0 } ?: 0
        val mayorCarga = presas.maxOfOrNull { cargas[it.identificador] ?: 0 } ?: 0
        if (mayorCarga - menorCarga > 1) {
            fantasmas.forEach { it.objetivoPacmanId = -1 }
            cargas.clear()
        }
        fantasmas.filter { it.objetivoPacmanId < 0 }.sortedBy { it.identificador }.forEach { fantasma ->
            val presa = presas.minWithOrNull(
                compareBy<PacmanColono> { cargas[it.identificador] ?: 0 }
                    .thenBy { distancia(it.columna, it.fila, fantasma.columna, fantasma.fila) }
            ) ?: return@forEach
            fantasma.objetivoPacmanId = presa.identificador
            cargas[presa.identificador] = (cargas[presa.identificador] ?: 0) + 1
        }
    }

    private fun esFrontera(columna: Int, fila: Int): Boolean {
        if (!celdaValida(columna, fila) || esDentroBase(columna, fila)) return false
        for (direccion in DireccionPacman.entries) {
            val vecinaColumna = columna + direccion.dx
            val vecinaFila = fila + direccion.dy
            if (vecinaColumna in 1 until columnas - 1 && vecinaFila in 1 until filas - 1 &&
                !tunel[vecinaFila][vecinaColumna] && !esDentroBase(vecinaColumna, vecinaFila)
            ) return true
        }
        return false
    }

    private fun obtenerFronterasAbiertas(): java.util.TreeSet<Int> {
        cacheFronterasAbiertas?.let { return it }
        val fronteras = java.util.TreeSet<Int>()
        for (fila in 1 until filas - 1) {
            for (columna in 1 until columnas - 1) {
                if (tunel[fila][columna] && esFrontera(columna, fila)) fronteras.add(indice(columna, fila))
            }
        }
        cacheFronterasAbiertas = fronteras
        return fronteras
    }

    private fun actualizarCacheFronterasCercanas(columna: Int, fila: Int) {
        val fronteras = cacheFronterasAbiertas ?: return
        if (tunel[fila][columna] && esFrontera(columna, fila)) {
            fronteras.add(indice(columna, fila))
        } else {
            fronteras.remove(indice(columna, fila))
        }
        for (direccion in DireccionPacman.entries) {
            val vecinaColumna = columna + direccion.dx
            val vecinaFila = fila + direccion.dy
            if (vecinaColumna !in 1 until columnas - 1 || vecinaFila !in 1 until filas - 1) continue
            val vecina = indice(vecinaColumna, vecinaFila)
            if (tunel[vecinaFila][vecinaColumna] && esFrontera(vecinaColumna, vecinaFila)) {
                fronteras.add(vecina)
            } else {
                fronteras.remove(vecina)
            }
        }
    }

    private fun avanzarPacman(pacman: PacmanColono) {
        if (pacman.animacionMuerte > 0) return
        if (pacman.rol != RolPacman.DEFENSOR && buscarProteccion(pacman)) return
        when (pacman.rol) {
            RolPacman.OBRERO -> avanzarObrero(pacman)
            RolPacman.EXPLORADOR -> avanzarExplorador(pacman)
            RolPacman.DEFENSOR -> avanzarDefensor(pacman)
        }
    }

    private fun avanzarObrero(pacman: PacmanColono) {
        asegurarExpansion(pacman)
        val frontera = pacman.objetivoTunelColumna.takeIf { it >= 0 }?.let { it to pacman.objetivoTunelFila }
        if (frontera == null || !esFrontera(frontera.first, frontera.second)) {
            pacman.objetivoTunelColumna = -1; pacman.objetivoTunelFila = -1
            return
        }
        if (pacman.columna != frontera.first || pacman.fila != frontera.second) {
            if (!moverHacia(pacman, frontera.first, frontera.second)) {
                reasignarFronteraAccesible(pacman, frontera)
            }
            return
        }
        asegurarExpansion(pacman)
        val vecinos = vecinosCerrados(pacman.columna, pacman.fila)
        if (vecinos.isEmpty()) {
            pacman.objetivoTunelColumna = -1
            pacman.objetivoTunelFila = -1
            return
        }
        val opciones = vecinos.map { celda -> celda to direccionEntre(pacman.columna, pacman.fila, celda.first, celda.second) }
        val contraria = direccionOpuesta(pacman.direccion)
        val sinRetroceso = opciones.filter { it.second != contraria }
        val recto = sinRetroceso.firstOrNull { it.second == pacman.direccion }
        val siguiente = when {
            recto != null && (sinRetroceso.size == 1 || azar.nextFloat() < .38f) -> recto
            sinRetroceso.isNotEmpty() -> sinRetroceso.random(azar)
            else -> opciones.random(azar)
        }
        abrirTunel(siguiente.first.first, siguiente.first.second)
        pacman.ultimaColumna = pacman.columna
        pacman.ultimaFila = pacman.fila
        pacman.direccion = siguiente.second
        pacman.columna = siguiente.first.first
        pacman.fila = siguiente.first.second
        pacman.objetivoTunelColumna = pacman.columna
        pacman.objetivoTunelFila = pacman.fila
        gastarResistencia(pacman, GASTO_RESISTENCIA_OBRERO_POR_CELDA)
    }

    private fun avanzarExplorador(pacman: PacmanColono) {
        recogerFrutaEnCelda(pacman)
        if (frutasAlmacenadas >= capacidadAlmacen) {
            val destino = obtenerObjetivoBase(pacman)
            if (pacman.columna != destino.first || pacman.fila != destino.second) {
                moverHaciaBase(pacman, destino.first, destino.second)
            }
            return
        }
        pacman.objetivoBaseColumna = -1
        pacman.objetivoBaseFila = -1
        val fruta = frutas.firstOrNull { it.columna == pacman.objetivoFrutaColumna && it.fila == pacman.objetivoFrutaFila }
        if (fruta == null) {
            pacman.objetivoFrutaColumna = -1
            pacman.objetivoFrutaFila = -1
            reasignarFrutaAccesible(pacman, -1 to -1)
            return
        }
        if (!moverHacia(pacman, fruta.columna, fruta.fila)) {
            reasignarFrutaAccesible(pacman, fruta.columna to fruta.fila)
        }
    }

    private fun reasignarFronteraAccesible(obrero: PacmanColono, excluir: Pair<Int, Int>): Boolean {
        val reservadas = pacmanes.asSequence()
            .filter { it.identificador != obrero.identificador && it.rol == RolPacman.OBRERO && !it.agotado && !it.refugiadoPorAmenaza }
            .mapNotNull { otro ->
                if (otro.objetivoTunelColumna >= 0) otro.objetivoTunelColumna to otro.objetivoTunelFila else null
            }
            .toSet()
        val destino = tunelesAbiertos()
            .filter { esFrontera(it.first, it.second) && it != excluir && it !in reservadas }
            .sortedBy { distancia(obrero.columna, obrero.fila, it.first, it.second) }
            .firstOrNull { candidato ->
                (obrero.columna to obrero.fila) == candidato || siguientePaso(
                    obrero.columna, obrero.fila, candidato.first, candidato.second,
                    fantasma = false, pasoAnterior = obrero.ultimaColumna to obrero.ultimaFila,
                ) != null
            }
        obrero.objetivoTunelColumna = destino?.first ?: -1
        obrero.objetivoTunelFila = destino?.second ?: -1
        return destino != null
    }

    private fun reasignarFrutaAccesible(explorador: PacmanColono, excluir: Pair<Int, Int>): Boolean {
        val reservadas = pacmanes.asSequence()
            .filter { it.identificador != explorador.identificador && it.rol == RolPacman.EXPLORADOR && !it.refugiadoPorAmenaza }
            .mapNotNull { otro ->
                if (otro.objetivoFrutaColumna >= 0) otro.objetivoFrutaColumna to otro.objetivoFrutaFila else null
            }
            .toSet()
        val destino = frutas.asSequence()
            .map { it.columna to it.fila }
            .filter { it != excluir && it !in reservadas }
            .sortedBy { distancia(explorador.columna, explorador.fila, it.first, it.second) }
            .firstOrNull { candidato ->
                siguientePaso(
                    explorador.columna, explorador.fila, candidato.first, candidato.second,
                    fantasma = false, pasoAnterior = explorador.ultimaColumna to explorador.ultimaFila,
                ) != null
            }
        explorador.objetivoFrutaColumna = destino?.first ?: -1
        explorador.objetivoFrutaFila = destino?.second ?: -1
        return destino != null
    }

    private fun recogerFrutaEnCelda(pacman: PacmanColono) {
        if (pacman.rol != RolPacman.EXPLORADOR || frutasAlmacenadas >= capacidadAlmacen) return
        val fruta = frutas.firstOrNull { it.columna == pacman.columna && it.fila == pacman.fila } ?: return
        inventario[fruta.tipo.clave] = (inventario[fruta.tipo.clave] ?: 0) + 1
        frutasAlmacenadas++
        frutas.remove(fruta)
    }

    private fun avanzarDefensor(pacman: PacmanColono) {
        val fantasma = fantasmas.firstOrNull { it.identificador == pacman.objetivoFantasmaId }
        if (fantasma != null) {
            pacman.objetivoFantasmaId = fantasma.identificador
            pacman.objetivoPatrullaColumna = -1
            pacman.objetivoPatrullaFila = -1
            val interceptacion = proyectarPosicion(fantasma.columna, fantasma.fila, fantasma.direccion)
            moverHacia(pacman, interceptacion.first, interceptacion.second)
            return
        }
        pacman.objetivoFantasmaId = -1
        if (pacman.objetivoPatrullaColumna >= 0) {
            moverHacia(pacman, pacman.objetivoPatrullaColumna, pacman.objetivoPatrullaFila)
        }
    }

    private fun generarFantasma() {
        var cantidadCandidatos = 0
        var columnaElegida = -1
        var filaElegida = -1
        for ((columna, fila) in tunelesAbiertos()) {
            if (esDentroBase(columna, fila) || abs(columna - columnaBase) + abs(fila - filaBase) <= 8) continue
            cantidadCandidatos++
            if (azar.nextInt(cantidadCandidatos) == 0) {
                columnaElegida = columna
                filaElegida = fila
            }
        }
        if (columnaElegida < 0) return
        fantasmas += FantasmaColonia(siguienteIdentificador++, tiposFantasma.random(azar), columnaElegida, filaElegida)
    }

    private fun avanzarFantasmas() {
        fantasmasEnAvance.clear()
        fantasmasEnAvance.addAll(fantasmas)
        pacmanesPorIdEnAvance.clear()
        var indicePacman = 0
        while (indicePacman < pacmanes.size) {
            val pacman = pacmanes[indicePacman++]
            pacmanesPorIdEnAvance[pacman.identificador] = pacman
        }
        var indiceFantasma = 0
        while (indiceFantasma < fantasmasEnAvance.size) {
            val fantasma = fantasmasEnAvance[indiceFantasma++]
            var defensorCercano: PacmanColono? = null
            var distanciaDefensorCercano = Int.MAX_VALUE
            indicePacman = 0
            while (indicePacman < pacmanes.size) {
                val pacman = pacmanes[indicePacman++]
                if (pacman.rol == RolPacman.DEFENSOR && pacman.animacionMuerte == 0 && !pacman.agotado) {
                    val distanciaDefensor = distancia(pacman.columna, pacman.fila, fantasma.columna, fantasma.fila)
                    if (distanciaDefensor < distanciaDefensorCercano) {
                        distanciaDefensorCercano = distanciaDefensor
                        defensorCercano = pacman
                    }
                }
            }
            val alcanceHuida = 4 + (nivelesMejora[MejoraColonia.DEFENSA] ?: 0) * 2
            val defensorCerca = distanciaDefensorCercano <= alcanceHuida
            fantasma.asustado = defensorCerca
            if (defensorCercano != null && distanciaDefensorCercano <= 2) {
                fantasmas.remove(fantasma)
                puntos += 25
                gastarResistencia(defensorCercano, GASTO_RESISTENCIA_DEFENSOR_POR_FANTASMA)
                continue
            }
            val siguiente = if (defensorCerca && defensorCercano != null) {
                pasoHuyendoDeDefensor(fantasma, defensorCercano)
            } else {
                val presa = pacmanesPorIdEnAvance[fantasma.objetivoPacmanId]
                    ?.takeIf { it.animacionMuerte == 0 }
                if (presa == null || esDentroBase(presa.columna, presa.fila)) {
                    pasoErranteLejosDeBase(fantasma)
                } else {
                    val objetivo = presa.columna to presa.fila
                    siguientePaso(
                        fantasma.columna, fantasma.fila, objetivo.first, objetivo.second,
                        fantasma = true, pasoAnterior = fantasma.ultimaColumna to fantasma.ultimaFila,
                    )
                }
            }
            if (siguiente != null) {
                fantasma.ultimaColumna = fantasma.columna; fantasma.ultimaFila = fantasma.fila
                fantasma.direccion = direccionEntre(fantasma.columna, fantasma.fila, siguiente.first, siguiente.second)
                fantasma.columna = siguiente.first; fantasma.fila = siguiente.second
            }
        }
    }

    private fun resolverCombates() {
        var indicePacman = 0
        while (indicePacman < pacmanes.size) {
            val pacman = pacmanes[indicePacman++]
            if (pacman.rol == RolPacman.DEFENSOR || pacman.animacionMuerte != 0) continue
            var indiceFantasma = 0
            while (indiceFantasma < fantasmas.size) {
                val fantasma = fantasmas[indiceFantasma++]
                if (fantasma.columna == pacman.columna && fantasma.fila == pacman.fila && !fantasma.asustado) {
                    pacman.animacionMuerte = 1
                    puntos = (puntos - 25).coerceAtLeast(0)
                    break
                }
            }
        }
    }

    private fun buscarProteccion(pacman: PacmanColono): Boolean {
        val alcanceAmenaza = 5 + (nivelesMejora[MejoraColonia.DEFENSA] ?: 0)
        if (pacman.refugiadoPorAmenaza) {
            val dentroBase = esDentroBase(pacman.columna, pacman.fila)
            val amenazaCerca = hayAmenazaCercaBase(alcanceAmenaza)
            if (!dentroBase || amenazaCerca) {
                if (!dentroBase) {
                    val refugio = obtenerObjetivoBase(pacman, evitarFantasmas = true)
                    moverHaciaBase(pacman, refugio.first, refugio.second, evitarFantasmas = true)
                }
                return true
            }
            pacman.refugiadoPorAmenaza = false
            pacman.objetivoBaseColumna = -1
            pacman.objetivoBaseFila = -1
        }
        if (esDentroBase(pacman.columna, pacman.fila)) {
            val defensorFuera = pacmanes.any {
                it.rol == RolPacman.DEFENSOR && it.animacionMuerte == 0 && !it.agotado &&
                    !esDentroBase(it.columna, it.fila)
            }
            if (!defensorFuera && hayAmenazaCercaBase(alcanceAmenaza)) {
                activarRefugio(pacman)
                return true
            }
            return false
        }

        if (pacman.rol == RolPacman.OBRERO && !pacman.agotado) asegurarExpansion(pacman)
        val amenazas = fantasmas.filter {
            !it.asustado && distancia(it.columna, it.fila, pacman.columna, pacman.fila) <= alcanceAmenaza
        }
        if (amenazas.isEmpty()) return false

        val defensoresFuera = pacmanes.filter {
            it.rol == RolPacman.DEFENSOR && it.animacionMuerte == 0 && !it.agotado &&
                !esDentroBase(it.columna, it.fila)
        }
        if (defensoresFuera.isEmpty()) {
            if (pacman.rol == RolPacman.OBRERO && excavarParaHuir(pacman, amenazas, defensor = null)) {
                activarRefugio(pacman)
                return true
            }
            activarRefugio(pacman)
            val refugio = obtenerObjetivoBase(pacman, evitarFantasmas = true)
            moverHaciaBase(pacman, refugio.first, refugio.second, evitarFantasmas = true)
            return true
        }

        val defensor = defensoresFuera
            .minByOrNull { distancia(it.columna, it.fila, pacman.columna, pacman.fila) }
        val vecinos = DireccionPacman.entries.map { (pacman.columna + it.dx) to (pacman.fila + it.dy) }
            .filter { movimientoValido(pacman.columna, pacman.fila, it.first, it.second, fantasma = false) }
        if (vecinos.isEmpty()) return false

        if (pacman.rol == RolPacman.OBRERO && excavarParaHuir(pacman, amenazas, defensor)) return true

        val sinRetroceso = vecinos.filterNot { it.first == pacman.ultimaColumna && it.second == pacman.ultimaFila }
            .ifEmpty { vecinos }
        val distanciaAmenazaActual = amenazas.minOf { distancia(it.columna, it.fila, pacman.columna, pacman.fila) }
        val pasosSeguros = sinRetroceso.filter { paso ->
            amenazas.minOf { distancia(it.columna, it.fila, paso.first, paso.second) } >= distanciaAmenazaActual
        }.ifEmpty { sinRetroceso }
        val destinoDefensor = defensor?.let { it.columna to it.fila }
        val mayorDistanciaSegura = pasosSeguros.maxOf { paso ->
            amenazas.minOf { distancia(it.columna, it.fila, paso.first, paso.second) }
        }
        val pasoElegido = pasosSeguros.filter { paso ->
            amenazas.minOf { distancia(it.columna, it.fila, paso.first, paso.second) } == mayorDistanciaSegura
        }.minByOrNull { paso ->
            destinoDefensor?.let { distancia(paso.first, paso.second, it.first, it.second) } ?: 0
        } ?: return false

        moverA(pacman, pasoElegido)
        return true
    }

    private fun activarRefugio(pacman: PacmanColono) {
        pacman.refugiadoPorAmenaza = true
        pacman.objetivoFrutaColumna = -1
        pacman.objetivoFrutaFila = -1
        pacman.objetivoTunelColumna = -1
        pacman.objetivoTunelFila = -1
    }

    private fun hayAmenazaCercaBase(alcance: Int): Boolean {
        val puerta = entradaBase()
        return fantasmas.any {
            !it.asustado && distancia(it.columna, it.fila, puerta.first, puerta.second) <= alcance + 2
        }
    }

    private fun excavarParaHuir(
        obrero: PacmanColono,
        amenazas: List<FantasmaColonia>,
        defensor: PacmanColono?,
    ): Boolean {
        if (obrero.rol != RolPacman.OBRERO || obrero.agotado || obrero.resistenciaActual <= 0f) return false
        val distanciaAmenazaActual = amenazas.minOf { distancia(it.columna, it.fila, obrero.columna, obrero.fila) }
        val opcion = vecinosCerrados(obrero.columna, obrero.fila)
            .filter { !esDentroBase(it.first, it.second) }
            .filter { celda ->
                amenazas.minOf { distancia(it.columna, it.fila, celda.first, celda.second) } > distanciaAmenazaActual
            }
            .maxWithOrNull(
                compareBy<Pair<Int, Int>> { celda ->
                    amenazas.minOf { distancia(it.columna, it.fila, celda.first, celda.second) }
                }.thenBy { celda ->
                    defensor?.let { -distancia(it.columna, it.fila, celda.first, celda.second) } ?: 0
                }
            ) ?: return false

        abrirTunel(opcion.first, opcion.second)
        obrero.ultimaColumna = obrero.columna
        obrero.ultimaFila = obrero.fila
        obrero.direccion = direccionEntre(obrero.columna, obrero.fila, opcion.first, opcion.second)
        obrero.columna = opcion.first
        obrero.fila = opcion.second
        obrero.objetivoTunelColumna = -1
        obrero.objetivoTunelFila = -1
        gastarResistencia(obrero, GASTO_RESISTENCIA_OBRERO_POR_CELDA)
        return true
    }

    private fun moverHacia(
        pacman: PacmanColono,
        columna: Int,
        fila: Int,
        evitarFantasmas: Boolean = false,
    ): Boolean {
        val paso = siguientePaso(
            pacman.columna, pacman.fila, columna, fila, fantasma = false,
            pasoAnterior = pacman.ultimaColumna to pacman.ultimaFila,
            evitarFantasmas = evitarFantasmas,
        ) ?: return false
        moverA(pacman, paso)
        return true
    }

    private fun moverHaciaBase(
        pacman: PacmanColono,
        columna: Int,
        fila: Int,
        evitarFantasmas: Boolean = false,
    ): Boolean {
        if (pacman.columna == columna && pacman.fila == fila) {
            rutasBaseCacheadas.remove(pacman.identificador)
            return true
        }
        if (puertaInteriorOcupada(pacman)) return false
        if (evitarFantasmas) {
            rutasBaseCacheadas.remove(pacman.identificador)
            return moverHacia(pacman, columna, fila, evitarFantasmas = true)
        }

        val puertaInterior = puertaInteriorBase()
        val posicionActual = indice(pacman.columna, pacman.fila)
        val destino = indice(columna, fila)
        var ruta = rutasBaseCacheadas[pacman.identificador]
        if (ruta == null || ruta.destino != destino || ruta.revisionMapa != revisionEstructuraMapa ||
            ruta.posicionEsperada != posicionActual || ruta.siguientePaso >= ruta.pasos.size
        ) {
            val pasos = buscarRutaCompletaBase(pacman, columna, fila) ?: run {
                rutasBaseCacheadas.remove(pacman.identificador)
                pacman.objetivoBaseColumna = -1
                pacman.objetivoBaseFila = -1
                return false
            }
            ruta = RutaBaseCacheada(
                destino = destino,
                revisionMapa = revisionEstructuraMapa,
                pasos = pasos,
                posicionEsperada = posicionActual,
            )
            rutasBaseCacheadas[pacman.identificador] = ruta
        }

        val siguiente = ruta.pasos[ruta.siguientePaso]
        val siguienteColumna = siguiente % columnas
        val siguienteFila = siguiente / columnas
        if (!movimientoValido(pacman.columna, pacman.fila, siguienteColumna, siguienteFila, fantasma = false)) {
            val ocupadoEnBase = esDentroBase(siguienteColumna, siguienteFila) && pacmanes.any {
                it.identificador != pacman.identificador &&
                    it.columna == siguienteColumna && it.fila == siguienteFila
            }
            if (ocupadoEnBase && siguienteColumna == puertaInterior.first && siguienteFila == puertaInterior.second) {
                return false
            }
            rutasBaseCacheadas.remove(pacman.identificador)
            pacman.objetivoBaseColumna = -1
            pacman.objetivoBaseFila = -1
            return false
        }

        ruta.siguientePaso++
        ruta.posicionEsperada = siguiente
        moverA(pacman, siguienteColumna to siguienteFila)
        return true
    }

    private fun buscarRutaCompletaBase(
        pacman: PacmanColono,
        destinoColumna: Int,
        destinoFila: Int,
    ): IntArray? {
        val pasoAnterior = pacman.ultimaColumna to pacman.ultimaFila
        var primerPaso = buscarRutaAEstrella(
            pacman.columna, pacman.fila, destinoColumna, destinoFila,
            fantasma = false,
            pasoAnterior = pasoAnterior,
            evitarFantasmas = false,
            evitarRetroceso = true,
            capturarCamino = true,
        )
        if (primerPaso == null) {
            primerPaso = buscarRutaAEstrella(
                pacman.columna, pacman.fila, destinoColumna, destinoFila,
                fantasma = false,
                pasoAnterior = pasoAnterior,
                evitarFantasmas = false,
                evitarRetroceso = false,
                capturarCamino = true,
            )
        }
        if (primerPaso == null || longitudPasosRutaTemporal == 0) return null
        return pasosRutaTemporal.copyOf(longitudPasosRutaTemporal)
    }

    private fun moverA(pacman: PacmanColono, paso: Pair<Int, Int>) {
        actualizarDireccion(pacman, paso.first, paso.second)
        pacman.ultimaColumna = pacman.columna; pacman.ultimaFila = pacman.fila
        pacman.columna = paso.first; pacman.fila = paso.second
        recogerFrutaEnCelda(pacman)
    }

    private fun siguientePaso(
        origenColumna: Int, origenFila: Int, destinoColumna: Int, destinoFila: Int,
        fantasma: Boolean, pasoAnterior: Pair<Int, Int>? = null, evitarFantasmas: Boolean = false,
    ): Pair<Int, Int>? {
        if (origenColumna == destinoColumna && origenFila == destinoFila) return null
        val evitarRetroceso = pasoAnterior != null
        val primerResultado = buscarRutaAEstrella(
            origenColumna, origenFila, destinoColumna, destinoFila,
            fantasma, pasoAnterior, evitarFantasmas, evitarRetroceso,
        )
        if (primerResultado != null || !evitarRetroceso) return primerResultado
        return buscarRutaAEstrella(
                origenColumna, origenFila, destinoColumna, destinoFila,
                fantasma, pasoAnterior, evitarFantasmas, false,
            )
    }

    private fun buscarRutaAEstrella(
        origenColumna: Int,
        origenFila: Int,
        destinoColumna: Int,
        destinoFila: Int,
        fantasma: Boolean,
        pasoAnterior: Pair<Int, Int>?,
        evitarFantasmas: Boolean,
        evitarRetroceso: Boolean,
        capturarCamino: Boolean = false,
    ): Pair<Int, Int>? {
        prepararBuffersRuta()
        val inicio = indice(origenColumna, origenFila)
        val fin = indice(destinoColumna, destinoFila)
        val marca = siguienteMarcaRuta()
        val requiereOcupacionBase = !fantasma &&
            (esDentroBase(origenColumna, origenFila) || esDentroBase(destinoColumna, destinoFila))
        if (requiereOcupacionBase) {
            var indicePacman = 0
            while (indicePacman < pacmanes.size) {
                val pacman = pacmanes[indicePacman++]
                if (pacman.columna in 0 until columnas && pacman.fila in 0 until filas && esDentroBase(pacman.columna, pacman.fila)) {
                    marcarOcupacionRuta(indice(pacman.columna, pacman.fila), marca, 1)
                }
            }
        }
        if (evitarFantasmas) {
            var indiceFantasma = 0
            while (indiceFantasma < fantasmas.size) {
                val fantasmaActual = fantasmas[indiceFantasma++]
                if (!fantasmaActual.asustado && fantasmaActual.columna in 0 until columnas && fantasmaActual.fila in 0 until filas) {
                    marcarOcupacionRuta(indice(fantasmaActual.columna, fantasmaActual.fila), marca, 2)
                }
            }
        }

        tamanoHeapRuta = 0
        costesRuta[inicio] = 0
        estadoRuta[inicio] = marca
        predecesoresRuta[inicio] = inicio
        insertarHeapRuta(inicio, distancia(origenColumna, origenFila, destinoColumna, destinoFila))
        while (tamanoHeapRuta > 0) {
            val actual = extraerMinimoHeapRuta()
            if (estadoRuta[actual] == -marca) continue
            estadoRuta[actual] = -marca
            if (actual == fin) break

            val columna = actual % columnas
            val fila = actual / columnas
            val heuristicaActual = distancia(columna, fila, destinoColumna, destinoFila)
            val siguienteCosto = costesRuta[actual] + 1
            val direcciones = DireccionPacman.entries
            var indiceDireccion = 0
            while (indiceDireccion < direcciones.size) {
                val direccion = direcciones[indiceDireccion++]
                val siguienteColumna = columna + direccion.dx
                val siguienteFila = fila + direccion.dy
                if (actual == inicio && evitarRetroceso && pasoAnterior?.first == siguienteColumna && pasoAnterior.second == siguienteFila) continue
                if (siguienteColumna <= 0 || siguienteColumna >= columnas - 1 ||
                    siguienteFila <= 0 || siguienteFila >= filas - 1
                ) continue
                val siguiente = indice(siguienteColumna, siguienteFila)
                val estadoSiguiente = estadoRuta[siguiente]
                if (estadoSiguiente == -marca) continue
                val ocupacion = ocupacionRuta[siguiente]
                val bloqueadoPorFantasma = evitarFantasmas &&
                    (ocupacion ushr 2) == marca && (ocupacion and 2) != 0
                val movimientoPermitido = if (fantasma) {
                    tunel[siguienteFila][siguienteColumna] && !esDentroBase(siguienteColumna, siguienteFila)
                } else {
                    movimientoValido(
                        columna, fila, siguienteColumna, siguienteFila, fantasma = false,
                        marcaOcupacionPacman = if (requiereOcupacionBase) marca else 0,
                    )
                }
                if (bloqueadoPorFantasma || !movimientoPermitido) continue

                val estabaEnCola = estadoSiguiente == marca
                if (estabaEnCola && siguienteCosto >= costesRuta[siguiente]) continue
                costesRuta[siguiente] = siguienteCosto
                predecesoresRuta[siguiente] = actual
                estadoRuta[siguiente] = marca
                val cambioHeuristica = when (direccion) {
                    DireccionPacman.IZQUIERDA -> if (columna > destinoColumna) -1 else 1
                    DireccionPacman.DERECHA -> if (columna < destinoColumna) -1 else 1
                    DireccionPacman.ARRIBA -> if (fila > destinoFila) -1 else 1
                    DireccionPacman.ABAJO -> if (fila < destinoFila) -1 else 1
                }
                val estimacion = siguienteCosto + heuristicaActual + cambioHeuristica
                if (estabaEnCola) actualizarHeapRuta(siguiente, estimacion) else insertarHeapRuta(siguiente, estimacion)
            }
        }
        if (estadoRuta[fin] != -marca) return null
        var paso = fin
        var longitudCamino = 1
        while (predecesoresRuta[paso] != inicio) {
            paso = predecesoresRuta[paso]
            longitudCamino++
        }
        if (capturarCamino) {
            if (pasosRutaTemporal.size < longitudCamino) pasosRutaTemporal = IntArray(longitudCamino)
            paso = fin
            for (indiceCamino in longitudCamino - 1 downTo 0) {
                pasosRutaTemporal[indiceCamino] = paso
                if (indiceCamino > 0) paso = predecesoresRuta[paso]
            }
            longitudPasosRutaTemporal = longitudCamino
        }
        return (paso % columnas) to (paso / columnas)
    }

    private fun marcarOcupacionRuta(celda: Int, marca: Int, tipo: Int) {
        val ocupacion = ocupacionRuta[celda]
        ocupacionRuta[celda] = if ((ocupacion ushr 2) == marca) {
            ocupacion or tipo
        } else {
            (marca shl 2) or tipo
        }
    }

    private fun insertarHeapRuta(nodo: Int, prioridad: Int) {
        prioridadesRuta[nodo] = prioridad
        var posicion = tamanoHeapRuta++
        while (posicion > 0) {
            val padre = (posicion - 1) ushr 1
            val nodoPadre = colaRuta[padre]
            if (prioridadesRuta[nodoPadre] < prioridad ||
                (prioridadesRuta[nodoPadre] == prioridad && nodoPadre < nodo)
            ) break
            colaRuta[posicion] = nodoPadre
            posicionesHeapRuta[nodoPadre] = posicion
            posicion = padre
        }
        colaRuta[posicion] = nodo
        posicionesHeapRuta[nodo] = posicion
    }

    private fun actualizarHeapRuta(nodo: Int, prioridad: Int) {
        prioridadesRuta[nodo] = prioridad
        var posicion = posicionesHeapRuta[nodo]
        while (posicion > 0) {
            val padre = (posicion - 1) ushr 1
            val nodoPadre = colaRuta[padre]
            if (prioridadesRuta[nodoPadre] < prioridad ||
                (prioridadesRuta[nodoPadre] == prioridad && nodoPadre < nodo)
            ) break
            colaRuta[posicion] = nodoPadre
            posicionesHeapRuta[nodoPadre] = posicion
            posicion = padre
        }
        colaRuta[posicion] = nodo
        posicionesHeapRuta[nodo] = posicion
    }

    private fun extraerMinimoHeapRuta(): Int {
        val minimo = colaRuta[0]
        val ultimo = colaRuta[--tamanoHeapRuta]
        if (tamanoHeapRuta == 0) return minimo

        var posicion = 0
        while (true) {
            val hijoIzquierdo = posicion * 2 + 1
            if (hijoIzquierdo >= tamanoHeapRuta) break
            val hijoDerecho = hijoIzquierdo + 1
            var hijoMenor = hijoIzquierdo
            if (hijoDerecho < tamanoHeapRuta) {
                val izquierdo = colaRuta[hijoIzquierdo]
                val derecho = colaRuta[hijoDerecho]
                if (prioridadesRuta[derecho] < prioridadesRuta[izquierdo] ||
                    (prioridadesRuta[derecho] == prioridadesRuta[izquierdo] && derecho < izquierdo)
                ) hijoMenor = hijoDerecho
            }
            val nodoHijo = colaRuta[hijoMenor]
            if (prioridadesRuta[ultimo] < prioridadesRuta[nodoHijo] ||
                (prioridadesRuta[ultimo] == prioridadesRuta[nodoHijo] && ultimo < nodoHijo)
            ) break
            colaRuta[posicion] = nodoHijo
            posicionesHeapRuta[nodoHijo] = posicion
            posicion = hijoMenor
        }
        colaRuta[posicion] = ultimo
        posicionesHeapRuta[ultimo] = posicion
        return minimo
    }

    private fun prepararBuffersRuta() {
        val capacidadRequerida = columnas * filas
        if (estadoRuta.size >= capacidadRequerida) return
        var capacidad = maxOf(4096, estadoRuta.size)
        while (capacidad < capacidadRequerida) {
            capacidad = (capacidad.toLong() * 2L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
        estadoRuta = IntArray(capacidad)
        predecesoresRuta = IntArray(capacidad)
        colaRuta = IntArray(capacidad)
        costesRuta = IntArray(capacidad)
        prioridadesRuta = IntArray(capacidad)
        posicionesHeapRuta = IntArray(capacidad)
        ocupacionRuta = IntArray(capacidad)
        marcaRuta = 0
    }

    private fun siguienteMarcaRuta(): Int {
        if (marcaRuta >= MAX_MARCA_RUTA) {
            estadoRuta.fill(0)
            ocupacionRuta.fill(0)
            marcaRuta = 1
        } else {
            marcaRuta++
        }
        return marcaRuta
    }

    private fun proyectarPosicion(columna: Int, fila: Int, direccion: DireccionPacman): Pair<Int, Int> {
        var x = columna; var y = fila
        repeat(2) {
            val nx = x + direccion.dx; val ny = y + direccion.dy
            if (celdaValida(nx, ny, fantasma = true)) { x = nx; y = ny } else return@repeat
        }
        return x to y
    }

    private fun pasoErranteLejosDeBase(fantasma: FantasmaColonia): Pair<Int, Int>? {
        val cantidadVecinos = cargarVecinosFantasma(fantasma)
        if (cantidadVecinos == 0) return null
        var cantidadCandidatos = 0
        for (i in 0 until cantidadVecinos) {
            val vecino = vecinosFantasmaScratch[i]
            if (vecino % columnas != fantasma.ultimaColumna || vecino / columnas != fantasma.ultimaFila) {
                vecinosFantasmaScratch[cantidadCandidatos++] = vecino
            }
        }
        if (cantidadCandidatos == 0) cantidadCandidatos = cantidadVecinos
        val distanciaActual = distancia(fantasma.columna, fantasma.fila, columnaBase, filaBase)
        var hayOpcionQueSeAleja = false
        for (i in 0 until cantidadCandidatos) {
            val vecino = vecinosFantasmaScratch[i]
            if (distancia(vecino % columnas, vecino / columnas, columnaBase, filaBase) >= distanciaActual) {
                hayOpcionQueSeAleja = true
                break
            }
        }
        var distanciaMaxima = Int.MIN_VALUE
        for (i in 0 until cantidadCandidatos) {
            val vecino = vecinosFantasmaScratch[i]
            val distanciaBase = distancia(vecino % columnas, vecino / columnas, columnaBase, filaBase)
            if ((!hayOpcionQueSeAleja || distanciaBase >= distanciaActual) && distanciaBase > distanciaMaxima) {
                distanciaMaxima = distanciaBase
            }
        }
        var cantidadMaxima = 0
        for (i in 0 until cantidadCandidatos) {
            val vecino = vecinosFantasmaScratch[i]
            val distanciaBase = distancia(vecino % columnas, vecino / columnas, columnaBase, filaBase)
            if ((!hayOpcionQueSeAleja || distanciaBase >= distanciaActual) && distanciaBase == distanciaMaxima) {
                cantidadMaxima++
            }
        }
        var seleccion = azar.nextInt(cantidadMaxima)
        for (i in 0 until cantidadCandidatos) {
            val vecino = vecinosFantasmaScratch[i]
            val distanciaBase = distancia(vecino % columnas, vecino / columnas, columnaBase, filaBase)
            if ((!hayOpcionQueSeAleja || distanciaBase >= distanciaActual) && distanciaBase == distanciaMaxima) {
                if (seleccion-- == 0) return (vecino % columnas) to (vecino / columnas)
            }
        }
        return null
    }

    private fun pasoHuyendoDeDefensor(
        fantasma: FantasmaColonia,
        defensor: PacmanColono,
    ): Pair<Int, Int>? {
        val cantidadVecinos = cargarVecinosFantasma(fantasma)
        if (cantidadVecinos == 0) return null
        var cantidadCandidatos = 0
        for (i in 0 until cantidadVecinos) {
            val vecino = vecinosFantasmaScratch[i]
            if (vecino % columnas != fantasma.ultimaColumna || vecino / columnas != fantasma.ultimaFila) {
                vecinosFantasmaScratch[cantidadCandidatos++] = vecino
            }
        }
        if (cantidadCandidatos == 0) cantidadCandidatos = cantidadVecinos
        var mejorVecino = vecinosFantasmaScratch[0]
        var distanciaMaxima = distancia(mejorVecino % columnas, mejorVecino / columnas, defensor.columna, defensor.fila)
        for (i in 1 until cantidadCandidatos) {
            val vecino = vecinosFantasmaScratch[i]
            val distanciaDefensor = distancia(vecino % columnas, vecino / columnas, defensor.columna, defensor.fila)
            if (distanciaDefensor > distanciaMaxima) {
                mejorVecino = vecino
                distanciaMaxima = distanciaDefensor
            }
        }
        return (mejorVecino % columnas) to (mejorVecino / columnas)
    }

    private fun cargarVecinosFantasma(fantasma: FantasmaColonia): Int {
        var cantidad = 0
        var indiceDireccion = 0
        while (indiceDireccion < DireccionPacman.entries.size) {
            val direccion = DireccionPacman.entries[indiceDireccion++]
            val columna = fantasma.columna + direccion.dx
            val fila = fantasma.fila + direccion.dy
            if (celdaValida(columna, fila, fantasma = true)) {
                vecinosFantasmaScratch[cantidad++] = indice(columna, fila)
            }
        }
        return cantidad
    }

    private fun entradaBase(): Pair<Int, Int> = (columnaBase - anchoBase - 1) to filaBase
    private fun vecinosCerrados(columna: Int, fila: Int): List<Pair<Int, Int>> = DireccionPacman.entries
        .map { (columna + it.dx) to (fila + it.dy) }.filter { it.first in 1 until columnas - 1 && it.second in 1 until filas - 1 && !tunel[it.second][it.first] && !esDentroBase(it.first, it.second) }
    private fun tunelesAbiertos(): Collection<Pair<Int, Int>> {
        cacheTunelesAbiertos?.let { return it }
        val tunelesPorFila = Array<MutableList<Pair<Int, Int>>>(filas) { ArrayList() }
        var cantidad = 0
        for (fila in 1 until filas - 1) {
            for (columna in 1 until columnas - 1) {
                if (tunel[fila][columna]) {
                    tunelesPorFila[fila].add(columna to fila)
                    cantidad++
                }
            }
        }
        filasTunelesAbiertos = tunelesPorFila
        cantidadTunelesCache = cantidad
        cacheTunelesAbiertos = vistaTunelesAbiertos
        return vistaTunelesAbiertos
    }

    private fun abrirTunel(columna: Int, fila: Int) {
        if (tunel[fila][columna]) return
        tunel[fila][columna] = true
        celdasTunelModificadas.add(indice(columna, fila))
        if (columna !in 1 until columnas - 1 || fila !in 1 until filas - 1) return
        actualizarCacheFronterasCercanas(columna, fila)
        val tuneles = filasTunelesAbiertos?.get(fila) ?: return
        var izquierda = 0
        var derecha = tuneles.size
        while (izquierda < derecha) {
            val medio = (izquierda + derecha) ushr 1
            val existente = tuneles[medio]
            if (existente.first < columna) {
                izquierda = medio + 1
            } else {
                derecha = medio
            }
        }
        tuneles.add(izquierda, columna to fila)
        cantidadTunelesCache++
    }

    private fun invalidarCacheTuneles() {
        cacheTunelesAbiertos = null
        filasTunelesAbiertos = null
        cantidadTunelesCache = 0
        cacheFronterasAbiertas = null
        rutasBaseCacheadas.clear()
        revisionEstructuraMapa++
        celdasTunelModificadas.clear()
    }

    /** Copia las celdas excavadas desde el último fotograma sin crear una lista temporal. */
    @Synchronized
    fun copiarCeldasTunelModificadas(destino: MutableList<Int>) {
        destino.clear()
        destino.addAll(celdasTunelModificadas)
        celdasTunelModificadas.clear()
    }
    private fun indice(columna: Int, fila: Int) = fila * columnas + columna
    private fun distancia(columnaA: Int, filaA: Int, columnaB: Int, filaB: Int) = abs(columnaA - columnaB) + abs(filaA - filaB)

    private fun direccionOpuesta(direccion: DireccionPacman): DireccionPacman = when (direccion) {
        DireccionPacman.IZQUIERDA -> DireccionPacman.DERECHA
        DireccionPacman.DERECHA -> DireccionPacman.IZQUIERDA
        DireccionPacman.ARRIBA -> DireccionPacman.ABAJO
        DireccionPacman.ABAJO -> DireccionPacman.ARRIBA
    }

    private fun actualizarDireccion(pacman: PacmanColono, nuevaColumna: Int, nuevaFila: Int) {
        pacman.direccion = direccionEntre(pacman.columna, pacman.fila, nuevaColumna, nuevaFila)
    }
    private fun direccionEntre(columnaA: Int, filaA: Int, columnaB: Int, filaB: Int): DireccionPacman = when {
        columnaB < columnaA -> DireccionPacman.IZQUIERDA
        columnaB > columnaA -> DireccionPacman.DERECHA
        filaB < filaA -> DireccionPacman.ARRIBA
        else -> DireccionPacman.ABAJO
    }

    @Synchronized
    fun serializar(): String {
        val capacidadInicial = (
            (columnas.toLong() * filas + 5L) / 6L + pacmanes.size * 420L + fantasmas.size * 180L + frutas.size * 80L + 512L
            ).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return buildString(capacidadInicial) {
            append('{')
            append("\"puntos\":").append(puntos)
            append(",\"nivelBase\":").append(nivelBase)
            append(",\"ciclo\":").append(ciclo)
            append(",\"columnas\":").append(columnas)
            append(",\"filas\":").append(filas)
            append(",\"columnaBase\":").append(columnaBase)
            append(",\"filaBase\":").append(filaBase)

            append(",\"mejoras\":{")
            var primero = true
            for ((mejora, nivel) in nivelesMejora) {
                if (!primero) append(',') else primero = false
                appendJsonString(mejora.name).append(':').append(nivel)
            }
            append('}')

            append(",\"inventario\":{")
            primero = true
            for ((clave, cantidad) in inventario) {
                if (!primero) append(',') else primero = false
                appendJsonString(clave).append(':').append(cantidad)
            }
            append('}')

            append(",\"tunel\":\"")
            var bits = 0
            var cantidadBits = 0
            for (fila in tunel) for (abierto in fila) {
                bits = (bits shl 1) or if (abierto) 1 else 0
                cantidadBits++
                if (cantidadBits == 6) {
                    append(ALFABETO_TUNEL[bits])
                    bits = 0
                    cantidadBits = 0
                }
            }
            if (cantidadBits > 0) append(ALFABETO_TUNEL[bits shl (6 - cantidadBits)])
            append('"')

            append(",\"pacmanes\":[")
            pacmanes.forEachIndexed { indice, pacman ->
                val primero = indice == 0
                if (!primero) append(',')
                append('{')
                append("\"id\":").append(pacman.identificador)
                append(",\"rol\":"); appendJsonString(pacman.rol.name)
                append(",\"columna\":").append(pacman.columna)
                append(",\"fila\":").append(pacman.fila)
                append(",\"direccion\":"); appendJsonString(pacman.direccion.name)
                append(",\"objetivoPatrullaColumna\":").append(pacman.objetivoPatrullaColumna)
                append(",\"objetivoPatrullaFila\":").append(pacman.objetivoPatrullaFila)
                append(",\"objetivoFantasmaId\":").append(pacman.objetivoFantasmaId)
                append(",\"objetivoFrutaColumna\":").append(pacman.objetivoFrutaColumna)
                append(",\"objetivoFrutaFila\":").append(pacman.objetivoFrutaFila)
                append(",\"objetivoTunelColumna\":").append(pacman.objetivoTunelColumna)
                append(",\"objetivoTunelFila\":").append(pacman.objetivoTunelFila)
                append(",\"objetivoBaseColumna\":").append(pacman.objetivoBaseColumna)
                append(",\"objetivoBaseFila\":").append(pacman.objetivoBaseFila)
                append(",\"ultimaColumna\":").append(pacman.ultimaColumna)
                append(",\"ultimaFila\":").append(pacman.ultimaFila)
                append(",\"resistenciaActual\":").append(pacman.resistenciaActual)
                append(",\"agotado\":").append(pacman.agotado)
                append(",\"refugiadoPorAmenaza\":").append(pacman.refugiadoPorAmenaza)
                append('}')
            }
            append(']')

            append(",\"fantasmas\":[")
            fantasmas.forEachIndexed { indice, fantasma ->
                if (indice > 0) append(',')
                append('{')
                append("\"id\":").append(fantasma.identificador)
                append(",\"tipo\":"); appendJsonString(fantasma.tipo)
                append(",\"columna\":").append(fantasma.columna)
                append(",\"fila\":").append(fantasma.fila)
                append(",\"direccion\":"); appendJsonString(fantasma.direccion.name)
                append(",\"objetivoPacmanId\":").append(fantasma.objetivoPacmanId)
                append(",\"ultimaColumna\":").append(fantasma.ultimaColumna)
                append(",\"ultimaFila\":").append(fantasma.ultimaFila)
                append('}')
            }
            append(']')

            append(",\"frutas\":[")
            frutas.forEachIndexed { indice, fruta ->
                if (indice > 0) append(',')
                append('{')
                append("\"columna\":").append(fruta.columna)
                append(",\"fila\":").append(fruta.fila)
                append(",\"tipo\":"); appendJsonString(fruta.tipo.clave)
                append('}')
            }
            append("]}")
        }
    }

    private fun StringBuilder.appendJsonString(valor: String): StringBuilder {
        append('"')
        for (caracter in valor) {
            when (caracter) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (caracter.code < 0x20) {
                        append("\\u00")
                        append("0123456789abcdef"[(caracter.code shr 4) and 0xF])
                        append("0123456789abcdef"[caracter.code and 0xF])
                    } else {
                        append(caracter)
                    }
                }
            }
        }
        return append('"')
    }

    @Synchronized
    fun restaurar(serializado: String): Boolean = runCatching {
        val datos = JSONObject(serializado)
        nuevaPartida()
        puntos = datos.optInt("puntos", 0); nivelBase = datos.optInt("nivelBase", 1); ciclo = datos.optInt("ciclo", 0)
        columnas = datos.optInt("columnas", 58).coerceAtLeast(58)
        filas = datos.optInt("filas", 32).coerceAtLeast(32)
        posicionBaseX = datos.optInt("columnaBase", columnas / 2).coerceIn(1, columnas - 2)
        posicionBaseY = datos.optInt("filaBase", filas / 2).coerceIn(1, filas - 2)
        desplazamientoCoordenadasX = 0
        desplazamientoCoordenadasY = 0
        tunel = Array(filas) { BooleanArray(columnas) }
        invalidarCacheTuneles()
        val mejoras = datos.optJSONObject("mejoras")
        MejoraColonia.entries.forEach { nivelesMejora[it] = mejoras?.optInt(it.name, 0) ?: 0 }
        val mapa = datos.optString("tunel")
        val cantidadCeldas = columnas * filas
        if (mapa.length == cantidadCeldas) {
            mapa.forEachIndexed { indice, bit -> tunel[indice / columnas][indice % columnas] = bit == '1' }
        } else if (mapa.length == (cantidadCeldas + 5) / 6) {
            var indiceCelda = 0
            for (caracter in mapa) {
                val valor = ALFABETO_TUNEL.indexOf(caracter)
                if (valor < 0) break
                for (desplazamiento in 5 downTo 0) {
                    if (indiceCelda >= cantidadCeldas) break
                    tunel[indiceCelda / columnas][indiceCelda % columnas] = (valor and (1 shl desplazamiento)) != 0
                    indiceCelda++
                }
            }
        }
        pacmanes.clear(); fantasmas.clear(); frutas.clear()
        val habitantes = datos.optJSONArray("pacmanes") ?: JSONArray()
        for (indice in 0 until habitantes.length()) {
            val habitante = habitantes.getJSONObject(indice)
            val rol = RolPacman.valueOf(habitante.getString("rol"))
            val pacman = PacmanColono(
                identificador = habitante.getInt("id"), rol = rol,
                columna = habitante.getInt("columna"), fila = habitante.getInt("fila"),
                direccion = DireccionPacman.valueOf(habitante.optString("direccion", "DERECHA")),
                objetivoPatrullaColumna = habitante.optInt("objetivoPatrullaColumna", -1),
                objetivoPatrullaFila = habitante.optInt("objetivoPatrullaFila", -1),
                objetivoFantasmaId = habitante.optInt("objetivoFantasmaId", -1),
                objetivoFrutaColumna = habitante.optInt("objetivoFrutaColumna", -1),
                objetivoFrutaFila = habitante.optInt("objetivoFrutaFila", -1),
                objetivoTunelColumna = habitante.optInt("objetivoTunelColumna", -1),
                objetivoTunelFila = habitante.optInt("objetivoTunelFila", -1),
                objetivoBaseColumna = habitante.optInt("objetivoBaseColumna", -1),
                objetivoBaseFila = habitante.optInt("objetivoBaseFila", -1),
                ultimaColumna = habitante.optInt("ultimaColumna", -1), ultimaFila = habitante.optInt("ultimaFila", -1),
                resistenciaActual = habitante.optDouble("resistenciaActual", capacidadResistencia(rol).toDouble()).toFloat(),
                // Versiones anteriores llegaron a guardar a los exploradores agotados,
                // aunque ese rol no consume resistencia y nunca debe quedar agotado.
                agotado = rol != RolPacman.EXPLORADOR && habitante.optBoolean("agotado", false),
                refugiadoPorAmenaza = habitante.optBoolean("refugiadoPorAmenaza", false),
            )
            pacman.resistenciaActual = pacman.resistenciaActual.coerceIn(0f, capacidadResistencia(rol))
            if (rol != RolPacman.EXPLORADOR && pacman.resistenciaActual <= 0f) pacman.agotado = true
            pacmanes += pacman
        }
        corregirSolapamientosBase()
        val apariciones = datos.optJSONArray("fantasmas") ?: JSONArray()
        for (indice in 0 until apariciones.length()) {
            val aparicion = apariciones.getJSONObject(indice)
            fantasmas += FantasmaColonia(
                identificador = aparicion.getInt("id"), tipo = aparicion.getString("tipo"),
                columna = aparicion.getInt("columna"), fila = aparicion.getInt("fila"),
                direccion = DireccionPacman.valueOf(aparicion.optString("direccion", "IZQUIERDA")),
                objetivoPacmanId = aparicion.optInt("objetivoPacmanId", -1),
                ultimaColumna = aparicion.optInt("ultimaColumna", -1), ultimaFila = aparicion.optInt("ultimaFila", -1),
            )
        }
        val comida = datos.optJSONArray("frutas") ?: JSONArray()
        for (indice in 0 until comida.length()) {
            val alimento = comida.getJSONObject(indice)
            tiposFruta.find { it.clave == alimento.getString("tipo") }?.let { frutas += FrutaColonia(alimento.getInt("columna"), alimento.getInt("fila"), it) }
        }
        val existencias = datos.optJSONObject("inventario")
        inventario.keys.forEach { inventario[it] = existencias?.optInt(it, 0) ?: 0 }
        frutasAlmacenadas = inventario.values.sum()
        siguienteIdentificador = (pacmanes.maxOfOrNull { it.identificador } ?: 0).coerceAtLeast(fantasmas.maxOfOrNull { it.identificador } ?: 0) + 1
        reconstruirTareasTrasRestaurar()
        true
    }.getOrDefault(false)

    /** Los objetivos de movimiento no son estado persistente: se recalculan a partir del mundo restaurado. */
    private fun reconstruirTareasTrasRestaurar() {
        tiempoMovimientoPacman.clear()
        pacmanes.forEach { pacman ->
            pacman.objetivoPatrullaColumna = -1
            pacman.objetivoPatrullaFila = -1
            pacman.objetivoFantasmaId = -1
            pacman.objetivoFrutaColumna = -1
            pacman.objetivoFrutaFila = -1
            pacman.objetivoTunelColumna = -1
            pacman.objetivoTunelFila = -1
            pacman.objetivoBaseColumna = -1
            pacman.objetivoBaseFila = -1
            pacman.ultimaColumna = -1
            pacman.ultimaFila = -1
            pacman.refugiadoPorAmenaza = false
            tiempoMovimientoPacman[pacman.identificador] = 0L
        }
        fantasmas.forEach { fantasma ->
            fantasma.asustado = false
            fantasma.objetivoPacmanId = -1
            fantasma.ultimaColumna = -1
            fantasma.ultimaFila = -1
        }
        asignarTareasColectivas()
    }
}
