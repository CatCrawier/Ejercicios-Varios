package com.example.tic_tac_toe

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

enum class NivelDificultad { FACIL, MEDIO, DIFICIL }

/** IA de tres en raya: 0 = vacía, 1 = X, 2 = O. */
class TicTacToeAI(private val aleatorio: Random = Random.Default) {
    fun encontrarMejorMovimiento(
        tablero: IntArray,
        nivel: NivelDificultad,
        marcaIA: Int = O,
        marcaRival: Int = X
    ): Pair<Int, Int>? {
        require(tablero.size == TAMANO_TABLERO) { "El tablero debe tener 9 posiciones" }
        require(marcaIA != marcaRival && marcaIA != VACIO && marcaRival != VACIO) { "Las marcas no son válidas" }
        if (obtenerGanador(tablero) != VACIO || estaTableroLleno(tablero)) return null
        return when (nivel) {
            NivelDificultad.FACIL -> movimientoAleatorio(tablero)
            NivelDificultad.MEDIO -> if (aleatorio.nextBoolean()) movimientoMinimax(tablero, marcaIA, marcaRival) else movimientoAleatorio(tablero)
            NivelDificultad.DIFICIL -> movimientoMinimax(tablero, marcaIA, marcaRival)
        }
    }

    fun obtenerGanador(tablero: IntArray): Int {
        for (linea in LINEAS_GANADORAS) {
            val marca = tablero[linea[0]]
            if (marca != VACIO && linea.all { tablero[it] == marca }) return marca
        }
        return VACIO
    }

    fun estaTableroLleno(tablero: IntArray): Boolean = tablero.none { it == VACIO }

    private fun movimientoMinimax(tablero: IntArray, marcaIA: Int, marcaRival: Int): Pair<Int, Int>? {
        var mejorPuntaje = Int.MIN_VALUE
        var mejorMovimiento: Pair<Int, Int>? = null
        posicionesVacias(tablero).forEach { posicion ->
            tablero[posicion] = marcaIA
            val puntaje = minimax(tablero, 0, false, marcaIA, marcaRival)
            tablero[posicion] = VACIO
            if (puntaje > mejorPuntaje) {
                mejorPuntaje = puntaje
                mejorMovimiento = posicion / COLUMNAS to posicion % COLUMNAS
            }
        }
        return mejorMovimiento
    }

    private fun minimax(tablero: IntArray, profundidad: Int, maximizando: Boolean, marcaIA: Int, marcaRival: Int): Int {
        when (obtenerGanador(tablero)) {
            marcaIA -> return 10 - profundidad
            marcaRival -> return profundidad - 10
        }
        if (estaTableroLleno(tablero)) return 0
        return if (maximizando) {
            var mejorPuntaje = Int.MIN_VALUE
            posicionesVacias(tablero).forEach { posicion ->
                tablero[posicion] = marcaIA
                mejorPuntaje = max(mejorPuntaje, minimax(tablero, profundidad + 1, false, marcaIA, marcaRival))
                tablero[posicion] = VACIO
            }
            mejorPuntaje
        } else {
            var mejorPuntaje = Int.MAX_VALUE
            posicionesVacias(tablero).forEach { posicion ->
                tablero[posicion] = marcaRival
                mejorPuntaje = min(mejorPuntaje, minimax(tablero, profundidad + 1, true, marcaIA, marcaRival))
                tablero[posicion] = VACIO
            }
            mejorPuntaje
        }
    }

    private fun movimientoAleatorio(tablero: IntArray): Pair<Int, Int>? =
        posicionesVacias(tablero).randomOrNull(aleatorio)?.let { it / COLUMNAS to it % COLUMNAS }

    private fun posicionesVacias(tablero: IntArray): List<Int> = tablero.indices.filter { tablero[it] == VACIO }

    companion object {
        const val VACIO = 0
        const val X = 1
        const val O = 2
        private const val TAMANO_TABLERO = 9
        private const val COLUMNAS = 3
        private val LINEAS_GANADORAS = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
    }
}
