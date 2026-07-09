package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.digikriya.moneflo.helper.playEntranceAnimation

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        findViewById<android.view.View>(R.id.btn_mulai).setOnClickListener {
            markOnboardingSeen()
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.btn_masuk).setOnClickListener {
            markOnboardingSeen()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.tv_bantuan).setOnClickListener {
            Toast.makeText(this, "Hubungi kami di support@moneflo.app", Toast.LENGTH_SHORT).show()
        }

        playEntranceAnimation()
    }

    private fun markOnboardingSeen() {
        getSharedPreferences("moneflo_prefs", MODE_PRIVATE)
            .edit().putBoolean("onboarding_seen", true).apply()
    }

    override fun onBackPressed() {
        markOnboardingSeen()
        super.onBackPressed()
    }
}
