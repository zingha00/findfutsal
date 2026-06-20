package com.utama.findfutsall.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.OwnerBooking
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.PriceFormatter
import de.hdodenhof.circleimageview.CircleImageView

class OwnerBookingAdapter(
    bookings: List<OwnerBooking>,
    private val onDetail: (OwnerBooking) -> Unit
) : RecyclerView.Adapter<OwnerBookingAdapter.ViewHolder>() {

    // Selalu buat list BARU (independent copy), jangan pegang referensi list asli dari luar
    private var bookings: MutableList<OwnerBooking> = bookings.toMutableList()

    fun updateData(newBookings: List<OwnerBooking>) {
        bookings = newBookings.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookingId: TextView         = itemView.findViewById(R.id.tvBookingId)
        private val tvStatus: TextView            = itemView.findViewById(R.id.tvBookingStatus)
        private val tvCustomer: TextView          = itemView.findViewById(R.id.tvCustomerName)
        private val ivUserPhoto: CircleImageView  = itemView.findViewById(R.id.ivUserPhoto)
        private val tvFieldName: TextView         = itemView.findViewById(R.id.tvFieldName)
        private val tvPlayDate: TextView          = itemView.findViewById(R.id.tvPlayDate)
        private val tvTotalPrice: TextView        = itemView.findViewById(R.id.tvTotalPrice)
        private val btnDetail: MaterialButton     = itemView.findViewById(R.id.btnDetail)

        fun bind(booking: OwnerBooking) {
            tvBookingId.text  = "#BK-${String.format("%03d", booking.id)}"
            tvCustomer.text   = booking.customerName
            tvFieldName.text  = booking.fieldName
            tvPlayDate.text   = "${formatDate(booking.playDate)} | ${booking.startTime} - ${booking.endTime}"
            tvTotalPrice.text = PriceFormatter.format(booking.totalPrice)
            tvStatus.text     = booking.status

            // Foto user -- gabungkan dengan BASE_URL kalau masih path relatif dari server
            val context     = itemView.context
            val photoName   = booking.userPhoto
            val baseUrl     = Constants.BASE_URL.replace("/api/", "/")
            val userPhotoUrl = when {
                photoName.isEmpty()          -> null
                photoName.startsWith("http") -> photoName
                else                         -> "$baseUrl$photoName"
            }

            if (userPhotoUrl != null) {
                Glide.with(context)
                    .load(userPhotoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_profile)
                    .into(ivUserPhoto)
            } else {
                ivUserPhoto.setImageResource(R.drawable.ic_profile)
            }

            // Warna badge status saja -- aksi Konfirmasi/Tolak SEKARANG hanya ada
            // di halaman OwnerBookingDetailActivity, bukan di card list ini lagi.
            // Tombol "Lihat Detail" SELALU terlihat untuk semua status.
            when (booking.status) {
                "Menunggu" -> {
                    tvStatus.setTextColor(Color.parseColor("#856404"))
                    tvStatus.setBackgroundResource(R.drawable.bg_tag)
                }
                "Terkonfirmasi" -> {
                    tvStatus.setTextColor(Color.parseColor("#065F46"))
                    tvStatus.setBackgroundResource(R.drawable.bg_slot_available)
                }
                "Batal" -> {
                    tvStatus.setTextColor(Color.parseColor("#991B1B"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_batal)
                }
                "Selesai" -> {
                    tvStatus.setTextColor(Color.parseColor("#1D4ED8"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                }
            }

            btnDetail.setOnClickListener { onDetail(booking) }
            itemView.setOnClickListener { onDetail(booking) }
        }

        private fun formatDate(date: String): String {
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("id"))
                val out = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale("id"))
                out.format(sdf.parse(date)!!)
            } catch (e: Exception) { date }
        }
    }
}