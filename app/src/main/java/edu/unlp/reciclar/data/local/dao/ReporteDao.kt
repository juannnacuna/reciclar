package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Reporte

@Dao
interface ReporteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReporte(reporte: Reporte): Long

    @Query("SELECT * FROM reportes")
    suspend fun getAllReportes(): List<Reporte>
}