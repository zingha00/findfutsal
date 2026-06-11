package com.utama.findfutsall.ui.owner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.databinding.FragmentOwnerBerandaBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

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
    }

    private fun setupHeader() {
        binding.tvOwnerName.text = sessionManager.getUserName() ?: "Arena Manager"
    }

    private fun setupMenuClicks() {
        binding.cardPesanan.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(2)
        }
        binding.cardLapangan.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(1)
        }
        binding.cardJadwal.setOnClickListener {
            Toast.makeText(requireContext(), "Jadwal - segera hadir", Toast.LENGTH_SHORT).show()
        }
        binding.cardLaporan.setOnClickListener {
            Toast.makeText(requireContext(), "Laporan - segera hadir", Toast.LENGTH_SHORT).show()
        }
        binding.tvLihatSemua.setOnClickListener {
            (activity as? OwnerDashboardActivity)?.navigateTo(2)
        }
        binding.btnEditVenue.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Venue - segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadOwnerData() {
        if (_binding == null) return
        val userId = sessionManager.getUserId()
        android.util.Log.d("OWNER_DEBUG", "userId=$userId")

        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getOwnerData(mapOf("user_id" to userId))

                if (_binding == null) return@launch

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val data = body["data"] as? Map<String, Any>
                        data?.let { updateUI(it) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("OWNER_DEBUG", "Error: ${e.message}")
            }
        }
    }

    private fun updateUI(data: Map<String, Any>) {
        if (_binding == null) return

        binding.tvVenueName.text    = data["name"]?.toString() ?: "-"
        binding.tvVenueAddress.text = data["address"]?.toString() ?: "-"
        binding.tvVenuePhone.text   = data["phone"]?.toString() ?: "-"

        val openTime  = data["openTime"]?.toString() ?: "06:00"
        val closeTime = data["closeTime"]?.toString() ?: "23:00"
        binding.tvVenueJam.text = "$openTime - $closeTime"

        val price = data["price"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        binding.tvVenueHarga.text = "Rp ${String.format("%,d", price).replace(',', '.')}/jam"

        val totalFields = data["total_fields"]?.toString()?.toIntOrNull() ?: 0
        binding.tvTotalBooking.text = totalFields.toString()
        binding.tvSlotTersedia.text = totalFields.toString()
        binding.tvTotalRevenue.text = "Rp 0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}