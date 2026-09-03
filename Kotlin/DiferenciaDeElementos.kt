fun main() {
    val invitados = setOf("Ana", "Carlos", "Elena", "David", "Beatriz")
    val confirmados = setOf("Ana", "Elena", "David")

    val faltantes = invitados.subtract(confirmados)
    println("Faltaron a la reunión: $faltantes")
}