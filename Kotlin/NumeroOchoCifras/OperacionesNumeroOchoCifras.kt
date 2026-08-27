import java.math.BigInteger

fun main() {
    var numero = ""

    do {
        print("Ingrese un numero entero positivo de ocho cifras: ")
        val entrada = readln().trim()
        val esValido = entrada.length == 8 && entrada.all { it.isDigit() } && entrada[0] != '0'

        if (esValido) {
            numero = entrada
        } else {
            println("Error: debe ingresar exactamente ocho cifras y la primera no puede ser 0.")
        }
    } while (!esValido)

    val digitos = numero.map { it.digitToInt() }

    val sumaCentro = digitos[3] + digitos[4]
    val multiplicacionExtremos = digitos[0] * digitos[7]
    val resta = digitos[6] - digitos[1]
    val potencia = BigInteger.valueOf(digitos[2].toLong()).pow(digitos[5])

    println("\na. Suma: $sumaCentro (${digitos[3]} + ${digitos[4]})")
    println("b. Multiplicación: $multiplicacionExtremos (${digitos[0]} * ${digitos[7]})")
    println("c. Resta: $resta (${digitos[6]} - ${digitos[1]})")
    println("d. Potencia: $potencia (${digitos[2]} ^ ${digitos[5]})")
}
