package com.teamkita.paskita.ui.bottomnavigation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.teamkita.paskita.R
import com.teamkita.paskita.databinding.ActivityBottomNavigationBinding


class BottomNavigation : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.meowBottom.add(MeowBottomNavigation.Model(1, R.drawable.ic_home_black_24dp))
        binding.meowBottom.add(MeowBottomNavigation.Model(2, R.drawable.ic_dashboard_black_24dp))
        binding.meowBottom.add(MeowBottomNavigation.Model(3, R.drawable.baseline_shopping_cart_24))
        binding.meowBottom.add(MeowBottomNavigation.Model(4, R.drawable.baseline_monetization_on_24))
        binding.meowBottom.add(MeowBottomNavigation.Model(5, R.drawable.baseline_account_circle_24))

        binding.meowBottom.setOnShowListener { item ->
            var fragment: Fragment? = null
            when (item.id) {
                1 -> fragment = HomeFragment()
                2 -> fragment = KategoriFragment()
                3 -> fragment = KeranjangFragment()
                4 -> fragment = TransaksiFragment()
                5 -> fragment = ProfileFragment()
            }
            if (fragment != null) {
                loadFragment(fragment)
            }
        }

        binding.meowBottom.show(1, true)
        binding.meowBottom.setOnClickMenuListener { item ->

        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

}