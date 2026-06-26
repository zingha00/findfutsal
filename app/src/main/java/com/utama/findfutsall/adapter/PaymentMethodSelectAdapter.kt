package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.PaymentMethod

class PaymentMethodSelectAdapter(
    private var methods: List<PaymentMethod>,
    private val onSelect: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodSelectAdapter.ViewHolder>() {

    private var selectedId: Int = -1

    fun updateData(newMethods: List<PaymentMethod>) {
        methods = newMethods
        if (methods.isNotEmpty()) {
            selectedId = methods[0].id
            onSelect(methods[0])
        }
        notifyDataSetChanged()
    }

    fun getSelected(): PaymentMethod? = methods.find { it.id == selectedId }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(methods[position])
    }

    override fun getItemCount() = methods.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivLogo: ImageView         = itemView.findViewById(R.id.ivLogo)
        private val tvType: TextView          = itemView.findViewById(R.id.tvType)
        private val tvAccountInfo: TextView   = itemView.findViewById(R.id.tvAccountInfo)
        private val rbSelected: RadioButton   = itemView.findViewById(R.id.rbSelected)

        fun bind(method: PaymentMethod) {
            tvType.text = method.type

            val logoRes = when (method.type.uppercase()) {
                "BCA"      -> R.drawable.logo_bca
                "BNI"      -> R.drawable.logo_bni
                "BRI"      -> R.drawable.logo_bri
                "MANDIRI"  -> R.drawable.logo_mandiri
                "SEABANK"  -> R.drawable.logo_seabank
                "JAGO"     -> R.drawable.logo_jago
                "OVO"      -> R.drawable.logo_ovo
                "DANA"     -> R.drawable.logo_dana
                "GOPAY"    -> R.drawable.logo_gopay
                "QRIS"     -> R.drawable.ic_wallet
                else       -> R.drawable.ic_wallet
            }
            ivLogo.setImageResource(logoRes)

            tvAccountInfo.text = if (method.type == "QRIS") {
                method.accountName
            } else {
                "a.n. ${method.accountName} • ${method.accountNumber}"
            }

            rbSelected.isChecked = method.id == selectedId

            itemView.setOnClickListener {
                selectedId = method.id
                onSelect(method)
                notifyDataSetChanged()
            }
        }
    }
}