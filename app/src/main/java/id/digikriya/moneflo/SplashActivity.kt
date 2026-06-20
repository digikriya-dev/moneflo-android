package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.digikriya.moneflo.database.DatabaseHelper

/**
 * SplashActivity — entry point aplikasi.
 *
 * Alur:
 * 1. Buka database (onCreate otomatis membuat tabel & seed jika belum ada)
 * 2. Cek session aktif
 *    - Ada session aktif → DashboardActivity
 *    - Tidak ada        → LoginActivity
 *
 * Daftarkan ini sebagai LAUNCHER di AndroidManifest.xml,
 * ganti LoginActivity sebagai launcher.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DatabaseHelper(this)
        val session = db.getActiveSession()

        val destination = if (session != null) {
            // TODO: ganti dengan DashboardActivity::class.java saat sudah dibuat
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }
}