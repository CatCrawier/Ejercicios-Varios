package com.example.crud

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction

class ContactoDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ContactosDB.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "contactos"
        const val TABLE_CATEGORIAS = "categorias"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_APELLIDO1 = "apellido1"
        const val COLUMN_APELLIDO2 = "apellido2"
        const val COLUMN_OCUPACION = "ocupacion"
        const val COLUMN_EDAD = "edad"
        const val COLUMN_CATEGORIA_ID = "categoria_id"
        const val COLUMN_CATEGORIA_NOMBRE = "categoria_nombre"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        crearTablaCategorias(db)
        insertarCategoriasIniciales(db)
        crearTablaContactos(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.transaction {
                crearTablaCategorias(db)
                insertarCategoriasIniciales(db)
                db.execSQL("ALTER TABLE $TABLE_NAME RENAME TO ${TABLE_NAME}_anterior")
                crearTablaContactos(db)
                db.execSQL(
                    """INSERT INTO $TABLE_NAME ($COLUMN_ID, $COLUMN_NOMBRE, $COLUMN_APELLIDO1, $COLUMN_APELLIDO2, $COLUMN_OCUPACION, $COLUMN_EDAD, $COLUMN_CATEGORIA_ID)
                       SELECT $COLUMN_ID, $COLUMN_NOMBRE, $COLUMN_APELLIDO1, $COLUMN_APELLIDO2, $COLUMN_OCUPACION, $COLUMN_EDAD, 1
                       FROM ${TABLE_NAME}_anterior"""
                )
                db.execSQL("DROP TABLE ${TABLE_NAME}_anterior")
            }
        }
    }

    fun insertar(contacto: Contacto): Long = writableDatabase.use { db ->
        db.insert(TABLE_NAME, null, valoresContacto(contacto))
    }

    fun editar(contacto: Contacto): Int = writableDatabase.use { db ->
        db.update(TABLE_NAME, valoresContacto(contacto), "$COLUMN_ID=?", arrayOf(contacto.id.toString()))
    }

    fun eliminar(id: Int): Int = writableDatabase.use { db ->
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    /** Restaura el mismo registro para la acción Deshacer del Snackbar. */
    fun restaurar(contacto: Contacto): Long = writableDatabase.use { db ->
        db.insert(TABLE_NAME, null, valoresContacto(contacto).apply { put(COLUMN_ID, contacto.id) })
    }

    fun verTodos(): ArrayList<Contacto> = consultarContactos(null)

    fun verPorCategoria(categoriaId: Int): ArrayList<Contacto> = consultarContactos(categoriaId)

    fun verUno(id: Int): Contacto? = readableDatabase.use { db ->
        db.rawQuery("$consultaBase WHERE c.$COLUMN_ID = ?", arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToFirst()) cursorAContacto(cursor) else null
        }
    }

    fun verCategorias(): List<Categoria> = readableDatabase.use { db ->
        db.rawQuery("SELECT $COLUMN_ID, $COLUMN_NOMBRE FROM $TABLE_CATEGORIAS ORDER BY $COLUMN_NOMBRE", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(Categoria(cursor.getInt(0), cursor.getString(1)))
                }
            }
        }
    }

    private fun consultarContactos(categoriaId: Int?): ArrayList<Contacto> = readableDatabase.use { db ->
        val (consulta, argumentos) = if (categoriaId == null) {
            consultaBase to null
        } else {
            "$consultaBase WHERE c.$COLUMN_CATEGORIA_ID = ?" to arrayOf(categoriaId.toString())
        }
        db.rawQuery(consulta, argumentos).use { cursor ->
            ArrayList<Contacto>().apply {
                while (cursor.moveToNext()) add(cursorAContacto(cursor))
            }
        }
    }

    private fun valoresContacto(contacto: Contacto) = ContentValues().apply {
        put(COLUMN_NOMBRE, contacto.nombre)
        put(COLUMN_APELLIDO1, contacto.apellido1)
        put(COLUMN_APELLIDO2, contacto.apellido2)
        put(COLUMN_OCUPACION, contacto.ocupacion)
        put(COLUMN_EDAD, contacto.edad)
        put(COLUMN_CATEGORIA_ID, contacto.categoriaId)
    }

    private fun cursorAContacto(cursor: Cursor) = Contacto(
        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
        apellido1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO1)),
        apellido2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO2)),
        ocupacion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCUPACION)),
        edad = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EDAD)),
        categoriaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA_ID)),
        categoriaNombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA_NOMBRE))
    )

    private fun crearTablaCategorias(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIAS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOMBRE TEXT NOT NULL UNIQUE
        )""".trimIndent())
    }

    private fun insertarCategoriasIniciales(db: SQLiteDatabase) {
        listOf("General", "Amigos", "Familia", "Trabajo").forEach { nombre ->
            db.insertWithOnConflict(TABLE_CATEGORIAS, null, ContentValues().apply {
                put(COLUMN_NOMBRE, nombre)
            }, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun crearTablaContactos(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOMBRE TEXT,
            $COLUMN_APELLIDO1 TEXT,
            $COLUMN_APELLIDO2 TEXT,
            $COLUMN_OCUPACION TEXT,
            $COLUMN_EDAD INTEGER,
            $COLUMN_CATEGORIA_ID INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY($COLUMN_CATEGORIA_ID) REFERENCES $TABLE_CATEGORIAS($COLUMN_ID)
        )""".trimIndent())
    }

    private val consultaBase = """SELECT c.*, ca.$COLUMN_NOMBRE AS $COLUMN_CATEGORIA_NOMBRE
        FROM $TABLE_NAME c
        INNER JOIN $TABLE_CATEGORIAS ca ON ca.$COLUMN_ID = c.$COLUMN_CATEGORIA_ID""".trimIndent()
}
