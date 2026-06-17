package com.utama.findfutsall.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utama.findfutsall.databinding.ActivityStaticPageBinding

class StaticPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaticPageBinding

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_TERMS   = "terms"
        const val TYPE_PRIVACY = "privacy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_TERMS
        loadContent(type)
    }

    private fun loadContent(type: String) {
        if (type == TYPE_PRIVACY) {
            binding.tvPageTitle.text = "Kebijakan Privasi"
            binding.tvContent.text = PRIVACY_POLICY_TEXT
        } else {
            binding.tvPageTitle.text = "Syarat & Ketentuan"
            binding.tvContent.text = TERMS_TEXT
        }
    }

    private val TERMS_TEXT = """
1. PENERIMAAN SYARAT

Dengan mengakses dan menggunakan aplikasi FindFutsall, Anda menyetujui untuk terikat dengan seluruh syarat dan ketentuan yang tercantum di bawah ini. Jika Anda tidak menyetujui syarat ini, mohon untuk tidak menggunakan aplikasi ini.

2. PENDAFTARAN AKUN

Pengguna wajib memberikan informasi yang akurat, lengkap, dan terkini saat melakukan pendaftaran akun. Pengguna bertanggung jawab penuh atas kerahasiaan kata sandi dan seluruh aktivitas yang terjadi pada akun miliknya.

Setiap pengguna hanya diperbolehkan memiliki satu akun aktif. Akun yang terbukti digunakan untuk tujuan penipuan atau pelanggaran hukum akan dihentikan tanpa pemberitahuan sebelumnya.

3. PEMESANAN LAPANGAN

Pengguna dapat memesan lapangan futsal yang tersedia melalui aplikasi sesuai dengan jadwal dan ketersediaan yang ditampilkan. Harga yang tercantum dapat berubah sewaktu-waktu mengikuti kebijakan masing-masing pemilik lapangan.

Pemesanan dianggap sah setelah pembayaran berhasil dikonfirmasi oleh sistem. Pengguna wajib hadir sesuai dengan jadwal yang telah dipesan.

4. PEMBATALAN DAN PENGEMBALIAN DANA

Pembatalan pemesanan dapat dilakukan sesuai dengan kebijakan pembatalan yang berlaku pada masing-masing lapangan. Pengembalian dana akan diproses sesuai dengan ketentuan waktu yang berlaku, dan dapat dipotong biaya administrasi.

Pembatalan yang dilakukan kurang dari batas waktu yang ditentukan oleh pemilik lapangan tidak akan mendapatkan pengembalian dana.

5. KEWAJIBAN PENGGUNA

Pengguna wajib menjaga ketertiban dan kebersihan fasilitas lapangan selama menggunakan layanan. Segala bentuk kerusakan yang diakibatkan oleh kelalaian pengguna menjadi tanggung jawab pengguna yang bersangkutan.

Pengguna dilarang menggunakan aplikasi untuk tujuan yang melanggar hukum, termasuk namun tidak terbatas pada penipuan, pencucian uang, atau aktivitas ilegal lainnya.

6. PERAN PEMILIK LAPANGAN (OWNER)

Pemilik lapangan bertanggung jawab atas keakuratan informasi lapangan yang didaftarkan, termasuk harga, jam operasional, dan fasilitas yang tersedia. Pemilik lapangan wajib memproses pemesanan dan pembatalan sesuai dengan kebijakan yang telah disepakati.

7. PEMBAYARAN

Seluruh transaksi pembayaran dilakukan melalui metode yang telah disediakan dalam aplikasi. FindFutsall tidak bertanggung jawab atas transaksi yang dilakukan di luar sistem resmi aplikasi.

8. PERUBAHAN LAYANAN

FindFutsall berhak untuk mengubah, menangguhkan, atau menghentikan sebagian maupun seluruh fitur layanan tanpa pemberitahuan sebelumnya, demi peningkatan kualitas layanan.

9. BATASAN TANGGUNG JAWAB

FindFutsall berperan sebagai perantara antara pengguna dan pemilik lapangan. Kami tidak bertanggung jawab atas kerugian yang timbul akibat kelalaian pihak ketiga, termasuk kondisi fasilitas lapangan yang berada di luar kendali kami.

10. PERUBAHAN SYARAT DAN KETENTUAN

Syarat dan ketentuan ini dapat diperbarui dari waktu ke waktu. Pengguna disarankan untuk memeriksa halaman ini secara berkala untuk mengetahui perubahan terbaru.

11. KONTAK

Jika Anda memiliki pertanyaan mengenai syarat dan ketentuan ini, silakan hubungi tim dukungan kami melalui menu Pusat Bantuan pada aplikasi.
    """.trimIndent()

    private val PRIVACY_POLICY_TEXT = """
1. INFORMASI YANG KAMI KUMPULKAN

Kami mengumpulkan informasi yang Anda berikan secara langsung saat mendaftar, termasuk nama, alamat email, nomor telepon, dan foto profil. Kami juga dapat mengumpulkan informasi terkait riwayat pemesanan dan preferensi penggunaan aplikasi.

2. PENGGUNAAN INFORMASI

Informasi yang dikumpulkan digunakan untuk:
- Memproses dan mengelola pemesanan lapangan
- Mengirimkan notifikasi terkait status pemesanan
- Meningkatkan kualitas layanan dan pengalaman pengguna
- Menghubungi pengguna terkait dukungan pelanggan

3. KEAMANAN DATA

Kami menerapkan langkah-langkah keamanan teknis dan organisasi yang wajar untuk melindungi data pribadi Anda dari akses, perubahan, atau pengungkapan yang tidak sah. Kata sandi Anda disimpan dalam bentuk terenkripsi dan tidak dapat dilihat oleh pihak manapun, termasuk tim kami.

4. BERBAGI INFORMASI DENGAN PIHAK KETIGA

Kami tidak akan menjual, menyewakan, atau membagikan informasi pribadi Anda kepada pihak ketiga untuk tujuan pemasaran tanpa persetujuan Anda. Informasi dapat dibagikan kepada pemilik lapangan sebatas yang diperlukan untuk memproses pemesanan, seperti nama dan nomor telepon.

5. HAK PENGGUNA

Anda memiliki hak untuk:
- Mengakses dan memperbarui informasi pribadi Anda melalui menu Pengaturan Profil
- Mengubah kata sandi akun Anda kapan saja
- Meminta penghapusan akun dengan menghubungi tim dukungan kami

6. PENYIMPANAN DATA

Data pribadi Anda akan disimpan selama akun Anda masih aktif. Jika Anda menghapus akun, data Anda akan dihapus dari sistem kami sesuai dengan kebijakan retensi data yang berlaku, kecuali data yang wajib disimpan untuk kepentingan hukum.

7. COOKIE DAN TEKNOLOGI PELACAKAN

Aplikasi kami dapat menggunakan teknologi penyimpanan lokal untuk menyimpan preferensi pengguna seperti status notifikasi dan sesi login, demi kenyamanan penggunaan aplikasi.

8. PERUBAHAN KEBIJAKAN PRIVASI

Kebijakan privasi ini dapat diperbarui dari waktu ke waktu untuk menyesuaikan dengan perubahan layanan atau ketentuan hukum yang berlaku. Perubahan akan diinformasikan melalui aplikasi.

9. PERSETUJUAN

Dengan menggunakan aplikasi FindFutsall, Anda menyetujui pengumpulan dan penggunaan informasi sesuai dengan kebijakan privasi ini.

10. KONTAK

Jika Anda memiliki pertanyaan atau kekhawatiran mengenai privasi data Anda, silakan hubungi kami melalui menu Pusat Bantuan pada aplikasi.
    """.trimIndent()
}