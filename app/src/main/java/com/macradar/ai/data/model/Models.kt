package com.macradar.ai.data.model

import com.google.gson.annotations.SerializedName

// ===== FOOTBALL API MODELS =====

data class ApiResponse<T>(
    @SerializedName("response") val response: List<T>,
    @SerializedName("results") val results: Int,
    @SerializedName("errors") val errors: Any?,
    @SerializedName("paging") val paging: Paging?
)

data class Paging(
    @SerializedName("current") val current: Int,
    @SerializedName("total") val total: Int
)

data class FixtureResponse(
    @SerializedName("fixture") val fixture: Fixture,
    @SerializedName("league") val league: League,
    @SerializedName("teams") val teams: Teams,
    @SerializedName("goals") val goals: Goals,
    @SerializedName("score") val score: Score,
    @SerializedName("statistics") val statistics: List<TeamStats>? = null,
    @SerializedName("events") val events: List<MatchEvent>? = null,
    @SerializedName("lineups") val lineups: List<Lineup>? = null
)

data class Fixture(
    @SerializedName("id") val id: Int,
    @SerializedName("referee") val referee: String?,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("date") val date: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("status") val status: FixtureStatus,
    @SerializedName("venue") val venue: Venue?
)

data class FixtureStatus(
    @SerializedName("long") val long: String,
    @SerializedName("short") val short: String,
    @SerializedName("elapsed") val elapsed: Int?
)

data class Venue(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("city") val city: String?
)

data class League(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("flag") val flag: String?,
    @SerializedName("season") val season: Int,
    @SerializedName("round") val round: String
)

data class Teams(
    @SerializedName("home") val home: Team,
    @SerializedName("away") val away: Team
)

data class Team(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("winner") val winner: Boolean?
)

data class Goals(
    @SerializedName("home") val home: Int?,
    @SerializedName("away") val away: Int?
)

data class Score(
    @SerializedName("halftime") val halftime: Goals,
    @SerializedName("fulltime") val fulltime: Goals,
    @SerializedName("extratime") val extratime: Goals?,
    @SerializedName("penalty") val penalty: Goals?
)

data class TeamStats(
    @SerializedName("team") val team: Team,
    @SerializedName("statistics") val statistics: List<StatItem>
)

data class StatItem(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: Any?
)

data class MatchEvent(
    @SerializedName("time") val time: EventTime,
    @SerializedName("team") val team: Team,
    @SerializedName("player") val player: PlayerName,
    @SerializedName("assist") val assist: PlayerName?,
    @SerializedName("type") val type: String,
    @SerializedName("detail") val detail: String,
    @SerializedName("comments") val comments: String?
)

data class EventTime(
    @SerializedName("elapsed") val elapsed: Int,
    @SerializedName("extra") val extra: Int?
)

data class PlayerName(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?
)

data class Lineup(
    @SerializedName("team") val team: Team,
    @SerializedName("formation") val formation: String?,
    @SerializedName("startXI") val startXI: List<PlayerLineup>,
    @SerializedName("substitutes") val substitutes: List<PlayerLineup>
)

data class PlayerLineup(
    @SerializedName("player") val player: LineupPlayer
)

data class LineupPlayer(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("number") val number: Int,
    @SerializedName("pos") val pos: String?,
    @SerializedName("grid") val grid: String?
)

// Team Season Statistics
data class TeamSeasonStats(
    @SerializedName("team") val team: Team,
    @SerializedName("league") val league: League,
    @SerializedName("form") val form: String,
    @SerializedName("fixtures") val fixtures: FixtureRecord,
    @SerializedName("goals") val goals: GoalRecord,
    @SerializedName("biggest") val biggest: BiggestRecord?,
    @SerializedName("clean_sheet") val cleanSheet: CleanSheet?,
    @SerializedName("failed_to_score") val failedToScore: CleanSheet?
)

data class FixtureRecord(
    @SerializedName("played") val played: RecordStats,
    @SerializedName("wins") val wins: RecordStats,
    @SerializedName("draws") val draws: RecordStats,
    @SerializedName("loses") val loses: RecordStats
)

data class RecordStats(
    @SerializedName("home") val home: Int,
    @SerializedName("away") val away: Int,
    @SerializedName("total") val total: Int
)

data class GoalRecord(
    @SerializedName("for") val goalsFor: GoalAverage,
    @SerializedName("against") val against: GoalAverage
)

data class GoalAverage(
    @SerializedName("total") val total: RecordStats,
    @SerializedName("average") val average: GoalAverageValues,
    @SerializedName("minute") val minute: Map<String, MinuteGoal?>?
)

data class GoalAverageValues(
    @SerializedName("home") val home: String,
    @SerializedName("away") val away: String,
    @SerializedName("total") val total: String
)

data class MinuteGoal(
    @SerializedName("total") val total: Int?,
    @SerializedName("percentage") val percentage: String?
)

data class BiggestRecord(
    @SerializedName("streak") val streak: StreakRecord?,
    @SerializedName("wins") val wins: HomeAway?,
    @SerializedName("loses") val loses: HomeAway?
)

data class StreakRecord(
    @SerializedName("wins") val wins: Int,
    @SerializedName("draws") val draws: Int,
    @SerializedName("loses") val loses: Int
)

data class HomeAway(
    @SerializedName("home") val home: String?,
    @SerializedName("away") val away: String?
)

data class CleanSheet(
    @SerializedName("home") val home: Int,
    @SerializedName("away") val away: Int,
    @SerializedName("total") val total: Int
)

// Standings
data class StandingResponse(
    @SerializedName("league") val league: StandingLeague
)

data class StandingLeague(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("season") val season: Int,
    @SerializedName("standings") val standings: List<List<StandingItem>>
)

data class StandingItem(
    @SerializedName("rank") val rank: Int,
    @SerializedName("team") val team: Team,
    @SerializedName("points") val points: Int,
    @SerializedName("goalsDiff") val goalsDiff: Int,
    @SerializedName("form") val form: String,
    @SerializedName("all") val all: StandingRecord,
    @SerializedName("home") val home: StandingRecord,
    @SerializedName("away") val away: StandingRecord,
    @SerializedName("update") val update: String
)

data class StandingRecord(
    @SerializedName("played") val played: Int,
    @SerializedName("win") val win: Int,
    @SerializedName("draw") val draw: Int,
    @SerializedName("lose") val lose: Int,
    @SerializedName("goals") val goals: RecordGoals
)

data class RecordGoals(
    @SerializedName("for") val goalsFor: Int,
    @SerializedName("against") val against: Int
)

// ===== APP INTERNAL MODELS =====

data class PredictionModel(
    val matchId: Int,
    val homeWinProbability: Int,
    val drawProbability: Int,
    val awayWinProbability: Int,
    val confidenceScore: Int,
    val riskLevel: String,
    val over25Probability: Int,
    val under25Probability: Int,
    val bttsYesProbability: Int,
    val bttsNoProbability: Int,
    val htGoalYesProbability: Int,
    val htGoalNoProbability: Int,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val aiComment: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Prediction saved to SharedPreferences for history tracking
data class SavedPrediction(
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val leagueName: String,
    val matchDate: String,
    val predictedWinner: String,   // "HOME", "DRAW", "AWAY"
    val homeWinProb: Int,
    val drawProb: Int,
    val awayWinProb: Int,
    val predictedHomeScore: Int,
    val predictedAwayScore: Int,
    val confidenceScore: Int,
    val over25Prob: Int,
    val bttsYesProb: Int,
    // Actual results (filled after match)
    val actualHomeGoals: Int = -1,  // -1 = not finished yet
    val actualAwayGoals: Int = -1,
    val actualWinner: String = "",   // "HOME", "DRAW", "AWAY"
    val isResultChecked: Boolean = false,
    // Outcome tracking
    val winnerCorrect: Boolean = false,
    val scoreCorrect: Boolean = false,
    val over25Correct: Boolean = false,
    val bttsCorrect: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)

data class TopPrediction(
    val rank: Int,
    val fixtureId: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeLogo: String,
    val awayLogo: String,
    val confidence: Int,
    val leagueName: String,
    val predictedWinner: String
)

// Stats for the accuracy screen
data class PredictionStats(
    val total: Int,
    val checked: Int,      // how many results have been resolved
    val winnerCorrect: Int,
    val scoreCorrect: Int,
    val over25Correct: Int,
    val bttsCorrect: Int,
    val winnerRate: Float,  // percentage
    val scoreRate: Float,
    val over25Rate: Float,
    val bttsRate: Float
)
