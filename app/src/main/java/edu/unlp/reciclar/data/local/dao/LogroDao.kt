package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.UsuarioLogros

@Dao
interface LogroDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogro(logro: Logro): Long

    @Query("SELECT * FROM logros")
    suspend fun getAllLogros(): List<Logro>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usuarioLogro: UsuarioLogros)

    @Query("SELECT EXISTS(SELECT 1 FROM usuario_logros WHERE usuarioId = :userId AND logroId = :logroId)")
    suspend fun yaTieneLogro(userId: Int, logroId: Int): Boolean
}