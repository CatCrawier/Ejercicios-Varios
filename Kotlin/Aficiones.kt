fun main() {
    val aficionesPersona1 = setOf("Lectura", "Cine", "Música", "Viajar")
    val aficionesPersona2 = setOf("Cine", "Gimnasio", "Viajar", "Cocina")

    val aficionesCompartidas = aficionesPersona1.intersect(aficionesPersona2)
    println(aficionesCompartidas)
}