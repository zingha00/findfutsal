package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ItemFieldHorizontalBinding
import com.utama.findfutsall.utils.PriceFormatter

class FieldHorizontalAdapter(
    private var fields: List<Field>,
    private val onItemClick: (Field) -> Unit
) : RecyclerView.Adapter<FieldHorizontalAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFieldHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFieldHorizontalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val field = fields[position]
        with(holder.binding) {
            tvFieldName.text    = field.name
            tvFieldAddress.text = field.address
            tvFieldPrice.text   = "${PriceFormatter.format(field.price)}/jam"
            tvFieldRating.text  = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"

            val context   = root.context
            val photoName = field.photo ?: ""
            val baseUrl   = com.utama.findfutsall.utils.Constants.BASE_URL.replace("/api/", "/")
            val fullUrl   = when {
                photoName.startsWith("http") -> photoName
                photoName.isNotEmpty()       -> "$baseUrl$photoName"
                else                         -> null
            }

            // Placeholder & error pakai vector drawable ringan (bukan field_1.png ~1.6MB)
            if (fullUrl != null) {
                Glide.with(context)
                    .load(fullUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image_error)
                    .centerCrop()
                    .into(ivFieldPhoto)
            } else {
                ivFieldPhoto.setImageResource(R.drawable.placeholder_image)
            }

            root.setOnClickListener { onItemClick(field) }
        }
    }

    override fun getItemCount() = fields.size

    fun updateData(newFields: List<Field>) {
        fields = newFields
        notifyDataSetChanged()
    }
}