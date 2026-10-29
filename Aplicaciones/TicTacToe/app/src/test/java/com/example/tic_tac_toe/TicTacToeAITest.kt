package com.example.tic_tac_toe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TicTacToeAITest {
    private val inteligenciaArtificial = TicTacToeAI()

    @Test
    fun nivelDificilTomaMovimientoGanador() {
        val tablero = intArrayOf(2, 2, 0, 1, 1, 0, 0, 0, 0)
        assertEquals(0 to 2, inteligenciaArtificial.encontrarMejorMovimiento(tablero, NivelDificultad.DIFICIL))
    }

    @Test
    fun nivelDificilBloqueaMovimientoGanadorDelJugador() {
        val tablero = intArrayOf(1, 1, 0, 2, 0, 0, 0, 0, 0)
        assertEquals(0 to 2, inteligenciaArtificial.encontrarMejorMovimiento(tablero, NivelDificultad.DIFICIL))
    }

    @Test
    fun nivelFacilDevuelveCasillaVacia() {
        val tablero = intArrayOf(1, 2, 1, 2, 0, 0, 1, 0, 2)
        val movimiento = inteligenciaArtificial.encontrarMejorMovimiento(tablero, NivelDificultad.FACIL)
        assertNotEquals(null, movimiento)
        assertEquals(0, tablero[movimiento!!.first * 3 + movimiento.second])
    }

    @Test
    fun nivelDificilFuncionaCuandoLaIAUsaX() {
        val tablero = intArrayOf(1, 1, 0, 2, 2, 0, 0, 0, 0)

        assertEquals(
            0 to 2,
            inteligenciaArtificial.encontrarMejorMovimiento(
                tablero,
                NivelDificultad.DIFICIL,
                TicTacToeAI.X,
                TicTacToeAI.O
            )
        )
    }
}
