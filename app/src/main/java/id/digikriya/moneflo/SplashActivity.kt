package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.digikriya.moneflo.database.DatabaseHelper


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DatabaseHelper(this)
        val session = db.getActiveSession()
        val prefs = getSharedPreferences("moneflo_prefs", MODE_PRIVATE)

        val destination = if (session != null) {
            prefs.edit().putLong("current_user_id", session.userId).apply()
            DashboardActivity::class.java
        } else {
            prefs.edit().remove("current_user_id").apply()
            if (prefs.getBoolean("onboarding_seen", false)) {
                LoginActivity::class.java
            } else {
                OnboardingActivity::class.java
            }
        }

        startActivity(Intent(this, destination))
        finish()
    }
}