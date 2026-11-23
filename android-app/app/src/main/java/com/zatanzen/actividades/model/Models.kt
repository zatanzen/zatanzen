package com.zatanzen.actividades.model

data class LatLng(val lat: Double, val lon: Double)

enum class Category(val label: String, val emoji: String, val color: Long) {
    CONVERSACION("Conversación", "💬", 0xFF3F51B5),
    DEPORTES("Ver deportes", "⚽", 0xFF2E7D32),
    JUEGOS("Juegos de mesa", "🎲", 0xFF6A1B9A),
    CITAS("Citas", "❤️", 0xFFC2185B),
    AMISTAD("Amistad", "🤝", 0xFFEF6C00),
    OTRO("Otro", "✨", 0xFF546E7A),
}

data class User(
    val id: String,
    val name: String,
    val bio: String,
)

/** An activity that shows up on the map between [startsAt] and [endsAt]. */
data class Activity(
    val id: String,
    val hostId: String,
    val title: String,
    val category: Category,
    val topic: String,
    val description: String,
    val placeName: String,
    val location: LatLng,
    val startsAt: Long,
    val endsAt: Long,
    val requirements: List<String>,
    /** Minimum average rating needed to join. 0 means anyone can join. */
    val minRating: Double,
    /** When true, people with conflict reports cannot join. */
    val excludeConflicts: Boolean,
    val maxParticipants: Int,
    /** Includes the host. */
    val participantIds: List<String>,
) {
    fun isActive(now: Long) = now in startsAt until endsAt
    fun isFull() = participantIds.size >= maxParticipants
}

data class Review(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val activityId: String,
    val activityTitle: String,
    val stars: Int,
    val comment: String,
    /** The reviewer reported a conflict with this person. */
    val conflict: Boolean,
    val createdAt: Long,
)

data class UserStats(
    /** Average stars, or null if nobody has rated this person yet. */
    val rating: Double?,
    val reviewCount: Int,
    val conflictReports: Int,
) {
    companion object {
        fun from(allReviews: List<Review>, userId: String): UserStats {
            val mine = allReviews.filter { it.toUserId == userId }
            return UserStats(
                rating = if (mine.isEmpty()) null else mine.map { it.stars }.average(),
                reviewCount = mine.size,
                conflictReports = mine.count { it.conflict },
            )
        }
    }
}

sealed interface JoinCheck {
    data object Ok : JoinCheck
    data object AlreadyJoined : JoinCheck
    data object Ended : JoinCheck
    data object Full : JoinCheck
    data class RatingTooLow(val required: Double, val actual: Double?) : JoinCheck
    data class HasConflictReports(val count: Int) : JoinCheck
}

/** Entry filter: decides whether [userId] may join [activity] right now. */
fun checkJoin(activity: Activity, userId: String, stats: UserStats, now: Long): JoinCheck = when {
    userId in activity.participantIds -> JoinCheck.AlreadyJoined
    !activity.isActive(now) -> JoinCheck.Ended
    activity.isFull() -> JoinCheck.Full
    activity.minRating > 0 && (stats.rating == null || stats.rating < activity.minRating) ->
        JoinCheck.RatingTooLow(activity.minRating, stats.rating)
    activity.excludeConflicts && stats.conflictReports > 0 ->
        JoinCheck.HasConflictReports(stats.conflictReports)
    else -> JoinCheck.Ok
}
