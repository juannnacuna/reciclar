package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "usuario_logros",
    primaryKeys = ["usuarioId", "logroId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["usuarioId"]),
        ForeignKey(entity = Cupon::class, parentColumns = ["id"], childColumns = ["logroId"])
    ]
)
data class UsuarioLogros(
    val usuarioId: Int,
    val logroId: Int,
    val fechaObtencion: Long = System.currentTimeMillis()
)