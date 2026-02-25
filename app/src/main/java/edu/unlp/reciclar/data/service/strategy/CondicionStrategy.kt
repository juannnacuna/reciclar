package edu.unlp.reciclar.data.service.strategy

import org.json.JSONObject

/**
 * Strategy para evaluar si un usuario cumple una condición de logro.
 *
 * Cada tipo de condición (puntos_totales, residuos_tipo, canjes_totales, etc.)
 * implementa esta interfaz. Esto permite agregar nuevos tipos de logro
 * sin modificar LogroService ni un when() creciente.
 */
interface CondicionStrategy {
    suspend fun cumple(usuarioId: Int, params: JSONObject): Boolean
}

