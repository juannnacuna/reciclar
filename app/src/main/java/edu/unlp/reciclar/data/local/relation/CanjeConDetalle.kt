package edu.unlp.reciclar.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Cupon

data class CanjeConDetalle(
    @Embedded val canje: Canje,
    @Relation(
        parentColumn = "cuponId",
        entityColumn = "id"
    )
    val detalle: Cupon
)