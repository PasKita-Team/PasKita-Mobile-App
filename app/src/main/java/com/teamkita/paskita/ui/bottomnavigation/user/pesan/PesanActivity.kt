package com.teamkita.paskita.ui.bottomnavigation.user.pesan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.databinding.ActivityPesanBinding

class PesanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPesanBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupaAction()
        setupData()
    }

    private fun setupData() {
        val uid_penjual= intent.getStringExtra("uid_penjual")
        val nama_toko= intent.getStringExtra("nama_toko")

        binding.tvNamaUser.text = nama_toko
        getfotoPenjual(uid_penjual)
    }

    private fun getfotoPenjual(uid_penjual: String?) {

        val userDocument = uid_penjual?.let { db.collection("penjual").document(it) }
        userDocument?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val url_foto_penjual = userData?.get("url_profile_toko") as String

                    Glide.with(this)
                        .load(url_foto_penjual)
                        .into(binding.ivPhotoUser)
                }
            }
            ?.addOnFailureListener { e ->
                Log.w("GetUserData", "Gagal mengambil data: ", e)
            }
    }

    private fun setupaAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DaftarPesanActivity::class.java)
            startActivity(intent)

            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }
    }
}