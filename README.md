# FindFutsall

Aplikasi pemesanan lapangan futsal berbasis Android yang menghubungkan pemain dengan pemilik venue secara digital. FindFutsall menyediakan pengalaman booking end-to-end — mulai dari pencarian lapangan, pemilihan slot jam, pembayaran, hingga pemberian rating dan ulasan sekaligus dashboard pengelolaan penuh untuk pemilik lapangan.

## Daftar Isi

- [Fitur Utama](#fitur-utama)
- [Teknologi yang Digunakan](#teknologi-yang-digunakan)
- [Struktur Proyek](#struktur-proyek)
- [Prasyarat](#prasyarat)
- [Instalasi & Setup](#instalasi--setup)
- [Konfigurasi Database](#konfigurasi-database)
- [Menjalankan Aplikasi](#menjalankan-aplikasi)
- [Build APK/AAB](#build-apkaab)
- [Struktur API](#struktur-api)
- [Kontribusi](#kontribusi)

## Fitur Utama

### Untuk Pemain (User)
- Registrasi dan login (manual serta Google Sign-In)
- Pencarian lapangan dengan filter kategori dan harga, serta pagination
- Detail lapangan lengkap dengan fasilitas, jam operasional, dan lokasi (terintegrasi Google Maps)
- Pemilihan tanggal dan slot jam secara real-time
- Booking dengan berbagai metode pembayaran (transfer bank, e-wallet, QRIS)
- Upload bukti pembayaran
- Riwayat booking dan status (Menunggu, Terkonfirmasi, Selesai, Batal)
- Sistem rating dan ulasan dengan upload foto (khusus booking yang sudah selesai)
- Lapangan favorit
- Manajemen profil dan keamanan akun

### Untuk Pemilik Lapangan (Owner)
- Pendaftaran sebagai pemilik venue (2 langkah: info venue, operasional & fasilitas)
- Dashboard dengan ringkasan pendapatan, total booking, dan slot tersedia
- Manajemen lapangan (tambah, edit, foto, harga, jenis permukaan)
- Manajemen booking dengan konfirmasi/penolakan pesanan
- Laporan keuangan dengan grafik pendapatan (harian/bulanan/tahunan) dan target bulanan
- Export laporan ke PDF
- Manajemen metode pembayaran (rekening bank dan e-wallet)
- Melihat rating dan ulasan per lapangan

## Teknologi yang Digunakan

**Aplikasi Mobile**
- Kotlin (Android Native)
- MVVM Architecture + ViewBinding
- Retrofit & OkHttp (komunikasi API)
- Glide (pemuatan gambar)
- Material Components

**Backend**
- PHP Native
- MySQL

**Server Lokal (Development)**
- XAMPP (Apache + MySQL + PHP)

## Struktur Proyek

```
FindFutsall/
├── app/
│   └── src/main/java/com/utama/findfutsall/
│       ├── data/
│       │   ├── api/          # ApiService, ApiClient (Retrofit)
│       │   └── model/        # Data class request/response
│       ├── ui/
│       │   ├── auth/         # Login, Register, Forgot Password
│       │   ├── main/         # Home, Explore, Favorite, Profile
│       │   ├── detail/       # Detail Lapangan, Payment, Booking
│       │   └── owner/        # Dashboard Owner, Keuangan, Booking, Lapangan
│       ├── adapter/           # RecyclerView Adapters
│       ├── viewmodel/         # ViewModel per fitur
│       └── utils/             # Constants, SessionManager, Formatter
├── backend/
│   └── api/                  # Seluruh endpoint PHP
└── uploads/                  # Folder penyimpanan file upload (fields, users, payment_proofs, reviews)
```

## Prasyarat

Sebelum memulai, pastikan sudah terinstal:

- [Android Studio](https://developer.android.com/studio) (versi terbaru disarankan)
- [XAMPP](https://www.apachefriends.org/) (atau server PHP + MySQL lain)
- JDK 11 atau lebih baru
- Git

## Instalasi & Setup

### 1. Clone Repository

```bash
git clone https://github.com/zingha00/findfutsal.git
cd findfutsal
```

### 2. Setup Backend

1. Pindahkan folder `api/` ke dalam `htdocs` XAMPP, contoh:
   ```
   C:\xampp\htdocs\findfutsall\api\
   ```
2. Buat folder upload yang sejajar dengan `api/` (bukan di dalamnya):
   ```
   C:\xampp\htdocs\findfutsall\uploads\fields\
   C:\xampp\htdocs\findfutsall\uploads\users\
   C:\xampp\htdocs\findfutsall\uploads\payment_proofs\
   C:\xampp\htdocs\findfutsall\uploads\reviews\
   ```
3. Jalankan **Apache** dan **MySQL** dari XAMPP Control Panel.

### 3. Konfigurasi IP Address

Karena aplikasi diakses dari perangkat mobile ke server lokal, IP address harus disesuaikan setiap kali berpindah jaringan.

**Cek IP lokal komputer:**
```bash
ipconfig
```
Catat `IPv4 Address` dari adapter WiFi yang aktif.

**Update di 2 tempat:**

`api/config.php`:
```php
define('BASE_URL', "http://[IP_KAMU]/findfutsall/api/");
```

`app/src/main/java/com/utama/findfutsall/utils/Constants.kt`:
```kotlin
const val BASE_URL = "http://[IP_KAMU]/findfutsall/api/"
```

> Pastikan perangkat mobile dan komputer server terhubung ke jaringan WiFi yang sama.

## Konfigurasi Database

1. Buka **phpMyAdmin** (`http://localhost/phpmyadmin`)
2. Buat database baru bernama `findfutsall`
3. Import file SQL yang tersedia di folder `backend/database/` (jika ada), atau jalankan skrip pembuatan tabel berikut secara berurutan:
   - `users`
   - `owners`
   - `fields`
   - `bookings`
   - `payments`
   - `payment_methods`
   - `owner_targets`
   - `reviews`

Pastikan kolom `payment_methods.type` bertipe `VARCHAR`, bukan `ENUM`, agar dapat menampung semua jenis bank/e-wallet.

## Menjalankan Aplikasi

1. Buka project di Android Studio
2. Tunggu proses **Gradle Sync** selesai
3. Pastikan IP address di `Constants.kt` sudah sesuai jaringan saat ini
4. Sambungkan perangkat Android (atau gunakan emulator) yang terhubung ke jaringan WiFi yang sama dengan server
5. Klik **Run** ▶️

## Build APK/AAB

Untuk menghasilkan file rilis (APK atau Android App Bundle):

1. Buka menu Build → Generate Signed Bundle / APK
2. Pilih Android App Bundle (untuk submission Play Store) atau APK (untuk distribusi langsung)
3. Buat atau pilih Keystore — simpan file `.jks` dan kredensialnya di tempat aman, karena diperlukan untuk setiap update build berikutnya
4. Pilih build variant release
5. Klik **Finish**

File hasil build akan tersedia di `app/release/`.

## Struktur API

Seluruh endpoint backend menggunakan format JSON melalui HTTP POST. Beberapa endpoint utama:

| Endpoint | Fungsi |
|---|---|
| `login.php` | Autentikasi pengguna |
| `register.php` | Registrasi pengguna baru |
| `google_auth.php` | Login/registrasi via Google |
| `get_fields.php` | Daftar seluruh lapangan |
| `booking_with_proof.php` | Membuat booking dengan bukti pembayaran |
| `get_owner_stats.php` | Statistik dashboard pemilik |
| `get_owner_keuangan.php` | Data laporan keuangan |
| `add_review.php` | Mengirim rating dan ulasan |
| `get_reviews.php` | Mengambil ulasan suatu lapangan |

Dokumentasi lengkap parameter setiap endpoint dapat dilihat langsung pada masing-masing file PHP di folder `api/`.

## Kontribusi

Proyek ini dikembangkan sebagai bagian dari tugas akademik. Saran dan perbaikan dapat diajukan melalui Pull Request dengan deskripsi perubahan yang jelas.

---

Catatan Pengembangan: Aplikasi ini menggunakan server lokal (XAMPP) untuk keperluan pengembangan dan demonstrasi. Untuk penggunaan produksi, disarankan migrasi ke hosting dengan domain dan SSL yang valid, serta penyesuaian `BASE_URL` sesuai domain produksi.