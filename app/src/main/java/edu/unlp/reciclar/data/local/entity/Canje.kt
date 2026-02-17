package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "canjes",
    primaryKeys = ["usuarioId", "cuponId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["localId"], childColumns = ["usuarioId"]),
        ForeignKey(entity = Cupon::class, parentColumns = ["id"], childColumns = ["cuponId"])
    ]
)
data class Canje(
    val usuarioId: String,
    val cuponId: Int,
    val fechaCanje: Long = System.currentTimeMillis(),
    val fueUsado: Boolean = false
)