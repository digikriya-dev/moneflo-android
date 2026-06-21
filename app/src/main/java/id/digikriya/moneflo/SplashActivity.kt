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

        val destination = if (session != null) {
            DashboardActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, destination))
        finish()
    }
}