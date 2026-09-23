package com.example.perfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    val iniciarSesion = remember { mutableStateOf(false) }

    if (iniciarSesion.value) {
        PerfilEstudiante(modifier = modifier)
    } else {
        LoginScreen(
            modifier = modifier,
            onLogin = { iniciarSesion.value = true }
        )
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Inicio de sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextField(
            value = usuario,
            onValueChange = {
                usuario = it
                mostrarError = false
            },
            label = { Text("Usuario") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                mostrarError = false
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (usuario == "estudiante" && contrasena == "1234") {
                    onLogin()
                } else {
                    mostrarError = true
                }
            }
        ) {
            Text("Iniciar sesión")
        }
        if (mostrarError) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Usuario o contraseña incorrectos",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun PerfilEstudiante(modifier: Modifier = Modifier) {
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Perfil del estudiante",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.size(150.dp)) {
                    Image(
                        painter = painterResource(R.drawable.a8c8ca966dab8d95d560e457513beb0e8),
                        contentDescription = "Foto del estudiante",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Adrian Vasquez Perez", style = MaterialTheme.typography.titleLarge)
                Text("Técnico en Sistemas", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                DatoPerfil(icono = "✉", texto = "Correo: advasquez@cesde.net")
                DatoPerfil(icono = "☎", texto = "Teléfono: 3238141619")
                DatoPerfil(icono = "●", texto = "Ciudad: Bogotá")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { mensaje = "Perfil seleccionado" }) {
                    Text("Ver perfil")
                }
                if (mensaje.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(mensaje)
                }
            }
        }
    }
}

@Composable
fun DatoPerfil(icono: String, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icono)
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewInicioSesion() {
        MaterialTheme {
        LoginScreen(onLogin = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPerfilEstudiante() {
        MaterialTheme {
        PerfilEstudiante()
    }
}
