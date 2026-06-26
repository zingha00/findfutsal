package com.utama.findfutsall.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.utama.findfutsall.R

class ProviderDropdownAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, R.layout.item_dropdown_provider, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_dropdown_provider, parent, false)

        val ivLogo: ImageView = view.findViewById(R.id.ivDropdownLogo)
        val tvName: TextView  = view.findViewById(R.id.tvDropdownName)

        val name = items[position]
        tvName.text = name

        val logoRes = when (name.uppercase()) {
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

        return view
    }
}