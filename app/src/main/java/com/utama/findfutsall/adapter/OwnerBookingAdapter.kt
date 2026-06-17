package com.utama.findfutsall.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.OwnerBooking
import com.utama.findfutsall.utils.PriceFormatter
import de.hdodenhof.circleimageview.CircleImageView

class OwnerBookingAdapter(
    bookings: List<OwnerBooking>,
    private val onKonfirmasi: (OwnerBooking) -> Unit,
    private val onBatal: (OwnerBooking) -> Unit,
    private val onWhatsapp: (OwnerBooking) -> Unit,
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
        private val layoutAksi: View              = itemView.findViewById(R.id.layoutAksi)
        private val btnKonfirmasi: MaterialButton = itemView.findViewById(R.id.btnKonfirmasi)
        private val btnBatal: MaterialButton      = itemView.findViewById(R.id.btnBatal)
        private val btnWhatsapp: MaterialButton   = itemView.findViewById(R.id.btnWhatsapp)
        private val btnDetail: MaterialButton     = itemView.findViewById(R.id.btnDetail)

        fun bind(booking: OwnerBooking) {
            tvBookingId.text  = "#BK-${String.format("%03d", booking.id)}"
            tvCustomer.text   = booking.customerName
            tvFieldName.text  = booking.fieldName
            tvPlayDate.text   = "${formatDate(booking.playDate)} | ${booking.startTime} - ${booking.endTime}"
            tvTotalPrice.text = PriceFormatter.format(booking.totalPrice)
            tvStatus.text     = booking.status

            if (booking.userPhoto.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(booking.userPhoto)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(ivUserPhoto)
            } else {
                ivUserPhoto.setImageResource(R.drawable.ic_profile)
            }

            when (booking.status) {
                "Menunggu" -> {
                    tvStatus.setTextColor(Color.parseColor("#856404"))
                    tvStatus.setBackgroundResource(R.drawable.bg_tag)
                    layoutAksi.visibility = View.VISIBLE
                    btnDetail.visibility  = View.GONE
                }
                "Terkonfirmasi" -> {
                    tvStatus.setTextColor(Color.parseColor("#065F46"))
                    tvStatus.setBackgroundResource(R.drawable.bg_slot_available)
                    layoutAksi.visibility = View.GONE
                    btnDetail.visibility  = View.VISIBLE
                }
                "Batal" -> {
                    tvStatus.setTextColor(Color.parseColor("#991B1B"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_batal)
                    layoutAksi.visibility = View.GONE
                    btnDetail.visibility  = View.VISIBLE
                }
                "Selesai" -> {
                    tvStatus.setTextColor(Color.parseColor("#1D4ED8"))
                    tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                    layoutAksi.visibility = View.GONE
                    btnDetail.visibility  = View.VISIBLE
                }
                else -> {
                    layoutAksi.visibility = View.VISIBLE
                    btnDetail.visibility  = View.GONE
                }
            }

            btnKonfirmasi.setOnClickListener { onKonfirmasi(booking) }
            btnBatal.setOnClickListener { onBatal(booking) }
            btnWhatsapp.setOnClickListener { onWhatsapp(booking) }
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