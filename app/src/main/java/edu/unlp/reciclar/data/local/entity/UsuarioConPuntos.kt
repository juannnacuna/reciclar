package edu.unlp.reciclar.data.local.entity

import androidx.room.DatabaseView

/**
 * Vista calculada de Room.
 * No almacena puntos — los calcula en SQL cada vez que se consulta:
 *  - puntosTotales   = SUM de puntos de todos los residuos reciclados por el usuario.
 *  - puntosDisponibles = puntosTotales − puntos gastados en canjes (cupon.puntosNecesarios).
 */
@DatabaseView(
    viewName = "usuarios_con_puntos",
    value = """
        SELECT
            u.id,
            u.username,
            COALESCE(SUM(r.puntos), 0) AS puntosTotales,
            COALESCE(SUM(r.puntos), 0) - COALESCE(
                (SELECT SUM(c.puntosNecesarios)
                 FROM canjes ca
                 JOIN cupones c ON ca.cuponId = c.id
                 WHERE CAST(ca.usuarioId AS INTEGER) = u.id),
                0
            ) AS puntosDisponibles
        FROM usuarios u
        LEFT JOIN residuos r ON r.usuarioId = u.id
        GROUP BY u.id
    """
)
data class UsuarioConPuntos(
    val id: Int,
    val username: String,
    val puntosTotales: Int,
    val puntosDisponibles: Int
)
