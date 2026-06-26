package com.macradar.ai.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.macradar.ai.R
import com.macradar.ai.data.model.TopPrediction
import com.macradar.ai.databinding.ItemTopPredictionBinding
import com.macradar.ai.utils.ScoreColorHelper

class TopPredictionsAdapter(
    private val onItemClick: (Int) -> Unit  // passes fixtureId
) : ListAdapter<TopPrediction, TopPredictionsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemTopPredictionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopPrediction) {
            binding.apply {
                tvRank.text = "#${item.rank}"
                tvMatchName.text = "${item.homeTeam} - ${item.awayTeam}"
                tvLeague.text = item.leagueName
                tvConfidence.text = "${item.confidence}"
                tvConfidence.setTextColor(ScoreColorHelper.getScoreColor(root.context, item.confidence))

                Glide.with(root.context).load(item.homeLogo)
                    .placeholder(R.drawable.ic_team_placeholder).into(ivHomeLogo)
                Glide.with(root.context).load(item.awayLogo)
                    .placeholder(R.drawable.ic_team_placeholder).into(ivAwayLogo)

                root.setOnClickListener { onItemClick(item.fixtureId) }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TopPrediction>() {
        override fun areItemsTheSame(a: TopPrediction, b: TopPrediction) = a.fixtureId == b.fixtureId
        override fun areContentsTheSame(a: TopPrediction, b: TopPrediction) = a == b
    }
}
