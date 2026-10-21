package com.example.crud

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

@Suppress("unused")
class ContactoDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ContactosDB.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "contactos"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_APELLIDO1 = "apellido1"
        const val COLUMN_APELLIDO2 = "apellido2"
        const val COLUMN_OCUPACION = "ocupacion"
        const val COLUMN_EDAD = "edad"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT,
                $COLUMN_APELLIDO1 TEXT,
                $COLUMN_APELLIDO2 TEXT,
                $COLUMN_OCUPACION TEXT,
                $COLUMN_EDAD INTEGER
            )
        """.trimIndent()
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 1. Método Insertar
    fun insertar(contacto: Contacto): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, contacto.nombre)
            put(COLUMN_APELLIDO1, contacto.apellido1)
            put(COLUMN_APELLIDO2, contacto.apellido2)
            put(COLUMN_OCUPACION, contacto.ocupacion)
            put(COLUMN_EDAD, contacto.edad)
        }
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    // 2. Método Editar / Actualizar
    fun editar(contacto: Contacto): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, contacto.nombre)
            put(COLUMN_APELLIDO1, contacto.apellido1)
            put(COLUMN_APELLIDO2, contacto.apellido2)
            put(COLUMN_OCUPACION, contacto.ocupacion)
            put(COLUMN_EDAD, contacto.edad)
        }
        val filasAfectadas = db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(contacto.id.toString()))
        db.close()
        return filasAfectadas
    }

    // 3. Método Eliminar
    fun eliminar(id: Int): Int {
        val db = writableDatabase
        val filasAfectadas = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return filasAfectadas
    }

    // 4. Método Ver Todos los Registros
    fun verTodos(): ArrayList<Contacto> {
        val lista = ArrayList<Contacto>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val contacto = Contacto(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    apellido1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO1)),
                    apellido2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO2)),
                    ocupacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCUPACION)),
                    edad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EDAD))
                )
                lista.add(contacto)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // 5. Método Ver Un Solo Registro
    fun verUno(id: Int): Contacto? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var contacto: Contacto? = null

        if (cursor.moveToFirst()) {
            contacto = Contacto(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                apellido1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO1)),
                apellido2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO2)),
                ocupacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCUPACION)),
                edad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EDAD))
            )
        }
        cursor.close()
        db.close()
        return contacto
    }
}
