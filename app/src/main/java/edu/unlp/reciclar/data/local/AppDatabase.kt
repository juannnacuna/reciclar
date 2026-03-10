package edu.unlp.reciclar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.unlp.reciclar.data.local.dao.CanjeDao
import edu.unlp.reciclar.data.local.dao.ConfiguracionDao
import edu.unlp.reciclar.data.local.dao.CuponDao
import edu.unlp.reciclar.data.local.dao.LogroDao
import edu.unlp.reciclar.data.local.dao.ReporteDao
import edu.unlp.reciclar.data.local.dao.ResiduoDao
import edu.unlp.reciclar.data.local.dao.TriviaDao
import edu.unlp.reciclar.data.local.dao.UsuarioDao
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Configuracion
import edu.unlp.reciclar.data.local.entity.Consejo
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.Reporte
import edu.unlp.reciclar.data.local.entity.Residuo
import edu.unlp.reciclar.data.local.entity.Trivia
import edu.unlp.reciclar.data.local.entity.TriviaRespuesta
import edu.unlp.reciclar.data.local.entity.Usuario
import edu.unlp.reciclar.data.local.entity.UsuarioLogros
import edu.unlp.reciclar.data.local.entity.UsuarioConPuntos

@Database(
    entities = [
        Canje::class, Cupon::class, Logro::class, Residuo::class, Reporte::class,
        Usuario::class, UsuarioLogros::class,
        Trivia::class, Consejo::class, TriviaRespuesta::class,
        Configuracion::class
    ],
    views = [UsuarioConPuntos::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun canjeDao(): CanjeDao
    abstract fun configuracionDao(): ConfiguracionDao
    abstract fun cuponDao(): CuponDao
    abstract fun logroDao(): LogroDao
    abstract fun reporteDao(): ReporteDao
    abstract fun residuoDao(): ResiduoDao
    abstract fun triviaDao(): TriviaDao
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}