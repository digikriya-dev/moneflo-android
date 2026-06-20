package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper

// =====================================================================
// ForgotPasswordActivity
// Halaman pertama: input username → validasi → lanjut ke ResetPassword
// =====================================================================
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var btnLanjutkan: View

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        db = DatabaseHelper(this)

        etUsername  = findViewById(R.id.et_username)
        tilUsername = findViewById(R.id.til_username)
        btnLanjutkan = findViewById(R.id.btn_lanjutkan)

        btnLanjutkan.setOnClickListener {
            val username = etUsername.text.toString().trim()

            if (username.isEmpty()) {
                tilUsername.error = "Username wajib diisi"
                return@setOnClickListener
            }

            if (!db.isUsernameExist(username)) {
                tilUsername.error = "Username tidak ditemukan"
                return@setOnClickListener
            }

            tilUsername.error = null
            val user = db.getUserByUsername(username)!!

            // Lanjut ke halaman reset password, bawa userId
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("USER_ID", user.id)
            startActivity(intent)
        }
    }
}
