package com.example.juego

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private enum class Estado {
        LISTO, JUGANDO, TERMINADO
    }

    private var estadoActual = Estado.LISTO
    private var nivel = NIVEL_UNO
    private var puntos = 0
    private var tiempoRestante = SEGUNDOS_NIVEL_UNO
    private var temporizador: CountDownTimer? = null
    private val manejador = Handler(Looper.getMainLooper())

    private lateinit var txtTiempo: TextView
    private lateinit var txtPuntos: TextView
    private lateinit var txtNivel: TextView
    private lateinit var txtMensaje: TextView
    private lateinit var objetivo: TextView
    private lateinit var zonaJuego: FrameLayout
    private lateinit var btnIniciar: Button

    private val movimientoNivelDos = object : Runnable {
        override fun run() {
            if (estadoActual == Estado.JUGANDO && nivel == NIVEL_DOS) {
                moverObjetivo()
                manejador.postDelayed(this, INTERVALO_MOVIMIENTO_NIVEL_DOS_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTiempo = findViewById(R.id.txtTiempo)
        txtPuntos = findViewById(R.id.txtPuntos)
        txtNivel = findViewById(R.id.txtNivel)
        txtMensaje = findViewById(R.id.txtMensaje)
        objetivo = findViewById(R.id.objetivo)
        zonaJuego = findViewById(R.id.zonaJuego)
        btnIniciar = findViewById(R.id.btnIniciar)

        btnIniciar.setOnClickListener { iniciarJuego() }
        objetivo.setOnClickListener { registrarAcierto() }
        actualizarInterfaz()
    }

    private fun iniciarJuego() {
        if (estadoActual == Estado.JUGANDO) return

        temporizador?.cancel()
        manejador.removeCallbacks(movimientoNivelDos)
        nivel = NIVEL_UNO
        puntos = 0
        tiempoRestante = SEGUNDOS_NIVEL_UNO
        estadoActual = Estado.JUGANDO
        configurarTamanoObjetivo(TAMANO_OBJETIVO_NIVEL_UNO_DP)
        actualizarInterfaz()

        zonaJuego.post {
            if (estadoActual == Estado.JUGANDO && nivel == NIVEL_UNO) {
                moverObjetivo()
                iniciarTemporizador(DURACION_NIVEL_UNO_MS)
            }
        }
    }

    private fun iniciarTemporizador(duracionMs: Long) {
        temporizador?.cancel()
        temporizador = object : CountDownTimer(duracionMs, INTERVALO_TICK_MS) {
            override fun onTick(millisUntilFinished: Long) {
                if (estadoActual != Estado.JUGANDO) {
                    cancel()
                    return
                }

                tiempoRestante = ((millisUntilFinished + 999L) / 1000L).toInt()
                actualizarInterfaz()
            }

            override fun onFinish() {
                if (estadoActual == Estado.JUGANDO) {
                    tiempoRestante = 0
                    finalizarJuego()
                }
            }
        }.start()
    }

    private fun registrarAcierto() {
        if (estadoActual != Estado.JUGANDO) return

        puntos++
        if (nivel == NIVEL_UNO && puntos > PUNTOS_PARA_NIVEL_DOS) {
            iniciarNivelDos()
        } else {
            actualizarInterfaz()
            moverObjetivo()
        }
    }

    private fun iniciarNivelDos() {
        nivel = NIVEL_DOS
        tiempoRestante = SEGUNDOS_NIVEL_DOS
        temporizador?.cancel()
        configurarTamanoObjetivo(TAMANO_OBJETIVO_NIVEL_DOS_DP)
        actualizarInterfaz()
        Toast.makeText(this, R.string.aviso_nivel_dos, Toast.LENGTH_LONG).show()

        objetivo.post {
            if (estadoActual == Estado.JUGANDO && nivel == NIVEL_DOS) {
                moverObjetivo()
                iniciarTemporizador(DURACION_NIVEL_DOS_MS)
                manejador.removeCallbacks(movimientoNivelDos)
                manejador.postDelayed(movimientoNivelDos, INTERVALO_MOVIMIENTO_NIVEL_DOS_MS)
            }
        }
    }

    private fun configurarTamanoObjetivo(tamanoDp: Int) {
        val tamanoPx = (tamanoDp * resources.displayMetrics.density).toInt()
        objetivo.layoutParams = (objetivo.layoutParams as FrameLayout.LayoutParams).apply {
            width = tamanoPx
            height = tamanoPx
        }
        objetivo.textSize = if (nivel == NIVEL_DOS) 34f else 52f
    }

    private fun moverObjetivo() {
        if (estadoActual != Estado.JUGANDO) return

        val maxX = zonaJuego.width - objetivo.width
        val maxY = zonaJuego.height - objetivo.height
        if (maxX < 0 || maxY < 0) return

        val parametros = objetivo.layoutParams as FrameLayout.LayoutParams
        parametros.gravity = Gravity.TOP or Gravity.START
        parametros.leftMargin = Random.nextInt(maxX + 1)
        parametros.topMargin = Random.nextInt(maxY + 1)
        objetivo.layoutParams = parametros
    }

    private fun finalizarJuego() {
        estadoActual = Estado.TERMINADO
        temporizador = null
        manejador.removeCallbacks(movimientoNivelDos)
        actualizarInterfaz()
    }

    private fun actualizarInterfaz() {
        txtTiempo.text = getString(R.string.tiempo_formato, tiempoRestante)
        txtPuntos.text = getString(R.string.puntos_formato, puntos)
        txtNivel.text = getString(R.string.nivel_formato, nivel)

        when (estadoActual) {
            Estado.LISTO -> {
                txtMensaje.text = getString(R.string.mensaje_inicio)
                txtMensaje.visibility = View.VISIBLE
                objetivo.visibility = View.GONE
                btnIniciar.text = getString(R.string.iniciar_juego)
                btnIniciar.isEnabled = true
            }
            Estado.JUGANDO -> {
                txtMensaje.visibility = View.GONE
                objetivo.visibility = View.VISIBLE
                btnIniciar.text = getString(R.string.jugando)
                btnIniciar.isEnabled = false
            }
            Estado.TERMINADO -> {
                objetivo.visibility = View.GONE
                txtMensaje.text = getString(R.string.mensaje_final, puntos, nivel)
                txtMensaje.visibility = View.VISIBLE
                btnIniciar.text = getString(R.string.volver_a_jugar)
                btnIniciar.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        temporizador?.cancel()
        temporizador = null
        manejador.removeCallbacks(movimientoNivelDos)
        super.onDestroy()
    }

    private companion object {
        const val NIVEL_UNO = 1
        const val NIVEL_DOS = 2
        const val PUNTOS_PARA_NIVEL_DOS = 30
        const val SEGUNDOS_NIVEL_UNO = 30
        const val SEGUNDOS_NIVEL_DOS = 20
        const val DURACION_NIVEL_UNO_MS = 30_000L
        const val DURACION_NIVEL_DOS_MS = 20_000L
        const val INTERVALO_TICK_MS = 1_000L
        const val INTERVALO_MOVIMIENTO_NIVEL_DOS_MS = 650L
        const val TAMANO_OBJETIVO_NIVEL_UNO_DP = 64
        const val TAMANO_OBJETIVO_NIVEL_DOS_DP = 44
    }
}
