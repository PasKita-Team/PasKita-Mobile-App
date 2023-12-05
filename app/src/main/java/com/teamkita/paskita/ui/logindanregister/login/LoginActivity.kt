package com.teamkita.paskita.ui.logindanregister.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.teamkita.paskita.databinding.ActivityLoginBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.logindanregister.lupapassword.LupaPasswordActivity
import com.teamkita.paskita.ui.logindanregister.register.RegisterActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupView()
        setupAction()
        playAnimation()
    }

    private fun setupAction() {

        binding.ivBack.setOnClickListener {
            startActivity(Intent(this, BottomNavigation::class.java))
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.lupaPassword.setOnClickListener {
            startActivity(Intent(this, LupaPasswordActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            showLoading(true)
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (!(email.isEmpty() || password.isEmpty())) {
                loginDenganEmail(email, password)
            } else {
                showLoading(false)
                Toast.makeText(applicationContext, "Data Tidak Terdaftar", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun loginDenganEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) {
                    showLoading(false)
                    Toast.makeText(applicationContext, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(applicationContext, BottomNavigation::class.java))
                } else {
                    showLoading(false)
                    binding.emailEditTextLayout.error = "Silahkan Cek Email Dan Verifikasi Akun Anda"
                }
            } else {
                showLoading(false)
                Toast.makeText(applicationContext, "Login Anda Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }


    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val message =
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val lupaPassword = ObjectAnimator.ofFloat(binding.lupaPassword, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(100)
        val register = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                message,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                lupaPassword,
                login,
                register
            )
            startDelay = 100
        }.start()
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }
}