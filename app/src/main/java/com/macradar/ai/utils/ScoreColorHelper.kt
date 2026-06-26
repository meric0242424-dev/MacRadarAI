package com.macradar.ai.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.macradar.ai.R

object ScoreColorHelper {
    fun getScoreColor(context: Context, score: Int): Int {
        return when {
            score >= 80 -> ContextCompat.getColor(context, R.color.score_high)
            score >= 65 -> ContextCompat.getColor(context, R.color.score_medium)
            else -> ContextCompat.getColor(context, R.color.score_low)
        }
    }

    fun getRiskColor(context: Context, riskLevel: String): Int {
        return when (riskLevel.lowercase()) {
            "düşük risk" -> ContextCompat.getColor(context, R.color.win_color)
            "orta risk" -> ContextCompat.getColor(context, R.color.draw_color)
            else -> ContextCompat.getColor(context, R.color.loss_color)
        }
    }
}
