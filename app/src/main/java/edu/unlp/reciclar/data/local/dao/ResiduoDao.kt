package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Residuo

@Dao
interface ResiduoDao {
    @Insert
    suspend fun insertReciclaje(residuo: Residuo): Long

    @Query("SELECT * FROM residuos " +
            "WHERE id = :id")
    suspend fun getReciclajeById(id: Int): Residuo?

    @Query("SELECT * FROM residuos " +
            "WHERE usuarioId = :usuarioId " +
            "AND (timestamp BETWEEN :startDate AND :endDate)")
    suspend fun getReciclajesByUsuario(usuarioId: Int, startDate: Long, endDate: Long): List<Residuo>

    @Query("SELECT SUM(puntos) FROM residuos " +
            "WHERE usuarioId = :usuarioId")
    suspend fun puntosTotales(usuarioId: Int): Int

    @Query("SELECT COUNT(*) FROM residuos " +
            "WHERE usuarioId = :usuarioId " +
            "AND tipo = :tipo")
    suspend fun residuosTipo(usuarioId: Int, tipo: String): Int
}


