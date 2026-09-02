fun main() {
    fun ejercicio3(agenda: Map<String, String>) {
        println("\n╔════════════════════ AGENDA TELEFÓNICA ════════════════════╗")
        agenda.forEach { (nombre, numero) ->
            println("║  👤 ${nombre.padEnd(25)} ☎  $numero")
        }
        println("╚═══════════════════════════════════════════════════════════╝")
    }

    print("¿Cuántos contactos desea ingresar? ")
    val cantidad = readln().toInt()
    val agenda = mutableMapOf<String, String>()

    for (indice in 0 until cantidad) {
        print("Ingrese el nombre del contacto ${indice + 1}: ")
        val nombre = readln()
        print("Ingrese el número de teléfono del contacto ${indice + 1}: ")
        val numero = readln()
        agenda[nombre] = numero
    }

    ejercicio3(agenda)
}
