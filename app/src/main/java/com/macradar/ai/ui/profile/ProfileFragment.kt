package com.macradar.ai.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.macradar.ai.R
import com.macradar.ai.data.cache.ApiCache
import com.macradar.ai.data.repository.PredictionStorage

class ProfileFragment : Fragment() {

    private lateinit var storage: PredictionStorage
    private lateinit var cache: ApiCache

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = PredictionStorage(requireContext())
        cache = ApiCache(requireContext())

        loadStats(view)
        setupRows(view)
    }

    private fun loadStats(view: View) {
        val stats = storage.getStats()
        view.findViewById<TextView>(R.id.tvProfileTotal).text = stats.total.toString()
        view.findViewById<TextView>(R.id.tvProfileChecked).text = stats.checked.toString()
        view.findViewById<TextView>(R.id.tvProfileWinRate).text =
            if (stats.checked > 0) "%.0f%%".format(stats.winnerRate) else "-"
    }

    private fun setupRows(view: View) {
        view.findViewById<View>(R.id.rowClearCache).setOnClickListener {
            cache.clear()
            Toast.makeText(requireContext(), "Önbellek temizlendi. API verileri yeniden çekilecek.", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.rowClearHistory).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Tahmin Geçmişini Sıfırla")
                .setMessage("Tüm tahmin geçmişiniz ve başarı istatistikleriniz silinecek. Bu işlem geri alınamaz. Onaylıyor musunuz?")
                .setPositiveButton("Sıfırla") { _, _ ->
                    storage.clearAll()
                    loadStats(view)
                    Toast.makeText(requireContext(), "Tahmin geçmişi sıfırlandı.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Vazgeç", null)
                .show()
        }

        view.findViewById<View>(R.id.rowShare).setOnClickListener {
            val text = "Maç Radar AI ile yapay zeka destekli futbol tahminlerini keşfet! ⚽🤖"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, "Uygulamayı Paylaş"))
        }
    }
}
