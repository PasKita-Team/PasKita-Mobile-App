package com.teamkita.paskita.ui.bottomnavigation.user.adapter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.data.Transaksi
import com.teamkita.paskita.databinding.ListItemTransaksiBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.daftartransaksi.DetailTransaksiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.home.DetailProdukHome
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.PesanActivity

class TransaksiAdapter : ListAdapter<Transaksi, TransaksiAdapter.MyViewHolder>(
    DIFF_CALLBACK
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemTransaksiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val review = getItem(position)
        holder.bind(review)
    }

    class MyViewHolder(private val binding: ListItemTransaksiBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaksi: Transaksi) {

            val nUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()

            val userDocument = nUser?.let { db.collection("penjual").document(it.uid) }
            userDocument?.get()
                ?.addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.data
                        val namaToko = userData?.get("namaToko") as String

                        if (namaToko == transaksi.namaToko){
                            binding.btnTanyaPenjual.visibility = View.GONE
                        }

                        binding.tvTanggal.text = transaksi.tanggal
                        binding.tvProses.text = transaksi.status
                        binding.tvNamaProduk.text = transaksi.namaProduk
                        "${transaksi.jumlahProduk} Barang".also { binding.tvJumlah.text = it }
                        binding.tvTotalBelanja.text = transaksi.totalBayar

                        Glide.with(itemView)
                            .load(transaksi.url_foto_produk)
                            .into(binding.ivImage)

                        itemView.setOnClickListener {
                            val intent = Intent(itemView.context, DetailTransaksiActivity::class.java)
                            intent.putExtra("transaksi", transaksi)

                            val optionsCompat: ActivityOptionsCompat =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    itemView.context as Activity,
                                    Pair(binding.ivImage, "image"),
                                    Pair(binding.tvNamaProduk, "nama_produk"),
                                    Pair(binding.tvTotalBelanja, "harga"),
                                )
                            itemView.context.startActivity(intent, optionsCompat.toBundle())
                        }

                        binding.btnTanyaPenjual.setOnClickListener {
                            val intent = Intent(itemView.context, PesanActivity::class.java)
                            intent.putExtra("nama_toko", transaksi.namaToko)
                            intent.putExtra("uid_penjual", transaksi.uid_penjual)
                            val optionsCompat: ActivityOptionsCompat =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    itemView.context as Activity,
                                    Pair(binding.ivImage, "image"),
                                    Pair(binding.tvNamaProduk, "nama_produk"),
                                    Pair(binding.tvTotalBelanja, "harga"),
                                )
                            itemView.context.startActivity(intent, optionsCompat.toBundle())
                        }
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.w("GetUserData", "Gagal mengambil data: ", e)
                }

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaksi>() {
            override fun areItemsTheSame(oldItem: Transaksi, newItem: Transaksi): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Transaksi, newItem: Transaksi): Boolean {
                return oldItem == newItem
            }
        }
    }
}