package id.digikriya.moneflo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper

// =====================================================================
// ResetPasswordActivity
// Halaman kedua: input password baru & konfirmasi → simpan ke DB
// =====================================================================
class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var etPasswordBaru: TextInputEditText
    private lateinit var etKonfirmasiPassword: TextInputEditText
    private lateinit var tilPasswordBaru: TextInputLayout
    private lateinit var tilKonfirmasiPassword: TextInputLayout
    private lateinit var btnSimpan: View

    private lateinit var db: DatabaseHelper
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        userId = intent.getLongExtra("USER_ID", -1L)
        if (userId == -1L) { finish(); return }

        db = DatabaseHelper(this)

        etPasswordBaru       = findViewById(R.id.et_password_baru)
        etKonfirmasiPassword = findViewById(R.id.et_konfirmasi_password)
        tilPasswordBaru      = findViewById(R.id.til_password_baru)
        tilKonfirmasiPassword = findViewById(R.id.til_konfirmasi_password)
        btnSimpan            = findViewById(R.id.btn_simpan)

        btnSimpan.setOnClickListener {
            if (validateInputs()) performReset()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val passwordBaru  = etPasswordBaru.text.toString()
        val konfirmasi    = etKonfirmasiPassword.text.toString()

        if (passwordBaru.length < 6) {
            tilPasswordBaru.error = "Password minimal 6 karakter"
            isValid = false
        } else {
            tilPasswordBaru.error = null
        }

        if (konfirmasi != passwordBaru) {
            tilKonfirmasiPassword.error = "Konfirmasi password tidak sama"
            isValid = false
        } else {
            tilKonfirmasiPassword.error = null
        }

        return isValid
    }

    private fun performReset() {
        val passwordBaru = etPasswordBaru.text.toString()
        val berhasil = db.updatePassword(userId, passwordBaru)

        if (berhasil) {
            Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show()
            // Kembali ke Login, bersihkan backstack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            Toast.makeText(this, "Gagal mengubah password", Toast.LENGTH_SHORT).show()
        }
    }
}
