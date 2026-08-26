import java.math.BigInteger

fun main() {
    var numero = ""

    for (entrada in Secuencia {
        print("Ingrese un numero entero positivo de ocho cifras: ")
        readln().trim()
    }) {

        if (entrada.length == 8 && entrada.all { it.isDigit() } && entrada[0] != '0') {
            numero = entrada
            break
        }

        println("Error: debe ingresar exactamente ocho cifras y la primera no puede ser 0.")
    }

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
