fun main() {
    val texto = "el gato come pescado y el perro come carne"
    val palabrasUnicas = texto.lowercase().split("\\s+".toRegex()).toSet()

    println("Palabras diferentes: ${palabrasUnicas.size}")
}