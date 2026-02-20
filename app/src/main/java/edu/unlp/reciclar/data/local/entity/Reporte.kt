package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reportes",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ]
)
data class Reporte(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val qrCode: String,
    val photoPath: String,  //se guarda el path a la foto
    val tipoSugerido: String,
    val descripcion: String
)