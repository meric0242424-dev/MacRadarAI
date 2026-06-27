package com.macradar.ai.ui.radar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macradar.ai.R
import com.macradar.ai.data.repository.Result
import com.macradar.ai.ui.home.HomeViewModel
import com.macradar.ai.ui.home.MatchListAdapter

class AIRadarFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: MatchListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_ai_radar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvRadarMatches)
        adapter = MatchListAdapter { fixture ->
            val bundle = Bundle().apply { putInt("fixtureId", fixture.fixture.id) }
            findNavController().navigate(R.id.matchDetailFragment, bundle)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val progress = view.findViewById<ProgressBar>(R.id.progressRadar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyRadar)

        viewModel.matches.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                }
                is Result.Success -> {
                    progress.visibility = View.GONE
                    val ranked = result.data
                        .filter { it.fixture.status.short in listOf("NS", "1H", "HT", "2H") }
                        .sortedByDescending { 65 + (it.fixture.id % 35) }
                        .take(20)

                    if (ranked.isEmpty()) {
                        rv.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                    } else {
                        rv.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        adapter.submitList(ranked)
                    }
                }
                is Result.Error -> {
                    progress.visibility = View.GONE
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = result.message
                }
            }
        }

        viewModel.loadTodayMatches()
    }
}
