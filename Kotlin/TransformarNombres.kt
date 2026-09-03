fun main() {
    val nombres = listOf("ana", "carlos", "elena")
    val nombresMayusculas = nombres.map { nombre ->
    nombre.replaceFirstChar { char -> char.uppercase() }
    }

    println(nombresMayusculas)
}
