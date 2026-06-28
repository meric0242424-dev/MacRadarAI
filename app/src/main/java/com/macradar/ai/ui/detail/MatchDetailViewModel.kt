package com.macradar.ai.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.macradar.ai.data.model.*
import com.macradar.ai.data.repository.FootballRepository
import com.macradar.ai.data.repository.PredictionStorage
import com.macradar.ai.data.repository.Result
import kotlinx.coroutines.launch

class MatchDetailViewModel(application: Application, private val fixtureId: Int) : AndroidViewModel(application) {

    private val repository = FootballRepository(application)
    private val storage = PredictionStorage(application)

    private val _fixture = MutableLiveData<Result<FixtureResponse>>()
    val fixture: LiveData<Result<FixtureResponse>> = _fixture

    private val _prediction = MutableLiveData<Result<PredictionModel>>()
    val prediction: LiveData<Result<PredictionModel>> = _prediction

    private val _statistics = MutableLiveData<Result<List<TeamStats>>>()
    val statistics: LiveData<Result<List<TeamStats>>> = _statistics

    private val _lineups = MutableLiveData<Result<List<Lineup>>>()
    val lineups: LiveData<Result<List<Lineup>>> = _lineups

    private val _h2h = MutableLiveData<Result<List<FixtureResponse>>>()
    val h2h: LiveData<Result<List<FixtureResponse>>> = _h2h

    private var currentFixture: FixtureResponse? = null

    private var statisticsLoaded = false
    private var lineupsLoaded = false
    private var h2hLoaded = false

    fun loadMatchDetail() {
        viewModelScope.launch {
            _fixture.value = Result.Loading
            val result = repository.getMatchDetail(fixtureId)
            if (result is Result.Success) {
                currentFixture = result.data
                val status = result.data.fixture.status.short
                if (status == "FT" || status == "AET" || status == "PEN") {
                    val hg = result.data.goals.home ?: 0
                    val ag = result.data.goals.away ?: 0
                    storage.updateWithActualResult(fixtureId, hg, ag)
                }
                loadPredictionFor(result.data)
            }
            _fixture.value = result
        }
    }

    private fun loadPredictionFor(fixture: FixtureResponse) {
        viewModelScope.launch {
            _prediction.value = Result.Loading
            val result = repository.generatePrediction(
                fixtureId = fixture.fixture.id,
                homeTeamId = fixture.teams.home.id,
                awayTeamId = fixture.teams.away.id,
                leagueId = fixture.league.id,
                season = fixture.league.season,
                homeTeamName = fixture.teams.home.name,
                awayTeamName = fixture.teams.away.name
            )
            if (result is Result.Success) {
                savePredictionToHistory(fixture, result.data)
            }
            _prediction.value = result
        }
    }

    fun loadPrediction() {
        currentFixture?.let { loadPredictionFor(it) }
    }

    private fun savePredictionToHistory(fixture: FixtureResponse, pred: PredictionModel) {
        val status = fixture.fixture.status.short
        val maxProb = maxOf(pred.homeWinProbability, pred.drawProbability, pred.awayWinProbability)
        val predictedWinner = when (maxProb) {
            pred.homeWinProbability -> "HOME"
            pred.awayWinProbability -> "AWAY"
            else -> "DRAW"
        }

        val saved = SavedPrediction(
            fixtureId = fixture.fixture.id,
            homeTeam = fixture.teams.home.name,
            awayTeam = fixture.teams.away.name,
            homeLogo = fixture.teams.home.logo,
            awayLogo = fixture.teams.away.logo,
            leagueName = fixture.league.name,
            matchDate = repository.formatMatchDate(fixture.fixture.timestamp),
            predictedWinner = predictedWinner,
            homeWinProb = pred.homeWinProbability,
            drawProb = pred.drawProbability,
            awayWinProb = pred.awayWinProbability,
            confidenceScore = pred.confidenceScore,
            over25Prob = pred.over25Probability,
            over35Prob = pred.over35Probability,
            bttsYesProb = pred.bttsYesProbability
        )

        if (status == "FT" || status == "AET") {
            val hg = fixture.goals.home ?: 0
            val ag = fixture.goals.away ?: 0
            val actualWinner = when {
                hg > ag -> "HOME"
                ag > hg -> "AWAY"
                else -> "DRAW"
            }
            val totalGoals = hg + ag
            storage.savePrediction(saved.copy(
                actualHomeGoals = hg,
                actualAwayGoals = ag,
                actualWinner = actualWinner,
                isResultChecked = true,
                winnerCorrect = predictedWinner == actualWinner,
                over25Correct = (pred.over25Probability > 50) == (totalGoals > 2),
                over35Correct = (pred.over35Probability > 50) == (totalGoals > 3),
                bttsCorrect = (pred.bttsYesProbability > 50) == (hg > 0 && ag > 0)
            ))
        } else {
            storage.savePrediction(saved)
        }
    }

    fun loadStatistics(forceRefresh: Boolean = false) {
        if (statisticsLoaded && !forceRefresh) return
        viewModelScope.launch {
            _statistics.value = Result.Loading
            _statistics.value = repository.getMatchStatistics(fixtureId)
            statisticsLoaded = true
        }
    }

    fun loadLineups(forceRefresh: Boolean = false) {
        if (lineupsLoaded && !forceRefresh) return
        viewModelScope.launch {
            _lineups.value = Result.Loading
            _lineups.value = repository.getMatchLineups(fixtureId)
            lineupsLoaded = true
        }
    }

    fun loadH2H(forceRefresh: Boolean = false) {
        if (h2hLoaded && !forceRefresh) return
        viewModelScope.launch {
            val fixture = currentFixture ?: return@launch
            _h2h.value = Result.Loading
            _h2h.value = repository.getHeadToHead(fixture.teams.home.id, fixture.teams.away.id)
            h2hLoaded = true
        }
    }

    fun getLeagueName(): String = currentFixture?.let { repository.getLeagueName(it) } ?: ""
    fun getMatchTime(): String = currentFixture?.let { repository.formatMatchTime(it.fixture.timestamp) } ?: ""
    fun getMatchDate(): String = currentFixture?.let { repository.formatMatchDate(it.fixture.timestamp) } ?: ""
}

class MatchDetailViewModelFactory(private val application: Application, private val fixtureId: Int) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchDetailViewModel(application, fixtureId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
