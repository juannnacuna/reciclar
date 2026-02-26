package edu.unlp.reciclar.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import edu.unlp.reciclar.data.local.entity.Logro

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

    val todosLosLogros = listOf(
            Logro(
                id = 1,
                nombre = "Iniciativa Verde",
                descripcion = "Obtener 200 puntos acumulados",
                condicion = """
            {
              "tipo": "puntos_totales",
              "valor": 200
            }
        """.trimIndent()
            ),
        Logro(
    id = 2,
    nombre = "Comprador compulsivo",
    descripcion = "Canjear 10 cupones",
    condicion = """
            {
              "tipo": "canjes_totales",
              "valor": 10
            }
        """.trimIndent()
    ),

    Logro(
    id = 3,
    nombre = "Rey del Plástico",
    descripcion = "Reciclar 10 residuos de plástico",
    condicion = """
            {
              "tipo": "residuos_tipo",
              "residuo": "Plastico",
              "cantidad": 10
            }
        """.trimIndent()
    )
    )

    // ── Trivias de concientización ambiental ─────────────────

    private data class TriviaSeed(
        val pregunta: String,
        val opcionA: String,
        val opcionB: String,
        val opcionC: String,
        val opcionD: String,
        val respuestaCorrectaIndex: Int,
        val explicacion: String
    )

    private val triviasSeed = listOf(
        TriviaSeed(
            pregunta = "¿Cuánto tarda en degradarse una botella de plástico?",
            opcionA = "10 años",
            opcionB = "100 años",
            opcionC = "450 años",
            opcionD = "1000 años",
            respuestaCorrectaIndex = 2,
            explicacion = "Una botella de plástico PET tarda aproximadamente 450 años en degradarse completamente en la naturaleza."
        ),
        TriviaSeed(
            pregunta = "¿Qué porcentaje del agua del planeta es dulce y accesible?",
            opcionA = "25%",
            opcionB = "10%",
            opcionC = "3%",
            opcionD = "Menos del 1%",
            respuestaCorrectaIndex = 3,
            explicacion = "Solo el 2.5% del agua del planeta es dulce, y de esa menos del 1% es accesible para consumo humano."
        ),
        TriviaSeed(
            pregunta = "¿Cuál de estos materiales NO es reciclable?",
            opcionA = "Cartón limpio",
            opcionB = "Papel de servilleta usado",
            opcionC = "Lata de aluminio",
            opcionD = "Botella de vidrio",
            respuestaCorrectaIndex = 1,
            explicacion = "El papel de servilleta usado contiene residuos orgánicos que impiden su reciclaje. Va al contenedor de residuos orgánicos o compost."
        ),
        TriviaSeed(
            pregunta = "¿Cuántos litros de agua se necesitan para producir 1 kg de carne vacuna?",
            opcionA = "500 litros",
            opcionB = "1.500 litros",
            opcionC = "5.000 litros",
            opcionD = "15.000 litros",
            respuestaCorrectaIndex = 3,
            explicacion = "La producción de 1 kg de carne vacuna requiere aproximadamente 15.000 litros de agua, considerando el riego de cultivos para alimento animal, el agua de bebida y el proceso industrial."
        ),
        TriviaSeed(
            pregunta = "¿Qué significan las 3R de la ecología?",
            opcionA = "Reciclar, Reutilizar, Recuperar",
            opcionB = "Reducir, Reutilizar, Reciclar",
            opcionC = "Reducir, Recuperar, Rechazar",
            opcionD = "Reciclar, Reducir, Rechazar",
            respuestaCorrectaIndex = 1,
            explicacion = "Las 3R son Reducir, Reutilizar y Reciclar, en ese orden de prioridad. Primero reducir el consumo, luego reutilizar lo que se pueda, y finalmente reciclar."
        ),
        TriviaSeed(
            pregunta = "¿Cuál es el gas de efecto invernadero más abundante producido por actividad humana?",
            opcionA = "Metano (CH₄)",
            opcionB = "Óxido nitroso (N₂O)",
            opcionC = "Dióxido de carbono (CO₂)",
            opcionD = "Vapor de agua",
            respuestaCorrectaIndex = 2,
            explicacion = "El CO₂ es el gas de efecto invernadero más abundante generado por actividades humanas, principalmente por la quema de combustibles fósiles."
        ),
        TriviaSeed(
            pregunta = "¿Cuánto tiempo tarda un chicle en degradarse?",
            opcionA = "1 año",
            opcionB = "5 años",
            opcionC = "25 años",
            opcionD = "No se degrada nunca",
            respuestaCorrectaIndex = 1,
            explicacion = "Un chicle tarda aproximadamente 5 años en degradarse. Su base es un polímero sintético similar al plástico."
        ),
        TriviaSeed(
            pregunta = "¿Qué tipo de energía renovable genera más electricidad a nivel mundial?",
            opcionA = "Solar",
            opcionB = "Eólica",
            opcionC = "Hidroeléctrica",
            opcionD = "Geotérmica",
            respuestaCorrectaIndex = 2,
            explicacion = "La energía hidroeléctrica es la fuente renovable que más electricidad genera a nivel mundial, representando cerca del 16% de la generación eléctrica global."
        ),
        TriviaSeed(
            pregunta = "¿Cuántas veces se puede reciclar el vidrio sin perder calidad?",
            opcionA = "5 veces",
            opcionB = "10 veces",
            opcionC = "50 veces",
            opcionD = "Infinitas veces",
            respuestaCorrectaIndex = 3,
            explicacion = "El vidrio es 100% reciclable y se puede reciclar infinitas veces sin perder calidad ni pureza, a diferencia del plástico o el papel."
        ),
        TriviaSeed(
            pregunta = "¿Qué porcentaje de la deforestación mundial se debe a la agricultura?",
            opcionA = "10%",
            opcionB = "30%",
            opcionC = "50%",
            opcionD = "Más del 70%",
            respuestaCorrectaIndex = 3,
            explicacion = "Más del 70% de la deforestación tropical se debe a la expansión agrícola, incluyendo ganadería y cultivos como soja y palma aceitera."
        ),
        TriviaSeed(
            pregunta = "¿Cuántas bolsas de plástico se usan por minuto en el mundo?",
            opcionA = "100 mil",
            opcionB = "1 millón",
            opcionC = "10 millones",
            opcionD = "100 millones",
            respuestaCorrectaIndex = 1,
            explicacion = "Se estima que se usan aproximadamente 1 millón de bolsas de plástico por minuto en todo el mundo, la mayoría con un tiempo de uso de apenas 15 minutos."
        ),
        TriviaSeed(
            pregunta = "¿Qué es el compostaje?",
            opcionA = "Quemar residuos orgánicos",
            opcionB = "Transformar residuos orgánicos en abono natural",
            opcionC = "Enterrar basura en el suelo",
            opcionD = "Separar residuos por color",
            respuestaCorrectaIndex = 1,
            explicacion = "El compostaje es un proceso biológico que transforma residuos orgánicos (restos de comida, hojas, etc.) en abono natural rico en nutrientes para el suelo."
        ),
        TriviaSeed(
            pregunta = "¿La Gran Isla de Basura del Pacífico tiene un tamaño comparable a qué país?",
            opcionA = "Uruguay",
            opcionB = "Francia",
            opcionC = "Argentina",
            opcionD = "España",
            respuestaCorrectaIndex = 1,
            explicacion = "La Gran Isla de Basura del Pacífico tiene una extensión estimada de 1.6 millones de km², comparable al territorio de Francia (tres veces su tamaño)."
        ),
        TriviaSeed(
            pregunta = "¿Cuánta energía se ahorra al reciclar una lata de aluminio?",
            opcionA = "25%",
            opcionB = "50%",
            opcionC = "75%",
            opcionD = "95%",
            respuestaCorrectaIndex = 3,
            explicacion = "Reciclar una lata de aluminio ahorra hasta un 95% de la energía necesaria para fabricar una nueva desde cero, además de reducir la minería."
        ),
        TriviaSeed(
            pregunta = "¿Cuál es la principal causa de pérdida de biodiversidad?",
            opcionA = "Contaminación del aire",
            opcionB = "Destrucción de hábitats",
            opcionC = "Cambio climático",
            opcionD = "Caza furtiva",
            respuestaCorrectaIndex = 1,
            explicacion = "La destrucción y fragmentación de hábitats naturales es la principal causa de pérdida de biodiversidad a nivel global, afectando al 85% de las especies amenazadas."
        )
    )

    // ── Consejos de concientización ambiental ────────────────

    private data class ConsejoSeed(
        val titulo: String,
        val contenido: String
    )

    private val consejosSeed = listOf(
        ConsejoSeed(
            titulo = "Llevá tu bolsa reutilizable",
            contenido = "Una bolsa de tela puede reemplazar más de 700 bolsas de plástico a lo largo de su vida útil. Llevá siempre una en tu mochila o cartera."
        ),
        ConsejoSeed(
            titulo = "Cerrá la canilla mientras te cepillás",
            contenido = "Dejar la canilla abierta mientras te lavás los dientes desperdicia hasta 12 litros de agua por minuto. En un año podés ahorrar más de 8.000 litros."
        ),
        ConsejoSeed(
            titulo = "Separá tus residuos en origen",
            contenido = "Tener dos cestos en casa (reciclable y no reciclable) facilita enormemente el proceso de reciclaje y reduce la contaminación en los rellenos sanitarios."
        ),
        ConsejoSeed(
            titulo = "Usá luz natural siempre que puedas",
            contenido = "Aprovechá la luz del sol durante el día. Además de ahorrar energía, la luz natural mejora tu estado de ánimo y productividad."
        ),
        ConsejoSeed(
            titulo = "Evitá los productos de un solo uso",
            contenido = "Cubiertos, vasos y sorbetes descartables tardan cientos de años en degradarse. Optá por alternativas reutilizables: son más económicas a largo plazo."
        ),
        ConsejoSeed(
            titulo = "Compostá tus residuos orgánicos",
            contenido = "Los restos de frutas, verduras, yerba y café pueden convertirse en abono para tus plantas. El compostaje reduce un 40% el volumen de tu basura."
        ),
        ConsejoSeed(
            titulo = "Desenchufá los aparatos que no usás",
            contenido = "Los electrodomésticos en stand-by consumen energía innecesariamente ('consumo vampiro'). Desenchufarlos puede ahorrarte hasta un 10% en tu factura de luz."
        ),
        ConsejoSeed(
            titulo = "Elegí productos con menos packaging",
            contenido = "Al hacer las compras, preferí productos con envases mínimos o reciclables. El packaging representa cerca del 40% de los residuos plásticos que generamos."
        ),
        ConsejoSeed(
            titulo = "Plantá un árbol o cuidá una planta",
            contenido = "Un solo árbol puede absorber hasta 22 kg de CO₂ al año y producir oxígeno suficiente para dos personas. Cada planta cuenta."
        ),
        ConsejoSeed(
            titulo = "Reutilizá antes de reciclar",
            contenido = "Antes de tirar algo al contenedor de reciclaje, pensá si podés darle otro uso: frascos como recipientes, remeras como trapos, cajas como organizadores."
        ),
        ConsejoSeed(
            titulo = "Duchas más cortas",
            contenido = "Reducir tu ducha de 10 a 5 minutos ahorra aproximadamente 100 litros de agua cada vez. ¡Ponete un temporizador!"
        ),
        ConsejoSeed(
            titulo = "Caminá, pedaleá o usá transporte público",
            contenido = "El transporte es responsable del 25% de las emisiones de CO₂. Elegir caminar, andar en bici o usar transporte público hace una diferencia real."
        )
    )

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Usuario de prueba
            db.execSQL(
                "INSERT INTO usuarios (id, username) " +
                "VALUES ($USER_ID, 'admin')"
            )

            todosLosLogros.forEachIndexed { index, logro ->
                db.execSQL(
                    "INSERT INTO logros (id, nombre, descripcion, condicion) " +
                    "VALUES ('${logro.id}', '${logro.nombre}', '${logro.descripcion}', '${logro.condicion}')"
                )
            }

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

            // Trivias de concientización ambiental
            triviasSeed.forEachIndexed { index, trivia ->
                val id = index + 1
                db.execSQL(
                    "INSERT INTO trivias (id, pregunta, opcionA, opcionB, opcionC, opcionD, respuestaCorrectaIndex, explicacion) " +
                    "VALUES ($id, '${trivia.pregunta.replace("'", "''")}', " +
                    "'${trivia.opcionA.replace("'", "''")}', " +
                    "'${trivia.opcionB.replace("'", "''")}', " +
                    "'${trivia.opcionC.replace("'", "''")}', " +
                    "'${trivia.opcionD.replace("'", "''")}', " +
                    "${trivia.respuestaCorrectaIndex}, " +
                    "'${trivia.explicacion.replace("'", "''")}')"
                )
            }

            // Consejos de concientización ambiental
            consejosSeed.forEachIndexed { index, consejo ->
                val id = index + 1
                db.execSQL(
                    "INSERT INTO consejos (id, titulo, contenido) " +
                    "VALUES ($id, '${consejo.titulo.replace("'", "''")}', " +
                    "'${consejo.contenido.replace("'", "''")}')"
                )
            }
        }
    }
}
