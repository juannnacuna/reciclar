package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion")
data class Configuracion(
    @PrimaryKey val clave: String,
    val valor: String
)

