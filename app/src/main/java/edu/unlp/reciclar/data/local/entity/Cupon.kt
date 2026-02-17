package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cupones")
data class Cupon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcionHtml: String,
    val puntosNecesarios: Int, // Costo del cupón
    val vigencia: Long
)