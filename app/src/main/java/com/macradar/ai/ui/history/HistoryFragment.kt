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
import androidx.cardview.widget.CardView
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
        view.findViewById<TextView>(R.id.tvScoreRate).text = if (stats.checked > 0) "%.0f%%".format(stats.scoreRate) else "-"
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
        val tvPrediction: TextView = view.findViewById(R.id.tvHistoryPrediction)
        val tvResult: TextView = view.findViewById(R.id.tvHistoryResult)
        val tvDate: TextView = view.findViewById(R.id.tvHistoryDate)
        val tvOutcome: TextView = view.findViewById(R.id.tvHistoryOutcome)
        val tvConfidence: TextView = view.findViewById(R.id.tvHistoryConfidence)
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
        holder.tvConfidence.text = "AI: ${item.confidenceScore}"

        val winnerLabel = when (item.predictedWinner) {
            "HOME" -> item.homeTeam
            "AWAY" -> item.awayTeam
            else -> "Beraberlik"
        }
        holder.tvPrediction.text = "Tahmin: $winnerLabel (${item.predictedHomeScore}-${item.predictedAwayScore})"

        if (item.isResultChecked) {
            holder.tvResult.text = "Sonuç: ${item.actualHomeGoals} - ${item.actualAwayGoals}"
            holder.tvResult.visibility = View.VISIBLE
            if (item.winnerCorrect) {
                holder.tvOutcome.text = "✓ DOĞRU"
                holder.tvOutcome.setTextColor(ctx.getColor(R.color.accent_green))
            } else {
                holder.tvOutcome.text = "✗ YANLIŞ"
                holder.tvOutcome.setTextColor(ctx.getColor(R.color.live_red))
            }
        } else {
            holder.tvResult.visibility = View.GONE
            holder.tvOutcome.text = "⏳ Bekleniyor"
            holder.tvOutcome.setTextColor(ctx.getColor(R.color.text_secondary))
        }
    }
}
