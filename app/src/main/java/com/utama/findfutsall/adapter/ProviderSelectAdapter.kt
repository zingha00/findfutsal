package com.utama.findfutsall.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R

class ProviderSelectAdapter(
    private val providers: List<String>,
    private val logoMap: Map<String, Int>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<ProviderSelectAdapter.ViewHolder>() {

    private var selectedProvider: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(providers[position])
    }

    override fun getItemCount() = providers.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivLogo: ImageView      = itemView.findViewById(R.id.ivProviderLogo)
        private val tvName: TextView       = itemView.findViewById(R.id.tvProviderName)
        private val ivCheckmark: ImageView = itemView.findViewById(R.id.ivCheckmark)

        fun bind(provider: String) {
            tvName.text = provider
            val logoRes = logoMap[provider] ?: R.drawable.ic_wallet
            ivLogo.setImageResource(logoRes)

            ivCheckmark.visibility = if (provider == selectedProvider) View.VISIBLE else View.INVISIBLE

            itemView.setOnClickListener {
                Log.d("ProviderSelect", "User mengklik provider: '$provider'")
                selectedProvider = provider
                onSelect(provider)
                notifyDataSetChanged()
            }
        }
    }
}