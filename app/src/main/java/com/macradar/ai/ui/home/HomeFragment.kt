package com.macradar.ai.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.macradar.ai.R
import com.macradar.ai.data.model.FixtureResponse
import com.macradar.ai.data.repository.Result
import com.macradar.ai.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var matchAdapter: MatchListAdapter
    private lateinit var liveMatchAdapter: LiveMatchAdapter
    private lateinit var topPredictionsAdapter: TopPredictionsAdapter
    private var isLiveTab = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupTabLayout()
        setupObservers()
        setupTopPredictions()
        binding.tvDailyLimit.visibility = View.GONE
        binding.progressDailyLimit.visibility = View.GONE
        viewModel.loadTodayMatches()
    }

    private fun setupAdapters() {
        matchAdapter = MatchListAdapter { fixture -> navigateToMatchDetail(fixture) }
        liveMatchAdapter = LiveMatchAdapter(
            onPredictClick = { fixture -> viewModel.requestLiveGoalPrediction(fixture) },
            onCardClick = { fixture -> navigateToMatchDetail(fixture) }
        )
        binding.rvMatches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matchAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
            setItemViewCacheSize(20)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        isLiveTab = false
                        binding.rvMatches.adapter = matchAdapter
                        viewModel.loadTodayMatches()
                    }
                    1 -> {
                        isLiveTab = false
                        binding.rvMatches.adapter = matchAdapter
                        viewModel.loadTomorrowMatches()
                    }
                    2 -> {
                        isLiveTab = true
                        binding.rvMatches.adapter = liveMatchAdapter
                        viewModel.loadLiveMatches()
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupTopPredictions() {
        topPredictionsAdapter = TopPredictionsAdapter { fixtureId ->
            val bundle = Bundle().apply { putInt("fixtureId", fixtureId) }
            findNavController().navigate(R.id.matchDetailFragment, bundle)
        }
        binding.rvTopPredictions.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = topPredictionsAdapter
        }
        viewModel.loadTopPredictions()
    }

    private fun setupObservers() {
        viewModel.matches.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.rvMatches.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.tvNoMatches.visibility = View.VISIBLE
                        binding.tvNoMatches.text = if (isLiveTab) {
                            "Şu anda devam eden canlı maç bulunmuyor."
                        } else {
                            "Maç bulunamadı."
                        }
                        binding.rvMatches.visibility = View.GONE
                    } else {
                        binding.tvNoMatches.visibility = View.GONE
                        binding.rvMatches.visibility = View.VISIBLE
                        if (isLiveTab) {
                            liveMatchAdapter.submitList(result.data)
                        } else {
                            matchAdapter.submitList(result.data)
                        }
                    }
                    binding.layoutError.visibility = View.GONE
                }
                is Result.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.rvMatches.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvError.text = result.message
                }
            }
        }
        viewModel.topPredictions.observe(viewLifecycleOwner) { predictions ->
            topPredictionsAdapter.submitList(predictions)
        }
        viewModel.liveGoalPredictions.observe(viewLifecycleOwner) { labels ->
            if (isLiveTab) {
                liveMatchAdapter.setPredictionLabels(labels)
            }
        }
    }

    private fun navigateToMatchDetail(fixture: FixtureResponse) {
        val bundle = Bundle().apply { putInt("fixtureId", fixture.fixture.id) }
        findNavController().navigate(R.id.matchDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
