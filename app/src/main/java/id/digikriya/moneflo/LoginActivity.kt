package id.digikriya.moneflo

import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.digikriya.moneflo.database.DatabaseHelper
import id.digikriya.moneflo.helper.GoogleAuthHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var btnSignIn: View
    private lateinit var btnGoogle: View
    private lateinit var cbRememberMe: CheckBox
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUp: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var logoContainer: View
    private lateinit var formContainer: View

    private lateinit var db: DatabaseHelper
    private lateinit var googleAuth: GoogleAuthHelper

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleAuth.handleSignInResult(
            data      = result.data,
            onSuccess = { userId, isNewUser ->
                db.setLoginSession(userId)
                val msg = if (isNewUser) "Akun Google berhasil didaftarkan!" else "Login dengan Google berhasil!"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                goToDashboard()
            },
            onError = { message ->
                setLoadingState(false)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db         = DatabaseHelper(this)
        googleAuth = GoogleAuthHelper(this)

        initViews()
        setupListeners()
        playEntryAnimation()
        startBackgroundAnimation()
    }

    private fun initViews() {
        etUsername       = findViewById(R.id.et_username)
        etPassword       = findViewById(R.id.et_password)
        tilUsername      = findViewById(R.id.til_username)
        tilPassword      = findViewById(R.id.til_password)
        btnSignIn        = findViewById(R.id.btn_sign_in)
        btnGoogle        = findViewById(R.id.btn_google)
        cbRememberMe     = findViewById(R.id.cb_remember_me)
        tvForgotPassword = findViewById(R.id.tv_forgot_password)
        tvSignUp         = findViewById(R.id.tv_sign_up)
        progressBar      = findViewById(R.id.progress_bar)
        logoContainer    = findViewById(R.id.logo_container)
        formContainer    = findViewById(R.id.form_container)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener {
            if (validateInputs()) performLogin()
        }

        btnGoogle.setOnClickListener {
            setLoadingState(true)
            googleSignInLauncher.launch(googleAuth.getSignInIntent())
        }

        tvForgotPassword.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvSignUp.setOnClickListener {
            animateClick(it)
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        if (username.isEmpty()) {
            tilUsername.error = "Username tidak boleh kosong"
            shakeView(tilUsername)
            isValid = false
        } else {
            tilUsername.error = null
        }

        if (password.isEmpty()) {
            tilPassword.error = "Password tidak boleh kosong"
            shakeView(tilPassword)
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin() {
        setLoadingState(true)
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        if (!db.isUsernameExist(username)) {
            setLoadingState(false)
            tilUsername.error = "Username tidak ditemukan"
            shakeView(tilUsername)
            return
        }

        val user = db.loginUser(username, password)
        if (user == null) {
            setLoadingState(false)
            tilPassword.error = "Password salah"
            shakeView(tilPassword)
            return
        }

        if (cbRememberMe.isChecked) {
            db.setLoginSession(user.id)
        }

        setLoadingState(false)
        goToDashboard()
    }

    private fun goToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoadingState(isLoading: Boolean) {
        btnSignIn.isEnabled    = !isLoading
        btnGoogle.isEnabled    = !isLoading
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        etUsername.isEnabled   = !isLoading
        etPassword.isEnabled   = !isLoading
    }

    private fun playEntryAnimation() {
        logoContainer.translationY = -100f
        logoContainer.alpha = 0f
        formContainer.translationY = 150f
        formContainer.alpha = 0f

        val logoAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logoContainer, "translationY", -100f, 0f),
                ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f)
            )
            duration = 700
            interpolator = DecelerateInterpolator()
        }

        val formAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(formContainer, "translationY", 150f, 0f),
                ObjectAnimator.ofFloat(formContainer, "alpha", 0f, 1f)
            )
            duration = 700
            startDelay = 300
            interpolator = DecelerateInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(logoAnim, formAnim)
            start()
        }
    }

    private fun startBackgroundAnimation() {
        val logoPulse = AnimatorSet()
        logoPulse.playTogether(
            ObjectAnimator.ofFloat(logoContainer, "scaleX", 1f, 1.05f, 1f),
            ObjectAnimator.ofFloat(logoContainer, "scaleY", 1f, 1.05f, 1f),
            ObjectAnimator.ofFloat(logoContainer, "alpha", 1f, 0.85f, 1f)
        )
        logoPulse.duration = 2500
        logoPulse.interpolator = DecelerateInterpolator()
        logoPulse.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { logoPulse.start() }
        })
        logoContainer.postDelayed({ logoPulse.start() }, 1000)
    }

    private fun animateClick(view: View) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.93f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.93f, 1f)
            )
            duration = 200
            interpolator = OvershootInterpolator()
            start()
        }
    }

    private fun shakeView(view: View) {
        ObjectAnimator.ofFloat(view, "translationX", 0f, -16f, 16f, -12f, 12f, -8f, 8f, 0f).apply {
            duration = 400
            start()
        }
    }
}