package com.utama.findfutsall.ui.owner

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utama.findfutsall.adapter.OwnerFieldAdapter
import com.utama.findfutsall.data.api.ApiClient
import com.utama.findfutsall.data.model.Field
import com.utama.findfutsall.databinding.FragmentOwnerLapanganBinding
import com.utama.findfutsall.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OwnerLapanganFragment : Fragment() {

    private var _binding: FragmentOwnerLapanganBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: OwnerFieldAdapter
    private val allFields = mutableListOf<Field>()

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == FieldFormActivity.RESULT_SAVED) {
            loadLapangan()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerLapanganBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupRecyclerView()
        setupSearch()
        setupTambahButton()
        loadLapangan()
    }

    private fun setupRecyclerView() {
        adapter = OwnerFieldAdapter(
            fields = allFields,
            onEdit = { field -> openEditForm(field) },
            onDelete = { field -> deleteLapangan(field) },
            onMore = { field ->
                Toast.makeText(requireContext(), "Detail: ${field.name}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvLapangan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLapangan.adapter = adapter
        binding.rvLapangan.visibility  = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun setupSearch() {
        binding.etSearchLapangan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterLapangan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTambahButton() {
        binding.btnTambahLapangan.setOnClickListener {
            val intent = Intent(requireContext(), FieldFormActivity::class.java)
            formLauncher.launch(intent)
        }
    }

    private fun openEditForm(field: Field) {
        val intent = Intent(requireContext(), FieldFormActivity::class.java)
        intent.putExtra(FieldFormActivity.EXTRA_FIELD, field)
        formLauncher.launch(intent)
    }

    private fun filterLapangan(query: String) {
        val filtered = if (query.isEmpty()) allFields
        else allFields.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
        showFields(filtered.isNotEmpty())
    }

    private fun loadLapangan() {
        if (_binding == null) return
        binding.progressLapangan.visibility = View.VISIBLE

        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.instance.getOwnerFields(mapOf("user_id" to userId))
                }

                if (_binding == null) return@launch

                withContext(Dispatchers.Main) {
                    binding.progressLapangan.visibility = View.GONE

                    if (response.isSuccessful && response.body()?.get("success") == true) {
                        val body = response.body()!!

                        @Suppress("UNCHECKED_CAST")
                        val rawFields = body["fields"] as? List<Map<String, Any>> ?: emptyList()
                        val total     = body["total"]?.toString()?.toDoubleOrNull()?.toInt() ?: rawFields.size
                        val tersedia  = body["tersedia"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0

                        allFields.clear()
                        allFields.addAll(rawFields.map { parseField(it) })

                        adapter.notifyDataSetChanged()
                        updateStats(total, tersedia)

                        binding.root.post { showFields(allFields.isNotEmpty()) }
                    } else {
                        showFields(false)
                    }
                }
            } catch (e: Exception) {
                if (_binding == null) return@launch
                withContext(Dispatchers.Main) {
                    binding.progressLapangan.visibility = View.GONE
                    showFields(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteLapangan(field: Field) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Lapangan")
            .setMessage("Apakah Anda yakin ingin menghapus \"${field.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val url  = com.utama.findfutsall.utils.Constants.BASE_URL + "delete_field.php"
                        val json = JSONObject().apply {
                            put("field_id", field.id)
                            put("user_id", sessionManager.getUserId())
                        }
                        val body = json.toString()
                            .toRequestBody("application/json".toMediaTypeOrNull())
                        val request  = Request.Builder().url(url).post(body).build()
                        val response = OkHttpClient().newCall(request).execute()
                        val resBody  = JSONObject(response.body?.string() ?: "{}")

                        withContext(Dispatchers.Main) {
                            if (resBody.optBoolean("success")) {
                                allFields.removeAll { it.id == field.id }
                                adapter.notifyDataSetChanged()
                                val tersedia = allFields.count { it.isActive }
                                updateStats(allFields.size, tersedia)
                                showFields(allFields.isNotEmpty())
                                Toast.makeText(requireContext(), "Lapangan dihapus", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), resBody.optString("message", "Gagal hapus"), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun parseField(data: Map<String, Any>): Field {
        return Field(
            id          = data["id"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
            name        = data["name"]?.toString() ?: "-",
            address     = data["address"]?.toString() ?: "-",
            price       = data["price"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
            rating      = data["rating"]?.toString()?.toFloatOrNull() ?: 0f,
            photo       = data["photo"]?.toString()?.takeIf { it.isNotEmpty() },
            category    = data["category"]?.toString() ?: "",
            phone       = data["phone"]?.toString()?.takeIf { it.isNotEmpty() },
            description = data["description"]?.toString()?.takeIf { it.isNotEmpty() },
            facilities  = data["facilities"]?.toString()?.takeIf { it.isNotEmpty() },
            openTime    = data["openTime"]?.toString() ?: "06:00",
            closeTime   = data["closeTime"]?.toString() ?: "23:00",
            isActive    = data["isActive"]?.toString()?.toBooleanStrictOrNull() ?: true,
            status      = data["status"]?.toString() ?: "active"
        )
    }

    private fun updateStats(total: Int, tersedia: Int) {
        if (_binding == null) return
        binding.tvStatTotalLapangan.text = total.toString()
        binding.tvStatTersedia.text      = tersedia.toString()
    }

    private fun showFields(hasData: Boolean) {
        if (_binding == null) return
        binding.rvLapangan.visibility  = if (hasData) View.VISIBLE else View.GONE
        binding.layoutEmpty.visibility = if (hasData) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}