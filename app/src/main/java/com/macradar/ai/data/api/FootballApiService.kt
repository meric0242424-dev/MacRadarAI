package com.macradar.ai.data.api

import com.macradar.ai.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FootballApiService {

    @GET("fixtures")
    suspend fun getFixturesByDate(
        @Query("date") date: String,
        @Query("timezone") timezone: String = "Europe/Istanbul",
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<FixtureResponse>>

    @GET("fixtures")
    suspend fun getLiveFixtures(
        @Query("live") live: String = "all",
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<FixtureResponse>>

    @GET("fixtures")
    suspend fun getFixtureById(
        @Query("id") id: Int,
        @Query("timezone") timezone: String = "Europe/Istanbul",
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<FixtureResponse>>

    @GET("fixtures/statistics")
    suspend fun getFixtureStatistics(
        @Query("fixture") fixtureId: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<TeamStats>>

    @GET("fixtures/events")
    suspend fun getFixtureEvents(
        @Query("fixture") fixtureId: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<MatchEvent>>

    @GET("fixtures/lineups")
    suspend fun getFixtureLineups(
        @Query("fixture") fixtureId: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<Lineup>>

    @GET("teams/statistics")
    suspend fun getTeamStatistics(
        @Query("team") teamId: Int,
        @Query("season") season: Int,
        @Query("league") leagueId: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<TeamSeasonStats>>

    @GET("standings")
    suspend fun getStandings(
        @Query("league") leagueId: Int,
        @Query("season") season: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<StandingResponse>>

    @GET("fixtures/headtohead")
    suspend fun getHeadToHead(
        @Query("h2h") h2h: String,
        @Query("last") last: Int = 10,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<FixtureResponse>>

    @GET("predictions")
    suspend fun getPredictions(
        @Query("fixture") fixtureId: Int,
        @Header("x-rapidapi-key") apiKey: String
    ): Response<ApiResponse<ApiPredictionResponse>>
}

data class ApiPredictionResponse(
    @com.google.gson.annotations.SerializedName("winner") val winner: ApiPredictionWinner?,
    @com.google.gson.annotations.SerializedName("win_or_draw") val winOrDraw: Boolean?,
    @com.google.gson.annotations.SerializedName("under_over") val underOver: String?,
    @com.google.gson.annotations.SerializedName("goals") val goals: ApiPredictionGoals?,
    @com.google.gson.annotations.SerializedName("advice") val advice: String?,
    @com.google.gson.annotations.SerializedName("percent") val percent: ApiPredictionPercent?
)

data class ApiPredictionWinner(
    @com.google.gson.annotations.SerializedName("id") val id: Int?,
    @com.google.gson.annotations.SerializedName("name") val name: String?,
    @com.google.gson.annotations.SerializedName("comment") val comment: String?
)

data class ApiPredictionGoals(
    @com.google.gson.annotations.SerializedName("home") val home: String?,
    @com.google.gson.annotations.SerializedName("away") val away: String?
)

data class ApiPredictionPercent(
    @com.google.gson.annotations.SerializedName("home") val home: String?,
    @com.google.gson.annotations.SerializedName("draw") val draw: String?,
    @com.google.gson.annotations.SerializedName("away") val away: String?
)
