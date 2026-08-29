fun main() {
    
    data class Producto(val nombre: String, val cantidad: Int, val precio: Int)
    
    // Creacion de lista mutable de productos con sus respectivas cantidades
    
    val inventario = mutableListOf(
        "Manzana" to 10,
        "Pera" to 20,
        "Naranja" to 30,
        "Uva" to 40,
        "Sandia" to 50,
        "Melon" to 60,
        "Fresa" to 70,
        "Piña" to 80,
        "Mango" to 90,
        "Banana" to 100,
    )
    
    // Creacion de lista mutable de precios
    
    val precios = mutableListOf<Int>(2000, 20000, 5000, 3000, 10000, 4500, 9000, 6000, 4000, 500)
    
    // Agregar un producto
    
    inventario.add("Coco" to 110)
    precios.add(5400)
    
    val productosConPrecio = inventario.zip(precios) { producto, precio ->
        Producto(producto.first, producto.second, precio)
    }

    println("Productos en orden ascendente:")
    productosConPrecio
        .sortedBy { it.nombre }
        .forEach {
            println("El nombre del producto es ${it.nombre}, la cantidad es ${it.cantidad} y el precio es ${it.precio}")
        }

    println("\nProductos en orden descendente:")
    productosConPrecio
        .sortedByDescending { it.nombre }
        .forEach {
            println("El nombre del producto es ${it.nombre}, la cantidad es ${it.cantidad} y el precio es ${it.precio}")
        }
    
}
