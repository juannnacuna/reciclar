package edu.unlp.reciclar.data.service.strategy

import javax.inject.Inject

/**
 * Factory que resuelve qué [CondicionStrategy] usar según el campo "tipo" del JSON.
 *
 * Para agregar un nuevo tipo de logro:
 *   1. Crear una clase que implemente CondicionStrategy
 *   2. Registrarla en el map de esta factory
 *
 * No hace falta tocar LogroService ni ningún when().
 */
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

