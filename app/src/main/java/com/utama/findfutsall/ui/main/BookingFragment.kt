package com.utama.findfutsall.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.BookingAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.Booking
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class BookingFragment : Fragment() {

    private lateinit var session: SessionManager
    private lateinit var rvBooking: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var adapter: BookingAdapter
    private val allBookings = mutableListOf<Booking>()
    private var activeFilter = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_booking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session     = SessionManager(requireContext())
        rvBooking   = view.findViewById(R.id.rvBooking)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)

        adapter = BookingAdapter(emptyList()) { booking ->
            openDetail(booking)
        }
        rvBooking.layoutManager = LinearLayoutManager(requireContext())
        rvBooking.adapter = adapter

        setupChips(view)
        loadBookings()
    }

    private fun openDetail(booking: Booking) {
        val intent = Intent(requireContext(), BookingDetailActivity::class.java).apply {
            putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.id)
            putExtra(BookingDetailActivity.EXTRA_FIELD_NAME, booking.fieldName)
            putExtra(BookingDetailActivity.EXTRA_FIELD_ADDRESS, booking.courtName)
            putExtra(BookingDetailActivity.EXTRA_FIELD_PHOTO, booking.fieldPhoto ?: "")
            putExtra(BookingDetailActivity.EXTRA_DATE, booking.date)
            putExtra(BookingDetailActivity.EXTRA_TIME, booking.time)
            putExtra(BookingDetailActivity.EXTRA_PRICE, "Rp ${booking.price}")
            putExtra(BookingDetailActivity.EXTRA_STATUS, booking.status)
            putExtra(BookingDetailActivity.EXTRA_CREATED_AT, booking.createdAt)
        }
        startActivity(intent)
    }

    private fun setupChips(view: View) {
        val chips = mapOf(
            "Semua"        to view.findViewById<TextView>(R.id.chipSemua),
            "Menunggu"     to view.findViewById(R.id.chipMenunggu),
            "Terkonfirmasi" to view.findViewById(R.id.chipDikonfirmasi),
            "Selesai"      to view.findViewById(R.id.chipSelesai),
            "Batal"        to view.findViewById(R.id.chipDibatalkan)
        )
        chips.forEach { (filter, chip) ->
            chip.setOnClickListener {
                activeFilter = filter
                chips.values.forEach {
                    it.setBackgroundResource(R.drawable.bg_chip_inactive)
                    it.setTextColor(Color.parseColor("#121212"))
                }
                chip.setBackgroundResource(R.drawable.bg_chip_active)
                chip.setTextColor(Color.WHITE)
                applyFilter(filter)
            }
        }
    }

    private fun applyFilter(filter: String) {
        val filtered = if (filter == "Semua") allBookings
        else allBookings.filter { it.status.equals(filter, ignoreCase = true) }
        adapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
    }

    private fun loadBookings() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getUserBookings(
                    mapOf("user_id" to userId)
                )
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val list = body["bookings"] as? List<Map<String, Any>> ?: emptyList()
                        allBookings.clear()
                        allBookings.addAll(list.map {
                            val start = it["start_time"]?.toString() ?: ""
                            val end   = it["end_time"]?.toString() ?: ""
                            Booking(
                                id          = (it["id"] as? Double)?.toInt() ?: 0,
                                fieldName   = it["field_name"]?.toString() ?: "",
                                courtName   = it["field_address"]?.toString() ?: "",
                                date        = it["play_date"]?.toString() ?: "",
                                time        = "$start - $end",
                                price       = formatPrice((it["total_price"] as? Double) ?: 0.0),
                                status      = it["status"]?.toString() ?: "",
                                fieldPhoto  = it["field_photo"]?.toString(),
                                startTime   = start,
                                endTime     = end,
                                totalPrice  = (it["total_price"] as? Double) ?: 0.0,
                                createdAt   = it["created_at"]?.toString() ?: ""
                            )
                        })
                        applyFilter(activeFilter)
                    }
                }
            } catch (e: Exception) {
                updateEmptyState(true)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        rvBooking.visibility   = if (isEmpty) View.GONE else View.VISIBLE
        layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun formatPrice(price: Double): String {
        return String.format("%,.0f", price).replace(",", ".")
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }
}