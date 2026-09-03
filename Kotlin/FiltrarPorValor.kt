fun main(){
    val estudiantes = mapOf("Ana" to 4.5, "Carlos" to 2.8, "Elena" to 3.0, "David" to 1.5)
    val aprobados = estudiantes.filterValues { nota -> nota >= 3.0 }

    println(aprobados)
}