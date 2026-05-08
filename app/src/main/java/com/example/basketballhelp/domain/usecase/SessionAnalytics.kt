package com.example.basketballhelp.domain.usecase

import com.example.basketballhelp.domain.model.Goal
import com.example.basketballhelp.domain.model.Session
import kotlin.math.roundToInt

enum class TrendDirection { IMPROVING, DECLINING, FLAT }
enum class RecommendationTone { SUCCESS, WARNING, FOCUS, NEUTRAL }

data class RecommendationCard(
    val title: String,
    val body: String,
    val tone: RecommendationTone,
)

data class GoalProgress(
    val goal: Goal,
    val currentValue: Float,
    val progress: Float,
)

object SessionAnalytics {
    fun percent(made: Int, attempted: Int): Int {
        if (attempted == 0) return 0
        return ((made.toFloat() / attempted.toFloat()) * 100f).roundToInt()
    }

    fun developmentScore(session: Session): Int {
        val freeThrowPct = percent(session.freeThrowsMade, session.freeThrowsAttempted)
        val spotPct = percent(session.spotShootingMade, session.spotShootingAttempted)
        val score =
            (session.leftHandControl / 10f) * 100f * 0.20f +
            (session.formShooting / 10f) * 100f * 0.20f +
            freeThrowPct * 0.15f +
            spotPct * 0.15f +
            (session.footwork / 10f) * 100f * 0.15f +
            (session.stopPopSpeed / 10f) * 100f * 0.10f +
            (session.confidence / 10f) * 100f * 0.05f
        return score.roundToInt().coerceIn(0, 100)
    }

    fun trend(values: List<Float>): TrendDirection {
        if (values.size < 2) return TrendDirection.FLAT
        val averageDiff = values.zipWithNext().map { (a, b) -> b - a }.average().toFloat()
        return when {
            averageDiff > 0.3f -> TrendDirection.IMPROVING
            averageDiff < -0.3f -> TrendDirection.DECLINING
            else -> TrendDirection.FLAT
        }
    }

    fun averageForWindow(sessions: List<Session>, days: Long, selector: (Session) -> Float): Float {
        if (sessions.isEmpty()) return 0f
        val latestDate = sessions.maxByOrNull { it.sessionDate }?.sessionDate ?: return 0f
        val filtered = sessions.filter { session ->
            val diff = java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.parse(session.sessionDate),
                java.time.LocalDate.parse(latestDate),
            )
            diff in 0..days
        }
        if (filtered.isEmpty()) return 0f
        return filtered.map(selector).average().toFloat()
    }

    fun goalProgress(goal: Goal, sessions: List<Session>): GoalProgress {
        val latest = sessions.maxByOrNull { it.sessionDate }
        val currentValue = latest?.metricValue(goal.metricName) ?: goal.baselineValue
        val range = (goal.targetValue - goal.baselineValue).takeIf { it != 0f } ?: 1f
        val progress = ((currentValue - goal.baselineValue) / range).coerceIn(0f, 1f)
        return GoalProgress(goal, currentValue, progress)
    }

    fun aggregateShooting(sessions: List<Session>): Map<String, Int> {
        val freeMade = sessions.sumOf { it.freeThrowsMade }
        val freeAttempted = sessions.sumOf { it.freeThrowsAttempted }
        val spotMade = sessions.sumOf { it.spotShootingMade }
        val spotAttempted = sessions.sumOf { it.spotShootingAttempted }
        val closeMade = sessions.sumOf { it.closeRangeMade }
        val closeAttempted = sessions.sumOf { it.closeRangeAttempted }
        val overallMade = freeMade + spotMade + closeMade
        val overallAttempted = freeAttempted + spotAttempted + closeAttempted
        return mapOf(
            "Free Throws %" to percent(freeMade, freeAttempted),
            "Spot Shots %" to percent(spotMade, spotAttempted),
            "Close Range %" to percent(closeMade, closeAttempted),
            "Overall Shooting %" to percent(overallMade, overallAttempted),
        )
    }

    fun recommendations(sessions: List<Session>): List<RecommendationCard> {
        val latest = sessions.maxByOrNull { it.sessionDate } ?: return listOf(
            RecommendationCard("Solid Session — Stay the Course", "Log a session to unlock personalized coaching recommendations.", RecommendationTone.NEUTRAL),
        )

        val cards = buildList {
            if (latest.leftHandControl < 5f) add(RecommendationCard("Double Weak-Hand Reps", "Left-hand control is still below the target zone. Add extra weak-hand volume before game-speed work.", RecommendationTone.FOCUS))
            if (latest.guideHand < 6f) add(RecommendationCard("Guide Hand Isolation Work", "Keep the guide hand quiet to clean up release mechanics.", RecommendationTone.FOCUS))
            if (percent(latest.freeThrowsMade, latest.freeThrowsAttempted) < 50) add(RecommendationCard("Free Throw Focus", "Free throw efficiency is below 50%. Slow the routine down and own the line.", RecommendationTone.WARNING))
            if (percent(latest.spotShootingMade, latest.spotShootingAttempted) < 40) add(RecommendationCard("Build Range Gradually", "Spot shooting percentage is still low. Stay closer until form holds up.", RecommendationTone.WARNING))
            if (latest.stopPopSpeed < 5f) add(RecommendationCard("No-Ball Footwork First", "Clean up deceleration and balance before adding the ball back in.", RecommendationTone.FOCUS))

            val leftHandStreak = consecutiveImprovementCount(sessions) { it.leftHandControl.toDouble() }
            if (leftHandStreak >= 3) add(RecommendationCard("Weak-Hand Breakthrough", "Left-hand control has improved in three straight sessions. Keep the same discipline.", RecommendationTone.SUCCESS))

            val freeThrowMadeStreak = consecutiveImprovementCount(sessions) { it.freeThrowsMade.toDouble() }
            if (freeThrowMadeStreak >= 3) add(RecommendationCard("Free Throw Momentum", "Free throw makes are trending up across three sessions. Stay consistent with routine and pace.", RecommendationTone.SUCCESS))

            val formDecline = consecutiveDeclineCount(sessions) { it.formShooting.toDouble() }
            if (formDecline >= 2) add(RecommendationCard("Form Shooting Reset", "Form shooting has slipped for two straight sessions. Rebuild with one-hand makes close to the rim.", RecommendationTone.WARNING))
        }

        return cards.ifEmpty {
            listOf(RecommendationCard("Solid Session — Stay the Course", "The latest work stayed on track. Keep stacking solid reps.", RecommendationTone.NEUTRAL))
        }
    }

    private fun consecutiveImprovementCount(sessions: List<Session>, selector: (Session) -> Double): Int {
        val sorted = sessions.sortedBy { it.sessionDate }
        var count = 0
        for (index in 1 until sorted.size) {
            if (selector(sorted[index]) > selector(sorted[index - 1])) {
                count += 1
            } else {
                count = 0
            }
        }
        return count
    }

    private fun consecutiveDeclineCount(sessions: List<Session>, selector: (Session) -> Double): Int {
        val sorted = sessions.sortedBy { it.sessionDate }
        var count = 0
        for (index in 1 until sorted.size) {
            if (selector(sorted[index]) < selector(sorted[index - 1])) {
                count += 1
            } else {
                count = 0
            }
        }
        return count
    }

    private fun Session.metricValue(metricName: String): Float = when (metricName) {
        "leftHandControl" -> leftHandControl
        "formShooting" -> formShooting
        "freeThrowPct" -> percent(freeThrowsMade, freeThrowsAttempted).toFloat()
        "spotShootingPct" -> percent(spotShootingMade, spotShootingAttempted).toFloat()
        "footwork" -> footwork
        "stopPopSpeed" -> stopPopSpeed
        else -> 0f
    }
}
