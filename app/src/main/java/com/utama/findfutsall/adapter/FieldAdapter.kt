package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ItemFieldBinding
import com.utama.findfutsall.utils.Constants
import com.utama.findfutsall.utils.PriceFormatter

class FieldAdapter(
    private var fields: List<Field>,
    private val onItemClick: (Field) -> Unit,
    private val onFavoriteClick: ((Field) -> Unit)? = null
) : RecyclerView.Adapter<FieldAdapter.FieldViewHolder>() {

    private val favoriteIds = mutableSetOf<Int>()

    fun setFavorites(ids: Set<Int>) {
        favoriteIds.clear()
        favoriteIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun toggleFavorite(fieldId: Int) {
        if (favoriteIds.contains(fieldId)) favoriteIds.remove(fieldId)
        else favoriteIds.add(fieldId)
        notifyDataSetChanged()
    }

    inner class FieldViewHolder(val binding: ItemFieldBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldViewHolder {
        val binding = ItemFieldBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldViewHolder, position: Int) {
        val field = fields[position]
        with(holder.binding) {
            tvFieldName.text     = field.name
            tvFieldAddress.text  = field.address
            tvFieldPrice.text    = "${PriceFormatter.format(field.price)}/jam"
            tvFieldRating.text   = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"
            tvFieldDistance.text = field.distance ?: ""

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
                    .placeholder(R.color.divider)
                    .error(R.drawable.field_1)
                    .centerCrop()
                    .into(ivFieldPhoto)
            } else {
                ivFieldPhoto.setImageResource(R.drawable.field_1)
            }

            // Set warna icon favorit
            val isFav = favoriteIds.contains(field.id)
            ivFavorite.setColorFilter(
                ContextCompat.getColor(context, if (isFav) R.color.error_red else android.R.color.white)
            )

            ivFavorite.setOnClickListener {
                val willBeFav = !favoriteIds.contains(field.id)

                ivFavorite.setColorFilter(
                    ContextCompat.getColor(context, if (willBeFav) R.color.error_red else android.R.color.white)
                )

                ivFavorite.animate()
                    .scaleX(1.3f).scaleY(1.3f)
                    .setDuration(120)
                    .withEndAction {
                        ivFavorite.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(120)
                            .start()
                    }.start()

                if (willBeFav) favoriteIds.add(field.id)
                else favoriteIds.remove(field.id)

                onFavoriteClick?.invoke(field)
            }

            btnBooking.setOnClickListener { onItemClick(field) }
            root.setOnClickListener { onItemClick(field) }
        }
    }

    override fun getItemCount() = fields.size

    fun updateData(newFields: List<Field>) {
        fields = newFields
        notifyDataSetChanged()
    }
}