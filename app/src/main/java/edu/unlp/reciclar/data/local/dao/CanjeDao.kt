package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.relation.CanjeConDetalle
import kotlinx.coroutines.flow.Flow

@Dao
interface CanjeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCanje(canje: Canje): Long

    @Query("SELECT * FROM canjes")
    suspend fun getAllCanjes(): List<Canje>

    @Transaction
    @Query("SELECT * FROM canjes WHERE cuponId = :cuponId")
    fun getCanjeConDetalle(cuponId: String): Flow<CanjeConDetalle>

    @Transaction
    @Query("SELECT * FROM canjes WHERE usuarioId = :usuarioId AND fechaCanje BETWEEN :desde AND :hasta")
    suspend fun getCanjesConDetallePorUsuarioYPeriodo(usuarioId: Int, desde: Long, hasta: Long): List<CanjeConDetalle>

    @Query("SELECT COUNT(*) FROM canjes WHERE usuarioId = :usuarioId")
    fun totalCanjes(usuarioId: Int): Int
}
