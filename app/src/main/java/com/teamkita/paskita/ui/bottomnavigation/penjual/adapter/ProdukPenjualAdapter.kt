package com.teamkita.paskita.ui.bottomnavigation.penjual.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ListItemProdukPenjualBinding
import com.teamkita.paskita.ui.bottomnavigation.penjual.DashboardPenjual
import com.teamkita.paskita.ui.bottomnavigation.penjual.DetailProdukPenjual
import com.teamkita.paskita.ui.bottomnavigation.penjual.EditProduk

class ProdukPenjualAdapter : ListAdapter<Produk, ProdukPenjualAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemProdukPenjualBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val review = getItem(position)
        holder.bind(review)
    }

    class MyViewHolder(private val binding: ListItemProdukPenjualBinding)
        : RecyclerView.ViewHolder(binding.root) {

        private val db = FirebaseFirestore.getInstance()
        fun bind(produk: Produk){
            binding.tvNamaProduk.text = produk.nama_produk
            binding.tvHarga.text = produk.harga_produk
            Glide.with(itemView)
                .load(produk.url_foto_produk)
                .into(binding.ivProduk)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailProdukPenjual::class.java)
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

            binding.btnEdit.setOnClickListener {
                val intent = Intent(itemView.context, EditProduk::class.java)
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

            binding.btnHapus.setOnClickListener {
                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Apakah kamu yakin ingin menghapus produk ini?")

                // Tombol jika user ingin menghapus
                builder.setPositiveButton("Ya") { _, _ ->
                    // Update ke penjual
                    val penjualDocument = db.collection("penjual").document(produk.uid_penjual.toString())
                    penjualDocument.get().addOnSuccessListener { document ->
                        if (document != null) {
                            val totalProdukSaatIniString = document.getString("total_produk")
                            val totalProdukSaatIni = totalProdukSaatIniString?.toInt() ?: 0
                            val totalProdukBaru = totalProdukSaatIni - 1

                            // Membuat objek untuk diupdate dengan total_produk yang baru
                            val updateData = hashMapOf(
                                "total_produk" to totalProdukBaru.toString(),
                            )

                            // Update total_produk pada dokumen penjual
                            penjualDocument.update(updateData as Map<String, Any>)
                                .addOnSuccessListener {
                                    // Lanjutkan untuk hapus data produk
                                    val produkDocument = produk.id_produk?.let { db.collection("produk").document(it) }
                                    produkDocument?.delete()?.addOnSuccessListener {
                                        Toast.makeText(itemView.context, "Produk Berhasil Di Hapus", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(itemView.context, DashboardPenjual::class.java)
                                        itemView.context.startActivity(intent)
                                        (itemView.context as Activity).finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Produk Adapter", "failure : ", e)
                                }
                        } else {
                            Log.d("Produk Adapter", "Dokumen penjual tidak ditemukan.")
                        }
                    }.addOnFailureListener { e ->
                        Log.w("Produk Adapter", "Gagal mendapatkan dokumen penjual: ", e)
                    }
                }

                // Tombol jika user membatalkan penghapusan
                builder.setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }


        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Produk>() {
            override fun areItemsTheSame(oldItem: Produk, newItem: Produk): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Produk, newItem: Produk): Boolean {
                return oldItem == newItem
            }
        }
    }
}