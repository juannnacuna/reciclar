package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "residuos",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"])
    ]
)
data class Residuo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val qrCode: String,
    val tipo: String,
    val puntos: Int
)