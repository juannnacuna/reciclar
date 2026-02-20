package edu.unlp.reciclar.data.mapper

import edu.unlp.reciclar.data.local.entity.Usuario
import edu.unlp.reciclar.data.remote.dto.UserData

/**
 * Función de extensión que convierte un DTO de la API a una Entidad de Room.
 */
fun UserData.toEntity(): Usuario {
    return Usuario(
        remoteId = this.id,
        username = this.username,
        puntosTotales = 0,
        puntosDisponibles = 0
    )
}