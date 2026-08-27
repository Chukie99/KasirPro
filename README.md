# 🛒 KasirPro — Android POS Offline

**Aplikasi kasir profesional untuk kedai kopi, kafe, resto, dan UMKM food & beverage — berjalan 100% offline, dilindungi lisensi Device ID.**

KasirPro adalah kawasan kasir pintar yang mendukung:
- ✅ **100% offline** — tidak perlu internet setelah instalasi
- ✅ **Aktivasi berbasis Device ID** — anti-piracy, tanpa cracking
- ✅ **Manajemen produk & meja** — CRUD penuh, gambar produk
- ✅ **Transaksi kasir** penuh — diskon, pajak (default 11%), payment Cash/QRIS/Transfer
- ✅ **Cetak struk thermal** (bluetooth 58mm/80mm, ESC/POS)
- ✅ **Laporan penjualan** — harian/mingguan/bulanan + grafik + export CSV
- ✅ **Backup & Restore** database (.db → Downloads)
- ✅ **Modern UI Material Design 3** — tema biru Google pastel (bukan ungu)

---

## ⚡ Cara Pakai

### 1. Build dari sumber (Android Studio)
```bash
git clone https://github.com/Chukie99/KasirPro.git
cd KasirPro
# Buka di Android Studio → Sync Gradle → Run on device/emulator
```

> **Butuh:** Android Studio Hedgehog+ (atau newer), Android SDK 34, JDK 17.  
> Target SDK: **API 34 (Android 14)** | Min SDK: **API 24 (Android 7.0)**

### 2. Install APK langsung
Unduh APK di [GitHub Releases](https://github.com/Chukie99/KasirPro/releases) atau minta dari admin.

### 3. Aktivasi (anti-bajak)
- Aplikasi akan menampilkan **Device ID** (8 karakter, auto-generate).
- Kirim Device ID ke admin/reseller via WhatsApp.
- Admin gunakan **Serial Generator** (`admin/serial-generator.html`) untuk generate serial.
- Masukkan serial → aplikasi terbuka ke dashboard.

### 4. Pakai kasir
- Tambahkan produk (nama, harga, stok, kategori, gambar)
- Tambahkan meja
- Pilih produk, ubah kuantitas, pilih meja + pembayaran
- Klik **Bayar** → struk langsung tercetak (jika printer terhubung)

---

## 📱 Fitur Lengkap

| Fitur | Status |
|-------|--------|
| Splash screen + aktivasi Device ID | ✅ |
| Dashboard grid produk (2 kolom, search, filter kategori) | ✅ |
| Transaksi kasir (keranjang, meja, pajak, diskon, payment) | ✅ |
| Manajemen produk (CRUD, kamera/galeri gambar) | ✅ |
| Manajemen meja (CRUD, status Kosong/Terisi/Reservasi) | ✅ |
| Laporan penjualan (harian/mingguan/bulanan, grafik, CSV export) | ✅ |
| Pengaturan toko (nama, alamat, telp, logo) | ✅ |
| Cetak struk thermal (bluetooth ESC/POS 58mm/80mm) | ✅ |
| Backup database (.db → Downloads) | ✅ |
| Restore database (.db) | ✅ |
| Dark mode | ✅ |
| Export CSV | ✅ |

---

## 🎨 Tema & Desain

| Warna | Hex |
|-------|-----|
| **Primary** | `#1A73E8` biru Google |
| **Primary Light** | `#E8F0FE` |
| **Secondary** | `#34A853` hijau |
| **Tertiary** | `#FBBC04` kuning |
| **Error** | `#D93025` |
| **Background** | `#F5F7FA` |

Typografi: **Roboto** — Bold heading, Regular body, Light small.

---

## ⚙️ Teknologi

| Layer | Teknologi |
|-------|-----------|
| Bahasa | **Kotlin** |
| UI | **Jetpack Compose** + Material Design 3 |
| Database | **Room (SQLite)** |
| Architecture | **MVVM** + Flow |
| Image Loading | **Coil** |
| Printer | **esc-pos-printer** (ESC/POS) |
| Chart | **MPAndroidChart** |
| DI | **Hilt** |
| CSV Export | **kotlin-csv** |
| Target SDK | API 34 | Min SDK | API 24 |

---

## 💰 Model Lisensi

KasirPro dilisensikan per device (per toko).

1. Reseller dapatkan APK bebas distribusi.
2. User instal, dapatkan **Device ID**.
3. Reseller generate **Serial Number** via admin tool.
4. User masukkan serial → aplikasi terbuka permanen.

> **Catatan:** Serial hanya berlaku untuk Device ID spesifik. Tidak bisa dipindah ke device lain.

---

## 📄 Hak Cipta

© 2025 KasirPro. All rights reserved.  
Dikembangkan oleh [Chukie99](https://github.com/Chukie99)

Lisensi: **MIT** — lihat [LICENSE](LICENSE).
