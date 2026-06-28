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
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FootballRepository(application)

    private val _matches = MutableLiveData<Result<List<FixtureResponse>>>()
    val matches: LiveData<Result<List<FixtureResponse>>> = _matches

    private val _topPredictions = MutableLiveData<List<TopPrediction>>()
    val topPredictions: LiveData<List<TopPrediction>> = _topPredictions

    private val _liveGoalPredictions = MutableLiveData<Map<Int, String>>(emptyMap())
    val liveGoalPredictions: LiveData<Map<Int, String>> = _liveGoalPredictions

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

    fun loadLiveMatches() {
        viewModelScope.launch {
            _matches.value = Result.Loading
            val result = repository.getLiveMatches()
            if (result is Result.Success) {
                _matches.value = Result.Success(result.data.take(25))
            } else {
                _matches.value = result
            }
        }
    }

    fun requestLiveGoalPrediction(fixture: FixtureResponse) {
        viewModelScope.launch {
            val minute = fixture.fixture.status.elapsed ?: 0
            val prob = repository.generateLiveGoalPrediction(
                homeTeamId = fixture.teams.home.id,
                awayTeamId = fixture.teams.away.id,
                leagueId = fixture.league.id,
                season = fixture.league.season,
                homeTeamName = fixture.teams.home.name,
                awayTeamName = fixture.teams.away.name,
                currentMinute = minute
            )
            val label = if (prob >= 50) "Gol Olur  %$prob" else "Gol Olmaz  %${100 - prob}"
            val updated = (_liveGoalPredictions.value ?: emptyMap()).toMutableMap()
            updated[fixture.fixture.id] = label
            _liveGoalPredictions.value = updated
        }
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
