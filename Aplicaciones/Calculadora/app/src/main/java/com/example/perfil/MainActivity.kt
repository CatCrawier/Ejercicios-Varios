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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import kotlin.math.ceil
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.random.Random

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
    var respuesta by remember { mutableDoubleStateOf(0.0) }
    var memoria by remember { mutableDoubleStateOf(0.0) }
    var preAns by remember { mutableDoubleStateOf(0.0) }
    var shift by remember { mutableStateOf(false) }
    var alpha by remember { mutableStateOf(false) }
    var historial by remember { mutableStateOf(listOf<Calculo>()) }
    var tecladoCompleto by remember { mutableStateOf(true) }

    MaterialTheme(colorScheme = darkColorScheme(background = Tinta, surface = Panel, primary = Ambar)) {
        // Surface fija el color del contenido (blanco): sin ella, los Text sin color salen negros sobre fondo oscuro.
        Surface(color = Tinta, contentColor = Color.White, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                // safeDrawingPadding: evita que el contenido quede bajo la barra de estado/navegación (edge-to-edge).
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    when (destino) {
                        Destino.CALCULADORA -> PantallaCalculadora(
                            expresion = expresion,
                            respuesta = respuesta,
                            memoria = memoria,
                            preAns = preAns,
                            shift = shift,
                            alpha = alpha,
                            tecladoCompleto = tecladoCompleto,
                            alAbrirMenu = { menuAbierto = true },
                            alCambiarExpresion = { expresion = it },
                            alCambiarRespuesta = { respuesta = it },
                            alCambiarMemoria = { memoria = it },
                            alCambiarPreAns = { preAns = it },
                            alCambiarShift = { shift = it },
                            alCambiarAlpha = { alpha = it },
                            alGuardar = { calculo -> historial = listOf(calculo) + historial }
                        )
                        Destino.MATEMATICAS -> PantallaFormulas("Fórmulas matemáticas", gruposFormulasMatematicas()) { menuAbierto = true }
                        Destino.FISICA -> PantallaFormulas("Fórmulas de física", gruposFormulasFisica()) { menuAbierto = true }
                        Destino.CONVERSOR -> PantallaConversor { menuAbierto = true }
                        Destino.HISTORIAL -> PantallaHistorial(historial) { menuAbierto = true }
                        Destino.CONFIGURACION -> PantallaConfiguracion(tecladoCompleto, { tecladoCompleto = it }) { menuAbierto = true }
                    }
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
}

@Composable
private fun PantallaCalculadora(
    expresion: String,
    respuesta: Double,
    memoria: Double,
    preAns: Double,
    shift: Boolean,
    alpha: Boolean,
    tecladoCompleto: Boolean,
    alAbrirMenu: () -> Unit,
    alCambiarExpresion: (String) -> Unit,
    alCambiarRespuesta: (Double) -> Unit,
    alCambiarMemoria: (Double) -> Unit,
    alCambiarPreAns: (Double) -> Unit,
    alCambiarShift: (Boolean) -> Unit,
    alCambiarAlpha: (Boolean) -> Unit,
    alGuardar: (Calculo) -> Unit
) {
    val resultadoEnVivo = evaluar(expresion, respuesta, memoria, preAns)
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("NORM   MATH   DECI", fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.weight(1f))
            if (shift) Text("S", color = Color(0xFF27200E), fontSize = 12.sp, modifier = Modifier.background(Ambar, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp))
            if (alpha) Text("A", color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Morado, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp))
            Spacer(Modifier.width(8.dp))
            Text("CalcES", fontWeight = FontWeight.Bold, color = Ambar)
        }
        Spacer(Modifier.height(7.dp))
        Surface(color = Pantalla, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.SpaceBetween) {
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
            Text(if (shift) "SHIFT" else if (alpha) "ALPHA" else "DEG", color = if (shift) Color(0xFF27200E) else Color.White,
                modifier = Modifier.background(if (shift) Ambar else if (alpha) Morado else Tecla, RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 4.dp))
        }
        Spacer(Modifier.height(6.dp))
        val filas = if (tecladoCompleto) teclasCompletas() else teclasCompactas()
        Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxSize()) {
            filas.forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                    fila.forEach { etiqueta ->
                        BotonCalculadora(etiqueta, leyendasShift[etiqueta], leyendasAlpha[etiqueta], Modifier.weight(1f).fillMaxHeight()) {
                            when (val accion = accionEspecial(etiqueta, shift, alpha)) {
                                ":SHIFT" -> { alCambiarShift(!shift); alCambiarAlpha(false) }
                                ":ALPHA" -> { alCambiarAlpha(!alpha); alCambiarShift(false) }
                                ":AC" -> { alCambiarExpresion(""); alCambiarShift(false); alCambiarAlpha(false) }
                                ":BORRAR" -> { alCambiarExpresion(expresion.dropLast(1)); alCambiarShift(false); alCambiarAlpha(false) }
                                ":IGUAL" -> {
                                    if (!resultadoEnVivo.tieneError && expresion.isNotBlank()) {
                                        alCambiarPreAns(respuesta)
                                        alCambiarRespuesta(resultadoEnVivo.valor)
                                        alGuardar(Calculo(expresion, resultadoEnVivo.texto))
                                        alCambiarExpresion(resultadoEnVivo.texto)
                                    }
                                    alCambiarShift(false)
                                    alCambiarAlpha(false)
                                }
                                ":M+" -> { alCambiarMemoria(memoria + resultadoEnVivo.valor); alCambiarShift(false); alCambiarAlpha(false) }
                                ":M-" -> { alCambiarMemoria(memoria - resultadoEnVivo.valor); alCambiarShift(false); alCambiarAlpha(false) }
                                ":STO" -> { alCambiarMemoria(resultadoEnVivo.valor); alCambiarShift(false); alCambiarAlpha(false) }
                                ":RCL" -> { alCambiarExpresion(expresion + textoNumero(memoria)); alCambiarShift(false); alCambiarAlpha(false) }
                                ":LIMPIAR" -> { alCambiarExpresion(""); alCambiarShift(false); alCambiarAlpha(false) }
                                ":HISTORIAL", ":MENU" -> { alAbrirMenu(); alCambiarShift(false); alCambiarAlpha(false) }
                                else -> {
                                    val insertar = accion ?: textoTecla(etiqueta)
                                    if (insertar.isNotEmpty()) alCambiarExpresion(expresion + insertar)
                                    alCambiarShift(false)
                                    alCambiarAlpha(false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonCalculadora(etiqueta: String, leyendaShift: String?, leyendaAlpha: String?, modifier: Modifier, alPulsar: () -> Unit) {
    val color = when (etiqueta) { "SHIFT" -> Ambar; "ALPHA" -> Morado; "AC", "⌫" -> Naranja; else -> Tecla }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leyendaShift.orEmpty(), color = Ambar, fontSize = 9.sp, maxLines = 1)
            Text(leyendaAlpha.orEmpty(), color = Morado, fontSize = 9.sp, maxLines = 1)
        }
        Button(
            onClick = alPulsar,
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (etiqueta == "SHIFT") Color(0xFF27200E) else Color.White),
            contentPadding = PaddingValues(1.dp)
        ) { Text(etiqueta, fontSize = if (etiqueta.length > 4) 13.sp else 21.sp, maxLines = 1) }
    }
}

private fun teclasCompletas() = listOf(
    listOf("SHIFT", "ALPHA", "◀", "▶", "MODE", "2nd"),
    listOf("CALC", "∫dx", "▲", "▼", "x⁻¹", "Logₓy"),
    listOf("x/y", "√x", "x²", "xʸ", "Log", "Ln"),
    listOf("(-)", "°' \"", "hyp", "Sin", "Cos", "Tan"),
    listOf("RCL", "ENG", "(", ")", "S⇔D", "M+"),
    listOf("7", "8", "9", "⌫", "AC"),
    listOf("4", "5", "6", "×", "÷"),
    listOf("1", "2", "3", "+", "−"),
    listOf("0", ".", "Exp", "Ans", "=")
)

private fun teclasCompactas() = listOf(
    listOf("√x", "x²", "(", ")", "⌫"),
    listOf("7", "8", "9", "÷", "AC"),
    listOf("4", "5", "6", "×", "−"),
    listOf("1", "2", "3", "+", "="),
    listOf("0", ".", "Ans", "xʸ", "Sin")
)

private val leyendasShift = mapOf(
    "CALC" to "SOLVE", "∫dx" to "d/dx", "▲" to "x!", "▼" to "Σ",
    "x/y" to "x/y", "√x" to "³√x", "x²" to "x³", "xʸ" to "ʸ√x", "Log" to "10ˣ", "Ln" to "eˣ",
    "(-)" to "STO", "°' \"" to "i", "hyp" to "%", "Sin" to "\"", "Cos" to "x/y", "Tan" to "M-",
    "RCL" to "CONST", "ENG" to "SI", ")" to "∞", "M+" to "",
    "7" to "MATRIX", "8" to "VECTOR", "9" to "FUNC", "⌫" to "nPr", "AC" to "CLR ALL",
    "4" to "STAT", "5" to "CMPLX", "6" to "DISTR", "×" to "Pol", "÷" to "Rec",
    "1" to "COPY", "2" to "Ran#", "3" to "π", "−" to "History"
)

private val leyendasAlpha = mapOf(
    "CALC" to "=", "▼" to "∏", "x/y" to "÷R", "√x" to "mod", "Ln" to "t",
    "(-)" to "CLRv", "°' \"" to "Cot", "hyp" to "Cot⁻¹", "Sin" to "x", "Cos" to "y", "Tan" to "m",
    "RCL" to "CONV", "ENG" to "Limit",
    "9" to "HELP", "⌫" to "GCD", "AC" to "LCM", "×" to "Ceil", "÷" to "Floor",
    "1" to "PASTE", "2" to "RanInt", "3" to "e", "+" to "PreAns"
)

private val insercionesShift = mapOf(
    "▲" to "!", "x/y" to "/", "√x" to "cbrt(", "x²" to "^3", "xʸ" to "root(",
    "Log" to "10^(", "Ln" to "e^(", "(-)" to ":STO", "hyp" to "%", "Cos" to "/", "Tan" to ":M-",
    "⌫" to ":BORRAR", "AC" to ":AC", "×" to "pol(", "÷" to "rec(", "2" to "rand(", "3" to "pi",
    "−" to ":HISTORIAL"
)

private val insercionesAlpha = mapOf(
    "x/y" to "mod", "√x" to "mod", "(-)" to ":LIMPIAR", "°' \"" to "cot(", "hyp" to "acot(",
    "Tan" to "m", "⌫" to "gcd(", "AC" to "lcm(", "×" to "ceil(", "÷" to "floor(",
    "2" to "randint(", "3" to "e", "+" to "preans"
)

private fun accionEspecial(etiqueta: String, shift: Boolean, alpha: Boolean): String? {
    if (etiqueta == "SHIFT") return ":SHIFT"
    if (etiqueta == "ALPHA") return ":ALPHA"
    val mapeada = when {
        shift -> insercionesShift[etiqueta]
        alpha -> insercionesAlpha[etiqueta]
        else -> null
    }
    if (!mapeada.isNullOrEmpty()) return mapeada
    return when (etiqueta) {
        "AC" -> ":AC"
        "⌫" -> ":BORRAR"
        "=" -> ":IGUAL"
        "RCL" -> ":RCL"
        "M+" -> ":M+"
        "MODE", "2nd" -> ":MENU"
        else -> null
    }
}

private fun textoTecla(etiqueta: String): String = when (etiqueta) {
    "×" -> "*"; "÷" -> "/"; "−" -> "-"; "√x", "√" -> "sqrt("; "x²" -> "^2"; "xʸ" -> "^"
    "x⁻¹" -> "^-1"; "Log", "Logₓy" -> "log("; "Ln" -> "ln("
    "Sin" -> "sin("; "Cos" -> "cos("; "Tan" -> "tan("
    "(-)" -> "-"; "Ans" -> "ans"; "Exp", "ENG" -> "E"; "x/y" -> "/"
    "hyp", "S⇔D", "RCL", "M+", "CALC", "∫dx", "◀", "▶", "MODE", "2nd", "°' \"" -> ""
    else -> etiqueta
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
                OpcionTeclado("Teclado completo", tecladoCompleto) { alCambiarTeclado(true); alSeleccionarDestino(Destino.CALCULADORA) }
                OpcionTeclado("Teclado compacto", !tecladoCompleto) { alCambiarTeclado(false); alSeleccionarDestino(Destino.CALCULADORA) }
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
private fun OpcionTeclado(titulo: String, seleccionada: Boolean, alPulsar: () -> Unit) {
    Row(Modifier.padding(horizontal = 14.dp, vertical = 4.dp).fillMaxWidth().background(if (seleccionada) Color(0xFF41365B) else Color.Transparent, RoundedCornerShape(6.dp)).clickable(onClick = alPulsar).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("⌨", modifier = Modifier.width(42.dp)); Text(titulo, color = if (seleccionada) Color(0xFFC6A8FF) else Color.White)
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
    var expandido by remember { mutableStateOf(grupos.firstOrNull()?.first) }
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
    val resultado = numero?.let { it / unidades.getValue(origen) * unidades.getValue(destino) }

    Column(Modifier.fillMaxSize()) {
        EncabezadoSeccion("Conversor de unidades", alAbrirMenu)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Conversores de unidades", style = MaterialTheme.typography.titleLarge, color = Ambar)
            Selector(categoria, conversores.keys.toList()) { valor ->
                val nombres = conversores.getValue(valor).keys.toList()
                categoria = valor
                origen = nombres.first()
                destino = nombres.getOrElse(1) { nombres.first() }
            }
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

private fun evaluar(expresion: String, respuesta: Double, memoria: Double, preAns: Double): Evaluacion {
    if (expresion.isBlank()) return Evaluacion(0.0, "0", false)
    return try {
        val valor = AnalizadorExpresiones(expresion, respuesta, memoria, preAns).analizar()
        if (valor.isFinite()) Evaluacion(valor, textoNumero(valor), false) else Evaluacion(0.0, "", true)
    } catch (_: Exception) { Evaluacion(0.0, "", true) }
}

private fun textoNumero(valor: Double): String {
    if (abs(valor) < 1e-12) return "0"
    return DecimalFormat("0.##########").format(valor).replace(',', '.')
}

private class AnalizadorExpresiones(entrada: String, private val respuesta: Double, private val memoria: Double, private val preAns: Double) {
    private val fuente = entrada.replace("×", "*").replace("÷", "/").replace("−", "-").replace("Ans", "ans").replace("π", "pi").replace(" ", "")
    private var indice = 0
    private var profundidad = 0

    fun analizar(): Double {
        val valor = expresion()
        if (indice != fuente.length) error("expresión no válida")
        return valor
    }

    private fun expresion(): Double {
        var valor = termino()
        while (true) valor = when {
            aceptar('+') -> valor + termino()
            aceptar('-') -> valor - termino()
            else -> return valor
        }
    }

    private fun termino(): Double {
        var valor = unario()
        while (true) valor = when {
            aceptar('*') -> valor * unario()
            aceptar('/') -> valor / unario()
            aceptarModulo() -> modulo(valor, unario())
            iniciaOperando() -> valor * unario() // multiplicación implícita: 2π, 3sin(30), 2(3)
            else -> return valor
        }
    }

    private fun iniciaOperando(): Boolean =
        indice < fuente.length && (fuente[indice] == '(' || fuente[indice] == '.' || fuente[indice].isLetterOrDigit())

    // El menos unario tiene menor precedencia que ^, así -2^2 = -(2^2) = -4
    private fun unario(): Double = when {
        aceptar('+') -> unario()
        aceptar('-') -> -unario()
        else -> potencia()
    }

    private fun potencia(): Double {
        val base = posfijo()
        return if (aceptar('^')) base.pow(unario()) else base
    }

    private fun posfijo(): Double {
        var valor = primario()
        while (indice < fuente.length) valor = when (fuente[indice]) {
            '!' -> { indice++; factorial(valor) }
            '%' -> { indice++; valor / 100.0 }
            else -> return valor
        }
        return valor
    }

    private fun primario(): Double {
        if (profundidad >= 200) error("expresión demasiado profunda")
        profundidad++
        try {
            if (aceptar('(')) { val valor = expresion(); cerrarParentesis(); return valor }
            if (indice >= fuente.length) error("fin de la expresión")
            if (fuente[indice].isDigit() || fuente[indice] == '.') return numero()
            val nombre = buildString { while (indice < fuente.length && fuente[indice].isLetter()) append(fuente[indice++]) }.lowercase()
            if (nombre.isEmpty()) error("símbolo no válido")
            when (nombre) { "ans" -> return respuesta; "preans" -> return preAns; "m" -> return memoria; "pi" -> return PI; "e" -> return E }
            if (!aceptar('(')) error("función incompleta")
            val argumentos = mutableListOf<Double>()
            if (!aceptar(')')) {
                argumentos.add(expresion())
                while (aceptar(',')) argumentos.add(expresion())
                cerrarParentesis()
            }
            return aplicarFuncion(nombre, argumentos)
        } finally { profundidad-- }
    }

    private fun aplicarFuncion(nombre: String, a: List<Double>): Double = when (nombre) {
        "sin" -> sin(Math.toRadians(a[0])); "cos" -> cos(Math.toRadians(a[0])); "tan" -> tan(Math.toRadians(a[0]))
        "asin" -> Math.toDegrees(asin(a[0])); "acos" -> Math.toDegrees(acos(a[0])); "atan" -> Math.toDegrees(atan(a[0]))
        "sinh" -> sinh(a[0]); "cosh" -> cosh(a[0]); "tanh" -> tanh(a[0])
        "sqrt" -> sqrt(a[0]); "cbrt" -> cbrt(a[0])
        "root" -> if (a.size >= 2) a[1].pow(1.0 / a[0]) else cbrt(a[0])
        "log" -> log10(a[0]); "ln" -> ln(a[0]); "exp" -> exp(a[0])
        "cot" -> 1.0 / tan(Math.toRadians(a[0])); "acot" -> Math.toDegrees(atan(1.0 / a[0]))
        "abs" -> abs(a[0]); "ceil" -> ceil(a[0]); "floor" -> floor(a[0]); "round" -> round(a[0])
        "hypot", "pol" -> hypot(a[0], a[1])
        "rec" -> a[0] * cos(Math.toRadians(a[1]))
        "gcd" -> mcd(a[0], a[1])
        "lcm" -> abs(a[0] * a[1]) / mcd(a[0], a[1])
        "rand" -> if (a.isEmpty()) Random.nextDouble() else Random.nextDouble() * a[0]
        "randint" -> {
            val inicio = ceil(a[0]).toInt(); val fin = floor(a[1]).toInt()
            if (fin < inicio) error("rango no válido")
            (inicio + Random.nextInt(fin - inicio + 1)).toDouble()
        }
        else -> error("función no reconocida")
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) error("factorial no válido")
        var resultado = 1.0
        var i = 2.0
        while (i <= n) { resultado *= i; i += 1.0 }
        return resultado
    }

    private fun mcd(x: Double, y: Double): Double {
        var a = abs(x.toLong()); var b = abs(y.toLong())
        while (b != 0L) { val t = b; b = a % b; a = t }
        return a.toDouble()
    }

    private fun aceptarModulo(): Boolean {
        if (fuente.startsWith("mod", indice)) { indice += "mod".length; return true }
        return false
    }

    private fun modulo(a: Double, b: Double): Double {
        val resto = a % b
        return if (resto != 0.0 && (resto < 0) != (b < 0)) resto + b else resto
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
    // Al final de la entrada los paréntesis abiertos se cierran solos: "sin(30" se evalúa como "sin(30)", como en una calculadora real.
    private fun cerrarParentesis() { if (!aceptar(')') && indice < fuente.length) error("paréntesis") }

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
