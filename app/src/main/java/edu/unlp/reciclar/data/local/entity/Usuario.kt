package edu.unlp.reciclar.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: Int,
    val username: String // mappeada en repository, viene de la api
)