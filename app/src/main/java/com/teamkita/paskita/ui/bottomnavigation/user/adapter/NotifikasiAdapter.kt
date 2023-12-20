package com.teamkita.paskita.ui.bottomnavigation.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.data.Notifikasi
import com.teamkita.paskita.databinding.ListItemNotifikasiBinding

class NotifikasiAdapter : ListAdapter<Notifikasi, NotifikasiAdapter.MyViewHolder>(
    DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemNotifikasiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val review = getItem(position)
        holder.bind(review)
    }

    class MyViewHolder(private val binding: ListItemNotifikasiBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(notif: Notifikasi) {

            binding.tvNotif.text = notif.nama_notif
            binding.tvPesan.text = notif.pesan

        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notifikasi>() {
            override fun areItemsTheSame(oldItem: Notifikasi, newItem: Notifikasi): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Notifikasi, newItem: Notifikasi): Boolean {
                return oldItem == newItem
            }
        }
    }
}