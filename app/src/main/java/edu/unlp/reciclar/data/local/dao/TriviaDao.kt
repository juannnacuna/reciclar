package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Consejo
import edu.unlp.reciclar.data.local.entity.Trivia
import edu.unlp.reciclar.data.local.entity.TriviaRespuesta

@Dao
interface TriviaDao {

    @Query(
        """
        SELECT * FROM trivias 
        WHERE id NOT IN (
            SELECT triviaId FROM trivia_respuestas WHERE usuarioId = :usuarioId
        ) 
        ORDER BY RANDOM() LIMIT 1
        """
    )
    suspend fun getTriviaNoRespondida(usuarioId: Int): Trivia?

    @Query("SELECT * FROM trivias ORDER BY RANDOM() LIMIT 1")
    suspend fun getTriviaRandom(): Trivia?

    @Query("SELECT COUNT(*) FROM trivias")
    suspend fun getTotalTrivias(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRespuesta(respuesta: TriviaRespuesta)

    @Query("SELECT COUNT(*) FROM trivia_respuestas WHERE usuarioId = :usuarioId AND esCorrecta = 1")
    suspend fun getRespuestasCorrectas(usuarioId: Int): Int

    @Query("SELECT COUNT(*) FROM trivia_respuestas WHERE usuarioId = :usuarioId")
    suspend fun getTotalRespuestas(usuarioId: Int): Int


    @Query("SELECT * FROM consejos ORDER BY RANDOM() LIMIT 1")
    suspend fun getConsejoRandom(): Consejo?

    @Query("SELECT * FROM consejos ORDER BY RANDOM() LIMIT :cantidad")
    suspend fun getConsejosRandom(cantidad: Int): List<Consejo>
}
