package com.teamkita.paskita.ui.bottomnavigation.user.notifikasi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamkita.paskita.data.Notifikasi
import com.teamkita.paskita.databinding.ActivityNotifikasiBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.NotifikasiAdapter

class NotifikasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotifikasiBinding
    private lateinit var viewModel: NotifikasiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifikasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NotifikasiViewModel::class.java]

        binding.rvNotifikasi.layoutManager = LinearLayoutManager(this)
        viewModel.fetchNotikasiList()

        viewModel.getNotifikasiListLiveData().observe(this) { listNotif ->
            setDataNotif(listNotif)
        }

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }
    }

    private fun setDataNotif(notif: List<Notifikasi>) {
        val notifAdapter = NotifikasiAdapter()
        notifAdapter.submitList(notif)
        binding.rvNotifikasi.adapter = notifAdapter
    }
}