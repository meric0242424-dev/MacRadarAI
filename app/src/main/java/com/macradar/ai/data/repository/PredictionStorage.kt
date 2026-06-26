package com.macradar.ai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macradar.ai.data.model.SavedPrediction
import com.macradar.ai.data.model.PredictionStats

/**
 * Saves and loads predictions from SharedPreferences as JSON.
 * Tracks real match results vs AI predictions for accuracy measurement.
 */
class PredictionStorage(context: Context) {

    private val prefs = context.getSharedPreferences("predictions_v1", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePrediction(pred: SavedPrediction) {
        val all = getAllPredictions().toMutableList()
        // Remove existing entry for same fixture if any
        all.removeAll { it.fixtureId == pred.fixtureId }
        all.add(0, pred) // newest first
        // Keep max 200 entries
        val trimmed = if (all.size > 200) all.take(200) else all
        val json = gson.toJson(trimmed)
        prefs.edit().putString("all_predictions", json).apply()
    }

    fun getAllPredictions(): List<SavedPrediction> {
        val json = prefs.getString("all_predictions", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SavedPrediction>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPredictionForFixture(fixtureId: Int): SavedPrediction? {
        return getAllPredictions().find { it.fixtureId == fixtureId }
    }

    /**
     * Called when we fetch a finished match result.
     * Updates the saved prediction with actual score and calculates correctness.
     */
    fun updateWithActualResult(fixtureId: Int, homeGoals: Int, awayGoals: Int) {
        val all = getAllPredictions().toMutableList()
        val index = all.indexOfFirst { it.fixtureId == fixtureId }
        if (index == -1) return

        val pred = all[index]
        if (pred.isResultChecked) return // already updated

        val actualWinner = when {
            homeGoals > awayGoals -> "HOME"
            awayGoals > homeGoals -> "AWAY"
            else -> "DRAW"
        }

        val actualOver25 = (homeGoals + awayGoals) > 2
        val actualBtts = homeGoals > 0 && awayGoals > 0

        val predictedOver25 = pred.over25Prob > 50
        val predictedBtts = pred.bttsYesProb > 50

        val updated = pred.copy(
            actualHomeGoals = homeGoals,
            actualAwayGoals = awayGoals,
            actualWinner = actualWinner,
            isResultChecked = true,
            winnerCorrect = pred.predictedWinner == actualWinner,
            scoreCorrect = pred.predictedHomeScore == homeGoals && pred.predictedAwayScore == awayGoals,
            over25Correct = predictedOver25 == actualOver25,
            bttsCorrect = predictedBtts == actualBtts
        )

        all[index] = updated
        val json = gson.toJson(all)
        prefs.edit().putString("all_predictions", json).apply()
    }

    fun getStats(): PredictionStats {
        val all = getAllPredictions()
        val checked = all.filter { it.isResultChecked }
        val n = checked.size

        if (n == 0) return PredictionStats(all.size, 0, 0, 0, 0, 0, 0f, 0f, 0f, 0f)

        val winnerCorrect = checked.count { it.winnerCorrect }
        val scoreCorrect = checked.count { it.scoreCorrect }
        val over25Correct = checked.count { it.over25Correct }
        val bttsCorrect = checked.count { it.bttsCorrect }

        return PredictionStats(
            total = all.size,
            checked = n,
            winnerCorrect = winnerCorrect,
            scoreCorrect = scoreCorrect,
            over25Correct = over25Correct,
            bttsCorrect = bttsCorrect,
            winnerRate = winnerCorrect * 100f / n,
            scoreRate = scoreCorrect * 100f / n,
            over25Rate = over25Correct * 100f / n,
            bttsRate = bttsCorrect * 100f / n
        )
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
