package com.utama.findfutsall.ui.owner

import android.app.Activity
import android.content.Intent
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
import java.text.NumberFormat
import java.util.Locale

class OwnerBerandaFragment : Fragment() {

    companion object {
        private const val REQUEST_EDIT_VENUE = 1001
    }

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
        loadBookingTerbaru()
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
        binding.btnEditVenue.setOnClickListener { openEditVenue() }
        binding.tvSlotTersedia.setOnClickListener { showSlotDatePicker() }
    }

    private var currentMapsLink = ""

    private fun openEditVenue() {
        val intent = Intent(requireContext(), EditVenueActivity::class.java).apply {
            putExtra(EditVenueActivity.EXTRA_NAME, binding.tvVenueName.text.toString())
            putExtra(EditVenueActivity.EXTRA_ADDRESS, binding.tvVenueAddress.text.toString())
            putExtra(EditVenueActivity.EXTRA_PHONE, binding.tvVenuePhone.text.toString())
            putExtra(EditVenueActivity.EXTRA_MAPS_LINK, currentMapsLink)
            val jamParts = binding.tvVenueJam.text.toString().split(" - ")
            putExtra(EditVenueActivity.EXTRA_OPEN_TIME, jamParts.getOrNull(0) ?: "06:00")
            putExtra(EditVenueActivity.EXTRA_CLOSE_TIME, jamParts.getOrNull(1) ?: "23:00")
        }
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_EDIT_VENUE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_VENUE && resultCode == Activity.RESULT_OK) {
            loadOwnerData()
        }
    }

    /**
     * Klik "Slot Tersedia" -- buka kalender untuk cek slot jam di tanggal lain,
     * bukan cuma hari ini. Hasilnya request ulang ke get_owner_stats.php
     * dengan parameter slot_date yang dipilih.
     */
    private fun showSlotDatePicker() {
        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale("id"))
            val selectedDate = sdf.format(java.util.Date(millis))
            loadOwnerStats(selectedDate)
        }
        picker.show(parentFragmentManager, "slot_date_picker")
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

    private fun loadOwnerStats(slotDate: String = "") {
        if (_binding == null) return
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                val requestBody = mutableMapOf<String, Any>("owner_id" to userId)
                if (slotDate.isNotEmpty()) requestBody["slot_date"] = slotDate

                val response = ApiClient.instance.getOwnerStats(requestBody)
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
        currentMapsLink = data["maps_link"]?.toString() ?: ""

        if (currentMapsLink.isNotEmpty()) {
            binding.tvMapsStatus.text = "✓ Sudah diisi"
            binding.tvMapsStatus.setTextColor(android.graphics.Color.parseColor("#1A4D2E"))
        } else {
            binding.tvMapsStatus.text = "Belum diisi"
            binding.tvMapsStatus.setTextColor(android.graphics.Color.parseColor("#999999"))
        }
        val openTime  = data["openTime"]?.toString() ?: "06:00"
        val closeTime = data["closeTime"]?.toString() ?: "23:00"
        binding.tvVenueJam.text = "$openTime - $closeTime"
        val price = data["price"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        binding.tvVenueHarga.text = "Rp ${formatAngka(price.toDouble())}/jam"

        val photoUrl = data["photo"]?.toString() ?: ""
        if (photoUrl.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(photoUrl)
                .placeholder(com.utama.findfutsall.R.drawable.ic_profile)
                .error(com.utama.findfutsall.R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivOwnerPhoto)
        } else {
            binding.ivOwnerPhoto.setImageResource(com.utama.findfutsall.R.drawable.ic_profile)
        }
    }

    private fun updateStatsUI(data: Map<String, Any>) {
        if (_binding == null) return

        fun parseDouble(key: String): Double {
            return when (val v = data[key]) {
                is Double -> v
                is Int    -> v.toDouble()
                is String -> v.toDoubleOrNull() ?: 0.0
                else      -> 0.0
            }
        }
        fun parseInt(key: String): Int = parseDouble(key).toInt()

        val pendapatan   = parseDouble("total_pendapatan")
        val biaya        = parseDouble("biaya")
        val totalBooking = parseInt("total_booking")
        val slotTersedia = parseInt("slot_tersedia")
        val targetPct    = parseInt("target_pct")
        val target       = parseDouble("target")
        val pencapaian   = parseDouble("pencapaian")

        binding.tvTotalRevenue.text
        binding.tvRevenueGrowth.text = "↑ Pendapatan bulan ini"
        binding.tvPendapatan.text    = "Rp ${formatAngka(pendapatan)}"
        binding.tvBiaya.text         = "Rp ${formatAngka(biaya)}"
        binding.tvTotalBooking.text  = totalBooking.toString()
        binding.tvSlotTersedia.text  = slotTersedia.toString()
        binding.tvTargetPct.text     = "$targetPct%"
        binding.progressTarget.progress = targetPct
        binding.tvDashPencapaian.text = "Rp ${formatAngka(pencapaian)}"
        binding.tvDashTarget.text     = "Rp ${formatAngka(target)}"
    }


    private fun loadBookingTerbaru() {
        if (_binding == null) return
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getOwnerBookings(mapOf("user_id" to userId))
                if (_binding == null) return@launch
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val list = body["bookings"] as? List<Map<String, Any>> ?: emptyList()
                        renderBookingTerbaru(list.take(3))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BERANDA", "Error loadBookingTerbaru: ${e.message}")
            }
        }
    }

    private fun renderBookingTerbaru(bookings: List<Map<String, Any>>) {
        if (_binding == null) return
        binding.containerBookingTerbaru.removeAllViews()

        if (bookings.isEmpty()) {
            val emptyText = android.widget.TextView(requireContext()).apply {
                text = "Belum ada booking terbaru"
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#999999"))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            binding.containerBookingTerbaru.addView(emptyText)
            return
        }

        bookings.forEachIndexed { index, booking ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(com.utama.findfutsall.R.layout.item_booking_terbaru_mini, binding.containerBookingTerbaru, false)

            val tvName   = itemView.findViewById<android.widget.TextView>(com.utama.findfutsall.R.id.tvMiniCustomerName)
            val tvField  = itemView.findViewById<android.widget.TextView>(com.utama.findfutsall.R.id.tvMiniFieldName)
            val tvDate   = itemView.findViewById<android.widget.TextView>(com.utama.findfutsall.R.id.tvMiniDate)
            val tvStatus = itemView.findViewById<android.widget.TextView>(com.utama.findfutsall.R.id.tvMiniStatus)

            tvName.text  = booking["customer_name"]?.toString() ?: "-"
            tvField.text = booking["field_name"]?.toString() ?: "-"
            val playDate = booking["play_date"]?.toString() ?: ""
            val startTime = booking["start_time"]?.toString() ?: ""
            tvDate.text = "$playDate • $startTime"

            val status = booking["status"]?.toString() ?: "-"
            tvStatus.text = status
            val (bgRes, textColor) = when (status) {
                "Terkonfirmasi" -> com.utama.findfutsall.R.drawable.bg_chip_active to android.graphics.Color.WHITE
                "Selesai"       -> com.utama.findfutsall.R.drawable.bg_status_selesai to android.graphics.Color.parseColor("#00A86B")
                "Menunggu"      -> com.utama.findfutsall.R.drawable.bg_slot_booked to android.graphics.Color.parseColor("#B45309")
                else            -> com.utama.findfutsall.R.drawable.bg_status_batal to android.graphics.Color.parseColor("#FF3B30")
            }
            tvStatus.setBackgroundResource(bgRes)
            tvStatus.setTextColor(textColor)

            itemView.setOnClickListener {
                (activity as? OwnerDashboardActivity)?.navigateTo(1) // pindah ke tab Booking
            }

            binding.containerBookingTerbaru.addView(itemView)

            // Divider antar item, kecuali item terakhir
            if (index < bookings.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { topMargin = 12; bottomMargin = 12 }
                    setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
                }
                binding.containerBookingTerbaru.addView(divider)
            }
        }
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