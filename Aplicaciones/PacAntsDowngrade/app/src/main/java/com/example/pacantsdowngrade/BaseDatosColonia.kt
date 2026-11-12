package com.example.pacantsdowngrade

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "partida_colonia")
data class PartidaColonia(
    @field:PrimaryKey val identificador: Int = 1,
    val estadoJson: String,
)

@Dao
interface PartidaColoniaDao {
    @Query("SELECT * FROM partida_colonia ORDER BY identificador")
    suspend fun cargarPartidas(): List<PartidaColonia>

    @Query("SELECT * FROM partida_colonia WHERE identificador = :identificador")
    suspend fun cargarPartida(identificador: Int): PartidaColonia?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPartida(partida: PartidaColonia)

    @Query("DELETE FROM partida_colonia WHERE identificador = :identificador")
    suspend fun borrarPartida(identificador: Int)
}

@Database(entities = [PartidaColonia::class], version = 1, exportSchema = false)
abstract class BaseDatosColonia : RoomDatabase() {
    abstract fun partidaColoniaDao(): PartidaColoniaDao

    companion object {
        @Volatile private var instancia: BaseDatosColonia? = null

        fun obtener(contexto: Context): BaseDatosColonia = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                contexto.applicationContext,
                BaseDatosColonia::class.java,
                "colonia_pacants_v2.db",
            ).build().also { instancia = it }
        }
    }
}
