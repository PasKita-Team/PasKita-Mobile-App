package com.teamkita.paskita.ui.bottomnavigation.user.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ListItemKeranjangBinding
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangViewModel
import com.teamkita.paskita.ui.bottomnavigation.user.transaksi.TransaksiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.transaksi.TransaksiViewModel
import me.abhinay.input.CurrencySymbols

class KeranjangProdukAdapter(private val context: KeranjangActivity) : ListAdapter<Produk, KeranjangProdukAdapter.MyViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemKeranjangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val review = getItem(position)
        holder.bind(review, context)
    }

    class MyViewHolder(private val binding: ListItemKeranjangBinding)
        : RecyclerView.ViewHolder(binding.root) {
        private val db = FirebaseFirestore.getInstance()
        fun bind(produk: Produk, context: KeranjangActivity) {

            val viewModel = ViewModelProvider(context)[KeranjangViewModel::class.java]

            binding.tvNamaToko.text = produk.nama_toko
            binding.tvNamaProduk.text = produk.nama_produk
            binding.tvHargaProduk.setText(produk.harga_produk.toString())
            Glide.with(itemView)
                .load(produk.url_foto_produk)
                .into(binding.ivImage)

            viewModel.currentNumber.observe(context, Observer {
                binding.tvJumlah.text = it.toString()
            })

            binding.ivTambah.setOnClickListener {
                viewModel.currentNumber.value = ++viewModel.number
            }

            binding.ivKurang.setOnClickListener {
                binding.ivKurang.setOnClickListener {
                    if (viewModel.number > 1){
                        viewModel.currentNumber.value = --viewModel.number
                    }
                }
            }

            binding.btnBeli.setOnClickListener {
                val jumlah = binding.tvJumlah.text.toString()
                val jumlahToInt = jumlah.toIntOrNull() ?: 0
                viewModel.number = jumlahToInt

                val hargaProdukString = produk.harga_produk

                val hargaRegex = Regex("\\d+(\\.\\d+)?")
                val matchResult = hargaRegex.find(hargaProdukString ?: "")

                val harga = matchResult?.value?.toDoubleOrNull() ?: 0.0

                viewModel.currentNumber.observe(context, Observer { number ->
                    val numberAsDouble = number.toDouble()
                    val totalHarga = numberAsDouble * (harga * 100)

                    binding.tvHargaProduk.setCurrency(CurrencySymbols.INDONESIA)
                    binding.tvHargaProduk.setDecimals(false)
                    binding.tvHargaProduk.setSeparator(".")
                    binding.tvHargaProduk.setText(totalHarga.toString())

                })

                val totalHarga = binding.tvHargaProduk.text.toString()
                val intent = Intent(itemView.context, TransaksiActivity::class.java)
                intent.putExtra("nama_toko", produk.nama_toko)
                intent.putExtra("nama_produk", produk.nama_produk)
                intent.putExtra("total_harga", totalHarga)
                intent.putExtra("harga", produk.harga_produk)
                intent.putExtra("jumlah", jumlah)
                intent.putExtra("url_foto_produk", produk.url_foto_produk)
                intent.putExtra("alamat_toko", produk.alamat_toko)
                intent.putExtra("produk", produk)
                itemView.context.startActivity(intent)
            }

            binding.ivHapus.setOnClickListener {

                val builder = AlertDialog.Builder(itemView.context)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Yakin Ingin Menhapus Produk Ini Dari Keranjang?")
                builder.setPositiveButton("Ya") { _, _ ->
                    val produkDocument = db.collection("produk").document(produk.id_produk.toString())
                    produkDocument.get().addOnSuccessListener { document ->
                        if (document != null) {

                            val updateData = hashMapOf(
                                "keranjang" to "false",
                                "keranjang_by" to "null",
                            )

                            produkDocument.update(updateData as Map<String, Any>)
                                .addOnSuccessListener {
                                    val intent = Intent(itemView.context, KeranjangActivity::class.java)
                                    itemView.context.startActivity(intent)
                                    (itemView.context as Activity).finish()
                                    Toast.makeText(itemView.context, "Berhasil Di Hapus Dari Keranjang", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Keranjang Adapter", "failure : ", e)
                                }
                        } else {
                            Log.d("Keranjang Adapter", "Dokumen tidak ditemukan.")
                        }
                    }.addOnFailureListener { e ->
                        Log.w("Keranjang Adapter", "Gagal mendapatkan dokumen: ", e)
                    }
                }
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