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

        viewModel.statistics.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showStatsMessage("İstatistikler yükleniyor...")
                is Result.Success -> displayStatistics(result.data)
                is Result.Error -> showStatsMessage("İstatistikler şu an için kullanılamıyor.")
            }
        }

        viewModel.h2h.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showComparisonMessage("Karşılaştırma verisi yükleniyor...")
                is Result.Success -> displayH2H(result.data)
                is Result.Error -> showComparisonMessage("Karşılaştırma verisi şu an için kullanılamıyor.")
            }
        }

        viewModel.lineups.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> showSquadsMessage("Kadrolar yükleniyor...")
                is Result.Success -> displayLineups(result.data)
                is Result.Error -> showSquadsMessage("Kadro bilgisi şu an için kullanılamıyor.")
            }
        }
    }

    private fun showComparisonMessage(message: String) {
        binding.layoutComparison.removeAllViews()
        val tv = android.widget.TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
        }
        binding.layoutComparison.addView(tv)
    }

    private fun displayH2H(matches: List<com.macradar.ai.data.model.FixtureResponse>) {
        binding.layoutComparison.removeAllViews()

        if (matches.isEmpty()) {
            showComparisonMessage("Bu takımlar arasında geçmiş maç verisi bulunamadı.")
            return
        }

        val title = android.widget.TextView(requireContext()).apply {
            text = "Son ${matches.size} Karşılaşma"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dp(16))
        }
        binding.layoutComparison.addView(title)

        for (match in matches.take(10)) {
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, dp(10))
            }

            val dateTv = android.widget.TextView(requireContext()).apply {
                text = viewModel.run {
                    val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("tr"))
                    sdf.format(java.util.Date(match.fixture.timestamp * 1000))
                }
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
                textSize = 11f
            }

            val scoreTv = android.widget.TextView(requireContext()).apply {
                text = "${match.teams.home.name}  ${match.goals.home ?: 0} - ${match.goals.away ?: 0}  ${match.teams.away.name}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                textSize = 14f
                setPadding(0, dp(4), 0, 0)
            }

            row.addView(dateTv)
            row.addView(scoreTv)
            binding.layoutComparison.addView(row)

            val divider = View(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
                )
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider_color))
            }
            binding.layoutComparison.addView(divider)
        }
    }

    private fun showSquadsMessage(message: String) {
        binding.layoutSquads.removeAllViews()
        val tv = android.widget.TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
        }
        binding.layoutSquads.addView(tv)
    }

    private fun displayLineups(lineups: List<com.macradar.ai.data.model.Lineup>) {
        binding.layoutSquads.removeAllViews()

        if (lineups.size < 2) {
            showSquadsMessage("Bu maç için henüz kadro bilgisi yayınlanmadı.\nKadrolar genellikle maçtan kısa süre önce açıklanır.")
            return
        }

        for (lineup in lineups) {
            val teamHeader = android.widget.TextView(requireContext()).apply {
                text = "${lineup.team.name}${if (lineup.formation != null) " (${lineup.formation})" else ""}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(12), 0, dp(8))
            }
            binding.layoutSquads.addView(teamHeader)

            for (playerLineup in lineup.startXI) {
                val p = playerLineup.player
                val row = android.widget.TextView(requireContext()).apply {
                    text = "${p.number}.  ${p.name}" + (if (p.pos != null) "  (${p.pos})" else "")
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                    textSize = 13f
                    setPadding(0, dp(4), 0, dp(4))
                }
                binding.layoutSquads.addView(row)
            }
        }
    }

    private fun showStatsMessage(message: String) {
        binding.layoutStats.removeAllViews()
        val tv = android.widget.TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            textSize = 14f
        }
        binding.layoutStats.addView(tv)
    }

    private fun displayStatistics(stats: List<com.macradar.ai.data.model.TeamStats>) {
        binding.layoutStats.removeAllViews()

        if (stats.size < 2) {
            showStatsMessage("Bu maç için henüz istatistik bulunmuyor.\nMaç başladığında canlı istatistikler burada görünecek.")
            return
        }

        val homeStats = stats[0]
        val awayStats = stats[1]

        val homeTitle = android.widget.TextView(requireContext()).apply {
            text = "${homeStats.team.name}  vs  ${awayStats.team.name}"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, dp(16))
        }
        binding.layoutStats.addView(homeTitle)

        val homeMap = homeStats.statistics.associateBy { it.type }
        val awayMap = awayStats.statistics.associateBy { it.type }
        val allTypes = (homeMap.keys + awayMap.keys).distinct()

        for (type in allTypes) {
            val homeVal = homeMap[type]?.value?.toString() ?: "0"
            val awayVal = awayMap[type]?.value?.toString() ?: "0"
            binding.layoutStats.addView(buildStatRow(type, homeVal, awayVal))
        }
    }

    private fun buildStatRow(label: String, homeVal: String, awayVal: String): View {
        val row = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
        }

        fun cell(text: String, weight: Float, bold: Boolean): android.widget.TextView {
            return android.widget.TextView(requireContext()).apply {
                this.text = text
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                textSize = 13f
                gravity = if (weight == 1f) android.view.Gravity.CENTER else android.view.Gravity.START
                if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            }
        }

        row.addView(cell(homeVal, 1f, true))
        row.addView(cell(label, 2f, false))
        row.addView(cell(awayVal, 1f, true))
        return row
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
