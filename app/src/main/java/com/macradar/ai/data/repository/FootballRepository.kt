package com.macradar.ai.data.repository

import com.macradar.ai.data.api.ApiClient
import com.macradar.ai.data.api.FootballApiService
import com.macradar.ai.data.model.*
import com.macradar.ai.utils.AIPredictionEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class FootballRepository {

    private val apiService: FootballApiService = ApiClient.footballApiService
    private val apiKey: String = ApiClient.API_KEY

    private val fixtureCache = mutableMapOf<String, List<FixtureResponse>>()
    private val predictionCache = mutableMapOf<Int, PredictionModel>()

    suspend fun getTodayMatches(): Result<List<FixtureResponse>> = withContext(Dispatchers.IO) {
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            fixtureCache[today]?.let { return@withContext Result.Success(it) }
            val response = apiService.getFixturesByDate(today, "Europe/Istanbul", apiKey)
            if (response.isSuccessful) {
                val fixtures = response.body()?.response ?: emptyList()
                val sorted = sortFixturesByLeaguePriority(fixtures)
                fixtureCache[today] = sorted
                Result.Success(sorted)
            } else {
                Result.Error("API Hatası: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getTomorrowMatches(): Result<List<FixtureResponse>> = withContext(Dispatchers.IO) {
        try {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            fixtureCache[tomorrow]?.let { return@withContext Result.Success(it) }
            val response = apiService.getFixturesByDate(tomorrow, "Europe/Istanbul", apiKey)
            if (response.isSuccessful) {
                val fixtures = response.body()?.response ?: emptyList()
                val sorted = sortFixturesByLeaguePriority(fixtures)
                fixtureCache[tomorrow] = sorted
                Result.Success(sorted)
            } else {
                Result.Error("API Hatası: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getLiveMatches(): Result<List<FixtureResponse>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLiveFixtures(apiKey = apiKey)
            if (response.isSuccessful) {
                Result.Success(response.body()?.response ?: emptyList())
            } else {
                Result.Error("API Hatası: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getMatchDetail(fixtureId: Int): Result<FixtureResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFixtureById(fixtureId, apiKey = apiKey)
            if (response.isSuccessful) {
                val fixture = response.body()?.response?.firstOrNull()
                    ?: return@withContext Result.Error("Maç bulunamadı")
                Result.Success(fixture)
            } else {
                Result.Error("API Hatası: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getMatchStatistics(fixtureId: Int): Result<List<TeamStats>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFixtureStatistics(fixtureId, apiKey)
            if (response.isSuccessful) {
                Result.Success(response.body()?.response ?: emptyList())
            } else {
                Result.Error("İstatistikler yüklenemedi")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getMatchLineups(fixtureId: Int): Result<List<Lineup>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFixtureLineups(fixtureId, apiKey)
            if (response.isSuccessful) {
                Result.Success(response.body()?.response ?: emptyList())
            } else {
                Result.Error("Kadrolar yüklenemedi")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getMatchEvents(fixtureId: Int): Result<List<MatchEvent>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFixtureEvents(fixtureId, apiKey)
            if (response.isSuccessful) {
                Result.Success(response.body()?.response ?: emptyList())
            } else {
                Result.Error("Olaylar yüklenemedi")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Bağlantı hatası")
        }
    }

    suspend fun getTeamStatistics(teamId: Int, leagueId: Int, season: Int): Result<TeamSeasonStats> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTeamStatistics(teamId, season, leagueId, apiKey)
                if (response.isSuccessful) {
                    val stats = response.body()?.response?.firstOrNull()
                        ?: return@withContext Result.Error("İstatistikler bulunamadı")
                    Result.Success(stats)
                } else {
                    Result.Error("Takım istatistikleri yüklenemedi")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Bağlantı hatası")
            }
        }

    suspend fun getStandings(leagueId: Int, season: Int): Result<List<StandingItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getStandings(leagueId, season, apiKey)
                if (response.isSuccessful) {
                    val standingResponse: StandingResponse? = response.body()?.response?.firstOrNull()
                    val standings: List<StandingItem> = standingResponse?.league?.standings?.firstOrNull() ?: emptyList()
                    Result.Success(standings)
                } else {
                    Result.Error("Puan durumu yüklenemedi")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Bağlantı hatası")
            }
        }

    suspend fun getHeadToHead(homeTeamId: Int, awayTeamId: Int): Result<List<FixtureResponse>> =
        withContext(Dispatchers.IO) {
            try {
                val h2hQuery = "$homeTeamId-$awayTeamId"
                val response = apiService.getHeadToHead(h2hQuery, 10, apiKey)
                if (response.isSuccessful) {
                    Result.Success(response.body()?.response ?: emptyList())
                } else {
                    Result.Error("H2H verisi yüklenemedi")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Bağlantı hatası")
            }
        }

    suspend fun generatePrediction(
        fixtureId: Int,
        homeTeamId: Int,
        awayTeamId: Int,
        leagueId: Int,
        season: Int,
        homeTeamName: String = "",
        awayTeamName: String = ""
    ): Result<PredictionModel> = withContext(Dispatchers.IO) {
        predictionCache[fixtureId]?.let {
            return@withContext Result.Success(it)
        }

        try {
            val homeStats = try {
                (getTeamStatistics(homeTeamId, leagueId, season) as? Result.Success)?.data
            } catch (e: Exception) { null }

            val awayStats = try {
                (getTeamStatistics(awayTeamId, leagueId, season) as? Result.Success)?.data
            } catch (e: Exception) { null }

            val h2h = try {
                (getHeadToHead(homeTeamId, awayTeamId) as? Result.Success)?.data
            } catch (e: Exception) { null }

            val prediction = AIPredictionEngine.calculatePrediction(
                homeStats, awayStats, h2h, homeTeamId, awayTeamId, homeTeamName, awayTeamName
            ).copy(matchId = fixtureId)

            predictionCache[fixtureId] = prediction
            Result.Success(prediction)

        } catch (e: Exception) {
            val defaultPred = AIPredictionEngine.calculatePrediction(
                null, null, null, homeTeamId, awayTeamId, homeTeamName, awayTeamName
            ).copy(matchId = fixtureId)
            Result.Success(defaultPred)
        }
    }

    private fun sortFixturesByLeaguePriority(fixtures: List<FixtureResponse>): List<FixtureResponse> {
        val priorityLeagueIds = listOf(203, 39, 140, 78, 135, 61, 2, 3)
        return fixtures.sortedWith(compareBy(
            { if (it.league.id in priorityLeagueIds) priorityLeagueIds.indexOf(it.league.id) else 999 },
            { it.fixture.timestamp }
        ))
    }

    fun formatMatchTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        return sdf.format(Date(timestamp * 1000))
    }

    fun formatMatchDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr"))
        sdf.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
        return sdf.format(Date(timestamp * 1000))
    }

    fun getLeagueName(fixture: FixtureResponse): String {
        return when (fixture.league.id) {
            203 -> "Süper Lig"
            39 -> "Premier Lig"
            140 -> "La Liga"
            78 -> "Bundesliga"
            135 -> "Serie A"
            61 -> "Ligue 1"
            2 -> "Şampiyonlar Ligi"
            3 -> "Avrupa Ligi"
            else -> fixture.league.name
        }
    }
}
