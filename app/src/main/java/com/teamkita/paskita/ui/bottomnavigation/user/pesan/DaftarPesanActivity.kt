package com.teamkita.paskita.ui.bottomnavigation.user.pesan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.teamkita.paskita.R
import com.teamkita.paskita.databinding.ActivityDaftarPesanBinding
import com.teamkita.paskita.databinding.ActivityPesanBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation

class DaftarPesanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaftarPesanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarPesanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }
    }
}