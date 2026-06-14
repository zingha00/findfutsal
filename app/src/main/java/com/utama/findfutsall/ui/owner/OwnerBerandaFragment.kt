package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.FragmentOwnerBerandaBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OwnerBerandaFragment : Fragment() {

    private var _binding: FragmentOwnerBerandaBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupHeader()
        setupMenuClicks()
        loadOwnerData()
        loadOwnerStats()
    }

    private fun setupHeader() {
        binding.tvOwnerName.text = sessionManager.getUserName() ?: "Arena Manager"
    }

    private fun setupMenuClicks() {
        binding.cardPesanan.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(1) // Booking
        }
        binding.cardLapangan.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(2) // Lapangan
        }
        binding.cardJadwal.setOnClickListener {
            Toast.makeText(requireContext(), "Jadwal - segera hadir", Toast.LENGTH_SHORT).show()
        }
        binding.cardLaporan.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(3) // Keuangan
        }
        binding.tvLihatSemua.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(1) // Booking
        }
        binding.btnEditVenue.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Venue - segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadOwnerData() {
        if (_binding == null) return
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getOwnerData(mapOf("user_id" to userId))
                if (_binding == null) return@launch
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val data = body["data"] as? Map<String, Any>
                        data?.let { updateVenueUI(it) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BERANDA", "Error loadOwnerData: ${e.message}")
            }
        }
    }

    private fun loadOwnerStats() {
        if (_binding == null) return
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getOwnerStats(mapOf("owner_id" to userId))
                if (_binding == null) return@launch
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        updateStatsUI(body)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BERANDA", "Error loadOwnerStats: ${e.message}")
            }
        }
    }

    private fun updateVenueUI(data: Map<String, Any>) {
        if (_binding == null) return
        binding.tvVenueName.text    = data["name"]?.toString() ?: "-"
        binding.tvVenueAddress.text = data["address"]?.toString() ?: "-"
        binding.tvVenuePhone.text   = data["phone"]?.toString() ?: "-"
        val openTime  = data["openTime"]?.toString() ?: "06:00"
        val closeTime = data["closeTime"]?.toString() ?: "23:00"
        binding.tvVenueJam.text = "$openTime - $closeTime"
        val price = data["price"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        binding.tvVenueHarga.text = "Rp ${formatAngka(price.toDouble())}/jam"
    }

    private fun updateStatsUI(data: Map<String, Any>) {
        if (_binding == null) return

        val pendapatan   = data["total_pendapatan"]?.toString()?.toDoubleOrNull() ?: 0.0
        val biaya        = data["biaya"]?.toString()?.toDoubleOrNull() ?: 0.0
        val totalBooking = data["total_booking"]?.toString()?.toIntOrNull() ?: 0
        val slotTersedia = data["slot_tersedia"]?.toString()?.toIntOrNull() ?: 0
        val targetPct    = data["target_pct"]?.toString()?.toIntOrNull() ?: 0
        val target       = data["target"]?.toString()?.toDoubleOrNull() ?: 0.0
        val pencapaian   = data["pencapaian"]?.toString()?.toDoubleOrNull() ?: 0.0

        binding.tvTotalRevenue.text  = "Rp ${formatAngka(pendapatan)}"
        binding.tvRevenueGrowth.text = "↑ Pendapatan bulan ini"
        binding.tvPendapatan.text    = "Rp ${formatAngka(pendapatan)}"
        binding.tvBiaya.text         = "Rp ${formatAngka(biaya)}"
        binding.tvTotalBooking.text  = totalBooking.toString()
        binding.tvSlotTersedia.text  = slotTersedia.toString()
        binding.tvTargetPct.text     = "$targetPct%"
        binding.progressTarget.progress = targetPct
    }

    private fun formatAngka(amount: Double): String {
        val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return fmt.format(amount.toLong())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}