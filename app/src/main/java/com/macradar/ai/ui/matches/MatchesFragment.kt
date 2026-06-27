package com.macradar.ai.ui.matches

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.macradar.ai.R
import com.macradar.ai.data.model.FixtureResponse
import com.macradar.ai.data.repository.Result
import com.macradar.ai.ui.home.HomeViewModel
import com.macradar.ai.ui.home.MatchListAdapter

class MatchesFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: MatchListAdapter
    private var dayFixtures: List<FixtureResponse> = emptyList()
    private var currentQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_matches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvAllMatches)
        adapter = MatchListAdapter { fixture ->
            val bundle = Bundle().apply { putInt("fixtureId", fixture.fixture.id) }
            findNavController().navigate(R.id.matchDetailFragment, bundle)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutMatches)
        tabLayout.addTab(tabLayout.newTab().setText("Bugün"))
        tabLayout.addTab(tabLayout.newTab().setText("Yarın"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) loadToday(view) else loadTomorrow(view)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        view.findViewById<EditText>(R.id.etSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                applyFilter(view)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        observeMatches(view)
        loadToday(view)
    }

    private fun loadToday(view: View) {
        viewModel.loadTodayMatches()
    }

    private fun loadTomorrow(view: View) {
        viewModel.loadTomorrowMatches()
    }

    private fun observeMatches(view: View) {
        val progress = view.findViewById<ProgressBar>(R.id.progressMatches)
        val rv = view.findViewById<RecyclerView>(R.id.rvAllMatches)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyMatches)

        viewModel.matches.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    progress.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.GONE
                }
                is Result.Success -> {
                    progress.visibility = View.GONE
                    dayFixtures = result.data
                    applyFilter(view)
                }
                is Result.Error -> {
                    progress.visibility = View.GONE
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = result.message
                }
            }
        }
    }

    private fun applyFilter(view: View) {
        val rv = view.findViewById<RecyclerView>(R.id.rvAllMatches)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyMatches)

        val filtered = if (currentQuery.isBlank()) {
            dayFixtures
        } else {
            val q = currentQuery.lowercase()
            dayFixtures.filter {
                it.teams.home.name.lowercase().contains(q) ||
                it.teams.away.name.lowercase().contains(q) ||
                it.league.name.lowercase().contains(q)
            }
        }

        if (filtered.isEmpty()) {
            rv.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = if (currentQuery.isBlank()) "Bugün için maç bulunamadı." else "\"$currentQuery\" için sonuç bulunamadı."
        } else {
            rv.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            adapter.submitList(filtered)
        }
    }
}
