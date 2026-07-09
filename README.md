# Moneflo

![Android Badge](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin Badge](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![License Badge](https://img.shields.io/badge/License-Apache%202.0-D22128?style=flat-square&logo=apache&logoColor=white)

**Moneflo** adalah aplikasi pencatatan keuangan pribadi berbasis Android yang dirancang untuk bekerja secara penuh tanpa koneksi internet (**offline-first**). Aplikasi ini membantu pengguna melacak pendapatan, pengeluaran, dan mengelola anggaran harian dengan aman karena seluruh data disimpan secara lokal di dalam perangkat.

Aplikasi ini dikembangkan dan dikelola oleh **[DIGIKRIYA.ID](https://lynk.id/digikriya.id)** di bawah naungan **PT Digital Kriya Indonesia**.

Project ini dikerjakan sebagai tugas UAS mata kuliah **Pemrograman Mobile 1** — TIF K 24A.

---

## Daftar Anggota Kelompok

<!-- TODO: lengkapi dengan data anggota kelompok yang sebenarnya -->

| Nama | NPM | Peran |
|---|---|---|
| _Nama Anggota 1_ | _NPM_ | _misal: Ketua / UI-UX / Backend_ |
| _Nama Anggota 2_ | _NPM_ | _misal: Database / Auth_ |
| _Nama Anggota 3_ | _NPM_ | _misal: Testing / Dokumentasi_ |

---

## Fitur Utama

- **Autentikasi**: Registrasi & login manual (username/password ter-hash), Google Sign-In, Lupa Password.
- **Dashboard**: Ringkasan saldo, grafik pie chart pengeluaran bulan berjalan, daftar transaksi terbaru.
- **Transaksi**: Tambah/edit/hapus transaksi (pemasukan & pengeluaran), kategori kustom.
- **Riwayat Transaksi**: Pencarian, filter kategori, filter bulan & tahun, dikelompokkan per tanggal.
- **Statistik**: Donut chart pengeluaran per kategori (mingguan/bulanan/tahunan), kategori tertinggi, rata-rata harian.
- **Profil**: Edit profil, ubah password, foto profil (dari galeri), logout.
- **Tema**: Mode Terang / Gelap / Ikuti Sistem, tersimpan permanen.

---

## Cara Menjalankan / Clone Project

### Prasyarat
- [Android Studio](https://developer.android.com/studio) (terbaru direkomendasikan)
- JDK 17+ (biasanya sudah termasuk di Android Studio)
- Android SDK dengan `compileSdk 34`, `minSdk 24`

### Langkah

```bash
# 1. Clone repository
git clone https://github.com/digikriya-dev/moneflo-android.git
cd moneflo-android

# 2. Buka project di Android Studio
#    File > Open > pilih folder moneflo-android

# 3. Tunggu Gradle sync selesai (otomatis mengunduh dependency)

# 4. Jalankan di emulator/device
#    Klik tombol Run (▶) atau:
./gradlew installDebug
```

### Build APK Release

APK release siap-install sudah tersedia di [`/apk/moneflo-release.apk`](apk/moneflo-release.apk). Untuk build ulang secara manual:

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

> Project ini ditandatangani memakai `moneflo-debug.keystore` (bukan keystore produksi) supaya SHA-1 tetap cocok dengan yang terdaftar di Firebase Console untuk Google Sign-In. Konfigurasinya ada di `app/build.gradle` (blok `signingConfigs.release`).

---

## Screenshot Aplikasi

<!-- TODO: tambahkan minimal 2 screenshot aplikasi (mis. Dashboard & Riwayat Transaksi).
     Simpan file gambar di folder screenshots/, lalu ganti placeholder di bawah. -->

| Dashboard | Riwayat Transaksi |
|---|---|
| ![Dashboard](screenshots/dashboard.png) | ![Riwayat Transaksi](screenshots/riwayat.png) |

---

## Video Penjelasan Project

<!-- TODO: ganti dengan link video (YouTube/Google Drive, akses publik) berisi:
     1) perkenalan anggota kelompok, 2) demo aplikasi, 3) penjelasan alur kode -->

📺 **Link Video:** _[isi link video di sini]_

---

## Use Case Diagram

![Moneflo Use Case Diagram](design/use-cases/use_case_diagram.jpg)

Laporan OOAD lengkap (Class Diagram, ERD, Sequence Diagram, Activity Diagram) ada di [`/ooad`](ooad/README.md).

---

## Tech Stack

- **Bahasa**: Kotlin
- **Platform**: Android Native (Activity-based, tanpa Fragment)
- **Database**: SQLite (`SQLiteOpenHelper` kustom, tanpa Room)
- **Auth**: Firebase Authentication + Google Sign-In
- **UI**: Material Components, ViewBinding manual (`findViewById`), MPAndroidChart untuk grafik
- **Arsitektur**: Activity per layar + helper class (`DatabaseHelper`, `GoogleAuthHelper`, dll) — bukan MVVM/Clean Architecture berlapis
