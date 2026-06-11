package com.utama.findfutsall.ui.owner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.TransaksiItem
import java.text.NumberFormat
import java.util.Locale

class TransaksiAdapter : ListAdapter<TransaksiItem, TransaksiAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<TransaksiItem>() {
            override fun areItemsTheSame(a: TransaksiItem, b: TransaksiItem) = a.id == b.id
            override fun areContentsTheSame(a: TransaksiItem, b: TransaksiItem) = a == b
        }

        fun formatRupiah(amount: Double): String {
            val fmt = NumberFormat.getNumberInstance(Locale("id", "ID"))
            return "Rp ${fmt.format(amount.toLong())}"
        }
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNomor: TextView       = view.findViewById(R.id.tvNomor)
        val tvNamaPenyewa: TextView = view.findViewById(R.id.tvNamaPenyewa)
        val tvLapangan: TextView    = view.findViewById(R.id.tvLapangan)
        val tvTanggal: TextView     = view.findViewById(R.id.tvTanggal)
        val tvWaktu: TextView       = view.findViewById(R.id.tvWaktu)
        val tvMetode: TextView      = view.findViewById(R.id.tvMetode)
        val tvStatus: TextView      = view.findViewById(R.id.tvStatus)
        val tvNominal: TextView     = view.findViewById(R.id.tvNominal)
        val tvBersih: TextView      = view.findViewById(R.id.tvBersih)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_keuangan, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val ctx  = holder.itemView.context

        holder.tvNomor.text       = "#${item.id}"
        holder.tvNamaPenyewa.text = item.customerName
        holder.tvLapangan.text    = item.fieldName
        holder.tvTanggal.text     = formatDate(item.playDate)
        holder.tvWaktu.text       = "${item.startTime} – ${item.endTime}"
        holder.tvMetode.text      = item.paymentMethod.ifEmpty { "-" }
        holder.tvNominal.text     = formatRupiah(item.totalPrice)
        holder.tvBersih.text      = formatRupiah(item.pendapatanBersih)

        // Status badge color
        val statusInfo = when (item.bookingStatus) {
            "Terkonfirmasi" -> Pair(R.drawable.bg_status_selesai, R.color.status_selesai_text)
            "Menunggu"      -> Pair(R.drawable.bg_slot_booked,    R.color.status_menunggu_text)
            else            -> Pair(R.drawable.bg_status_batal,   R.color.status_batal_text)
        }
        val bgRes = statusInfo.first
        val txtColor = statusInfo.second

        holder.tvStatus.text = item.bookingStatus
        holder.tvStatus.background = ContextCompat.getDrawable(ctx, bgRes)
        holder.tvStatus.setTextColor(ContextCompat.getColor(ctx, txtColor))
    }

    private fun formatDate(date: String): String {
        return try {
            val parts = date.split("-")
            val bulan = listOf("","Jan","Feb","Mar","Apr","Mei","Jun",
                "Jul","Agu","Sep","Okt","Nov","Des")
            "${parts[2]} ${bulan[parts[1].toInt()]} ${parts[0]}"
        } catch (e: Exception) { date }
    }
}