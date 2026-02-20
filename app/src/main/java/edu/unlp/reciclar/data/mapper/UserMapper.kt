package edu.unlp.reciclar.data.mapper

import edu.unlp.reciclar.data.local.entity.Usuario as UsuarioEntity
import edu.unlp.reciclar.domain.model.Usuario as UsuarioDomain
import edu.unlp.reciclar.data.remote.dto.UserData

/**
 * Función de extensión que convierte un DTO de la API a una Entidad de Room.
 */
fun UserData.toEntity(): UsuarioEntity {
    return UsuarioEntity(
        id = this.id,
        username = this.username,
        puntosTotales = 0,
        puntosDisponibles = 0
    )
}

fun UsuarioEntity.toDomain(): UsuarioDomain {
    return UsuarioDomain(
        id = this.id,
        username = this.username,
        puntosTotales = this.puntosTotales,
        puntosDisponibles = this.puntosDisponibles
    )
}