package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.R
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

    private fun setupRecyclerView() {
        adapter = OwnerBookingAdapter(
            bookings    = allBookings,
            onKonfirmasi = { booking -> updateStatus(booking, "Terkonfirmasi") },
            onBatal      = { booking ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Tolak Booking")
                    .setMessage("Tolak booking dari ${booking.customerName}?")
                    .setPositiveButton("Tolak") { _, _ -> updateStatus(booking, "Batal") }
                    .setNegativeButton("Tidak", null)
                    .show()
            },
            onWhatsapp   = { booking -> openWhatsapp(booking) }
        )
        binding.rvBooking.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooking.adapter = adapter
    }

    private fun setupChips() {
        val chips = mapOf(
            "Semua"        to binding.chipSemua,
            "Menunggu"     to binding.chipMenunggu,
            "Terkonfirmasi" to binding.chipTerkonfirmasi,
            "Selesai"      to binding.chipSelesai,
            "Batal"        to binding.chipBatal
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
        val filtered = if (filter == "Semua") allBookings
        else allBookings.filter { it.status == filter }
        adapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun loadBookings() {
        if (_binding == null) return
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
                                    fieldName    = obj.optString("field_name"),
                                    playDate     = obj.optString("play_date"),
                                    startTime    = obj.optString("start_time"),
                                    endTime      = obj.optString("end_time"),
                                    totalPrice   = obj.optDouble("total_price"),
                                    status       = obj.optString("status"),
                                    createdAt    = obj.optString("created_at")
                                )
                            )
                        }
                        applyFilter(activeFilter)
                    } else {
                        updateEmptyState(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    binding.progressBooking.visibility = View.GONE
                    updateEmptyState(true)
                }
            }
        }
    }

    private fun updateStatus(booking: OwnerBooking, newStatus: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("booking_id", booking.id)
                    put("status", newStatus)
                    put("user_id", sessionManager.getUserId())
                }
                val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(Constants.BASE_URL + "update_booking_status.php")
                    .post(body)
                    .build()
                val response = httpClient.newCall(request).execute()
                val resJson  = JSONObject(response.body?.string() ?: "{}")

                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    if (resJson.optBoolean("success")) {
                        Toast.makeText(requireContext(),
                            "Booking $newStatus", Toast.LENGTH_SHORT).show()
                        loadBookings()
                    } else {
                        Toast.makeText(requireContext(),
                            resJson.optString("message", "Gagal"), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openWhatsapp(booking: OwnerBooking) {
        val phone = booking.userPhone.replace(Regex("[^0-9]"), "")
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val waPhone = if (phone.startsWith("0")) "62${phone.substring(1)}" else phone
        val message = "Halo ${booking.customerName}, konfirmasi booking lapangan ${booking.fieldName} " +
                "pada ${booking.playDate} pukul ${booking.startTime}-${booking.endTime}."
        val intent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/$waPhone?text=${Uri.encode(message)}"))
        startActivity(intent)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (_binding == null) return
        binding.rvBooking.visibility    = if (isEmpty) View.GONE else View.VISIBLE
        binding.layoutEmpty.visibility  = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}