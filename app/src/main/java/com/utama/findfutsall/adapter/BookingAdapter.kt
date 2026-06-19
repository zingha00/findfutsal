package com.utama.findfutsall.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.data.model.Booking
import com.utama.findfutsall.databinding.ItemBookingBinding
import com.utama.findfutsall.utils.Constants

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
            // booking.price sudah berisi hasil PriceFormatter.format() lengkap (cth: "Rp90.000"), jadi tidak perlu tambah "Rp" lagi
            tvPrice.text = booking.price
            tvStatus.text = booking.status.uppercase()

            // Warna badge status
            when (booking.status.lowercase()) {
                "mendatang", "terkonfirmasi" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_chip_active)
                    tvStatus.setTextColor(Color.WHITE)
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
                "menunggu" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_status_selesai)
                    tvStatus.setTextColor(Color.parseColor("#B45309"))
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
                "selesai" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_status_selesai)
                    tvStatus.setTextColor(Color.parseColor("#00A86B"))
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
                "batal" -> {
                    tvStatus.setBackgroundResource(com.utama.findfutsall.R.drawable.bg_status_batal)
                    tvStatus.setTextColor(Color.parseColor("#FF3B30"))
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#999999"))
                }
                else -> {
                    btnAction.text = "Detail"
                    btnAction.backgroundTintList = android.content.res.ColorStateList
                        .valueOf(Color.parseColor("#00A86B"))
                }
            }

            // Load foto dari URL server pakai Glide (bukan resource lokal)
            val context   = root.context
            val baseUrl   = Constants.BASE_URL.replace("/api/", "/")
            val photoPath = booking.fieldPhoto ?: ""

            val fullUrl = when {
                photoPath.isEmpty()        -> null
                photoPath.startsWith("http") -> photoPath
                else                        -> baseUrl + photoPath
            }

            // Placeholder & error pakai vector drawable ringan
            // (sebelumnya bg_logo + findfutsall.png ~1.7MB, ganti supaya tidak membebani render)
            if (fullUrl != null) {
                Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(com.utama.findfutsall.R.drawable.placeholder_image)
                    .error(com.utama.findfutsall.R.drawable.placeholder_image_error)
                    .centerCrop()
                    .into(ivFieldPhoto)
            } else {
                ivFieldPhoto.setImageResource(com.utama.findfutsall.R.drawable.placeholder_image)
            }

            btnAction.setOnClickListener { onActionClick(booking) }
            root.setOnClickListener { onActionClick(booking) }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateData(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }
}