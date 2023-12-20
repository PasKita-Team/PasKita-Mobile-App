package com.teamkita.paskita.ui.bottomnavigation.user.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamkita.paskita.data.UlasanProduk
import com.teamkita.paskita.databinding.ListItemRatingBinding

class RatingAdapter(private val listUlasan: ArrayList<UlasanProduk>) :
    RecyclerView.Adapter<RatingAdapter.MyViewHolder>() {
    inner class MyViewHolder(private val binding: ListItemRatingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ulasan: UlasanProduk) {

            binding.rating.isEnabled = false
            binding.rating.rating = ulasan.rating
            binding.tvNamaUser.text = ulasan.pembeli
            binding.tvUlasan.text = ulasan.ulasan

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ListItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listUlasan.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentProduk = listUlasan[position]
        holder.bind(currentProduk)
    }
}
