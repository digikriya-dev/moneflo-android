package id.digikriya.moneflo.helper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.digikriya.moneflo.DashboardActivity
import id.digikriya.moneflo.HistoryActivity
import id.digikriya.moneflo.ProfileActivity
import id.digikriya.moneflo.R
import id.digikriya.moneflo.StatisticsActivity

/**
 * Navigasi 4-tab (Home/Transaksi/Stats/Profil) untuk app yang murni Activity-per-layar
 * (bukan Fragment). Setiap tab adalah root Activity-nya sendiri; pindah tab akan
 * finish() activity saat ini supaya back-stack tidak menumpuk.
 */
object BottomNavHelper {

    fun setup(bottomNav: BottomNavigationView, currentTabId: Int, activity: AppCompatActivity, userId: Long) {
        bottomNav.selectedItemId = currentTabId

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentTabId) return@setOnItemSelectedListener true

            val target: Class<out AppCompatActivity>? = when (item.itemId) {
                R.id.nav_dashboard -> DashboardActivity::class.java
                R.id.nav_history   -> HistoryActivity::class.java
                R.id.nav_stats     -> StatisticsActivity::class.java
                R.id.nav_profile   -> ProfileActivity::class.java
                else -> null
            }

            if (target != null) {
                val intent = Intent(activity, target).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    putExtra("USER_ID", userId)
                }
                activity.startActivity(intent)
                activity.finish()
                // Pindah tab harus terasa instan (seperti app dengan bottom nav pada
                // umumnya), bukan transisi "buka activity baru" ala sistem.
                activity.overridePendingTransition(0, 0)
                true
            } else {
                false
            }
        }
    }
}
