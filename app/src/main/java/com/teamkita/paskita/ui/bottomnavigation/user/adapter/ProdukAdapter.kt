package com.teamkita.paskita.ui.bottomnavigation.user.adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Favorite
import com.teamkita.paskita.data.Keranjang
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ListItemProdukBinding
import com.teamkita.paskita.ui.bottomnavigation.user.home.DetailProdukHome

class ProdukAdapter(private val listProduk: ArrayList<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.MyViewHolder>() {

    private var auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    inner class MyViewHolder(private val binding: ListItemProdukBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(produk: Produk) {
            val user = auth.currentUser

            binding.tvNamaProduk.text = produk.nama_produk
            binding.tvHarga.text = produk.harga_produk
            "${produk.nama_toko} - ${produk.alamat_toko}".also { binding.tvPenjual .text = it }
            Glide.with(itemView)
                .load(produk.url_foto_produk)
                .into(binding.ivProduk)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailProdukHome::class.java)
                intent.putExtra("produk", produk)

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.ivProduk, "image"),
                        Pair(binding.tvNamaProduk, "nama_produk"),
                        Pair(binding.tvHarga, "harga"),
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }

            if (user != null){
                binding.btnKeranjang.setOnClickListener {
                    val produkDocument = db.collection("keranjang").document("${produk.id_produk}_${user.uid}")
                    val user = auth.currentUser

                    val saveData = Keranjang(
                        id_produk =  produk.id_produk,
                        keranjang_by = user?.uid,
                    )
                    produkDocument.set(saveData)
                        .addOnSuccessListener {
                            Toast.makeText(itemView.context, "Di Tambahkan Ke Keranjang", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w("Produk Adapter", "failure : ", e)
                        }
                }

            }else {
                binding.btnKeranjang.setOnClickListener {
                    Toast.makeText(itemView.context, "Silahkan Login Terlebih Dahulu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ListItemProdukBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listProduk.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentProduk = listProduk[position]
        holder.bind(currentProduk)
    }
}
