package com.example.juego

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private enum class Estado {
        LISTO, JUGANDO, TERMINADO
    }

    private var estadoActual = Estado.LISTO
    private var puntos = 0
    private var tiempoRestante = SEGUNDOS_DE_JUEGO
    private var temporizador: CountDownTimer? = null

    private lateinit var txtTiempo: TextView
    private lateinit var txtPuntos: TextView
    private lateinit var txtMensaje: TextView
    private lateinit var objetivo: TextView
    private lateinit var zonaJuego: FrameLayout
    private lateinit var btnIniciar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTiempo = findViewById(R.id.txtTiempo)
        txtPuntos = findViewById(R.id.txtPuntos)
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
        puntos = 0
        tiempoRestante = SEGUNDOS_DE_JUEGO
        estadoActual = Estado.JUGANDO
        actualizarInterfaz()

        // La zona necesita estar medida antes de calcular una posición válida.
        zonaJuego.post {
            if (estadoActual == Estado.JUGANDO) {
                moverObjetivo()
                iniciarTemporizador()
            }
        }
    }

    private fun iniciarTemporizador() {
        temporizador?.cancel()
        temporizador = object : CountDownTimer(DURACION_JUEGO_MS, INTERVALO_TICK_MS) {
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
        actualizarInterfaz()
        moverObjetivo()
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
        actualizarInterfaz()
    }

    private fun actualizarInterfaz() {
        txtTiempo.text = getString(R.string.tiempo_formato, tiempoRestante)
        txtPuntos.text = getString(R.string.puntos_formato, puntos)

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
                txtMensaje.text = getString(R.string.mensaje_final, puntos)
                txtMensaje.visibility = View.VISIBLE
                btnIniciar.text = getString(R.string.volver_a_jugar)
                btnIniciar.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        temporizador?.cancel()
        temporizador = null
        super.onDestroy()
    }

    private companion object {
        const val SEGUNDOS_DE_JUEGO = 30
        const val DURACION_JUEGO_MS = 30_000L
        const val INTERVALO_TICK_MS = 1_000L
    }
}
