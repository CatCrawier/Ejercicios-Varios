fun main() {
    
    var consumo: Double
    var cuenta: Int
    var estrato: Int
    var tipo: Int
    var subsidio: Double
    
    do {
        println("Digite el numero de la cuenta: ")
        cuenta = readln().toInt()
    } while (cuenta < 100 || cuenta > 999)
    
    do {
        println("Digite el estrato socioeconomico: ")
        estrato = readln().toInt()
    } while (estrato < 1 || estrato > 6)
    
    do {
        println("Digite el tipo de servicio: ")
        println("1. Residencial")
        println("2. Comercial")
        println("3. Industrial")
        tipo = readln().toInt()
    } while (tipo < 1 || tipo > 3)
    
    do {
        println("Cantidad de kilovatios consumidos: ")
        consumo = readln().toDouble()
    } while (consumo < 0.0)
    
    // Calcular el costo de la factura segun el consumo (condicionales anidadas)
    
    /*if (consumo <= 150) {
        val costo = consumo * 320.45
    } else if (consumo > 150 && consumo <= 250) {
        val costo = consumo * 450.75
    } else if (consumo > 250 && consumo <= 350) {
        val costo = consumo * 501.65
    } else {
        val costo = consumo * 600.50
    } */
        
    // Calcular el costo de la factura segun el consumo (when)
    
    val costo = when (consumo) {
        in 0.0..150.0 -> consumo * 320.45
        in 151.0..250.0 -> consumo * 450.75
        in 251.0..350.0 -> consumo * 501.65
        else -> consumo * 600.50
    }
    
    val cargoFijo = when (estrato) {
        1 -> 13550.0
        2 -> 14280.0
        3 -> 15485.0
        4 -> 17090.0
        5 -> 19895.0
        6 -> 22530.0
        else -> error("Estrato no válido")
    }
    val total = cargoFijo + costo
    
    subsidio = when (tipo) {
        1 -> total * 0.3
        2 -> total * 0.27
        3 -> total * 0.18
        else -> 0.0
    }
    
    val totalPagar = total - subsidio
    println("El costo de la factura es: $totalPagar")
}
