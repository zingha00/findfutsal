package com.utama.findfutsall.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.data.model.Booking
import com.utama.findfutsall.databinding.ItemBookingBinding

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onActionClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        with(holder.binding) {
            tvFieldName.text = booking.fieldName
            tvCourtName.text = booking.courtName
            tvDate.text = booking.date
            tvTime.text = booking.time
            tvPrice.text = "Rp ${booking.price}"
            tvStatus.text = booking.status.uppercase()

            // Warna badge status
            when (booking.status.lowercase()) {
                "mendatang" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_active)
                    tvStatus.setTextColor(Color.WHITE)
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
                "selesai" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_status_selesai)
                    tvStatus.setTextColor(Color.parseColor("#00A86B"))
                    btnAction.text = "Beri Ulasan"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
                "dibatalkan" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_status_batal)
                    tvStatus.setTextColor(Color.parseColor("#FF3B30"))
                    btnAction.text = "Pesan Lagi"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#999999"))
                }
            }

            // Load foto
            val context = root.context
            val photoName = booking.fieldPhoto ?: ""
            val resourceId = if (photoName.isNotEmpty()) {
                context.resources.getIdentifier(photoName, "drawable", context.packageName)
            } else 0

            if (resourceId != 0) {
                ivFieldPhoto.setImageResource(resourceId)
            } else {
                ivFieldPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnAction.setOnClickListener { onActionClick(booking) }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}