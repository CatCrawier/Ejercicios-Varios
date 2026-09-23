package com.example.reservascine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reservascine.ui.theme.ReservasCineTheme
import java.util.Locale

private data class Pelicula(
    val nombre: String,
    val genero: String,
    val duracion: String,
    val clasificacion: String,
    val sinopsis: String,
    val horario: String,
    val precio: Int,
    val color: Color
)

private val peliculas = listOf(
    Pelicula("Aventura espacial", "Ciencia ficción", "2 h 05 min", "+12", "Una tripulación viaja para salvar su planeta.", "3:00 p. m. y 7:30 p. m.", 15000, Color(0xFF263E82)),
    Pelicula("La última misión", "Acción", "1 h 48 min", "+15", "Un agente debe impedir un ataque antes del amanecer.", "4:15 p. m. y 8:45 p. m.", 18000, Color(0xFF8B2635)),
    Pelicula("Risas en familia", "Comedia", "1 h 35 min", "Todo público", "Una familia convierte unas vacaciones en una gran aventura.", "2:00 p. m. y 5:30 p. m.", 12000, Color(0xFF36724A))
)

private enum class Pantalla { LOGIN, CARTELERA, COMPRA, RESUMEN }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ReservasCineTheme { AppReservas() } }
    }
}

@Composable
fun AppReservas() {
    var pantalla by remember { mutableStateOf(Pantalla.LOGIN) }
    var peliculaSeleccionada by remember { mutableStateOf<Pelicula?>(null) }
    var entradas by remember { mutableIntStateOf(1) }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        when (pantalla) {
            Pantalla.LOGIN -> LoginScreen(Modifier.padding(padding)) { pantalla = Pantalla.CARTELERA }
            Pantalla.CARTELERA -> Cartelera(Modifier.padding(padding)) { pelicula ->
                peliculaSeleccionada = pelicula
                entradas = 1
                pantalla = Pantalla.COMPRA
            }
            Pantalla.COMPRA -> peliculaSeleccionada?.let { pelicula ->
                CompraScreen(
                    pelicula = pelicula,
                    entradas = entradas,
                    modifier = Modifier.padding(padding),
                    onRestar = { if (entradas > 1) entradas-- },
                    onSumar = { if (entradas < 6) entradas++ },
                    onComprar = { pantalla = Pantalla.RESUMEN },
                    onVolver = { pantalla = Pantalla.CARTELERA }
                )
            }
            Pantalla.RESUMEN -> peliculaSeleccionada?.let { pelicula ->
                ResumenCompra(pelicula, entradas, Modifier.padding(padding)) {
                    peliculaSeleccionada = null
                    pantalla = Pantalla.CARTELERA
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onLogin: () -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarError by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎬 Reservas Cine", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(usuario, { usuario = it; mostrarError = false }, label = { Text("Usuario") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(contrasena, { contrasena = it; mostrarError = false }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            if (usuario.isNotBlank() && contrasena.isNotBlank()) onLogin() else mostrarError = true
        }) { Text("Iniciar sesión") }
        if (mostrarError) Text("Ingresa usuario y contraseña", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun Cartelera(modifier: Modifier = Modifier, onSeleccionar: (Pelicula) -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Cartelera", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Selecciona una película para comprar tus entradas.")
        peliculas.forEach { pelicula ->
            Spacer(Modifier.height(16.dp))
            PeliculaCard(pelicula, onSeleccionar)
        }
    }
}

@Composable
private fun PeliculaCard(pelicula: Pelicula, onSeleccionar: (Pelicula) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(5.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterPelicula(pelicula, Modifier.size(92.dp, 130.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(pelicula.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${pelicula.genero} · ${pelicula.duracion}")
                Text("Clasificación: ${pelicula.clasificacion}")
                Spacer(Modifier.height(6.dp))
                Text("Boleta: ${formatearPrecio(pelicula.precio)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onSeleccionar(pelicula) }) { Text("Comprar entradas") }
            }
        }
    }
}

@Composable
private fun CompraScreen(pelicula: Pelicula, entradas: Int, modifier: Modifier, onRestar: () -> Unit, onSumar: () -> Unit, onComprar: () -> Unit, onVolver: () -> Unit) {
    val total = pelicula.precio * entradas
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Button(onClick = onVolver) { Text("← Volver a cartelera") }
        Spacer(Modifier.height(14.dp))
        PosterPelicula(pelicula, Modifier.fillMaxWidth().height(180.dp))
        Spacer(Modifier.height(16.dp))
        Text(pelicula.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${pelicula.genero} · ${pelicula.duracion} · ${pelicula.clasificacion}")
        Spacer(Modifier.height(8.dp))
        Text("Sinopsis: ${pelicula.sinopsis}")
        Text("Horario disponible: ${pelicula.horario}")
        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Cantidad de entradas (máximo 6)", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Button(onClick = onRestar, enabled = entradas > 1) { Text("−") }
                    Text("$entradas", modifier = Modifier.padding(horizontal = 28.dp), style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = onSumar, enabled = entradas < 6) { Text("+") }
                }
                Spacer(Modifier.height(12.dp))
                Text("Precio por entrada: ${formatearPrecio(pelicula.precio)}")
                Text("Total: ${formatearPrecio(total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(onClick = onComprar, modifier = Modifier.fillMaxWidth()) { Text("Confirmar compra") }
    }
}

@Composable
private fun ResumenCompra(pelicula: Pelicula, entradas: Int, modifier: Modifier, onNuevaCompra: () -> Unit) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("¡Compra realizada correctamente!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Resumen de compra", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Película: ${pelicula.nombre}")
                Text("Horario: ${pelicula.horario}")
                Text("Entradas: $entradas")
                Text("Precio entrada: ${formatearPrecio(pelicula.precio)}")
                Text("Total: ${formatearPrecio(pelicula.precio * entradas)}", fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = onNuevaCompra) { Text("Comprar otra película") }
    }
}

@Composable
private fun PosterPelicula(pelicula: Pelicula, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(pelicula.color), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Imagen de ${pelicula.nombre}",
            modifier = Modifier.size(54.dp),
            contentScale = ContentScale.Fit
        )
        Text("🎬", style = MaterialTheme.typography.headlineMedium)
    }
}

private fun formatearPrecio(valor: Int): String = "$" + String.format(Locale.US, "%,d", valor).replace(',', '.')

@Preview(showBackground = true)
@Composable
private fun PreviewLogin() { ReservasCineTheme { LoginScreen(onLogin = {}) } }
