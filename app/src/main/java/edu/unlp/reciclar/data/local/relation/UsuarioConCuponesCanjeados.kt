package edu.unlp.reciclar.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Usuario

data class UsuarioConCuponesCanjeados(
    @Embedded val usuario: Usuario,
    @Relation(
        entity = Canje::class,
        parentColumn = "id",
        entityColumn = "usuarioId"
    )
    val cuponesCanjeados: List<CanjeConDetalle>
)