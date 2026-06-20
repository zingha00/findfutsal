package com.utama.findfutsall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.utama.findfutsall.R
import com.utama.findfutsall.data.model.PaymentMethod

class PaymentMethodAdapter(
    private var methods: List<PaymentMethod>,
    private val onDelete: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder>() {

    fun updateData(newMethods: List<PaymentMethod>) {
        methods = newMethods
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(methods[position])
    }

    override fun getItemCount() = methods.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivLogo: ImageView         = itemView.findViewById(R.id.ivLogo)
        private val tvType: TextView          = itemView.findViewById(R.id.tvType)
        private val tvAccountNumber: TextView = itemView.findViewById(R.id.tvAccountNumber)
        private val tvAccountName: TextView   = itemView.findViewById(R.id.tvAccountName)
        private val btnDelete: View           = itemView.findViewById(R.id.btnDelete)

        fun bind(method: PaymentMethod) {
            tvType.text          = method.type
            tvAccountNumber.text = method.accountNumber
            tvAccountName.text   = "a.n. ${method.accountName}"

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
                else       -> R.drawable.ic_wallet
            }
            ivLogo.setImageResource(logoRes)

            btnDelete.setOnClickListener { onDelete(method) }
        }
    }
}