package com.teamkita.paskita.ui.bottomnavigation.user.daftartransaksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.databinding.ActivityDetailTransaksiBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.penjual.DashboardPenjual
import com.teamkita.paskita.ui.bottomnavigation.user.profile.InformasiPribadi
import com.teamkita.paskita.ui.bottomnavigation.user.ratingdanulasan.Ulasan

class DetailTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransaksiBinding
    private val nUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        setUpData()

    }

    private fun setUpData() {
        val transaksi = intent.getParcelableExtra<Transaksi>("transaksi") as Transaksi

        val userDocument = nUser?.let { db.collection("penjual").document(it.uid) }
        userDocument?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val namaToko = userData?.get("namaToko") as String

                    if (namaToko == transaksi.namaToko){
                        binding.etResi.isEnabled = true
                        "Kirim".also { binding.btnSelesai.text = it }

                        binding.btnSelesai.setOnClickListener {
                            updateTransaksi(transaksi.uid_pembeli, transaksi.namaProduk)
                        }
                    }
                }
            }
            ?.addOnFailureListener { e ->
                Log.w("GetUserData", "Gagal mengambil data: ", e)
            }

        binding.tvNamaToko.text = transaksi.namaToko
        binding.tvNamaProduk.text = transaksi.namaProduk
        "${transaksi.jumlahProduk} Barang".also { binding.tvJumlah.text = it }
        "Total Belanja : ${transaksi.totalBayar}".also { binding.tvHargaProduk.text = it }
        binding.etCatataProduk.setText(transaksi.catatanProduk)

        Glide.with(this)
            .load(transaksi.url_foto_produk)
            .into(binding.ivImage)

        binding.etResi.setText(transaksi.no_resi)
        binding.etALamatLengkap.setText(transaksi.alamatPengiriman)
        binding.etKota.setText(transaksi.kota)
        binding.etProvinsi.setText(transaksi.provinsi)
        binding.etPengiriman.setText(transaksi.kurir)

        Glide.with(this)
            .load(transaksi.buktiPembayaran)
            .into(binding.ivPembayaran)

    }

    private fun updateTransaksi(uidPembeli: String?, namaProduk: String?) {
        val userDocumentRef = db.collection("transaksi").document(uidPembeli+"_"+namaProduk)
        val no_resi = binding.etResi.text.toString()

        val updates = hashMapOf(
            "no_resi" to no_resi,
            "status" to "Di Kirim"
        )

        userDocumentRef
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Update Resi Dan Status Berhasil",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, DashboardPenjual::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }

    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            finish()
        }

        val transaksi = intent.getParcelableExtra<Transaksi>("transaksi") as Transaksi
        binding.btnSelesai.isEnabled = !transaksi.status.equals("Selesai")

        binding.btnSelesai.setOnClickListener {
            val ulasan = Ulasan.newInstance(transaksi)
            ulasan.show(supportFragmentManager, Ulasan.TAG)
        }
    }
}