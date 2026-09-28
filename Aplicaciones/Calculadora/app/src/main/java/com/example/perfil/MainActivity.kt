package com.example.perfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private val Tinta = Color(0xFF15171C)
private val Panel = Color(0xFF26282F)
private val Tecla = Color(0xFF3E3E44)
private val Pantalla = Color(0xFFE0EFF0)
private val Ambar = Color(0xFFFFB72B)
private val Morado = Color(0xFF9B7BDB)
private val Naranja = Color(0xFFEE8946)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AplicacionCalcEs() }
    }
}

private enum class Destino { CALCULADORA, MATEMATICAS, FISICA, CONVERSOR, HISTORIAL, CONFIGURACION }
private data class Calculo(val expresion: String, val resultado: String)

@Composable
fun AplicacionCalcEs() {
    var destino by remember { mutableStateOf(Destino.CALCULADORA) }
    var menuAbierto by remember { mutableStateOf(false) }
    var expresion by remember { mutableStateOf("") }
    var respuesta by remember { mutableStateOf(0.0) }
    var historial by remember { mutableStateOf(listOf<Calculo>()) }
    var tecladoCompleto by remember { mutableStateOf(true) }

    MaterialTheme(colorScheme = darkColorScheme(background = Tinta, surface = Panel, primary = Ambar)) {
        Box(Modifier.fillMaxSize().background(Tinta)) {
            when (destino) {
                Destino.CALCULADORA -> PantallaCalculadora(
                    expresion = expresion,
                    respuesta = respuesta,
                    tecladoCompleto = tecladoCompleto,
                    alAbrirMenu = { menuAbierto = true },
                    alCambiarExpresion = { expresion = it },
                    alCambiarRespuesta = { respuesta = it },
                    alGuardar = { calculo -> historial = listOf(calculo) + historial }
                )
                Destino.MATEMATICAS -> PantallaFormulas("Fórmulas matemáticas", gruposFormulasMatematicas()) { menuAbierto = true }
                Destino.FISICA -> PantallaFormulas("Fórmulas de física", gruposFormulasFisica()) { menuAbierto = true }
                Destino.CONVERSOR -> PantallaConversor { menuAbierto = true }
                Destino.HISTORIAL -> PantallaHistorial(historial) { menuAbierto = true }
                Destino.CONFIGURACION -> PantallaConfiguracion(tecladoCompleto, { tecladoCompleto = it }) { menuAbierto = true }
            }
            if (menuAbierto) {
                MenuNavegacion(
                    destinoActual = destino,
                    alSeleccionarDestino = { destino = it; menuAbierto = false },
                    alCerrar = { menuAbierto = false },
                    tecladoCompleto = tecladoCompleto,
                    alCambiarTeclado = { tecladoCompleto = it }
                )
            }
        }
    }
}

@Composable
private fun PantallaCalculadora(
    expresion: String,
    respuesta: Double,
    tecladoCompleto: Boolean,
    alAbrirMenu: () -> Unit,
    alCambiarExpresion: (String) -> Unit,
    alCambiarRespuesta: (Double) -> Unit,
    alGuardar: (Calculo) -> Unit
) {
    val resultadoEnVivo = evaluar(expresion, respuesta)
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("NORM   MATH   DECI", fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.weight(1f))
            Text("CalcES", fontWeight = FontWeight.Bold, color = Ambar)
        }
        Spacer(Modifier.height(7.dp))
        Surface(color = Pantalla, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween) {
                Text(expresion.ifBlank { "0" }, color = Color(0xFF33383B), fontSize = 25.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(resultadoEnVivo.texto, color = if (resultadoEnVivo.tieneError) MaterialTheme.colorScheme.error else Color(0xFF101719), fontSize = 36.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("☰", color = Morado, fontSize = 30.sp, modifier = Modifier.clickable(onClick = alAbrirMenu))
            Text("∑", fontSize = 28.sp)
            Text("⚙", fontSize = 27.sp)
            Text("±", fontSize = 24.sp)
            Text("DEG", modifier = Modifier.background(Tecla, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        val filas = if (tecladoCompleto) teclasCompletas() else teclasCompactas()
        Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxSize()) {
            filas.forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    fila.forEach { etiqueta ->
                        BotonCalculadora(etiqueta, Modifier.weight(1f).fillMaxHeight()) {
                            when (etiqueta) {
                                "AC" -> alCambiarExpresion("")
                                "⌫" -> alCambiarExpresion(expresion.dropLast(1))
                                "=" -> if (!resultadoEnVivo.tieneError && expresion.isNotBlank()) {
                                    alCambiarRespuesta(resultadoEnVivo.valor)
                                    alGuardar(Calculo(expresion, resultadoEnVivo.texto))
                                    alCambiarExpresion(resultadoEnVivo.texto)
                                }
                                else -> alCambiarExpresion(expresion + textoTecla(etiqueta))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonCalculadora(etiqueta: String, modifier: Modifier, alPulsar: () -> Unit) {
    val especial = etiqueta in setOf("SHIFT", "ALPHA", "AC", "⌫")
    val color = when (etiqueta) { "SHIFT" -> Ambar; "ALPHA" -> Morado; "AC", "⌫" -> Naranja; else -> Tecla }
    Button(
        onClick = alPulsar,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (especial && etiqueta == "SHIFT") Color(0xFF27200E) else Color.White),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(1.dp)
    ) { Text(etiqueta, fontSize = if (etiqueta.length > 4) 13.sp else 21.sp, maxLines = 1) }
}

private fun teclasCompletas() = listOf(
    listOf("SHIFT", "ALPHA", "◀", "▶", "MODE"),
    listOf("CALC", "∫dx", "▲", "▼", "x⁻¹"),
    listOf("x/y", "√", "x²", "xʸ", "Log"),
    listOf("(-)", "hyp", "Sin", "Cos", "Tan"),
    listOf("RCL", "ENG", "(", ")", "S⇔D"),
    listOf("7", "8", "9", "⌫", "AC"),
    listOf("4", "5", "6", "×", "÷"),
    listOf("1", "2", "3", "+", "−"),
    listOf("0", ".", "Exp", "Ans", "=")
)

private fun teclasCompactas() = listOf(
    listOf("√", "x²", "(", ")", "⌫"),
    listOf("7", "8", "9", "÷", "AC"),
    listOf("4", "5", "6", "×", "−"),
    listOf("1", "2", "3", "+", "="),
    listOf("0", ".", "Ans", "xʸ", "Sin")
)

private fun textoTecla(etiqueta: String): String = when (etiqueta) {
    "×" -> "*"; "÷" -> "/"; "−" -> "-"; "√" -> "sqrt("; "x²" -> "^2"; "xʸ" -> "^"
    "x⁻¹" -> "^-1"; "Log" -> "log("; "Sin" -> "sin("; "Cos" -> "cos("; "Tan" -> "tan("
    "(-)" -> "-"; "Ans", "RCL" -> "ans"; "Exp", "ENG" -> "E"; "hyp" -> ""; "S⇔D" -> ""
    "SHIFT", "ALPHA", "◀", "▶", "MODE", "CALC", "∫dx", "▲", "▼", "x/y" -> ""
    else -> label
}

@Composable
private fun MenuNavegacion(
    destinoActual: Destino,
    alSeleccionarDestino: (Destino) -> Unit,
    alCerrar: () -> Unit,
    tecladoCompleto: Boolean,
    alCambiarTeclado: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = alCerrar)) {
        Surface(color = Color(0xFF1C1F24), modifier = Modifier.fillMaxHeight().fillMaxWidth(0.83f).clickable { }, shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)) {
            Column(Modifier.padding(top = 54.dp, bottom = 18.dp)) {
                ElementoMenu("∑", "Fórmulas matemáticas", Destino.MATEMATICAS, destinoActual, alSeleccionarDestino)
                ElementoMenu("⚛", "Fórmulas de física", Destino.FISICA, destinoActual, alSeleccionarDestino)
                Spacer(Modifier.height(25.dp))
                ElementoMenu("⇆", "Conversor de unidades", Destino.CONVERSOR, destinoActual, alSeleccionarDestino)
                Spacer(Modifier.height(25.dp))
                ElementoMenu("▣", "Historial de cálculos", Destino.HISTORIAL, destinoActual, alSeleccionarDestino)
                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFF3B3E44))
                ElementoMenu("⚙", "Configuración", Destino.CONFIGURACION, destinoActual, alSeleccionarDestino)
                OpcionTeclado("⌨", "Teclado completo", tecladoCompleto) { alCambiarTeclado(true); alSeleccionarDestino(Destino.CALCULADORA) }
                OpcionTeclado("⌨", "Teclado compacto", !tecladoCompleto) { alCambiarTeclado(false); alSeleccionarDestino(Destino.CALCULADORA) }
            }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ElementoMenu(icono: String, titulo: String, destino: Destino, destinoActual: Destino, alSeleccionarDestino: (Destino) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { alSeleccionarDestino(destino) }.padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icono, fontSize = 27.sp, modifier = Modifier.width(48.dp))
        Text(titulo, fontSize = 18.sp, color = if (destinoActual == destino) Ambar else Color.White)
    }
}

@Composable
private fun OpcionTeclado(icono: String, titulo: String, seleccionada: Boolean, alPulsar: () -> Unit) {
    Row(Modifier.padding(horizontal = 14.dp, vertical = 4.dp).fillMaxWidth().background(if (seleccionada) Color(0xFF41365B) else Color.Transparent, RoundedCornerShape(6.dp)).clickable(onClick = alPulsar).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icono, modifier = Modifier.width(42.dp)); Text(titulo, color = if (seleccionada) Color(0xFFC6A8FF) else Color.White)
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String, alAbrirMenu: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("☰", color = Morado, fontSize = 29.sp, modifier = Modifier.clickable(onClick = alAbrirMenu))
        Spacer(Modifier.width(18.dp))
        Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PantallaFormulas(titulo: String, grupos: List<Pair<String, List<Pair<String, String>>>>, alAbrirMenu: () -> Unit) {
    var expandido by remember { mutableStateOf<String?>(grupos.firstOrNull()?.first) }
    Column(Modifier.fillMaxSize()) {
        EncabezadoSeccion(titulo, alAbrirMenu)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Selecciona una categoría para consultar sus fórmulas.", color = Color.LightGray)
            grupos.forEach { (categoria, formulas) ->
                Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth().clickable { expandido = if (expandido == categoria) null else categoria }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(categoria, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(if (expandido == categoria) "⌃" else "⌄", color = Ambar, fontSize = 24.sp)
                        }
                        if (expandido == categoria) {
                            Spacer(Modifier.height(10.dp))
                            formulas.forEach { (nombre, formula) ->
                                Text(nombre, fontWeight = FontWeight.Medium, color = Ambar)
                                Text(formula, fontSize = 16.sp, modifier = Modifier.padding(bottom = 11.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PantallaConversor(alAbrirMenu: () -> Unit) {
    val conversores = conversoresUnidades()
    var categoria by remember { mutableStateOf("Longitud") }
    var cantidad by remember { mutableStateOf("1") }
    var origen by remember { mutableStateOf("Metro") }
    var destino by remember { mutableStateOf("Kilómetro") }
    val unidades = conversores.getValue(categoria)
    val numero = cantidad.replace(',', '.').toDoubleOrNull()
    val resultado = numero?.let { it * unidades.getValue(origen) / unidades.getValue(destino) }

    Column(Modifier.fillMaxSize()) {
        EncabezadoSeccion("Conversor de unidades", alAbrirMenu)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Conversores de unidades", style = MaterialTheme.typography.titleLarge, color = Ambar)
            Selector(categoria, conversores.keys.toList()) { valor -> categoria = valor; origen = conversores.getValue(valor).keys.first(); destino = conversores.getValue(valor).keys.drop(1).firstOrNull() ?: origen }
            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Desde", color = Color.LightGray)
            Selector(origen, unidades.keys.toList()) { origen = it }
            Text("Hacia", color = Color.LightGray)
            Selector(destino, unidades.keys.toList()) { destino = it }
            Card(colors = CardDefaults.cardColors(containerColor = Pantalla), modifier = Modifier.fillMaxWidth()) {
                Text(resultado?.let(::textoNumero) ?: "Ingresa una cantidad válida", color = Color(0xFF172024), fontSize = 30.sp, modifier = Modifier.padding(20.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun Selector(actual: String, opciones: List<String>, alSeleccionar: (String) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) { Text(actual, modifier = Modifier.weight(1f), textAlign = TextAlign.Start); Text("⌄") }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { abierto = false; alSeleccionar(opcion) }) }
        }
    }
}

@Composable
private fun PantallaHistorial(historial: List<Calculo>, alAbrirMenu: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        EncabezadoSeccion("Historial de cálculos", alAbrirMenu)
        if (historial.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aún no hay cálculos guardados.\nUsa = para guardar un resultado.", textAlign = TextAlign.Center, color = Color.LightGray) }
        } else Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            historial.forEach { calculo -> Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(calculo.expresion, color = Color.LightGray); Text("= ${calculo.resultado}", fontSize = 23.sp, color = Ambar) } } }
        }
    }
}

@Composable
private fun PantallaConfiguracion(tecladoCompleto: Boolean, alCambiarTeclado: (Boolean) -> Unit, alAbrirMenu: () -> Unit) {
    var decimales by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize()) {
        EncabezadoSeccion("Configuración", alAbrirMenu)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            FilaConfiguracion("Teclado completo", "Muestra las teclas científicas") { Switch(tecladoCompleto, onCheckedChange = alCambiarTeclado) }
            FilaConfiguracion("Resultados decimales", "Conserva hasta diez decimales") { Switch(decimales, onCheckedChange = { decimales = it }) }
            FilaConfiguracion("Ángulo", "Grados (DEG)") { Text("DEG", color = Ambar, fontWeight = FontWeight.Bold) }
            FilaConfiguracion("Tema", "Oscuro") { Text("●", color = Morado) }
            Spacer(Modifier.height(8.dp))
            Text("CalcES", color = Color.LightGray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FilaConfiguracion(titulo: String, descripcion: String, contenidoFinal: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(titulo, fontWeight = FontWeight.Medium); Text(descripcion, color = Color.LightGray, fontSize = 13.sp) }
            contenidoFinal()
        }
    }
}

private data class Evaluacion(val valor: Double, val texto: String, val tieneError: Boolean)

private fun evaluar(expresion: String, respuesta: Double): Evaluacion {
    if (expresion.isBlank()) return Evaluacion(0.0, "0", false)
    return try {
        val valor = AnalizadorExpresiones(expresion, respuesta).analizar()
        if (!valor.isFinite()) throw IllegalArgumentException()
        Evaluacion(valor, textoNumero(valor), false)
    } catch (_: Exception) { Evaluacion(0.0, "—", true) }
}

private fun textoNumero(valor: Double): String {
    if (abs(valor) < 1e-12) return "0"
    return DecimalFormat("0.##########").format(valor).replace(',', '.')
}

private class AnalizadorExpresiones(entrada: String, private val respuesta: Double) {
    private val fuente = entrada.replace("×", "*").replace("÷", "/").replace("−", "-").replace("Ans", "ans").replace(" ", "")
    private var indice = 0
    fun analizar(): Double { val valor = expresion(); if (indice != fuente.length) error("expresión no válida"); return valor }
    private fun expresion(): Double { var valor = termino(); while (true) { valor = when { aceptar('+') -> valor + termino(); aceptar('-') -> valor - termino(); else -> return valor } } }
    private fun termino(): Double { var valor = potencia(); while (true) { valor = when { aceptar('*') -> valor * potencia(); aceptar('/') -> valor / potencia(); else -> return valor } } }
    private fun potencia(): Double { var valor = unario(); if (aceptar('^')) valor = valor.pow(potencia()); return valor }
    private fun unario(): Double = when { aceptar('+') -> unario(); aceptar('-') -> -unario(); else -> primario() }
    private fun primario(): Double {
        if (aceptar('(')) { val valor = expresion(); if (!aceptar(')')) error("paréntesis"); return valor }
        if (indice >= fuente.length) error("fin de la expresión")
        if (fuente[indice].isDigit() || fuente[indice] == '.') return numero()
        val nombre = buildString { while (indice < fuente.length && fuente[indice].isLetter()) append(fuente[indice++]) }
        if (nombre.isEmpty()) error("símbolo no válido")
        if (nombre == "ans") return respuesta
        if (nombre == "pi") return PI
        if (nombre == "e") return E
        if (!aceptar('(')) error("función incompleta")
        val valor = expresion(); if (!aceptar(')')) error("función incompleta")
        return when (nombre.lowercase()) { "sin" -> sin(Math.toRadians(valor)); "cos" -> cos(Math.toRadians(valor)); "tan" -> tan(Math.toRadians(valor)); "asin" -> Math.toDegrees(asin(valor)); "acos" -> Math.toDegrees(acos(valor)); "atan" -> Math.toDegrees(atan(valor)); "sqrt" -> sqrt(valor); "log" -> log10(valor); "ln" -> ln(valor); else -> error("función no reconocida") }
    }
    private fun numero(): Double {
        val inicio = indice
        while (indice < fuente.length && (fuente[indice].isDigit() || fuente[indice] == '.')) indice++
        if (indice < fuente.length && fuente[indice] == 'E') {
            indice++
            if (indice < fuente.length && (fuente[indice] == '+' || fuente[indice] == '-')) indice++
            while (indice < fuente.length && fuente[indice].isDigit()) indice++
        }
        return fuente.substring(inicio, indice).toDouble()
    }
    private fun aceptar(caracter: Char): Boolean = (indice < fuente.length && fuente[indice] == caracter).also { if (it) indice++ }
}

private fun gruposFormulasMatematicas() = listOf(
    "Geometría" to listOf("Área del círculo" to "A = πr²", "Perímetro del círculo" to "P = 2πr", "Área del triángulo" to "A = bh / 2", "Volumen del cilindro" to "V = πr²h"),
    "Álgebra" to listOf("Binomio al cuadrado" to "(a + b)² = a² + 2ab + b²", "Diferencia de cuadrados" to "a² − b² = (a − b)(a + b)", "Ecuación cuadrática" to "x = (−b ± √(b² − 4ac)) / 2a"),
    "Trigonometría" to listOf("Identidad fundamental" to "sen²x + cos²x = 1", "Ley de senos" to "a/sen A = b/sen B = c/sen C", "Ley de cosenos" to "c² = a² + b² − 2ab cos C"),
    "Ecuaciones" to listOf("Recta" to "y − y₁ = m(x − x₁)", "Pendiente" to "m = (y₂ − y₁)/(x₂ − x₁)"),
    "Geometría analítica" to listOf("Distancia" to "d = √((x₂ − x₁)² + (y₂ − y₁)²)", "Punto medio" to "M = ((x₁+x₂)/2, (y₁+y₂)/2)"),
    "Integrales" to listOf("Potencia" to "∫xⁿ dx = xⁿ⁺¹/(n+1) + C", "Exponencial" to "∫eˣ dx = eˣ + C"),
    "Matrices" to listOf("Determinante 2×2" to "det(A) = ad − bc", "Inversa 2×2" to "A⁻¹ = 1/det(A) [d −b; −c a]"),
    "Estadística" to listOf("Media" to "x̄ = Σxᵢ/n", "Desviación estándar" to "σ = √(Σ(xᵢ−x̄)²/n)"),
    "Transformaciones" to listOf("Traslación" to "(x, y) → (x+h, y+k)", "Rotación 90°" to "(x, y) → (−y, x)"),
    "Favoritos" to listOf("Añade fórmulas" to "Las fórmulas consultadas pueden marcarse como favoritas.")
)

private fun gruposFormulasFisica() = listOf(
    "Mecánica" to listOf("Velocidad" to "v = d/t", "Aceleración" to "a = Δv/Δt", "Segunda ley de Newton" to "F = ma", "Energía cinética" to "Ec = mv²/2", "Energía potencial" to "Ep = mgh"),
    "Electricidad" to listOf("Ley de Ohm" to "V = IR", "Potencia eléctrica" to "P = VI", "Carga" to "Q = It", "Campo eléctrico" to "E = F/q"),
    "Física térmica" to listOf("Calor" to "Q = mcΔT", "Gas ideal" to "PV = nRT", "Conversión" to "K = °C + 273.15"),
    "Movimientos periódicos" to listOf("Frecuencia" to "f = 1/T", "Péndulo" to "T = 2π√(L/g)", "Resorte" to "T = 2π√(m/k)"),
    "Óptica" to listOf("Índice de refracción" to "n = c/v", "Lentes" to "1/f = 1/do + 1/di"),
    "Física atómica" to listOf("Energía del fotón" to "E = hf", "Equivalencia masa-energía" to "E = mc²"),
    "Constantes" to listOf("Gravedad" to "g = 9.80665 m/s²", "Velocidad de la luz" to "c = 299 792 458 m/s", "Planck" to "h = 6.62607015×10⁻³⁴ J·s")
)

private fun conversoresUnidades() = mapOf(
    "Longitud" to linkedMapOf("Metro" to 1.0, "Kilómetro" to 0.001, "Centímetro" to 100.0, "Milla" to 0.000621371),
    "Masa" to linkedMapOf("Kilogramo" to 1.0, "Gramo" to 1000.0, "Libra" to 2.20462262, "Onza" to 35.2739619),
    "Tiempo" to linkedMapOf("Segundo" to 1.0, "Minuto" to 1.0 / 60, "Hora" to 1.0 / 3600, "Día" to 1.0 / 86400),
    "Área" to linkedMapOf("Metro cuadrado" to 1.0, "Kilómetro cuadrado" to 0.000001, "Hectárea" to 0.0001, "Pie cuadrado" to 10.7639104)
)

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun VistaPreviaCalcEs() = AplicacionCalcEs()
