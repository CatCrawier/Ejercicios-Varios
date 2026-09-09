fun main() {
    var totalPersonas: Int? = null
    while (totalPersonas == null) {
        print("Ingrese la cantidad de personas encuestadas (n): ")
        val cantidadIngresada = readlnOrNull()?.trim()?.toIntOrNull()

        if (cantidadIngresada != null && cantidadIngresada >= 0) {
            totalPersonas = cantidadIngresada
        } else {
            println("Valor inválido. Ingrese un número entero mayor o igual a 0.")
        }
    }

    var votantes = 0
    var abstencionistas = 0

    val partidos = mutableMapOf(
        "Partido A" to 0,
        "Partido B" to 0,
        "Partido C" to 0,
        "Partido D" to 0,
        "Partido E" to 0
    )

    for (i in 1..totalPersonas) {
        println("\nEntrevista $i:")

        var vota: Boolean? = null
        while (vota == null) {
            print("¿Tiene intención de votar? (S/N): ")
            vota = when (readlnOrNull()?.trim()?.uppercase()) {
                "S" -> true
                "N" -> false
                else -> {
                    println("Opción inválida. Ingrese S o N.")
                    null
                }
            }
        }

        if (vota == true) {
            votantes++
            println("Partidos disponibles: ${partidos.keys.joinToString(", ")}")

            var partidoSeleccionado = false
            while (!partidoSeleccionado) {
                print("Seleccione un partido (A, B, C, D, E): ")
                val seleccion = readlnOrNull()?.trim()?.uppercase()
                val partidoClave = "Partido $seleccion"

                if (partidos.containsKey(partidoClave)) {
                    partidos[partidoClave] = partidos.getValue(partidoClave) + 1
                    partidoSeleccionado = true
                } else {
                    println("Opción inválida. Seleccione A, B, C, D o E.")
                }
            }
        } else {
            abstencionistas++
        }
    }

    println("\n--- RESULTADOS ELECCIONES ---")

    val partidosConVotos = partidos.filter { it.value > 0 }
    println("Partidos con al menos un voto: ${partidosConVotos.keys.joinToString(", ")}")

    println("\nVotos por partido:")
    partidos.forEach { (partido, votos) ->
        println("- $partido: $votos votos")
    }

    val primerLugar = partidos.maxByOrNull { it.value }
    println("\nPrimer lugar en la encuesta: ${primerLugar?.key} con ${primerLugar?.value} votos")

    val pctAbstencion = if (totalPersonas > 0) (abstencionistas.toDouble() / totalPersonas) * 100 else 0.0
    println("Porcentaje de abstención: $pctAbstencion%")

    val pctVotantes = if (totalPersonas > 0) (votantes.toDouble() / totalPersonas) * 100 else 0.0
    println("Porcentaje de personas con intención de votar: $pctVotantes%")

    println("\nPorcentaje de votos respecto a entrevistas válidas (votantes):")
    partidos.forEach { (partido, votos) ->
        val pctValidos = if (votantes > 0) (votos.toDouble() / votantes) * 100 else 0.0
        println("- $partido: $pctValidos%")
    }

    val ordenados = partidos.toList().sortedByDescending { it.second }
    println("\nPartidos ordenados de mayor a menor voto:")
    ordenados.forEach { (partido, votos) ->
        println("- $partido: $votos votos")
    }
}
