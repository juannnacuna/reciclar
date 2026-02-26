package edu.unlp.reciclar.data.service.strategy

import org.json.JSONObject

/** Strategy para evaluar si un usuario cumple una condición de logro. */
interface CondicionStrategy {
    suspend fun cumple(usuarioId: Int, params: JSONObject): Boolean
}

