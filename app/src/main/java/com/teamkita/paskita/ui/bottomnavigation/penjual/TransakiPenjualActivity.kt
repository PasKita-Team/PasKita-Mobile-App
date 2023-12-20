package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.databinding.ActivityTransakiPenjualBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.TransaksiAdapter

class TransakiPenjualActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransakiPenjualBinding
    private lateinit var listTransaksi: ArrayList<Transaksi>
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransakiPenjualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTransaksi.layoutManager = LinearLayoutManager(this)
        listTransaksi = arrayListOf()
        setDataTransaksi()

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, DashboardPenjual::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setDataTransaksi() {
        val produkCollection = db.collection("transaksi")
        val currentUser = auth.currentUser
        val query = produkCollection.whereEqualTo("uid_penjual", currentUser?.uid)
        query.get()
            .addOnSuccessListener {
                if (!it.isEmpty){
                    listTransaksi.clear()
                    for (data in it.documents){
                        val transaksi: Transaksi? = data.toObject(Transaksi::class.java)
                        if (transaksi != null) {
                            listTransaksi.add(transaksi)
                        }
                    }
                    val transaksiAdapter = TransaksiAdapter()
                    transaksiAdapter.submitList(listTransaksi)
                    binding.rvTransaksi.adapter = transaksiAdapter
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }
}