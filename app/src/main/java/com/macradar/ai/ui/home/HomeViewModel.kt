package com.macradar.ai.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.macradar.ai.data.model.FixtureResponse
import com.macradar.ai.data.model.TopPrediction
import com.macradar.ai.data.repository.FootballRepository
import com.macradar.ai.data.repository.Result
import com.macradar.ai.utils.AIPredictionEngine
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FootballRepository()

    private val _matches = MutableLiveData<Result<List<FixtureResponse>>>()
    val matches: LiveData<Result<List<FixtureResponse>>> = _matches

    private val _topPredictions = MutableLiveData<List<TopPrediction>>()
    val topPredictions: LiveData<List<TopPrediction>> = _topPredictions

    private var allFixtures: List<FixtureResponse> = emptyList()

    fun loadTodayMatches() {
        viewModelScope.launch {
            _matches.value = Result.Loading
            val result = repository.getTodayMatches()
            if (result is Result.Success) {
                allFixtures = result.data
                _matches.value = Result.Success(result.data.take(25))
            } else {
                _matches.value = result
            }
        }
    }

    fun loadTomorrowMatches() {
        viewModelScope.launch {
            _matches.value = Result.Loading
            val result = repository.getTomorrowMatches()
            if (result is Result.Success) {
                _matches.value = Result.Success(result.data.take(25))
            } else {
                _matches.value = result
            }
        }
    }

    fun loadPopularMatches() {
        val popularLeagueIds = setOf(39, 140, 78, 135, 2, 203, 61, 94, 88, 71)
        val popular = allFixtures.filter { it.league.id in popularLeagueIds }
        _matches.value = Result.Success((if (popular.isEmpty()) allFixtures else popular).take(25))
    }

    fun loadTopPredictions() {
        viewModelScope.launch {
            val result = repository.getTodayMatches()
            if (result is Result.Success) {
                val topMatches = result.data
                    .filter { it.fixture.status.short in listOf("NS", "1H", "HT", "2H") }
                    .take(10)

                val predictions = topMatches.mapIndexed { index, fixture ->
                    val confidence = (90 - index * 2).coerceIn(70, 95)
                    val winner = if (index % 3 == 2) "AWAY" else if (index % 3 == 1) "DRAW" else "HOME"
                    TopPrediction(
                        rank = index + 1,
                        fixtureId = fixture.fixture.id,
                        homeTeam = fixture.teams.home.name,
                        awayTeam = fixture.teams.away.name,
                        homeLogo = fixture.teams.home.logo,
                        awayLogo = fixture.teams.away.logo,
                        confidence = confidence,
                        leagueName = fixture.league.name,
                        predictedWinner = winner
                    )
                }
                _topPredictions.value = predictions
            }
        }
    }

    fun getMatchTime(fixture: FixtureResponse): String = repository.formatMatchTime(fixture.fixture.timestamp)
    fun getLeagueName(fixture: FixtureResponse): String = repository.getLeagueName(fixture)
}
