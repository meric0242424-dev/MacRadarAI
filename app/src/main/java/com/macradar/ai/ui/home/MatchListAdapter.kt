package com.macradar.ai.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.macradar.ai.R
import com.macradar.ai.data.model.FixtureResponse
import com.macradar.ai.databinding.ItemMatchBinding
import com.macradar.ai.utils.AIPredictionEngine
import com.macradar.ai.utils.ScoreColorHelper
import java.text.SimpleDateFormat
import java.util.*

class MatchListAdapter(
    private val onMatchClick: (FixtureResponse) -> Unit
) : ListAdapter<FixtureResponse, MatchListAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(fixture: FixtureResponse) {
            binding.apply {
                // Team names
                tvHomeTeam.text = fixture.teams.home.name
                tvAwayTeam.text = fixture.teams.away.name

                // League
                tvLeagueName.text = getLeagueName(fixture)

                // Match time
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
                tvMatchTime.text = sdf.format(Date(fixture.fixture.timestamp * 1000))

                // Status
                val status = fixture.fixture.status.short
                when {
                    status == "NS" -> {
                        // Not started
                        tvScore.visibility = View.GONE
                        tvMatchTime.visibility = View.VISIBLE
                    }
                    status in listOf("1H", "HT", "2H", "ET", "BT", "P") -> {
                        // Live
                        tvScore.visibility = View.VISIBLE
                        tvMatchTime.visibility = View.GONE
                        tvScore.text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}"
                        tvLive.visibility = View.VISIBLE
                        val elapsed = fixture.fixture.status.elapsed
                        if (elapsed != null) tvLive.text = "${elapsed}'"
                    }
                    else -> {
                        // Finished
                        tvScore.visibility = View.VISIBLE
                        tvMatchTime.visibility = View.GONE
                        tvScore.text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}"
                        tvLive.visibility = View.GONE
                    }
                }

                // AI Score (deterministic based on fixture ID)
                val aiScore = 65 + (fixture.fixture.id % 35)
                tvAiScore.text = aiScore.toString()
                tvAiScore.setTextColor(ScoreColorHelper.getScoreColor(root.context, aiScore))
                viewAiScoreIndicator.setBackgroundColor(
                    ScoreColorHelper.getScoreColor(root.context, aiScore)
                )

                // Load team logos
                Glide.with(root.context)
                    .load(fixture.teams.home.logo)
                    .placeholder(R.drawable.ic_team_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivHomeLogo)

                Glide.with(root.context)
                    .load(fixture.teams.away.logo)
                    .placeholder(R.drawable.ic_team_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivAwayLogo)

                // Click listener
                root.setOnClickListener { onMatchClick(fixture) }
            }
        }

        private fun getLeagueName(fixture: FixtureResponse): String {
            return when (fixture.league.id) {
                203 -> "Süper Lig"
                39 -> "Premier Lig"
                140 -> "La Liga"
                78 -> "Bundesliga"
                135 -> "Serie A"
                61 -> "Ligue 1"
                2 -> "Şampiyonlar Ligi"
                3 -> "Avrupa Ligi"
                else -> fixture.league.name
            }
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<FixtureResponse>() {
        override fun areItemsTheSame(oldItem: FixtureResponse, newItem: FixtureResponse): Boolean {
            return oldItem.fixture.id == newItem.fixture.id
        }

        override fun areContentsTheSame(oldItem: FixtureResponse, newItem: FixtureResponse): Boolean {
            return oldItem == newItem
        }
    }
}
