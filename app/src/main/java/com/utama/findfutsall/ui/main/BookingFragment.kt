package com.utama.findfutsall.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.utama.findfutsall.R
import com.utama.findfutsall.adapter.BookingAdapter
import com.utama.findfutsall.data.model.Booking
import com.utama.findfutsall.databinding.FragmentBookingBinding

class BookingFragment : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookingAdapter: BookingAdapter
    private var allBookings = listOf<Booking>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        // Tidak ada dummy data — tampilkan kosong sampai API booking tersedia
        checkEmpty(emptyList())
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(emptyList()) { _ -> }
        binding.rvBooking.adapter = bookingAdapter
    }

    private fun setupChips() {
        binding.chipSemua.setOnClickListener {
            setActiveChip(binding.chipSemua)
            bookingAdapter.updateData(allBookings)
            checkEmpty(allBookings)
        }
        binding.chipMenunggu.setOnClickListener {
            setActiveChip(binding.chipMenunggu)
            val filtered = allBookings.filter { it.status.lowercase() == "mendatang" }
            bookingAdapter.updateData(filtered)
            checkEmpty(filtered)
        }
        binding.chipDikonfirmasi.setOnClickListener {
            setActiveChip(binding.chipDikonfirmasi)
            val filtered = allBookings.filter { it.status.lowercase() == "dikonfirmasi" }
            bookingAdapter.updateData(filtered)
            checkEmpty(filtered)
        }
        binding.chipSelesai.setOnClickListener {
            setActiveChip(binding.chipSelesai)
            val filtered = allBookings.filter { it.status.lowercase() == "selesai" }
            bookingAdapter.updateData(filtered)
            checkEmpty(filtered)
        }
        binding.chipDibatalkan.setOnClickListener {
            setActiveChip(binding.chipDibatalkan)
            val filtered = allBookings.filter { it.status.lowercase() == "dibatalkan" }
            bookingAdapter.updateData(filtered)
            checkEmpty(filtered)
        }
    }

    private fun setActiveChip(activeChip: TextView) {
        listOf(
            binding.chipSemua, binding.chipMenunggu, binding.chipDikonfirmasi,
            binding.chipSelesai, binding.chipDibatalkan
        ).forEach {
            it.setBackgroundResource(R.drawable.bg_chip_inactive)
            it.setTextColor(Color.parseColor("#121212"))
        }
        activeChip.setBackgroundResource(R.drawable.bg_chip_active)
        activeChip.setTextColor(Color.WHITE)
    }

    private fun checkEmpty(list: List<Booking>) {
        if (_binding == null) return
        binding.layoutEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.rvBooking.visibility   = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}