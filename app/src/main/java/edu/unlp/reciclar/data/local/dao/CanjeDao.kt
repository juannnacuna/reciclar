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

    @Query("SELECT * FROM cupones")
    suspend fun getAllCanjes(): List<Canje>

    @Transaction
    @Query("SELECT * FROM canjes WHERE cuponId = :cuponId")
    fun getCanjeConDetalle(cuponId: String): Flow<CanjeConDetalle>
}