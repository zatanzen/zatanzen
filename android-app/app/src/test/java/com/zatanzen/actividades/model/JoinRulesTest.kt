package com.zatanzen.actividades.model

import org.junit.Assert.assertEquals
import org.junit.Test

class JoinRulesTest {
    private val now = 1_000_000L

    private fun activity(
        minRating: Double = 0.0,
        excludeConflicts: Boolean = false,
        max: Int = 5,
        participants: List<String> = listOf("host"),
        endsAt: Long = now + 3_600_000,
    ) = Activity(
        id = "a", hostId = "host", title = "t", category = Category.OTRO, topic = "", description = "",
        placeName = "", location = LatLng(0.0, 0.0), startsAt = now - 1, endsAt = endsAt,
        requirements = emptyList(), minRating = minRating, excludeConflicts = excludeConflicts,
        maxParticipants = max, participantIds = participants,
    )

    private val good = UserStats(rating = 4.6, reviewCount = 5, conflictReports = 0)

    @Test fun openActivityAcceptsAnyone() =
        assertEquals(JoinCheck.Ok, checkJoin(activity(), "me", UserStats(null, 0, 0), now))

    @Test fun ratingBelowMinimumIsRejected() =
        assertEquals(JoinCheck.RatingTooLow(4.8, 4.6), checkJoin(activity(minRating = 4.8), "me", good, now))

    @Test fun unratedUserCannotJoinActivityWithMinimum() =
        assertEquals(JoinCheck.RatingTooLow(3.0, null), checkJoin(activity(minRating = 3.0), "me", UserStats(null, 0, 0), now))

    @Test fun conflictReportsRejectedWhenExcluded() =
        assertEquals(
            JoinCheck.HasConflictReports(1),
            checkJoin(activity(excludeConflicts = true), "me", good.copy(conflictReports = 1), now),
        )

    @Test fun conflictReportsAllowedWhenNotExcluded() =
        assertEquals(JoinCheck.Ok, checkJoin(activity(), "me", good.copy(conflictReports = 1), now))

    @Test fun fullActivityIsRejected() =
        assertEquals(JoinCheck.Full, checkJoin(activity(max = 1), "me", good, now))

    @Test fun endedActivityIsRejected() =
        assertEquals(JoinCheck.Ended, checkJoin(activity(endsAt = now), "me", good, now))

    @Test fun participantIsAlreadyJoined() =
        assertEquals(JoinCheck.AlreadyJoined, checkJoin(activity(participants = listOf("host", "me")), "me", good, now))

    @Test fun statsAverageStarsAndCountConflicts() {
        val reviews = listOf(
            Review("1", "a", "me", "x", "x", 5, "", false, 0),
            Review("2", "b", "me", "x", "x", 3, "", true, 0),
            Review("3", "c", "other", "x", "x", 1, "", true, 0),
        )
        assertEquals(UserStats(4.0, 2, 1), UserStats.from(reviews, "me"))
    }
}
