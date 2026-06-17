package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field

class OwnerFieldAdapter(
    private var fields: MutableList<Field>,
    private val onEdit: (Field) -> Unit,
    private val onDelete: (Field) -> Unit,
    private val onMore: (Field) -> Unit
) : RecyclerView.Adapter<OwnerFieldAdapter.ViewHolder>() {

    fun updateData(newFields: List<Field>) {
        fields.clear()
        fields.addAll(newFields)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_field, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fields[position])
    }

    override fun getItemCount() = fields.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivFieldPhoto)
        private val tvName: TextView = itemView.findViewById(R.id.tvFieldName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvFieldCategory)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvFieldPrice)
        private val tvHours: TextView = itemView.findViewById(R.id.tvFieldHours)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvFieldAddress)
        private val tvRating: TextView = itemView.findViewById(R.id.tvFieldRating)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvFieldStatus)

        fun bind(field: Field) {
            tvName.text     = field.name
            tvCategory.text = field.category.ifEmpty { "Futsal" }
            tvAddress.text  = field.address
            tvPrice.text    = "Rp ${formatPrice(field.price)}/jam"
            tvRating.text   = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"
            tvHours.text    = if (!field.openTime.isNullOrEmpty() && !field.closeTime.isNullOrEmpty())
                "${field.openTime} - ${field.closeTime}" else "06:00 - 23:00"

            // Badge status
            if (field.isActive) {
                tvStatus.text = "✓ Tersedia"
                tvStatus.setBackgroundResource(R.drawable.bg_slot_available)
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_green))
            } else {
                tvStatus.text = "✗ Nonaktif"
                tvStatus.setBackgroundResource(R.drawable.bg_slot_unavailable)
                tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.error_red))
            }

            // Foto
            val context  = itemView.context
            val fullUrl  = field.photo

            if (!fullUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.color.divider)
                    .error(R.drawable.field_1)
                    .centerCrop()
                    .into(ivPhoto)
            } else {
                ivPhoto.setImageResource(R.drawable.field_1)
            }

            itemView.findViewById<View>(R.id.btnEdit).setOnClickListener   { onEdit(field) }
            itemView.findViewById<View>(R.id.btnDelete).setOnClickListener { onDelete(field) }
            itemView.findViewById<View>(R.id.btnMore).setOnClickListener   { onMore(field) }
        }

        private fun formatPrice(price: Int): String {
            val s      = price.toString()
            val result = StringBuilder()
            var count  = 0
            for (i in s.length - 1 downTo 0) {
                if (count > 0 && count % 3 == 0) result.insert(0, ".")
                result.insert(0, s[i])
                count++
            }
            return result.toString()
        }
    }
}