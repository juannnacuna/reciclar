package edu.unlp.reciclar.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.Usuario
import edu.unlp.reciclar.data.local.entity.UsuarioLogros

data class UsuarioConLogros (
    @Embedded val usuario: Usuario,
    @Relation(
        entity = UsuarioLogros::class,
        parentColumn = "id",
        entityColumn = "usuarioId"
    )
    val logros: List<Logro>
)