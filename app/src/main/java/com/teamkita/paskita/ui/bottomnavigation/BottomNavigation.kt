package com.teamkita.paskita.ui.bottomnavigation

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.teamkita.paskita.R
import com.teamkita.paskita.databinding.ActivityBottomNavigationBinding
import com.teamkita.paskita.preferences.SettingPreferences
import com.teamkita.paskita.preferences.dataStore
import com.teamkita.paskita.ui.bottomnavigation.user.cekproduk.CekProdukFragment
import com.teamkita.paskita.ui.bottomnavigation.user.home.HomeFragment
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.profile.ProfileFragment
import com.teamkita.paskita.ui.bottomnavigation.user.daftartransaksi.TransaksiFragment

class BottomNavigation : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()

    }

    private fun setupAction() {
        binding.meowBottom.add(MeowBottomNavigation.Model(1, R.drawable.ic_home_black_24dp))
        binding.meowBottom.add(MeowBottomNavigation.Model(2, R.drawable.baseline_content_paste_search_24))
        binding.meowBottom.add(MeowBottomNavigation.Model(3, R.drawable.baseline_monetization_on_24))
        binding.meowBottom.add(MeowBottomNavigation.Model(4, R.drawable.baseline_account_circle_24))

        binding.meowBottom.setOnShowListener { item ->
            var fragment: Fragment? = null
            when (item.id) {
                1 -> fragment = HomeFragment()
                2 -> fragment = CekProdukFragment()
                3 -> fragment = TransaksiFragment()
                4 -> fragment = ProfileFragment()
            }
            if (fragment != null) {
                loadFragment(fragment)
            }
        }
        binding.meowBottom.show(1, true)
        binding.meowBottom.setOnClickMenuListener {

        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

}