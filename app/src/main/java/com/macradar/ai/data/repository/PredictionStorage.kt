package com.macradar.ai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.macradar.ai.data.model.SavedPrediction
import com.macradar.ai.data.model.PredictionStats

class PredictionStorage(context: Context) {

    private val prefs = context.getSharedPreferences("predictions_v1", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePrediction(pred: SavedPrediction) {
        val all = getAllPredictions().toMutableList()
        all.removeAll { it.fixtureId == pred.fixtureId }
        all.add(0, pred)
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

    fun updateWithActualResult(fixtureId: Int, homeGoals: Int, awayGoals: Int) {
        val all = getAllPredictions().toMutableList()
        val index = all.indexOfFirst { it.fixtureId == fixtureId }
        if (index == -1) return

        val pred = all[index]
        if (pred.isResultChecked) return

        val actualWinner = when {
            homeGoals > awayGoals -> "HOME"
            awayGoals > homeGoals -> "AWAY"
            else -> "DRAW"
        }

        val totalGoals = homeGoals + awayGoals
        val actualOver25 = totalGoals > 2
        val actualOver35 = totalGoals > 3
        val actualBtts = homeGoals > 0 && awayGoals > 0

        val predictedOver25 = pred.over25Prob > 50
        val predictedOver35 = pred.over35Prob > 50
        val predictedBtts = pred.bttsYesProb > 50

        val updated = pred.copy(
            actualHomeGoals = homeGoals,
            actualAwayGoals = awayGoals,
            actualWinner = actualWinner,
            isResultChecked = true,
            winnerCorrect = pred.predictedWinner == actualWinner,
            over25Correct = predictedOver25 == actualOver25,
            over35Correct = predictedOver35 == actualOver35,
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
        val over25Correct = checked.count { it.over25Correct }
        val over35Correct = checked.count { it.over35Correct }
        val bttsCorrect = checked.count { it.bttsCorrect }

        return PredictionStats(
            total = all.size,
            checked = n,
            winnerCorrect = winnerCorrect,
            over25Correct = over25Correct,
            over35Correct = over35Correct,
            bttsCorrect = bttsCorrect,
            winnerRate = winnerCorrect * 100f / n,
            over25Rate = over25Correct * 100f / n,
            over35Rate = over35Correct * 100f / n,
            bttsRate = bttsCorrect * 100f / n
        )
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
