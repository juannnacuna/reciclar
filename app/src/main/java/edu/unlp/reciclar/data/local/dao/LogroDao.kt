package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Logro

@Dao
interface LogroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogro(logro: Logro): Long

    @Query("SELECT * FROM logros")
    suspend fun getAllLogros(): List<Logro>

}