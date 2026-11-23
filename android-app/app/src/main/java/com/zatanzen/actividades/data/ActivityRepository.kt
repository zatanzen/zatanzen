package com.zatanzen.actividades.data

import com.zatanzen.actividades.model.Activity
import com.zatanzen.actividades.model.Category
import com.zatanzen.actividades.model.JoinCheck
import com.zatanzen.actividades.model.LatLng
import com.zatanzen.actividades.model.Review
import com.zatanzen.actividades.model.User
import com.zatanzen.actividades.model.UserStats
import com.zatanzen.actividades.model.checkJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * In-memory store. Replace with a real backend (e.g. Firebase or Supabase)
 * so that activities are shared between phones.
 */
class ActivityRepository(val currentUserId: String = DemoData.ME) {
    private val _users = MutableStateFlow(DemoData.users.associateBy { it.id })
    val users: StateFlow<Map<String, User>> = _users.asStateFlow()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private var seeded = false

    fun seedAround(center: LatLng, now: Long) {
        if (seeded) return
        seeded = true
        _activities.value = DemoData.activitiesAround(center, now)
        _reviews.value = DemoData.reviews(now)
    }

    fun statsOf(userId: String): UserStats = UserStats.from(_reviews.value, userId)

    fun create(
        title: String,
        category: Category,
        topic: String,
        description: String,
        placeName: String,
        location: LatLng,
        durationMinutes: Int,
        requirements: List<String>,
        minRating: Double,
        excludeConflicts: Boolean,
        maxParticipants: Int,
        now: Long,
    ): Activity {
        val activity = Activity(
            id = UUID.randomUUID().toString(),
            hostId = currentUserId,
            title = title,
            category = category,
            topic = topic,
            description = description,
            placeName = placeName,
            location = location,
            startsAt = now,
            endsAt = now + durationMinutes * 60_000L,
            requirements = requirements,
            minRating = minRating,
            excludeConflicts = excludeConflicts,
            maxParticipants = maxParticipants,
            participantIds = listOf(currentUserId),
        )
        _activities.update { it + activity }
        return activity
    }

    fun join(activityId: String, now: Long): JoinCheck {
        val activity = _activities.value.find { it.id == activityId } ?: return JoinCheck.Ended
        val check = checkJoin(activity, currentUserId, statsOf(currentUserId), now)
        if (check == JoinCheck.Ok) {
            _activities.update { list ->
                list.map { if (it.id == activityId) it.copy(participantIds = it.participantIds + currentUserId) else it }
            }
        }
        return check
    }

    fun leave(activityId: String) {
        _activities.update { list ->
            list.map {
                if (it.id == activityId && it.hostId != currentUserId) {
                    it.copy(participantIds = it.participantIds - currentUserId)
                } else it
            }
        }
    }

    fun hasReviewed(toUserId: String, activityId: String): Boolean =
        _reviews.value.any { it.fromUserId == currentUserId && it.toUserId == toUserId && it.activityId == activityId }

    fun addReview(toUserId: String, activity: Activity, stars: Int, comment: String, conflict: Boolean, now: Long) {
        if (hasReviewed(toUserId, activity.id)) return
        _reviews.update {
            it + Review(
                id = UUID.randomUUID().toString(),
                fromUserId = currentUserId,
                toUserId = toUserId,
                activityId = activity.id,
                activityTitle = activity.title,
                stars = stars.coerceIn(1, 5),
                comment = comment,
                conflict = conflict,
                createdAt = now,
            )
        }
    }
}
