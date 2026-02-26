package edu.unlp.reciclar.data.service.strategy

import javax.inject.Inject

/** Resuelve qué [CondicionStrategy] usar según el campo "tipo" del JSON. */
class CondicionStrategyFactory @Inject constructor(
    puntosTotales: PuntosTotalesStrategy,
    residuosTipo: ResiduosTipoStrategy,
    canjesTotales: CanjesTotalesStrategy
) {
    private val strategies: Map<String, CondicionStrategy> = mapOf(
        "puntos_totales" to puntosTotales,
        "residuos_tipo" to residuosTipo,
        "canjes_totales" to canjesTotales
    )

    /**
     * Devuelve la strategy correspondiente al tipo, o null si no existe.
     */
    fun get(tipo: String): CondicionStrategy? = strategies[tipo]
}

