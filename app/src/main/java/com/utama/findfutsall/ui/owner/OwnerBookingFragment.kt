package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.OwnerBookingAdapter
import com.utama.findfutsall.data.model.OwnerBooking
import com.utama.findfutsall.databinding.FragmentOwnerBookingBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OwnerBookingFragment : Fragment() {

    private var _binding: FragmentOwnerBookingBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OwnerBookingAdapter
    private val allBookings = mutableListOf<OwnerBooking>()
    private var activeFilter = "Semua"
    private var isLoading = false

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        setupChips()
        loadBookings()
    }

    override fun onResume() {
        super.onResume()
        // Refresh setiap kembali dari halaman Detail (kalau status sudah diubah di sana)
        if (_binding != null) loadBookings()
    }

    private fun setupRecyclerView() {
        // Sekarang adapter HANYA punya satu aksi: buka Detail.
        // Konfirmasi/Tolak/WhatsApp dipindah ke OwnerBookingDetailActivity.
        adapter = OwnerBookingAdapter(
            bookings = allBookings,
            onDetail = { booking -> openDetail(booking) }
        )
        binding.rvBooking.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooking.adapter = adapter
    }

    private fun openDetail(booking: OwnerBooking) {
        val intent = Intent(requireContext(), OwnerBookingDetailActivity::class.java).apply {
            putExtra(OwnerBookingDetailActivity.EXTRA_BOOKING_ID, booking.id)
            putExtra(OwnerBookingDetailActivity.EXTRA_USER_NAME, booking.userName)
            putExtra(OwnerBookingDetailActivity.EXTRA_USER_PHONE, booking.userPhone)
            putExtra(OwnerBookingDetailActivity.EXTRA_USER_PHOTO, booking.userPhoto)
            putExtra(OwnerBookingDetailActivity.EXTRA_USER_EMAIL, booking.userEmail)
            putExtra(OwnerBookingDetailActivity.EXTRA_FIELD_NAME, booking.fieldName)
            putExtra(OwnerBookingDetailActivity.EXTRA_FIELD_ADDRESS, booking.fieldAddress)
            putExtra(OwnerBookingDetailActivity.EXTRA_FIELD_PHOTO, booking.fieldPhoto)
            putExtra(OwnerBookingDetailActivity.EXTRA_PLAY_DATE, booking.playDate)
            putExtra(OwnerBookingDetailActivity.EXTRA_START_TIME, booking.startTime)
            putExtra(OwnerBookingDetailActivity.EXTRA_END_TIME, booking.endTime)
            putExtra(OwnerBookingDetailActivity.EXTRA_TOTAL_PRICE, booking.totalPrice)
            putExtra(OwnerBookingDetailActivity.EXTRA_STATUS, booking.status)
            putExtra(OwnerBookingDetailActivity.EXTRA_CREATED_AT, booking.createdAt)
            putExtra(OwnerBookingDetailActivity.EXTRA_PAYMENT_METHOD, booking.paymentMethod)
            putExtra(OwnerBookingDetailActivity.EXTRA_PROOF_IMAGE, booking.proofImage)
        }
        startActivity(intent)
    }

    private fun setupChips() {
        val chips = mapOf(
            "Semua"         to binding.chipSemua,
            "Menunggu"      to binding.chipMenunggu,
            "Terkonfirmasi" to binding.chipTerkonfirmasi,
            "Selesai"       to binding.chipSelesai,
            "Batal"         to binding.chipBatal
        )
        chips.forEach { (filter, chip) ->
            chip.setOnClickListener {
                activeFilter = filter
                setActiveChip(chips, chip)
                applyFilter(filter)
            }
        }
    }

    private fun setActiveChip(chips: Map<String, TextView>, active: TextView) {
        chips.values.forEach {
            it.setBackgroundResource(R.drawable.bg_chip_inactive)
            it.setTextColor(Color.parseColor("#121212"))
        }
        active.setBackgroundResource(R.drawable.bg_chip_active)
        active.setTextColor(Color.WHITE)
    }

    private fun applyFilter(filter: String) {
        val filtered = if (filter == "Semua") allBookings.toList()
        else allBookings.filter { it.status == filter }
        adapter.updateData(filtered)
        binding.rvBooking.visibility   = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadBookings() {
        if (isLoading) return
        if (_binding == null) return
        isLoading = true
        binding.progressBooking.visibility = View.VISIBLE
        val userId = sessionManager.getUserId()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply { put("user_id", userId) }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(Constants.BASE_URL + "get_owner_bookings.php")
                    .post(body)
                    .build()
                val response = httpClient.newCall(request).execute()
                val resStr   = response.body?.string() ?: "{}"
                val resJson  = JSONObject(resStr)

                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    isLoading = false
                    binding.progressBooking.visibility = View.GONE

                    if (resJson.optBoolean("success")) {
                        val arr = resJson.optJSONArray("bookings") ?: JSONArray()
                        allBookings.clear()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            allBookings.add(
                                OwnerBooking(
                                    id           = obj.optInt("id"),
                                    customerName = obj.optString("customer_name"),
                                    userName     = obj.optString("user_name"),
                                    userPhone    = obj.optString("user_phone"),
                                    userPhoto    = obj.optString("user_photo"),
                                    userEmail    = obj.optString("user_email"),
                                    fieldName    = obj.optString("field_name"),
                                    fieldAddress = obj.optString("field_address"),
                                    fieldPhoto   = obj.optString("field_photo"),
                                    playDate     = obj.optString("play_date"),
                                    startTime    = obj.optString("start_time"),
                                    endTime      = obj.optString("end_time"),
                                    totalPrice   = obj.optDouble("total_price"),
                                    status       = obj.optString("status"),
                                    createdAt    = obj.optString("created_at"),
                                    paymentMethod = obj.optString("payment_method"),
                                    proofImage   = obj.optString("proof_image")
                                )
                            )
                        }
                        applyFilter(activeFilter)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resJson.optString("message", "Gagal load booking"),
                            Toast.LENGTH_LONG
                        ).show()
                        binding.rvBooking.visibility   = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    isLoading = false
                    binding.progressBooking.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.rvBooking.visibility   = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}