package edu.unlp.reciclar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import edu.unlp.reciclar.data.local.entity.Usuario
import edu.unlp.reciclar.data.local.relation.UsuarioConCuponesCanjeados
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    @Update
    suspend fun updateUsuario(usuario: Usuario)

    @Query("SELECT * FROM usuarios")
    suspend fun getAllUsuarios(): List<Usuario>

    @Transaction
    @Query("SELECT * FROM usuarios WHERE localId = :id")
    fun getUsuarioConCanjes(id: String): Flow<UsuarioConCuponesCanjeados>
}