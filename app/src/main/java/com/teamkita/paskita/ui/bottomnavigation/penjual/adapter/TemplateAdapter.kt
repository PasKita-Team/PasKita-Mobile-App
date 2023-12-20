package com.teamkita.paskita.ui.bottomnavigation.penjual.adapter

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.teamkita.paskita.data.HasilGenerate
import com.teamkita.paskita.databinding.ListItemGenerateBinding
import com.teamkita.paskita.ui.bottomnavigation.penjual.TambahProduk
import com.teamkita.paskita.ui.bottomnavigation.penjual.TambahProdukViewModel

class TemplateAdapter(private val context: TambahProduk) : ListAdapter<HasilGenerate, TemplateAdapter.MyViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemGenerateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val review = getItem(position)
        holder.bind(review, context)
    }

    class MyViewHolder(private val binding: ListItemGenerateBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(generate: HasilGenerate, context: TambahProduk) {

            val viewModel = ViewModelProvider(context)[TambahProdukViewModel::class.java]

            Glide.with(itemView)
                .load(generate.image_url)
                .into(binding.ivTemplate)

            binding.cbFotoProduk.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    viewModel.url_produk.value = generate.image_url
                }
            }

            binding.btnDownload.setOnClickListener {
                val imageUrl = generate.image_url
                val fileName = "generate_gambar_${generate.user_id}.jpg"

                val downloadManager = itemView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(imageUrl))
                    .setTitle("Download Gambar")
                    .setDescription("Deskripsi Download")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

                Toast.makeText(itemView.context, "Gambar Sedang Di Download", Toast.LENGTH_SHORT).show()
                downloadManager.enqueue(request)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HasilGenerate>() {
            override fun areItemsTheSame(oldItem: HasilGenerate, newItem: HasilGenerate): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: HasilGenerate, newItem: HasilGenerate): Boolean {
                return oldItem == newItem
            }
        }
    }
}