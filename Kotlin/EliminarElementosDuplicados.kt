fun main() {
    fun ejercicio2(numeros: List<Int>) {
        val numerosUnicos = numeros.toSet()
        println("Números únicos: $numerosUnicos")
    }

    print("¿Cuántos números desea ingresar? ")
    val cantidad = readln().toInt()
    val numeros = mutableListOf<Int>()

    for (indice in 0 until cantidad) {
        print("Ingrese el número ${indice + 1}: ")
        numeros.add(readln().toInt())
    }

    ejercicio2(numeros)
}