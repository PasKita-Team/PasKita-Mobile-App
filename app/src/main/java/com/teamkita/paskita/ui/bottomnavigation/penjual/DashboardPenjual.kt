package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityDashboardPenjualBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.penjual.adapter.ProdukPenjualAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.PesanActivity

class DashboardPenjual : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardPenjualBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardPenjualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        updateProfileToko()

        val layoutManager = LinearLayoutManager(this)
        binding.rvProduk.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvProduk.addItemDecoration(itemDecoration)

        getProdukPenjual()

        setupAction()
    }

    private fun setupAction() {

        binding.tvEditToko.setOnClickListener {
            val daftarPenjual = DaftarPenjual.newInstance()
            daftarPenjual.show(supportFragmentManager, DaftarPenjual.TAG)
        }

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            finish()
        }

        binding.ivPesan.setOnClickListener {
            val intent = Intent(this, PesanActivity::class.java)
            startActivity(intent)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(this, NotifikasiActivity::class.java)
            startActivity(intent)
        }

        binding.btnPesanan.setOnClickListener {
            val intent = Intent(this, TransakiPenjualActivity::class.java)
            startActivity(intent)
        }

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, TambahProduk::class.java)
            startActivity(intent)
        }

    }

    private fun getProdukPenjual() {
        showLoading(true)
        val firestore = FirebaseFirestore.getInstance()
        val produkCollection = firestore.collection("produk")

        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("uid_penjual", currentUser?.uid)

        query.get()
            .addOnSuccessListener { documents ->
                val produkList = mutableListOf<Produk>()
                for (document in documents) {
                    val produkData = document.toObject<Produk>()
                    produkList.add(produkData)
                }
                setDataProduk(produkList)
                showLoading(false)
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                println("Error getting documents: $exception")
            }
    }

    private fun setDataProduk(produk:  List<Produk>) {
        val produkAdapter = ProdukPenjualAdapter()
        produkAdapter.submitList(produk)
        binding.rvProduk.adapter = produkAdapter
    }

    private fun updateProfileToko() {
        showLoading(true)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val penjualDocument = db.collection("penjual").document(currentUser.uid)
            penjualDocument.addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {

                    val userDocument = db.collection("users").document(currentUser.uid)
                    userDocument.addSnapshotListener { user, _ ->
                        val sebagai = user?.getString("as")

                        val namaToko = document.getString("namaToko")
                        val alamatToko = document.getString("alamatToko")
                        val deskripsiToko = document.getString("deskripsiToko")
                        val urlProfileToko = document.getString("url_profile_toko")
                        val totalProduk = document.getString("total_produk")
                        val terjual = document.getString("terjual")

                        if (sebagai.equals("penjual premium")){
                            "$namaToko (Premium)".also { binding.tvNamaToko.text = it }
                        }else{
                            binding.tvNamaToko.text = namaToko
                        }
                        binding.tvAlamatToko.text = alamatToko
                        binding.tvDescToko.text = deskripsiToko
                        binding.tvTotalProduk.text = totalProduk
                        binding.tvTerjual.text = terjual

                        if (urlProfileToko != null){
                            Glide.with(applicationContext)
                                .load(urlProfileToko)
                                .placeholder(R.drawable.tulisan_paskita)
                                .error(R.drawable.baseline_error_24)
                                .into(binding.ivPhotoUser)
                        }

                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }

}