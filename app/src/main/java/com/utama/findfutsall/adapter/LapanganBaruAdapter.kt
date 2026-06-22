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
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.PriceFormatter

class LapanganBaruAdapter(
    private var fields: List<Field>,
    private val onItemClick: (Field) -> Unit,
    private val onFavoriteClick: (Field) -> Unit
) : RecyclerView.Adapter<LapanganBaruAdapter.ViewHolder>() {

    private val favoriteIds = mutableSetOf<Int>()

    fun setFavorites(ids: Set<Int>) {
        favoriteIds.clear()
        favoriteIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun toggleFavorite(fieldId: Int) {
        if (favoriteIds.contains(fieldId)) favoriteIds.remove(fieldId)
        else favoriteIds.add(fieldId)
        val position = fields.indexOfFirst { it.id == fieldId }
        if (position != -1) notifyItemChanged(position)
    }

    fun updateData(newFields: List<Field>) {
        fields = newFields
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivFieldPhoto: ImageView = itemView.findViewById(R.id.ivFieldPhoto)
        val ivFavorite: ImageView   = itemView.findViewById(R.id.ivFavorite)
        val tvFieldName: TextView   = itemView.findViewById(R.id.tvFieldName)
        val tvRating: TextView      = itemView.findViewById(R.id.tvRating)
        val tvPrice: TextView       = itemView.findViewById(R.id.tvPrice)
        val btnBooking: TextView    = itemView.findViewById(R.id.btnBooking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_field_baru_compact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val field = fields[position]
        holder.tvFieldName.text = field.name
        holder.tvRating.text    = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"
        holder.tvPrice.text     = "${PriceFormatter.format(field.price)}/jam"

        val context   = holder.itemView.context
        val photoName = field.photo ?: ""
        val baseUrl   = Constants.BASE_URL.replace("/api/", "/")
        val fullUrl   = when {
            photoName.startsWith("http") -> photoName
            photoName.isNotEmpty()       -> "$baseUrl$photoName"
            else                         -> null
        }

        if (fullUrl != null) {
            Glide.with(context)
                .load(fullUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image_error)
                .centerCrop()
                .into(holder.ivFieldPhoto)
        } else {
            holder.ivFieldPhoto.setImageResource(R.drawable.placeholder_image)
        }

        val isFav = favoriteIds.contains(field.id)
        holder.ivFavorite.setColorFilter(
            ContextCompat.getColor(
                context,
                if (isFav) R.color.error_red else android.R.color.white
            )
        )

        holder.itemView.setOnClickListener { onItemClick(field) }
        holder.btnBooking.setOnClickListener { onItemClick(field) }
        holder.ivFavorite.setOnClickListener {
            toggleFavorite(field.id)
            holder.ivFavorite.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(120)
                .withEndAction {
                    holder.ivFavorite.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(120)
                        .start()
                }.start()
            onFavoriteClick(field)
        }
    }

    override fun getItemCount() = fields.size
}