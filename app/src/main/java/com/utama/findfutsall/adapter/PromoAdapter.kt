package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.Promo
import com.utama.findfutsall.databinding.ItemPromoBinding

class PromoAdapter(
    private val promos: List<Promo>
) : RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    private val iklanImages = listOf(R.drawable.iklan1, R.drawable.iklan2, R.drawable.iklan3)

    inner class PromoViewHolder(val binding: ItemPromoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val binding = ItemPromoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PromoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        holder.binding.ivPromo.setImageResource(iklanImages[position % iklanImages.size])
        holder.binding.ivPromo.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
    }

    override fun getItemCount() = promos.size
}