@file:Suppress("DEPRECATION")

package com.teamkita.paskita.ui.logindanregister.register

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
import com.teamkita.paskita.databinding.ActivityRegisterBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.logindanregister.login.LoginActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupView()
        setupAction()
        playAnimation()
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

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            startActivity(Intent(this, BottomNavigation::class.java))
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.signupButton.setOnClickListener {
            showLoading(true)
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.repeatpasswordEditText.text.toString()

            if (!(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())) {
                if (password.length > 6) {
                    if (confirmPassword == password) {
                        registerAkun(email, password)
                    } else {
                        showLoading(false)
                        binding.repeatpasswordEditTextLayout.error = "Password Tidak Sama!!"
                    }
                } else {
                    showLoading(false)
                    binding.passwordEditTextLayout.error = "Password Harus Lebih Dari 6 Karakter!!"
                }
            } else {
                showLoading(false)
                Toast.makeText(applicationContext, "Ada Data Yang Masih Kosong!!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun registerAkun(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@RegisterActivity
            ) { task ->
                if (task.isSuccessful) {
                    auth.currentUser!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showLoading(false)
                                Toast.makeText(applicationContext, "Daftar Berhasil. Silahkan Cek Email Anda!!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(applicationContext, LoginActivity::class.java))
                            } else {
                                showLoading(false)
                                Toast.makeText(applicationContext, "Verifikasi Gagal Di Kirim", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(applicationContext, "Daftar Gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100)
        val lengkapidata =
            ObjectAnimator.ofFloat(binding.lengkapidata, View.ALPHA, 1f).setDuration(100)
        val emailTextView =
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val repeatpasswordTextView =
            ObjectAnimator.ofFloat(binding.repeatpasswordTextView, View.ALPHA, 1f).setDuration(100)
        val repeatpasswordEditTextLayout =
            ObjectAnimator.ofFloat(binding.repeatpasswordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)
        val login = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(100)


        AnimatorSet().apply {
            playSequentially(
                title,
                lengkapidata,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                repeatpasswordTextView,
                repeatpasswordEditTextLayout,
                signup,
                login
            )
            startDelay = 100
        }.start()
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }
}