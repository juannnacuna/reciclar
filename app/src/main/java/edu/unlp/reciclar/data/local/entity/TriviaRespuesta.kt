package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trivia_respuestas",
    foreignKeys = [
        ForeignKey(
            entity = Trivia::class,
            parentColumns = ["id"],
            childColumns = ["triviaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["triviaId"]),
        Index(value = ["usuarioId"])
    ]
)
data class TriviaRespuesta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val triviaId: Int,
    val usuarioId: Int,
    val respuestaIndex: Int,
    val esCorrecta: Boolean,
    val timestamp: Long
)
