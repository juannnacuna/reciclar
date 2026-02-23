package edu.unlp.reciclar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.CuponDao
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.dao.ReporteDao
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.dao.UsuarioDao
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.Reporte
import edu.unlp.reciclar.data.local.entity.Residuo
import edu.unlp.reciclar.data.local.entity.Usuario

@Database(entities = [Canje::class, Cupon::class, Logro::class, Residuo::class, Reporte::class, Usuario::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun canjeDao(): CanjeDao
    abstract fun cuponDao(): CuponDao
    abstract fun logroDao(): LogroDao
    abstract fun reporteDao(): ReporteDao
    abstract fun residuoDao(): ResiduoDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}