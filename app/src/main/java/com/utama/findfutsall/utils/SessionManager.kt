package com.utama.findfutsall.utils

import android.content.Context
import android.content.SharedPreferences
import com.utama.findfutsall.data.model.Field
import org.json.JSONArray
import org.json.JSONObject

class SessionManager(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME, Context.MODE_PRIVATE
    )

    // ── Base URL ──────────────────────────────────────────────────────────

    fun saveBaseUrl(ip: String) {
        val url = "http://$ip/findfutsall/api/"
        pref.edit().putString(Constants.KEY_BASE_URL, url).apply()
        Constants.BASE_URL = url
    }

    fun getBaseUrl(): String {
        return pref.getString(Constants.KEY_BASE_URL, "http://192.168.0.103/findfutsall/api/")
            ?: "http://192.168.0.103/findfutsall/api/"
    }

    fun loadBaseUrl() {
        Constants.BASE_URL = getBaseUrl()
    }

    // ── Login Session ─────────────────────────────────────────────────────

    fun saveLoginSession(
        token: String,
        userId: Int,
        name: String,
        username: String?,
        email: String,
        phone: String?,
        photo: String?,
        role: String? = null
    ) {
        val effectiveRole = role ?: "user"
        pref.edit()
            .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            .putString(Constants.KEY_TOKEN, token)
            .putInt(Constants.KEY_USER_ID, userId)
            .putString(Constants.KEY_USER_NAME, name)
            .putString(Constants.KEY_USER_USERNAME, username ?: "")
            .putString(Constants.KEY_USER_EMAIL, email)
            .putString(Constants.KEY_USER_PHONE, phone ?: "")
            .putString(Constants.KEY_USER_PHOTO, photo ?: "")
            .putString(Constants.KEY_USER_ROLE, effectiveRole)
            .apply()
    }

    // ── Getter ────────────────────────────────────────────────────────────

    fun isLoggedIn(): Boolean =
        pref.getBoolean(Constants.KEY_IS_LOGGED_IN, false)

    fun getToken(): String? =
        pref.getString(Constants.KEY_TOKEN, null)

    fun getUserId(): Int {
        return try {
            pref.getInt(Constants.KEY_USER_ID, 0)
        } catch (e: Exception) {
            val asString = try { pref.getString(Constants.KEY_USER_ID, "0") } catch (e2: Exception) { "0" }
            asString?.toIntOrNull() ?: 0
        }
    }

    fun getUserName(): String? =
        pref.getString(Constants.KEY_USER_NAME, null)

    fun getUserUsername(): String? =
        pref.getString(Constants.KEY_USER_USERNAME, null)

    fun getUserEmail(): String? =
        pref.getString(Constants.KEY_USER_EMAIL, null)

    fun getUserPhone(): String? =
        pref.getString(Constants.KEY_USER_PHONE, null)

    fun getUserPhoto(): String? =
        pref.getString(Constants.KEY_USER_PHOTO, null)

    fun getUserRole(): String =
        pref.getString(Constants.KEY_USER_ROLE, "user") ?: "user"

    fun isOwner(): Boolean = getUserRole() == "owner"

    fun isAdmin(): Boolean = getUserRole() == "admin"

    // ── Updater ───────────────────────────────────────────────────────────

    fun updateName(name: String) =
        pref.edit().putString(Constants.KEY_USER_NAME, name).apply()

    fun updatePhoto(photo: String) =
        pref.edit().putString(Constants.KEY_USER_PHOTO, photo).apply()

    fun updatePhone(phone: String) =
        pref.edit().putString(Constants.KEY_USER_PHONE, phone).apply()

    fun updateRole(role: String) =
        pref.edit().putString(Constants.KEY_USER_ROLE, role).apply()

    // ── Owner Status ──────────────────────────────────────────────────────

    fun setOwnerStatus(status: String) =
        pref.edit().putString(Constants.KEY_OWNER_STATUS, status).apply()

    fun getOwnerStatus(): String? =
        pref.getString(Constants.KEY_OWNER_STATUS, null)

    fun isOwnerPending(): Boolean = getOwnerStatus() == "pending"

    fun isOwnerApproved(): Boolean = getOwnerStatus() == "approved"

    // ── Owner Fields (lokal) ──────────────────────────────────────────────

    fun saveOwnerFields(fields: List<Field>) {
        val jsonArray = JSONArray()
        fields.forEach { field ->
            val obj = JSONObject().apply {
                put("id", field.id)
                put("name", field.name)
                put("address", field.address)
                put("price", field.price)
                put("rating", field.rating.toDouble())
                put("photo", field.photo ?: "")
                put("category", field.category)
                put("distance", field.distance ?: "")
                put("phone", field.phone ?: "")
                put("description", field.description ?: "")
                put("facilities", field.facilities ?: "")
                put("openTime", field.openTime ?: "")
                put("closeTime", field.closeTime ?: "")
            }
            jsonArray.put(obj)
        }
        pref.edit().putString(Constants.KEY_OWNER_FIELDS, jsonArray.toString()).apply()
    }

    fun getOwnerFields(): List<Field> {
        val json = pref.getString(Constants.KEY_OWNER_FIELDS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Field(
                    id          = obj.getInt("id"),
                    name        = obj.getString("name"),
                    address     = obj.getString("address"),
                    price       = obj.getInt("price"),
                    rating      = obj.optDouble("rating", 0.0).toFloat(),
                    photo       = obj.optString("photo").ifEmpty { null },
                    category    = obj.optString("category", ""),
                    distance    = obj.optString("distance").ifEmpty { null },
                    phone       = obj.optString("phone").ifEmpty { null },
                    description = obj.optString("description").ifEmpty { null },
                    facilities  = obj.optString("facilities").ifEmpty { null },
                    openTime    = obj.optString("openTime").ifEmpty { null },
                    closeTime   = obj.optString("closeTime").ifEmpty { null }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getNextFieldId(): Int {
        val fields = getOwnerFields()
        return if (fields.isEmpty()) 100 else fields.maxOf { it.id } + 1
    }

    // ── Session Utils ─────────────────────────────────────────────────────

    fun isSessionValid(): Boolean =
        isLoggedIn() && !getToken().isNullOrEmpty()

    fun getAuthHeader(): String? {
        val token = getToken() ?: return null
        return "Bearer $token"
    }

    fun clearSession() {
        val baseUrl = getBaseUrl()
        pref.edit().clear().apply()
        pref.edit().putString(Constants.KEY_BASE_URL, baseUrl).apply()
        Constants.BASE_URL = baseUrl
    }
}