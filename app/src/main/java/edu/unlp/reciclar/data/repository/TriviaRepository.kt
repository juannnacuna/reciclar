package edu.unlp.reciclar.data.repository

import edu.unlp.reciclar.data.local.dao.TriviaDao
import edu.unlp.reciclar.data.local.entity.Consejo
import edu.unlp.reciclar.data.local.entity.Trivia
import edu.unlp.reciclar.data.local.entity.TriviaRespuesta

class TriviaRepository(
    private val triviaDao: TriviaDao,
    private val userRepository: UserRepository
) {

    suspend fun getSiguienteTrivia(): Trivia? {
        val userId = userRepository.getUser().getOrNull()?.id
        return if (userId != null) {
            // Primero intentar una trivia no respondida
            triviaDao.getTriviaNoRespondida(userId) ?: triviaDao.getTriviaRandom()
        } else {
            triviaDao.getTriviaRandom()
        }
    }

    suspend fun responderTrivia(trivia: Trivia, respuestaIndex: Int): Boolean {
        val userId = userRepository.getUser().getOrNull()?.id ?: return false
        val esCorrecta = respuestaIndex == trivia.respuestaCorrectaIndex

        triviaDao.insertRespuesta(
            TriviaRespuesta(
                triviaId = trivia.id,
                usuarioId = userId,
                respuestaIndex = respuestaIndex,
                esCorrecta = esCorrecta,
                timestamp = System.currentTimeMillis()
            )
        )

        return esCorrecta
    }

    suspend fun getEstadisticas(): TriviaEstadisticas {
        val userId = userRepository.getUser().getOrNull()?.id ?: return TriviaEstadisticas()
        val correctas = triviaDao.getRespuestasCorrectas(userId)
        val total = triviaDao.getTotalRespuestas(userId)
        val totalTrivias = triviaDao.getTotalTrivias()
        return TriviaEstadisticas(
            respondidas = total,
            correctas = correctas,
            totalDisponibles = totalTrivias
        )
    }

    suspend fun getConsejoRandom(): Consejo? {
        return triviaDao.getConsejoRandom()
    }
}

data class TriviaEstadisticas(
    val respondidas: Int = 0,
    val correctas: Int = 0,
    val totalDisponibles: Int = 0
)
