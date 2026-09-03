fun main() {
    val texto = "Programación"
    println(contarLetras(texto))
}

fun contarLetras(texto: String): Map<Char, Int> {
    return texto.filter { it.isLetter() }
                .lowercase()
                .groupingBy { it }
                .eachCount()
}