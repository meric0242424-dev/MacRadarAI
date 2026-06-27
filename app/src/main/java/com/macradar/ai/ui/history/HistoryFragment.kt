package com.macradar.ai.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.macradar.ai.R
import com.macradar.ai.data.model.SavedPrediction
import com.macradar.ai.data.repository.PredictionStorage

class HistoryFragment : Fragment() {

    private lateinit var storage: PredictionStorage

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = PredictionStorage(requireContext())
        setupStats(view)
        setupList(view)
    }

    private fun setupStats(view: View) {
        val stats = storage.getStats()

        view.findViewById<TextView>(R.id.tvTotalPredictions).text = stats.total.toString()
        view.findViewById<TextView>(R.id.tvCheckedPredictions).text = stats.checked.toString()
        view.findViewById<TextView>(R.id.tvWinnerRate).text = if (stats.checked > 0) "%.0f%%".format(stats.winnerRate) else "-"
        view.findViewById<TextView>(R.id.tvScoreRate).text = if (stats.checked > 0) "%.0f%%".format(stats.over35Rate) else "-"
        view.findViewById<TextView>(R.id.tvOver25Rate).text = if (stats.checked > 0) "%.0f%%".format(stats.over25Rate) else "-"
        view.findViewById<TextView>(R.id.tvBttsRate).text = if (stats.checked > 0) "%.0f%%".format(stats.bttsRate) else "-"

        val winnerProgress = view.findViewById<ProgressBar>(R.id.progressWinnerRate)
        winnerProgress.max = 100
        winnerProgress.progress = stats.winnerRate.toInt()
    }

    private fun setupList(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvHistory)
        val predictions = storage.getAllPredictions()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = HistoryAdapter(predictions)

        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyHistory)
        if (predictions.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
    }
}

class HistoryAdapter(private val items: List<SavedPrediction>) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivHome: ImageView = view.findViewById(R.id.ivHistoryHome)
        val ivAway: ImageView = view.findViewById(R.id.ivHistoryAway)
        val tvMatch: TextView = view.findViewById(R.id.tvHistoryMatch)
        val tvLeague: TextView = view.findViewById(R.id.tvHistoryLeague)
        val tvResult: TextView = view.findViewById(R.id.tvHistoryResult)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvConfidence: TextView = view.findViewById(R.id.tvHistoryConfidence)
        val tvWinnerPick: TextView = view.findViewById(R.id.tvHistoryWinnerPick)
        val tvWinnerOutcome: TextView = view.findViewById(R.id.tvHistoryWinnerOutcome)
        val tvBttsPick: TextView = view.findViewById(R.id.tvHistoryBttsPick)
        val tvBttsOutcome: TextView = view.findViewById(R.id.tvHistoryBttsOutcome)
        val tvOverPick: TextView = view.findViewById(R.id.tvHistoryOverPick)
        val tvOverOutcome: TextView = view.findViewById(R.id.tvHistoryOverOutcome)
        val tvOver35Pick: TextView = view.findViewById(R.id.tvHistoryOver35Pick)
        val tvOver35Outcome: TextView = view.findViewById(R.id.tvHistoryOver35Outcome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return VH(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val ctx = holder.itemView.context

        Glide.with(ctx).load(item.homeLogo).placeholder(R.drawable.ic_team_placeholder).into(holder.ivHome)
        Glide.with(ctx).load(item.awayLogo).placeholder(R.drawable.ic_team_placeholder).into(holder.ivAway)

        holder.tvMatch.text = "${item.homeTeam} vs ${item.awayTeam}"
        holder.tvLeague.text = item.leagueName
        holder.tvDate.text = item.matchDate
        holder.tvConfidence.text = "AI Güven: ${item.confidenceScore}/100"

        val winnerLabel = when (item.predictedWinner) {
            "HOME" -> item.homeTeam
            "AWAY" -> item.awayTeam
            else -> "Beraberlik"
        }
        holder.tvWinnerPick.text = winnerLabel

        val predictedBtts = item.bttsYesProb > 50
        holder.tvBttsPick.text = if (predictedBtts) "KG Var" else "KG Yok"

        val predictedOver25 = item.over25Prob > 50
        holder.tvOverPick.text = if (predictedOver25) "2.5 Üst" else "2.5 Alt"

        val predictedOver35 = item.over35Prob > 50
        holder.tvOver35Pick.text = if (predictedOver35) "3.5 Üst" else "3.5 Alt"

        if (item.isResultChecked) {
            holder.tvResult.text = "Gerçek Sonuç: ${item.actualHomeGoals} - ${item.actualAwayGoals}"
            holder.tvResult.visibility = View.VISIBLE

            setOutcome(holder.tvWinnerOutcome, item.winnerCorrect, ctx)
            setOutcome(holder.tvBttsOutcome, item.bttsCorrect, ctx)
            setOutcome(holder.tvOverOutcome, item.over25Correct, ctx)
            setOutcome(holder.tvOver35Outcome, item.over35Correct, ctx)
        } else {
            holder.tvResult.visibility = View.GONE
            setPending(holder.tvWinnerOutcome, ctx)
            setPending(holder.tvBttsOutcome, ctx)
            setPending(holder.tvOverOutcome, ctx)
            setPending(holder.tvOver35Outcome, ctx)
        }
    }

    private fun setOutcome(tv: TextView, correct: Boolean, ctx: android.content.Context) {
        if (correct) {
            tv.text = "✓ Doğru"
            tv.setTextColor(ctx.getColor(R.color.accent_green))
        } else {
            tv.text = "✗ Yanlış"
            tv.setTextColor(ctx.getColor(R.color.live_red))
        }
    }

    private fun setPending(tv: TextView, ctx: android.content.Context) {
        tv.text = "⏳ Bekliyor"
        tv.setTextColor(ctx.getColor(R.color.text_secondary))
    }
}
