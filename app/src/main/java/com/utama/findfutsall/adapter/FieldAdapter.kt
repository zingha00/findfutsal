package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ItemFieldBinding

class FieldAdapter(
    private var fields: List<Field>,
    private val onItemClick: (Field) -> Unit
) : RecyclerView.Adapter<FieldAdapter.FieldViewHolder>() {

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
            tvFieldPrice.text    = "Rp ${field.price}k/jam"
            tvFieldRating.text   = field.rating.toString()
            tvFieldDistance.text = field.distance ?: ""

            val context   = root.context
            val photoName = field.photo ?: ""

            when {
                // URL dari server (http/https)
                photoName.startsWith("http") -> {
                    Glide.with(context)
                        .load(photoName)
                        .placeholder(R.drawable.field_1)
                        .error(R.drawable.field_1)
                        .centerCrop()
                        .into(ivFieldPhoto)
                }
                // Nama drawable lokal
                photoName.isNotEmpty() -> {
                    val resourceId = context.resources.getIdentifier(
                        photoName, "drawable", context.packageName
                    )
                    if (resourceId != 0) {
                        Glide.with(context).load(resourceId).centerCrop().into(ivFieldPhoto)
                    } else {
                        ivFieldPhoto.setImageResource(R.drawable.field_1)
                    }
                }
                else -> ivFieldPhoto.setImageResource(R.drawable.field_1)
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