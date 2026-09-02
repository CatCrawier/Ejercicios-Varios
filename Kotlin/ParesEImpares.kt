fun main() {
    fun ejercicio1(numeros: List<Int>) {
        val pares = numeros.filter { it % 2 == 0 }
        val impares = numeros.filter { it % 2 != 0 }
        println("Pares: $pares")
        println("Impares: $impares")
    }

    print("¿Cuántos números desea ingresar? ")
    val cantidad = readln().toInt()
    val numeros = mutableListOf<Int>()

    for (indice in 0 until cantidad) {
        print("Ingrese el número ${indice + 1}: ")
        numeros.add(readln().toInt())
    }

    ejercicio1(numeros)
}
