fun main (){
    val inventario = mapOf("manzana" to 1500, "pan" to 8000, "leche" to 1200)

fun calcularTotal(listaCompras: List<String>, precios: Map<String, Int>): Int {
    return listaCompras.sumOf { producto -> precios[producto] ?: 0 }
}

val compra = listOf("manzana", "leche", "manzana")
    println("Total a pagar: ${calcularTotal(compra, inventario)}")
}