package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Configuracion

@Dao
interface ConfiguracionDao {

    @Query("SELECT valor FROM configuracion WHERE clave = :clave")
    suspend fun getValor(clave: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfiguracion(configuracion: Configuracion)
}

