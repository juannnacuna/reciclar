package edu.unlp.reciclar.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.unlp.reciclar.data.remote.dto.PuntosUsuario

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val remoteId: Int, // mappeada en repository, viene de la api
    val username: String, // mappeada en repository, viene de la api
    val puntosTotales: Int,
    val puntosDisponibles: Int
)