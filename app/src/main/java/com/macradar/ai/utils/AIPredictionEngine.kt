package com.macradar.ai.utils

import com.macradar.ai.data.model.*
import kotlin.math.*

object AIPredictionEngine {

    fun calculatePrediction(
        homeTeamStats: TeamSeasonStats?,
        awayTeamStats: TeamSeasonStats?,
        h2hMatches: List<FixtureResponse>?,
        homeTeamId: Int,
        awayTeamId: Int
    ): PredictionModel {

        if (homeTeamStats == null && awayTeamStats == null) {
            return getFallbackPrediction(homeTeamId, awayTeamId)
        }

        val homeForm = calculateFormScore(homeTeamStats?.form?.takeLast(5) ?: "")
        val awayForm = calculateFormScore(awayTeamStats?.form?.takeLast(5) ?: "")

        val homeAttack = calculateAttackStrength(homeTeamStats, true)
        val awayAttack = calculateAttackStrength(awayTeamStats, false)

        val homeDefense = calculateDefenseStrength(homeTeamStats, true)
        val awayDefense = calculateDefenseStrength(awayTeamStats, false)

        val homeAdvantage = 1.15

        val homeStrength = (homeForm * 0.3 + homeAttack * 0.35 + homeDefense * 0.35) * homeAdvantage
        val awayStrength = awayForm * 0.3 + awayAttack * 0.35 + awayDefense * 0.35

        val h2hFactor = calculateH2HFactor(h2hMatches, homeTeamId)

        val adjustedHomeStrength = homeStrength * (1 + h2hFactor * 0.1)
        val adjustedAwayStrength = awayStrength * (1 - h2hFactor * 0.1)

        val totalStrength = adjustedHomeStrength + adjustedAwayStrength + 0.5

        var homeWinProb = ((adjustedHomeStrength / totalStrength) * 100).toInt()
        var awayWinProb = ((adjustedAwayStrength / totalStrength) * 100).toInt()
        var drawProb = 100 - homeWinProb - awayWinProb

        val sum = homeWinProb + drawProb + awayWinProb
        if (sum != 100) {
            drawProb += (100 - sum)
        }

        homeWinProb = homeWinProb.coerceIn(5, 85)
        awayWinProb = awayWinProb.coerceIn(5, 85)
        drawProb = (100 - homeWinProb - awayWinProb).coerceIn(5, 40)

        val total = homeWinProb + drawProb + awayWinProb
        if (total != 100) {
            homeWinProb = (homeWinProb * 100.0 / total).toInt()
            awayWinProb = (awayWinProb * 100.0 / total).toInt()
            drawProb = 100 - homeWinProb - awayWinProb
        }

        val homeAvgGoals = homeTeamStats?.goals?.goalsFor?.average?.home?.toDoubleOrNull() ?: 1.5
        val awayAvgGoals = awayTeamStats?.goals?.goalsFor?.average?.away?.toDoubleOrNull() ?: 1.2
        val homeAvgConceded = homeTeamStats?.goals?.against?.average?.home?.toDoubleOrNull() ?: 1.3
        val awayAvgConceded = awayTeamStats?.goals?.against?.average?.away?.toDoubleOrNull() ?: 1.6

        val expectedHomeGoals = (homeAvgGoals + awayAvgConceded) / 2
        val expectedAwayGoals = (awayAvgGoals + homeAvgConceded) / 2
        val expectedTotalGoals = expectedHomeGoals + expectedAwayGoals

        val over25Prob = calculateOver25Probability(expectedTotalGoals)
        val under25Prob = 100 - over25Prob

        val bttsProb = calculateBTTSProbability(expectedHomeGoals, expectedAwayGoals)
        val bttsNoProb = 100 - bttsProb

        val htGoalProb = calculateHTGoalProbability(expectedTotalGoals)
        val htNoGoalProb = 100 - htGoalProb

        val maxProb = maxOf(homeWinProb, drawProb, awayWinProb)
        val confidenceScore = calculateConfidenceScore(maxProb, homeTeamStats, awayTeamStats)

        val riskLevel = when {
            confidenceScore >= 75 -> "Düşük Risk"
            confidenceScore >= 55 -> "Orta Risk"
            else -> "Yüksek Risk"
        }

        val aiComment = generateAIComment(
            homeTeamStats, awayTeamStats, homeWinProb, awayWinProb,
            confidenceScore, expectedTotalGoals
        )

        val over35Prob = calculateOver35Probability(expectedTotalGoals)

        return PredictionModel(
            matchId = 0,
            homeWinProbability = homeWinProb,
            drawProbability = drawProb,
            awayWinProbability = awayWinProb,
            confidenceScore = confidenceScore,
            riskLevel = riskLevel,
            over25Probability = over25Prob,
            under25Probability = under25Prob,
            over35Probability = over35Prob,
            under35Probability = 100 - over35Prob,
            bttsYesProbability = bttsProb,
            bttsNoProbability = bttsNoProb,
            htGoalYesProbability = htGoalProb,
            htGoalNoProbability = htNoGoalProb,
            aiComment = aiComment
        )
    }

    private fun calculateFormScore(form: String): Double {
        if (form.isEmpty()) return 50.0
        var score = 0.0
        var weight = 1.0
        form.reversed().forEach { result ->
            when (result) {
                'W' -> score += 3.0 * weight
                'D' -> score += 1.0 * weight
                'L' -> score += 0.0
            }
            weight *= 0.9
        }
        return (score / (form.length * 3.0 * weight)) * 100
    }

    private fun calculateAttackStrength(stats: TeamSeasonStats?, isHome: Boolean): Double {
        if (stats == null) return 50.0
        val avgGoals = if (isHome) {
            stats.goals.goalsFor.average.home.toDoubleOrNull() ?: 1.3
        } else {
            stats.goals.goalsFor.average.away.toDoubleOrNull() ?: 1.1
        }
        return (avgGoals / 2.0 * 100).coerceIn(20.0, 90.0)
    }

    private fun calculateDefenseStrength(stats: TeamSeasonStats?, isHome: Boolean): Double {
        if (stats == null) return 50.0
        val avgConceded = if (isHome) {
            stats.goals.against.average.home.toDoubleOrNull() ?: 1.2
        } else {
            stats.goals.against.average.away.toDoubleOrNull() ?: 1.5
        }
        return (100 - (avgConceded / 3.0 * 100)).coerceIn(20.0, 90.0)
    }

    private fun calculateH2HFactor(h2hMatches: List<FixtureResponse>?, homeTeamId: Int): Double {
        if (h2hMatches.isNullOrEmpty()) return 0.0
        var homeWins = 0
        var awayWins = 0
        h2hMatches.takeLast(6).forEach { match ->
            when {
                match.teams.home.id == homeTeamId && match.teams.home.winner == true -> homeWins++
                match.teams.away.id == homeTeamId && match.teams.away.winner == true -> homeWins++
                else -> awayWins++
            }
        }
        return ((homeWins - awayWins).toDouble() / maxOf(h2hMatches.size, 1))
    }

    private fun calculateOver25Probability(expectedGoals: Double): Int {
        return when {
            expectedGoals >= 3.5 -> 78
            expectedGoals >= 3.0 -> 70
            expectedGoals >= 2.5 -> 62
            expectedGoals >= 2.0 -> 50
            expectedGoals >= 1.5 -> 38
            else -> 28
        }
    }

    private fun calculateOver35Probability(expectedGoals: Double): Int {
        return when {
            expectedGoals >= 4.5 -> 65
            expectedGoals >= 4.0 -> 55
            expectedGoals >= 3.5 -> 45
            expectedGoals >= 3.0 -> 35
            expectedGoals >= 2.5 -> 25
            else -> 15
        }
    }

    private fun calculateBTTSProbability(homeGoals: Double, awayGoals: Double): Int {
        val homeScoreProb = (1 - exp(-homeGoals)) * 100
        val awayScoreProb = (1 - exp(-awayGoals)) * 100
        return ((homeScoreProb / 100) * (awayScoreProb / 100) * 100).toInt().coerceIn(20, 80)
    }

    private fun calculateHTGoalProbability(expectedTotalGoals: Double): Int {
        val htExpected = expectedTotalGoals * 0.45
        return when {
            htExpected >= 1.5 -> 75
            htExpected >= 1.0 -> 65
            htExpected >= 0.8 -> 58
            else -> 48
        }
    }

    private fun calculateConfidenceScore(maxProb: Int, home: TeamSeasonStats?, away: TeamSeasonStats?): Int {
        var score = maxProb
        if (home != null) score += 5
        if (away != null) score += 5
        if (maxProb < 40) score -= 10
        return score.coerceIn(45, 95)
    }

    private fun generateAIComment(
        home: TeamSeasonStats?,
        away: TeamSeasonStats?,
        homeWinProb: Int,
        awayWinProb: Int,
        confidence: Int,
        expectedGoals: Double
    ): String {
        val homeName = home?.team?.name ?: "Ev Sahibi"
        val awayName = away?.team?.name ?: "Deplasman"

        return buildString {
            when {
                homeWinProb > 50 -> append("$homeName son dönemde iç sahada çok güçlü bir performans sergiliyor. ")
                awayWinProb > 50 -> append("$awayName deplasman performansıyla dikkat çekiyor. ")
                else -> append("Bu maç oldukça dengeli görünüyor. ")
            }

            if (expectedGoals > 2.8) {
                append("Maçın gol ziyafeti şeklinde geçmesi bekleniyor. ")
            } else if (expectedGoals < 1.8) {
                append("Savunma odaklı bir mücadele bekleniyor. ")
            }

            if (confidence >= 75) {
                append("AI sistemimiz bu maç için yüksek güven skoruyla tahmin yapıyor.")
            } else if (confidence >= 60) {
                append("Orta seviyede güven ile tahmin yapılmıştır.")
            } else {
                append("Bu maçta belirsizlik faktörü yüksek, dikkatli olunmalıdır.")
            }
        }
    }

    private fun getFallbackPrediction(homeTeamId: Int, awayTeamId: Int): PredictionModel {
        val seed = abs((homeTeamId * 31 + awayTeamId * 17))
        val rng = java.util.Random(seed.toLong())

        val homeWinProb = 30 + rng.nextInt(26)
        var awayWinProb = 20 + rng.nextInt(21)
        if (homeWinProb + awayWinProb >= 95) awayWinProb = 95 - homeWinProb
        val drawProb = (100 - homeWinProb - awayWinProb).coerceIn(10, 40)

        val total = homeWinProb + drawProb + awayWinProb
        val adjHomeWin = (homeWinProb * 100.0 / total).toInt()
        val adjDraw = (drawProb * 100.0 / total).toInt()
        val adjAwayWin = 100 - adjHomeWin - adjDraw

        val over25 = 45 + rng.nextInt(31)
        val over35 = (over25 * 0.55).toInt().coerceIn(20, 55)
        val btts = 40 + rng.nextInt(31)
        val htGoal = 45 + rng.nextInt(31)

        val maxProb = maxOf(adjHomeWin, adjDraw, adjAwayWin)
        val confidence = (55 + rng.nextInt(21)).coerceIn(50, 80)

        val riskLevel = when {
            confidence >= 70 -> "Düşük Risk"
            confidence >= 58 -> "Orta Risk"
            else -> "Yüksek Risk"
        }

        return PredictionModel(
            matchId = 0,
            homeWinProbability = adjHomeWin,
            drawProbability = adjDraw,
            awayWinProbability = adjAwayWin,
            confidenceScore = confidence,
            riskLevel = riskLevel,
            over25Probability = over25,
            under25Probability = 100 - over25,
            over35Probability = over35,
            under35Probability = 100 - over35,
            bttsYesProbability = btts,
            bttsNoProbability = 100 - btts,
            htGoalYesProbability = htGoal,
            htGoalNoProbability = 100 - htGoal,
            aiComment = "Bu lig/sezon için detaylı istatistik bulunamadı. Genel form ve eğilimlere dayalı tahmindir."
        )
    }

    fun calculateAIScore(prediction: PredictionModel): Int {
        val maxProb = maxOf(prediction.homeWinProbability, prediction.drawProbability, prediction.awayWinProbability)
        return ((prediction.confidenceScore * 0.5 + maxProb * 0.5)).toInt().coerceIn(60, 99)
    }
}
