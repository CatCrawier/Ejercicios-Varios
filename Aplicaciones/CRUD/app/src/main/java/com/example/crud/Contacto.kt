package com.example.crud

@Suppress("unused")
class Contacto {
    var id: Int = 0
    var nombre: String = ""
    var apellido1: String = ""
    var apellido2: String = ""
    var ocupacion: String = ""
    var edad: Int = 0
    var categoriaId: Int = 1
    var categoriaNombre: String = ""

    // Constructor sin parámetros
    constructor()

    // Constructor con todos los parámetros
    constructor(id: Int, nombre: String, apellido1: String, apellido2: String, ocupacion: String, edad: Int, categoriaId: Int, categoriaNombre: String) {
        this.id = id
        this.nombre = nombre
        this.apellido1 = apellido1
        this.apellido2 = apellido2
        this.ocupacion = ocupacion
        this.edad = edad
        this.categoriaId = categoriaId
        this.categoriaNombre = categoriaNombre
    }

    // Constructor auxiliar para inserciones (sin ID auto-incremental)
    constructor(nombre: String, apellido1: String, apellido2: String, ocupacion: String, edad: Int, categoriaId: Int) {
        this.nombre = nombre
        this.apellido1 = apellido1
        this.apellido2 = apellido2
        this.ocupacion = ocupacion
        this.edad = edad
        this.categoriaId = categoriaId
    }
}
