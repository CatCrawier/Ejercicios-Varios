import kotlin.random.Random

fun main() {
    val totalPersonas = 100

    var soloBlanca = 0
    var soloNegra = 0
    var ambas = 0
    var ninguna = 0
    var encuestaAutomatica = false

    for (intento in 1..Int.MAX_VALUE) {
        println("--- ENCUESTA DE CHOCOLATINAS ---")
        println("1. Realizar encuesta manualmente")
        println("2. Realizar encuesta automáticamente")
        print("Seleccione una opción (1-2): ")

        when (readlnOrNull()?.toIntOrNull()) {
            1 -> break
            2 -> {
                encuestaAutomatica = true
                break
            }
            else -> println("Opción no válida. Intente de nuevo.\n")
        }
    }

    for (i in 1..totalPersonas) {
        val opcion = if (encuestaAutomatica) {
            Random.nextInt(1, 5)
        } else {
            var opcionSeleccionada = 0

            for (intento in 1..Int.MAX_VALUE) {
                println("\nPersona $i:")
                println("1. Chocolatina Blanca")
                println("2. Chocolatina Negra")
                println("3. Ambas chocolatinas")
                println("4. Ninguna de las dos")
                print("Seleccione su opción (1-4): ")

                opcionSeleccionada = readlnOrNull()?.toIntOrNull() ?: 0

                if (opcionSeleccionada !in 1..4) {
                    println("Opción no válida. Intente de nuevo.")
                } else {
                    break
                }
            }

            opcionSeleccionada
        }

        when (opcion) {
            1 -> soloBlanca++
            2 -> soloNegra++
            3 -> ambas++
            4 -> ninguna++
        }
    }

    val pctBlanca = (soloBlanca.toDouble() / totalPersonas) * 100
    val pctNegra = (soloNegra.toDouble() / totalPersonas) * 100
    val pctAmbas = (ambas.toDouble() / totalPersonas) * 100
    val pctNinguna = (ninguna.toDouble() / totalPersonas) * 100

    println("\n--- RESULTADOS ---")
    println("1. Solo Chocolatina Blanca: $soloBlanca personas")
    println("2. Solo Chocolatina Negra: $soloNegra personas")
    println("3. Ambas chocolatinas: $ambas personas")
    println("4. Ninguna chocolatina: $ninguna personas")
    println("5. Porcentaje Solo Blanca: $pctBlanca%")
    println("6. Porcentaje Solo Negra: $pctNegra%")
    println("7. Porcentaje Ambas: $pctAmbas%")
    println("8. Porcentaje Ninguna: $pctNinguna%")
}
