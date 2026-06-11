package com.utama.findfutsall.ui.owner

import androidx.fragment.app.Fragment

/**
 * Base class untuk semua step fragment di RegisterOwnerActivity.
 * Setiap fragment wajib override validate() untuk cek input sebelum lanjut ke step berikutnya.
 */
abstract class RegisterOwnerBaseFragment : Fragment() {
    abstract fun validate(): Boolean
}