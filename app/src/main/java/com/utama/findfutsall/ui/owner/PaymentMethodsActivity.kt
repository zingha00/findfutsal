package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.adapter.PaymentMethodAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.PaymentMethod
import com.utama.findfutsall.databinding.ActivityPaymentMethodsBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.launch

class PaymentMethodsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentMethodsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PaymentMethodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentMethodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupRecyclerView()

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddPaymentMethodActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadPaymentMethods()
    }

    private fun setupRecyclerView() {
        adapter = PaymentMethodAdapter(emptyList()) { method ->
            confirmDelete(method)
        }
        binding.rvPaymentMethods.layoutManager = LinearLayoutManager(this)
        binding.rvPaymentMethods.adapter = adapter
    }

    private fun confirmDelete(method: PaymentMethod) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Metode Pembayaran")
            .setMessage("Hapus ${method.type} - ${method.accountNumber}?")
            .setPositiveButton("Hapus") { _, _ -> deletePaymentMethod(method) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deletePaymentMethod(method: PaymentMethod) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.deletePaymentMethod(
                    mapOf("id" to method.id, "user_id" to sessionManager.getUserId())
                )
                if (response.isSuccessful && response.body()?.get("success") == true) {
                    Toast.makeText(this@PaymentMethodsActivity, "Metode pembayaran dihapus", Toast.LENGTH_SHORT).show()
                    loadPaymentMethods()
                } else {
                    Toast.makeText(this@PaymentMethodsActivity, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentMethodsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPaymentMethods() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.getPaymentMethods(
                    mapOf("user_id" to sessionManager.getUserId())
                )
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body["success"] == true) {
                        @Suppress("UNCHECKED_CAST")
                        val list = body["data"] as? List<Map<String, Any>> ?: emptyList()
                        val methods = list.map {
                            PaymentMethod(
                                id            = (it["id"] as? Double)?.toInt() ?: 0,
                                type          = it["type"]?.toString() ?: "",
                                accountNumber = it["account_number"]?.toString() ?: "",
                                accountName   = it["account_name"]?.toString() ?: "",
                                isDefault     = it["is_default"] as? Boolean ?: false
                            )
                        }
                        adapter.updateData(methods)
                        updateEmptyState(methods.isEmpty())
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                updateEmptyState(true)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.layoutEmpty.visibility     = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvPaymentMethods.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}