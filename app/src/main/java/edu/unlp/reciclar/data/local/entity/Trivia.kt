package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trivias")
data class Trivia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pregunta: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val opcionD: String,
    val respuestaCorrectaIndex: Int, // 0=A, 1=B, 2=C, 3=D
    val explicacion: String
)
