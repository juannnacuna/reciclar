package edu.unlp.reciclar.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeder de datos de prueba para desarrollo.
 *
 * Se ejecuta una única vez cuando la base de datos se crea por primera vez (onCreate).
 * Asigna al usuario de ID 1: 52 reciclajes distribuidos en los últimos
 * 60 días y con una distribución variada de tipos para aprovechar la visualización
 * de estadísticas.
 *
 * Credenciales de prueba (proporcionadas por la API):
 *   usuario: admin
 *   contraseña: admin
 */
object DatabaseSeeder {

    const val USER_ID = 1

    /**
     * Distribución de los 52 reciclajes:
     *   Papel    → 14 entradas × 10 pts  = 140 pts
     *   Plástico → 13 entradas × 20 pts  = 260 pts
     *   Cartón   → 11 entradas × 15 pts  = 165 pts
     *   Vidrio   →  8 entradas × 30 pts  = 240 pts
     *   Metal    →  6 entradas × 50 pts  = 300 pts
     *   ─────────────────────────────────────────
     *   Total    → 52 entradas           = 1105 pts totales / 320 pts disponibles
     *
     * Actividad decreciente hacia el pasado para simular adopción del hábito:
     *   días  1–14 → 22 reciclajes (muy activo)
     *   días 15–30 → 18 reciclajes (sostenido)
     *   días 31–60 → 12 reciclajes (inicio del hábito)
     */
    private data class SeedEntry(val daysAgo: Int, val tipo: String, val puntos: Int)

    private val residuosEntries = listOf(
        // ── Semana 1 (días 1–7): muy activo ──────────────────────────────────
        SeedEntry(1,  "Plastico", 20),
        SeedEntry(1,  "Papel",    10),
        SeedEntry(2,  "Carton",   15),
        SeedEntry(3,  "Papel",    10),
        SeedEntry(3,  "Metal",    50),
        SeedEntry(4,  "Plastico", 20),
        SeedEntry(4,  "Vidrio",   30),
        SeedEntry(5,  "Papel",    10),
        SeedEntry(6,  "Carton",   15),
        SeedEntry(6,  "Plastico", 20),
        SeedEntry(7,  "Vidrio",   30),
        // ── Semana 2 (días 8–14): activo ─────────────────────────────────────
        SeedEntry(8,  "Papel",    10),
        SeedEntry(8,  "Carton",   15),
        SeedEntry(9,  "Metal",    50),
        SeedEntry(9,  "Plastico", 20),
        SeedEntry(10, "Papel",    10),
        SeedEntry(11, "Vidrio",   30),
        SeedEntry(11, "Carton",   15),
        SeedEntry(12, "Plastico", 20),
        SeedEntry(13, "Papel",    10),
        SeedEntry(14, "Metal",    50),
        SeedEntry(14, "Carton",   15),
        // ── Semanas 3–4 (días 15–30): sostenido ──────────────────────────────
        SeedEntry(15, "Plastico", 20),
        SeedEntry(16, "Papel",    10),
        SeedEntry(16, "Plastico", 20),
        SeedEntry(17, "Carton",   15),
        SeedEntry(18, "Vidrio",   30),
        SeedEntry(19, "Plastico", 20),
        SeedEntry(20, "Papel",    10),
        SeedEntry(20, "Carton",   15),
        SeedEntry(22, "Plastico", 20),
        SeedEntry(23, "Vidrio",   30),
        SeedEntry(24, "Metal",    50),
        SeedEntry(24, "Papel",    10),
        SeedEntry(26, "Carton",   15),
        SeedEntry(26, "Plastico", 20),
        SeedEntry(28, "Vidrio",   30),
        SeedEntry(30, "Papel",    10),
        SeedEntry(30, "Carton",   15),
        SeedEntry(31, "Plastico", 20),
        // ── Mes 2 (días 32–60): inicio del hábito ────────────────────────────
        SeedEntry(33, "Papel",    10),
        SeedEntry(36, "Metal",    50),
        SeedEntry(39, "Vidrio",   30),
        SeedEntry(39, "Papel",    10),
        SeedEntry(42, "Carton",   15),
        SeedEntry(45, "Papel",    10),
        SeedEntry(48, "Vidrio",   30),
        SeedEntry(51, "Plastico", 20),
        SeedEntry(51, "Papel",    10),
        SeedEntry(55, "Carton",   15),
        SeedEntry(58, "Metal",    50),
        SeedEntry(60, "Papel",    10)
    )

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Usuario de prueba
            db.execSQL(
                "INSERT INTO usuarios (id, username) " +
                "VALUES ($USER_ID, 'admin')"
            )

            // Residuos distribuidos en el tiempo
            // (strftime('%s','now') - N * 86400) * 1000  ≡  timestamp en ms hace N días
            residuosEntries.forEachIndexed { index, entry ->
                val ts = "(CAST(strftime('%s','now') AS INTEGER) - ${entry.daysAgo} * 86400) * 1000"
                db.execSQL(
                    "INSERT INTO residuos (usuarioId, timestamp, qrCode, tipo, puntos) " +
                    "VALUES ($USER_ID, $ts, 'SEED_${String.format("%03d", index + 1)}', '${entry.tipo}', ${entry.puntos})"
                )
            }

            // Cupones de prueba
            db.execSQL("""
                INSERT INTO cupones (id, nombre, descripcion, puntosNecesarios, vigencia)
                VALUES
                (1, 'Café gratis', 'Café gratis en buffet de la facultad', 200,
                    (CAST(strftime('%s','now') AS INTEGER) + 30 * 86400) * 1000),
            
                (2, 'Descuento fotocopias', '10% de descuento en fotocopias', 150,
                    (CAST(strftime('%s','now') AS INTEGER) + 45 * 86400) * 1000),
            
                (3, 'Sticker eco', 'Sticker exclusivo reciclador', 80,
                    (CAST(strftime('%s','now') AS INTEGER) + 60 * 86400) * 1000),
                    
                (4, 'Tele 65pulgadas 4k', 'Si lo podes canjear es porque reciclaste el equivalente al seamse de Punta Lara', 9999999999,
                    (CAST(strftime('%s','now') AS INTEGER) + 60 * 86400) * 1000)
            """)

            // Canjes de prueba
            db.execSQL("""
                INSERT INTO canjes (usuarioId, cuponId, fechaCanje, fueUsado)
                VALUES
                ($USER_ID, 1, (CAST(strftime('%s','now') AS INTEGER) - 2 * 86400) * 1000, 1),
                ($USER_ID, 2, (CAST(strftime('%s','now') AS INTEGER) - 5 * 86400) * 1000, 0)
            """)
        }
    }
}
