package com.zatanzen.actividades.data

import com.zatanzen.actividades.model.Activity
import com.zatanzen.actividades.model.Category
import com.zatanzen.actividades.model.LatLng
import com.zatanzen.actividades.model.Review
import com.zatanzen.actividades.model.User

/** Sample data so the app can be tried without a server. */
object DemoData {
    const val ME = "me"

    /** Used when the device location is unknown or permission was denied. */
    val DEFAULT_LOCATION = LatLng(4.6097, -74.0817)

    private const val MIN = 60_000L
    private const val HOUR = 60 * MIN
    private const val DAY = 24 * HOUR

    val users = listOf(
        User(ME, "Tú", "Me gusta conocer gente nueva y conversar de tecnología."),
        User("u1", "Camila", "Economista. Siempre lista para un buen debate con café o cerveza."),
        User("u2", "Andrés", "Fanático del fútbol y de las noches tranquilas."),
        User("u3", "Valentina", "Organizo noches de juegos de mesa en mi casa."),
        User("u4", "Diego", "Si hay partido, hay plan."),
        User("u5", "Sofía", "Nueva en la ciudad, buscando amigos."),
        User("u6", "Mateo", "Me gusta la fiesta."),
    )

    fun reviews(now: Long): List<Review> {
        var n = 0
        fun r(from: String, to: String, act: String, title: String, stars: Int, comment: String, conflict: Boolean = false, daysAgo: Int) =
            Review("r${n++}", from, to, act, title, stars, comment, conflict, now - daysAgo * DAY)
        return listOf(
            r("u1", ME, "past1", "Trivia en el pub", 5, "Muy buena onda, llegó puntual.", daysAgo = 2),
            r("u3", ME, "past1", "Trivia en el pub", 4, "Agradable.", daysAgo = 2),
            r("u4", ME, "past2", "Partida de póker", 5, "Excelente anfitrión.", daysAgo = 5),
            r("u6", ME, "past2", "Partida de póker", 4, "Todo bien.", daysAgo = 5),
            r("u2", ME, "old", "Asado del domingo", 5, "Llevó todo lo que se pidió.", daysAgo = 20),
            r("u2", "u1", "old", "Club de lectura", 5, "Conversación muy interesante.", daysAgo = 12),
            r("u3", "u1", "past1", "Trivia en el pub", 5, "Gran anfitriona.", daysAgo = 2),
            r("u4", "u2", "old", "Final de copa", 4, "Buen compañero de partido.", daysAgo = 9),
            r("u1", "u3", "past1", "Trivia en el pub", 5, "Súper divertida.", daysAgo = 2),
            r("u5", "u3", "old", "Noche de Dixit", 5, "Muy acogedora.", daysAgo = 15),
            r("u2", "u4", "old", "Final de copa", 4, "Puntual.", daysAgo = 9),
            r("u1", "u5", "old", "Café de idiomas", 5, "Muy amable.", daysAgo = 7),
            r(ME, "u6", "past2", "Partida de póker", 2, "Discutió con otro jugador.", conflict = true, daysAgo = 5),
            r("u4", "u6", "past2", "Partida de póker", 3, "Un poco intenso.", daysAgo = 5),
        )
    }

    fun activitiesAround(c: LatLng, now: Long): List<Activity> {
        fun at(dLat: Double, dLon: Double) = LatLng(c.lat + dLat, c.lon + dLon)
        return listOf(
            Activity(
                id = "a1", hostId = "u1", title = "Charla de economía",
                category = Category.CONVERSACION, topic = "Economía e inflación",
                description = "Estoy en el bar con una cerveza. Si te interesa hablar de economía y mercados, únete.",
                placeName = "Bar La Esquina", location = at(0.004, 0.003),
                startsAt = now - 20 * MIN, endsAt = now + 100 * MIN,
                requirements = listOf("Mayor de 18 años", "Consumir al menos una bebida"),
                minRating = 4.0, excludeConflicts = true, maxParticipants = 6,
                participantIds = listOf("u1", "u2"),
            ),
            Activity(
                id = "a2", hostId = "u4", title = "Final de fútbol en el bar",
                category = Category.DEPORTES, topic = "Final del campeonato",
                description = "Pantalla grande, buena mesa reservada. ¡Vamos a ver la final!",
                placeName = "Sports Bar El Gol", location = at(-0.006, 0.005),
                startsAt = now - 30 * MIN, endsAt = now + 150 * MIN,
                requirements = listOf("Camiseta de tu equipo (opcional)"),
                minRating = 3.5, excludeConflicts = false, maxParticipants = 10,
                participantIds = listOf("u4", "u6", "u2"),
            ),
            Activity(
                id = "a3", hostId = "u3", title = "Noche de Catan",
                category = Category.JUEGOS, topic = "Catan y otros juegos de mesa",
                description = "Juego en mi casa. La dirección exacta se comparte al unirte.",
                placeName = "Casa de Valentina", location = at(0.007, -0.006),
                startsAt = now - 5 * MIN, endsAt = now + 115 * MIN,
                requirements = listOf("Trae cerveza o snacks", "Llegar antes de las 21:00"),
                minRating = 4.8, excludeConflicts = true, maxParticipants = 4,
                participantIds = listOf("u3", "u1"),
            ),
            Activity(
                id = "a4", hostId = "u5", title = "Café y conversación tranquila",
                category = Category.AMISTAD, topic = "Conocer gente nueva",
                description = "Acabo de mudarme y quiero hacer amigos. ¡Ven a tomar un café!",
                placeName = "Café Central", location = at(-0.003, -0.004),
                startsAt = now - 10 * MIN, endsAt = now + 50 * MIN,
                requirements = emptyList(),
                minRating = 0.0, excludeConflicts = false, maxParticipants = 6,
                participantIds = listOf("u5"),
            ),
            Activity(
                id = "a5", hostId = "u2", title = "Copa de vino para conocernos",
                category = Category.CITAS, topic = "Cita tranquila",
                description = "Plan romántico y relajado, conversación sin presión.",
                placeName = "Vinoteca Roble", location = at(0.001, 0.008),
                startsAt = now - 15 * MIN, endsAt = now + 105 * MIN,
                requirements = listOf("Mayor de 18 años"),
                minRating = 4.5, excludeConflicts = true, maxParticipants = 2,
                participantIds = listOf("u2"),
            ),
            // Already finished: appear only in profile histories.
            Activity(
                id = "past1", hostId = "u1", title = "Trivia en el pub",
                category = Category.JUEGOS, topic = "Cultura general",
                description = "Trivia por equipos.", placeName = "Pub Irlandés",
                location = at(0.002, 0.002),
                startsAt = now - 2 * DAY - 3 * HOUR, endsAt = now - 2 * DAY,
                requirements = emptyList(), minRating = 0.0, excludeConflicts = false,
                maxParticipants = 8, participantIds = listOf("u1", ME, "u3"),
            ),
            Activity(
                id = "past2", hostId = ME, title = "Partida de póker",
                category = Category.JUEGOS, topic = "Póker sin apuestas",
                description = "Póker con fichas, solo por diversión.", placeName = "Mi casa",
                location = at(-0.002, 0.001),
                startsAt = now - 5 * DAY - 3 * HOUR, endsAt = now - 5 * DAY,
                requirements = listOf("Trae algo para picar"), minRating = 3.0, excludeConflicts = false,
                maxParticipants = 5, participantIds = listOf(ME, "u4", "u6"),
            ),
        )
    }
}
