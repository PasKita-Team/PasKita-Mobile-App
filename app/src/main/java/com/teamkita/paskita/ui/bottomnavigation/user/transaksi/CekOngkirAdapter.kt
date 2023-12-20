package com.teamkita.paskita.ui.bottomnavigation.user.transaksi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.teamkita.paskita.data.model.CostPostageFee
import com.teamkita.paskita.data.model.DiffUtilCost
import com.teamkita.paskita.databinding.ItemCostBinding
import com.teamkita.paskita.util.toRupiah

class CekOngkirAdapter(val context: TransaksiActivity)
    : ListAdapter<CostPostageFee, CekOngkirAdapter.CostViewHolder>(DiffUtilCost()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostViewHolder {
        val view = ItemCostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CostViewHolder(view)
    }

    override fun onBindViewHolder(holder: CostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bindItem(item, context)
    }

    class CostViewHolder(private val binding: ItemCostBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(item: CostPostageFee, context: TransaksiActivity) {
            binding.apply {
                codeTV.text = item.code
                serviceTV.text = item.service
                serviceDescriptionTV.text = item.description
                costValueTV.text = item.value?.toRupiah()
                "${item.etd} Hari".also { estimationTV.text = it }

                cbOngkir.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val selectedValue = item.value?.toRupiah() ?: ""
                        val viewModel = ViewModelProvider(context)[TransaksiViewModel::class.java]
                        viewModel.setSelectedValue(selectedValue)
                    }else{
                        val selectedValue = "Rp0"
                        val viewModel = ViewModelProvider(context)[TransaksiViewModel::class.java]
                        viewModel.setSelectedValue(selectedValue)
                    }
                }
            }
        }
    }
}