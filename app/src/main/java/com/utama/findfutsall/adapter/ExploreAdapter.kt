package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ItemFieldExploreBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.PriceFormatter

class ExploreAdapter(
    private var fields: List<Field>,
    private val onItemClick: (Field) -> Unit,
    private val onFavoriteClick: (Field) -> Unit,
    private val alwaysFavorite: Boolean = false
) : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

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

    inner class ExploreViewHolder(val binding: ItemFieldExploreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val binding = ItemFieldExploreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExploreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val field = fields[position]
        with(holder.binding) {
            tvFieldName.text = field.name
            tvRating.text    = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"
            tvDistance.text  = field.distance ?: ""
            tvPrice.text = "${PriceFormatter.format(field.price)}/jam"

            val context   = root.context
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
                    .into(ivFieldPhoto)
            } else {
                ivFieldPhoto.setImageResource(R.drawable.placeholder_image)
            }

            val isFav = alwaysFavorite || favoriteIds.contains(field.id)
            ivFavorite.setColorFilter(
                androidx.core.content.ContextCompat.getColor(
                    context,
                    if (isFav) R.color.error_red else android.R.color.white
                )
            )

            root.setOnClickListener { onItemClick(field) }
            btnBooking.setOnClickListener { onItemClick(field) }
            ivFavorite.setOnClickListener {
                toggleFavorite(field.id)
                ivFavorite.animate()
                    .scaleX(1.3f).scaleY(1.3f)
                    .setDuration(120)
                    .withEndAction {
                        ivFavorite.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(120)
                            .start()
                    }.start()
                onFavoriteClick(field)
            }
        }
    }

    override fun getItemCount() = fields.size

    fun updateData(newFields: List<Field>) {
        fields = newFields
        notifyDataSetChanged()
    }
}