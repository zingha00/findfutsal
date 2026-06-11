package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.ItemFieldHorizontalBinding

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
            tvFieldPrice.text   = "Rp ${formatPrice(field.price)}/jam"
            tvFieldRating.text  = if (field.rating > 0) String.format("%.1f", field.rating) else "0.0"

            val context   = root.context
            val photoName = field.photo ?: ""
            when {
                photoName.startsWith("http") -> {
                    Glide.with(context)
                        .load(photoName)
                        .placeholder(R.drawable.field_1)
                        .error(R.drawable.field_1)
                        .centerCrop()
                        .into(ivFieldPhoto)
                }
                photoName.isNotEmpty() -> {
                    val resId = context.resources.getIdentifier(
                        photoName, "drawable", context.packageName
                    )
                    if (resId != 0) Glide.with(context).load(resId).centerCrop().into(ivFieldPhoto)
                    else ivFieldPhoto.setImageResource(R.drawable.field_1)
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