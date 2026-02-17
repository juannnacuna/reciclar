package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Cupon

@Dao
interface CuponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCupon(cupon: Cupon): Long

    @Query("SELECT * FROM cupones")
    suspend fun getAllCupones(): List<Cupon>
}