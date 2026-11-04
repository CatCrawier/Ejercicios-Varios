package com.example.pacmandowngrade

import android.os.Bundle
import androidx.activity.ComponentActivity

class ActividadPrincipal : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(VistaJuegoPacman(this))
    }
}
