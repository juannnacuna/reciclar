package edu.unlp.reciclar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.unlp.reciclar.data.local.entity.Canje
import edu.unlp.reciclar.data.local.entity.Cupon
import edu.unlp.reciclar.data.local.entity.Logro
import edu.unlp.reciclar.data.local.entity.Reporte
import edu.unlp.reciclar.data.local.entity.Usuario

@Database(entities = [Canje::class, Cupon::class, Logro::class, Reporte::class, Usuario::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {


    //
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