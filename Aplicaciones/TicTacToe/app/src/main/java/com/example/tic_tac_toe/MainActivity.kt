package com.example.tic_tac_toe

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

enum class ModoJuego { CONTRA_IA, DOS_JUGADORES }

class MainActivity : AppCompatActivity() {
    private val inteligenciaArtificial = TicTacToeAI()
    private lateinit var casillas: List<MaterialButton>
    private lateinit var textoEstado: TextView
    private lateinit var selectorDificultad: Spinner
    private lateinit var selectorModo: Spinner
    private lateinit var selectorFigura: Spinner
    private var tablero = IntArray(TAMANO_TABLERO)
    private var jugadorActual = TicTacToeAI.X
    private var nivelDificultad = NivelDificultad.DIFICIL
    private var modoJuego = ModoJuego.CONTRA_IA
    private var marcaJugadorUno = TicTacToeAI.X
    private var juegoTerminado = false
    private var controlesListos = false

    private val marcaIA: Int get() = marcaOpuesta(marcaJugadorUno)

    override fun onCreate(estadoGuardado: Bundle?) {
        super.onCreate(estadoGuardado)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { vista, margenes ->
            val barrasSistema = margenes.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.setPadding(barrasSistema.left, barrasSistema.top, barrasSistema.right, barrasSistema.bottom)
            margenes
        }
        textoEstado = findViewById(R.id.statusText)
        selectorDificultad = findViewById(R.id.difficultySpinner)
        selectorModo = findViewById(R.id.gameModeSpinner)
        selectorFigura = findViewById(R.id.figureSpinner)
        casillas = listOf(findViewById(R.id.cell0), findViewById(R.id.cell1), findViewById(R.id.cell2), findViewById(R.id.cell3), findViewById(R.id.cell4), findViewById(R.id.cell5), findViewById(R.id.cell6), findViewById(R.id.cell7), findViewById(R.id.cell8))
        casillas.forEachIndexed { indice, casilla -> casilla.setOnClickListener { jugarMovimiento(indice) } }
        findViewById<MaterialButton>(R.id.restartButton).setOnClickListener { iniciarPartida() }
        configurarSelectores()
        restaurarJuego(estadoGuardado)
        selectorDificultad.setSelection(nivelDificultad.ordinal)
        selectorModo.setSelection(modoJuego.ordinal)
        selectorFigura.setSelection(if (marcaJugadorUno == TicTacToeAI.X) 0 else 1)
        controlesListos = true
        actualizarVisibilidadDificultad()
        actualizarTablero()
        iniciarTurnoIASiCorresponde()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(ESTADO_TABLERO, tablero)
        outState.putInt(ESTADO_JUGADOR, jugadorActual)
        outState.putInt(ESTADO_DIFICULTAD, nivelDificultad.ordinal)
        outState.putInt(ESTADO_MODO, modoJuego.ordinal)
        outState.putInt(ESTADO_MARCA_JUGADOR_UNO, marcaJugadorUno)
        outState.putBoolean(ESTADO_TERMINADO, juegoTerminado)
    }

    private fun configurarSelectores() {
        selectorDificultad.onItemSelectedListener = selector { nivelDificultad = NivelDificultad.entries[it] }
        selectorModo.onItemSelectedListener = selector {
            modoJuego = ModoJuego.entries[it]
            actualizarVisibilidadDificultad()
            if (controlesListos) iniciarPartida()
        }
        selectorFigura.onItemSelectedListener = selector {
            marcaJugadorUno = if (it == 0) TicTacToeAI.X else TicTacToeAI.O
            if (controlesListos) iniciarPartida()
        }
    }

    private fun selector(alSeleccionar: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(padre: AdapterView<*>, vista: View?, posicion: Int, id: Long) = alSeleccionar(posicion)
        override fun onNothingSelected(padre: AdapterView<*>) = Unit
    }

    private fun restaurarJuego(estadoGuardado: Bundle?) {
        val tableroGuardado = estadoGuardado?.getIntArray(ESTADO_TABLERO)
        if (tableroGuardado?.size == TAMANO_TABLERO) {
            tablero = tableroGuardado
            jugadorActual = estadoGuardado.getInt(ESTADO_JUGADOR, TicTacToeAI.X)
            nivelDificultad = NivelDificultad.entries[estadoGuardado.getInt(ESTADO_DIFICULTAD, NivelDificultad.DIFICIL.ordinal)]
            modoJuego = ModoJuego.entries[estadoGuardado.getInt(ESTADO_MODO, ModoJuego.CONTRA_IA.ordinal)]
            marcaJugadorUno = estadoGuardado.getInt(ESTADO_MARCA_JUGADOR_UNO, TicTacToeAI.X)
            juegoTerminado = estadoGuardado.getBoolean(ESTADO_TERMINADO, false)
        }
    }

    private fun jugarMovimiento(indice: Int) {
        if (juegoTerminado || esTurnoIA() || tablero[indice] != TicTacToeAI.VACIO) return
        tablero[indice] = jugadorActual
        if (!terminarJuegoSiCorresponde()) {
            jugadorActual = marcaOpuesta(jugadorActual)
            actualizarTablero()
            iniciarTurnoIASiCorresponde()
        } else actualizarTablero()
    }

    private fun iniciarTurnoIASiCorresponde() {
        if (esTurnoIA() && !juegoTerminado) casillas.first().post { jugarMovimientoIA() }
    }

    private fun jugarMovimientoIA() {
        if (!esTurnoIA() || juegoTerminado) return
        inteligenciaArtificial.encontrarMejorMovimiento(tablero, nivelDificultad, marcaIA, marcaJugadorUno)?.let { (fila, columna) ->
            tablero[fila * COLUMNAS + columna] = marcaIA
        }
        if (!terminarJuegoSiCorresponde()) jugadorActual = marcaOpuesta(jugadorActual)
        actualizarTablero()
    }

    private fun esTurnoIA() = modoJuego == ModoJuego.CONTRA_IA && jugadorActual == marcaIA

    private fun terminarJuegoSiCorresponde(): Boolean {
        if (inteligenciaArtificial.obtenerGanador(tablero) != TicTacToeAI.VACIO || inteligenciaArtificial.estaTableroLleno(tablero)) juegoTerminado = true
        return juegoTerminado
    }

    private fun iniciarPartida() {
        tablero = IntArray(TAMANO_TABLERO)
        jugadorActual = TicTacToeAI.X
        juegoTerminado = false
        actualizarTablero()
        iniciarTurnoIASiCorresponde()
    }

    private fun actualizarVisibilidadDificultad() {
        selectorDificultad.visibility = if (modoJuego == ModoJuego.CONTRA_IA) View.VISIBLE else View.GONE
    }

    private fun actualizarTablero() {
        casillas.forEachIndexed { indice, casilla ->
            val marca = marcaComoTexto(tablero[indice])
            casilla.text = marca
            casilla.isEnabled = !juegoTerminado && !esTurnoIA() && tablero[indice] == TicTacToeAI.VACIO
            casilla.contentDescription = if (marca.isEmpty()) getString(R.string.casilla_vacia) else getString(R.string.casilla_marcada, marca)
        }
        textoEstado.text = textoEstadoActual()
    }

    private fun textoEstadoActual(): String {
        val ganador = inteligenciaArtificial.obtenerGanador(tablero)
        if (ganador != TicTacToeAI.VACIO) return when (modoJuego) {
            ModoJuego.CONTRA_IA -> if (ganador == marcaJugadorUno) getString(R.string.gana_jugador) else getString(R.string.gana_inteligencia_artificial)
            ModoJuego.DOS_JUGADORES -> if (ganador == marcaJugadorUno) getString(R.string.gana_jugador_uno) else getString(R.string.gana_jugador_dos)
        }
        if (inteligenciaArtificial.estaTableroLleno(tablero)) return getString(R.string.empate)
        return when (modoJuego) {
            ModoJuego.CONTRA_IA -> if (esTurnoIA()) getString(R.string.turno_inteligencia_artificial, marcaComoTexto(marcaIA)) else getString(R.string.turno_jugador, marcaComoTexto(marcaJugadorUno))
            ModoJuego.DOS_JUGADORES -> if (jugadorActual == marcaJugadorUno) getString(R.string.turno_jugador_uno, marcaComoTexto(marcaJugadorUno)) else getString(R.string.turno_jugador_dos, marcaComoTexto(marcaOpuesta(marcaJugadorUno)))
        }
    }

    private fun marcaOpuesta(marca: Int) = if (marca == TicTacToeAI.X) TicTacToeAI.O else TicTacToeAI.X
    private fun marcaComoTexto(marca: Int) = if (marca == TicTacToeAI.X) "X" else if (marca == TicTacToeAI.O) "O" else ""

    private companion object {
        const val TAMANO_TABLERO = 9
        const val COLUMNAS = 3
        const val ESTADO_TABLERO = "tablero"
        const val ESTADO_JUGADOR = "jugador_actual"
        const val ESTADO_DIFICULTAD = "dificultad"
        const val ESTADO_MODO = "modo_juego"
        const val ESTADO_MARCA_JUGADOR_UNO = "marca_jugador_uno"
        const val ESTADO_TERMINADO = "juego_terminado"
    }
}
