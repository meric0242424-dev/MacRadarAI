package com.macradar.ai.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.macradar.ai.R
import com.macradar.ai.data.model.FixtureResponse

class LiveMatchAdapter(
    private val onPredictClick: (FixtureResponse) -> Unit,
    private val onCardClick: (FixtureResponse) -> Unit
) : ListAdapter<FixtureResponse, LiveMatchAdapter.LiveMatchViewHolder>(LiveMatchDiffCallback()) {

    private val predictionLabels = mutableMapOf<Int, String>()

    fun setPredictionLabels(labels: Map<Int, String>) {
        predictionLabels.putAll(labels)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveMatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_live_match, parent, false)
        return LiveMatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiveMatchViewHolder, position: Int) {
        val fixture = getItem(position)
        holder.bind(fixture, predictionLabels[fixture.fixture.id], onPredictClick, onCardClick)
    }

    class LiveMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            fixture: FixtureResponse,
            predictionLabel: String?,
            onPredictClick: (FixtureResponse) -> Unit,
            onCardClick: (FixtureResponse) -> Unit
        ) {
            val tvLeagueName = itemView.findViewById<android.widget.TextView>(R.id.tvLeagueName)
            val tvLive = itemView.findViewById<android.widget.TextView>(R.id.tvLive)
            val ivHomeLogo = itemView.findViewById<android.widget.ImageView>(R.id.ivHomeLogo)
            val ivAwayLogo = itemView.findViewById<android.widget.ImageView>(R.id.ivAwayLogo)
            val tvHomeTeam = itemView.findViewById<android.widget.TextView>(R.id.tvHomeTeam)
            val tvAwayTeam = itemView.findViewById<android.widget.TextView>(R.id.tvAwayTeam)
            val tvScore = itemView.findViewById<android.widget.TextView>(R.id.tvScore)
            val tvLiveGoalResult = itemView.findViewById<android.widget.TextView>(R.id.tvLiveGoalResult)
            val btnLiveGoalPrediction = itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLiveGoalPrediction)

            tvLeagueName.text = fixture.league.name
            val elapsed = fixture.fixture.status.elapsed ?: 0
            tvLive.text = "${elapsed}'"

            Glide.with(itemView).load(fixture.teams.home.logo)
                .placeholder(R.drawable.ic_team_placeholder).into(ivHomeLogo)
            Glide.with(itemView).load(fixture.teams.away.logo)
                .placeholder(R.drawable.ic_team_placeholder).into(ivAwayLogo)

            tvHomeTeam.text = fixture.teams.home.name
            tvAwayTeam.text = fixture.teams.away.name
            tvScore.text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}"

            if (predictionLabel != null) {
                tvLiveGoalResult.text = predictionLabel
                tvLiveGoalResult.visibility = View.VISIBLE
                btnLiveGoalPrediction.text = "YENİDEN HESAPLA"
            } else {
                tvLiveGoalResult.text = ""
                tvLiveGoalResult.visibility = View.GONE
                btnLiveGoalPrediction.text = "CANLI GOL TAHMİNİ"
            }

            btnLiveGoalPrediction.setOnClickListener { onPredictClick(fixture) }
            itemView.setOnClickListener { onCardClick(fixture) }
        }
    }

    class LiveMatchDiffCallback : DiffUtil.ItemCallback<FixtureResponse>() {
        override fun areItemsTheSame(oldItem: FixtureResponse, newItem: FixtureResponse): Boolean =
            oldItem.fixture.id == newItem.fixture.id

        override fun areContentsTheSame(oldItem: FixtureResponse, newItem: FixtureResponse): Boolean =
            oldItem == newItem
    }
}
