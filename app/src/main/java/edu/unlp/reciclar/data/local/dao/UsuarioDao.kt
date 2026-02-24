package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import edu.unlp.reciclar.data.local.entity.Usuario
import edu.unlp.reciclar.data.local.entity.UsuarioConPuntos
import edu.unlp.reciclar.data.local.relation.UsuarioConCuponesCanjeados
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    // Upsert utilizado para no pisar la seed de desarrollo de la db.
    @Upsert
    suspend fun insertUsuario(usuario: Usuario)

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getUsuarioById(id: String): Usuario?

    // Los puntos se calculan dinámicamente desde la vista usuarios_con_puntos.
    @Query("SELECT * FROM usuarios_con_puntos WHERE id = :id")
    suspend fun getUsuarioConPuntosById(id: String): UsuarioConPuntos?

    @Query("SELECT * FROM usuarios")
    suspend fun getAllUsuarios(): List<Usuario>

    @Transaction
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getUsuarioConCanjes(id: String): Flow<UsuarioConCuponesCanjeados>
}