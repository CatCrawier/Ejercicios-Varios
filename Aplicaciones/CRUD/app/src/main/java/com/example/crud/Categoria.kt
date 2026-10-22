package com.example.crud

data class Categoria(val id: Int, val nombre: String) {
    override fun toString(): String = nombre
}
