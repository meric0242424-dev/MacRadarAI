package com.macradar.ai.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.macradar.ai.R
import com.macradar.ai.data.model.PredictionModel
import com.macradar.ai.data.repository.Result
import com.macradar.ai.databinding.FragmentMatchDetailBinding
import com.macradar.ai.utils.ScoreColorHelper

class MatchDetailFragment : Fragment() {

    private var _binding: FragmentMatchDetailBinding? = null
    private val binding get() = _binding!!

    private val fixtureId: Int by lazy { arguments?.getInt("fixtureId") ?: 0 }

    private val viewModel: MatchDetailViewModel by viewModels {
        MatchDetailViewModelFactory(requireActivity().application, fixtureId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnShare.setOnClickListener { shareMatch() }
        setupTabLayout()
        setupObservers()
        viewModel.loadMatchDetail()
        viewModel.loadPrediction()
    }

    private fun setupTabLayout() {
        listOf("Genel", "İstatistik", "Karşılaştırma", "Kadrolar").forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it))
        }
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showGeneralTab()
                    1 -> showStatsTab()
                    2 -> showComparisonTab()
                    3 -> showSquadsTab()
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupObservers() {
        viewModel.fixture.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val fixture = result.data
                    binding.tvLeagueName.text = viewModel.getLeagueName()
                    binding.tvMatchDate.text = viewModel.getMatchDate()
                    binding.tvHomeTeam.text = fixture.teams.home.name
                    binding.tvAwayTeam.text = fixture.teams.away.name
                    Glide.with(this).load(fixture.teams.home.logo)
                        .placeholder(R.drawable.ic_team_placeholder).into(binding.ivHomeTeam)
                    Glide.with(this).load(fixture.teams.away.logo)
                        .placeholder(R.drawable.ic_team_placeholder).into(binding.ivAwayTeam)

                    val status = fixture.fixture.status.short
                    if (status == "NS") {
                        binding.tvMatchScore.text = viewModel.getMatchTime()
                        binding.tvMatchScore.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
                    } else if (status in listOf("1H", "HT", "2H", "ET")) {
                        val elapsed = fixture.fixture.status.elapsed ?: 0
                        binding.tvMatchScore.text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}  ${elapsed}'"
                        binding.tvMatchScore.setTextColor(ContextCompat.getColor(requireContext(), R.color.live_red))
                    } else {
                        binding.tvMatchScore.text = "${fixture.goals.home ?: 0} - ${fixture.goals.away ?: 0}"
                        binding.tvMatchScore.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    }
                }
                is Result.Error -> binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.prediction.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.cardPrediction.visibility = View.GONE
                    binding.progressPrediction.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    binding.progressPrediction.visibility = View.GONE
                    binding.cardPrediction.visibility = View.VISIBLE
                    displayPrediction(result.data)
                }
                is Result.Error -> binding.progressPrediction.visibility = View.GONE
            }
        }
    }

    private fun displayPrediction(pred: PredictionModel) {
        binding.apply {
            tvConfidenceScore.text = pred.confidenceScore.toString()
            circularProgressBar.progress = pred.confidenceScore
            circularProgressBar.progressTintList = android.content.res.ColorStateList.valueOf(
                ScoreColorHelper.getScoreColor(requireContext(), pred.confidenceScore)
            )

            tvHomeWinPercent.text = "%${pred.homeWinProbability}"
            tvDrawPercent.text = "%${pred.drawProbability}"
            tvAwayWinPercent.text = "%${pred.awayWinProbability}"

            tvRiskLevel.text = pred.riskLevel
            tvRiskLevel.setTextColor(ScoreColorHelper.getRiskColor(requireContext(), pred.riskLevel))

            setupGoalBar(progressOver25, pred.over25Probability)
            tvOver25Percent.text = "%${pred.over25Probability}"
            setupGoalBar(progressUnder25, pred.under25Probability)
            tvUnder25Percent.text = "%${pred.under25Probability}"

            setupGoalBar(progressOver35, pred.over35Probability)
            tvOver35Percent.text = "%${pred.over35Probability}"
            setupGoalBar(progressUnder35, pred.under35Probability)
            tvUnder35Percent.text = "%${pred.under35Probability}"

            setupGoalBar(progressBttsYes, pred.bttsYesProbability)
            tvBttsYesPercent.text = "%${pred.bttsYesProbability}"
            setupGoalBar(progressBttsNo, pred.bttsNoProbability)
            tvBttsNoPercent.text = "%${pred.bttsNoProbability}"

            setupGoalBar(progressHtGoalYes, pred.htGoalYesProbability)
            tvHtGoalYesPercent.text = "%${pred.htGoalYesProbability}"
            setupGoalBar(progressHtGoalNo, pred.htGoalNoProbability)
            tvHtGoalNoPercent.text = "%${pred.htGoalNoProbability}"

            val maxProb = maxOf(pred.homeWinProbability, pred.drawProbability, pred.awayWinProbability)
            tvPredictedWinner.text = when (maxProb) {
                pred.homeWinProbability -> "${tvHomeTeam.text} Kazanır"
                pred.awayWinProbability -> "${tvAwayTeam.text} Kazanır"
                else -> "Beraberlik Bekleniyor"
            }
            tvPredictedWinnerProb.text = "%$maxProb olasılıkla"

            cardPrediction.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_fade_in))
        }
    }

    private fun setupGoalBar(bar: android.widget.ProgressBar, percent: Int) {
        bar.max = 100; bar.progress = percent
    }

    private fun showGeneralTab() {
        binding.scrollViewGeneral.visibility = View.VISIBLE
        binding.layoutStats.visibility = View.GONE
        binding.layoutComparison.visibility = View.GONE
        binding.layoutSquads.visibility = View.GONE
    }

    private fun showStatsTab() {
        binding.scrollViewGeneral.visibility = View.GONE
        binding.layoutStats.visibility = View.VISIBLE
        binding.layoutComparison.visibility = View.GONE
        binding.layoutSquads.visibility = View.GONE
        viewModel.loadStatistics()
    }

    private fun showComparisonTab() {
        binding.scrollViewGeneral.visibility = View.GONE
        binding.layoutStats.visibility = View.GONE
        binding.layoutComparison.visibility = View.VISIBLE
        binding.layoutSquads.visibility = View.GONE
        viewModel.loadH2H()
    }

    private fun showSquadsTab() {
        binding.scrollViewGeneral.visibility = View.GONE
        binding.layoutStats.visibility = View.GONE
        binding.layoutComparison.visibility = View.GONE
        binding.layoutSquads.visibility = View.VISIBLE
        viewModel.loadLineups()
    }

    private fun shareMatch() {
        val fixture = (viewModel.fixture.value as? Result.Success)?.data ?: return
        val pred = (viewModel.prediction.value as? Result.Success)?.data
        val text = buildString {
            appendLine("⚽ MAÇ RADAR AI TAHMİNİ")
            appendLine("${fixture.teams.home.name} vs ${fixture.teams.away.name}")
            if (pred != null) {
                val maxProb = maxOf(pred.homeWinProbability, pred.drawProbability, pred.awayWinProbability)
                val winnerText = when (maxProb) {
                    pred.homeWinProbability -> "${fixture.teams.home.name} Kazanır"
                    pred.awayWinProbability -> "${fixture.teams.away.name} Kazanır"
                    else -> "Beraberlik"
                }
                appendLine("🏆 Tahmin: $winnerText (%$maxProb)")
                appendLine("🎯 Güven: ${pred.confidenceScore}/100  |  Risk: ${pred.riskLevel}")
                appendLine("📊 Ev %${pred.homeWinProbability}  |  Beraberlik %${pred.drawProbability}  |  Deplasman %${pred.awayWinProbability}")
                appendLine("📈 2.5 Üst: %${pred.over25Probability}  |  3.5 Üst: %${pred.over35Probability}  |  KG Var: %${pred.bttsYesProbability}")
            }
            appendLine("#MacRadarAI")
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, text)
        }
        startActivity(android.content.Intent.createChooser(intent, "Tahmini Paylaş"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
