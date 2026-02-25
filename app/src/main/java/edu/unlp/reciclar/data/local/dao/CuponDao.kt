package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Cupon
import kotlinx.coroutines.flow.Flow

@Dao
interface CuponDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCupon(cupon: Cupon): Long

    @Query("SELECT * FROM cupones")
    suspend fun getAllCupones(): List<Cupon>

    @Query("SELECT * FROM cupones WHERE id NOT IN (SELECT cuponId FROM canjes WHERE usuarioId = :usuarioId)")
    fun getCuponesNoCanjeadosPorUsuario(usuarioId: Int): Flow<List<Cupon>>
}