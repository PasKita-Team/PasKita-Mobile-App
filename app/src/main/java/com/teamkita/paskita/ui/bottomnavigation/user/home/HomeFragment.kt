package com.teamkita.paskita.ui.bottomnavigation.user.home

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.FragmentHomeBinding
import com.teamkita.paskita.preferences.SettingPreferences
import com.teamkita.paskita.preferences.dataStore
import com.teamkita.paskita.ui.bottomnavigation.ThemeViewModelSetting
import com.teamkita.paskita.ui.bottomnavigation.ViewModelFactoryThemeSetting
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.ProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.favorite.FavoriteProdukActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.DaftarPesanActivity
import com.teamkita.paskita.ui.bottomnavigation.user.produk.ProdukActivity


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private lateinit var listProduk: ArrayList<Produk>
    private var db = Firebase.firestore

    protected val RESULT_SPEECH = 1
    private val ID_BahasaIndonesia = "id"
    private val PERMISSION_REQUEST_CODE = 123

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            ivPesan.setOnClickListener {
                val intent = Intent(requireContext(), DaftarPesanActivity::class.java)
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            ivNotif.setOnClickListener {
                val intent = Intent(requireContext(), NotifikasiActivity::class.java)
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            cvMakanan.setOnClickListener {
                val intent = Intent(requireContext(), ProdukActivity::class.java)
                intent.putExtra("kategori", "Makanan")
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            cvMinuman.setOnClickListener {
                val intent = Intent(requireContext(), ProdukActivity::class.java)
                intent.putExtra("kategori", "Minuman")
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            cvKerajinan.setOnClickListener {
                val intent = Intent(requireContext(), ProdukActivity::class.java)
                intent.putExtra("kategori", "Kerajinan")
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            cvSambal.setOnClickListener {
                val intent = Intent(requireContext(), ProdukActivity::class.java)
                intent.putExtra("kategori", "Sambal")
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }

            tvPopuler.setOnClickListener {
                val intent = Intent(requireContext(), ProdukActivity::class.java)
                intent.putExtra("kategori", "Semua Kategori")
                startActivity(intent)

                activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
            }



            ivKeranjang.setOnClickListener {
                val intent = Intent(requireContext(), KeranjangActivity::class.java)
                startActivity(intent)
            }

            val initialHeight = resources.getDimensionPixelSize(R.dimen.initial_height)
            searchBar.setOnQueryChangeListener { _, newQuery ->
                // Deteksi saat pencarian dimulai atau dihentikan
                val isSearching = newQuery.isNotEmpty()

                val newHeight = if (isSearching) {
                    // Jika pencarian dimulai, atur tinggi yang berbeda
                    resources.getDimensionPixelSize(R.dimen.expanded_height)
                } else {
                    // Jika pencarian dihentikan, kembalikan ke tinggi awal
                    initialHeight
                }

                // Atur ulang layout_height secara programatik
                val layoutParams = searchBar.layoutParams
                layoutParams.height = newHeight
                searchBar.layoutParams = layoutParams
            }

            searchBar.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
                override fun onSuggestionClicked(searchSuggestion: SearchSuggestion?) {

                }

                override fun onSearchAction(currentQuery: String?) {
                    val cari = currentQuery.toString()

                    val intent = Intent(requireContext(), ProdukActivity::class.java)
                    intent.putExtra("cari", cari)
                    startActivity(intent)

                    activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
                }
            })

            searchBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.fav -> {
                        val intent = Intent(requireContext(), FavoriteProdukActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.mic -> {
                        val permission = ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.RECORD_AUDIO
                        )

                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.RECORD_AUDIO),
                                PERMISSION_REQUEST_CODE
                            )
                        } else {
                            val mic_google = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            mic_google.putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            mic_google.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ID_BahasaIndonesia)
                            try {
                                startActivityForResult(
                                    mic_google,
                                    RESULT_SPEECH
                                )
                                binding.searchBar.setSearchText("")
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    "Maaf, Perangkat Anda Tidak Mendukung Speech To Text",
                                    Toast.LENGTH_SHORT
                                ).show()
                                e.printStackTrace()
                            }
                        }
                    }

                }

            }

            rvHomeProduk.layoutManager = GridLayoutManager(requireActivity(), 2)
            listProduk = arrayListOf()
            db = FirebaseFirestore.getInstance()
            db.collection("produk").get()
                .addOnSuccessListener {
                    if (!it.isEmpty){
                        for (data in it.documents){
                            val produk: Produk? = data.toObject(Produk::class.java)
                            if (produk != null) {
                                listProduk.add(produk)
                            }
                        }
                        rvHomeProduk.adapter = ProdukAdapter(listProduk)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.get(0)?.let { firstResult ->
                    binding.searchBar.setSearchText(firstResult)

                    val intent = Intent(requireContext(), ProdukActivity::class.java)
                    intent.putExtra("cari", firstResult)
                    startActivity(intent)

                    activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    context,
                    "Izin telah diberikan, Anda dapat menggunakan mikrofon",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(context, "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val pref = SettingPreferences.getInstance(requireContext().applicationContext.dataStore)
        val mainViewModel = ViewModelProvider(requireActivity(), ViewModelFactoryThemeSetting(pref))[ThemeViewModelSetting::class.java]

        mainViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive: Boolean ->
            if (isDarkModeActive) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.switchTheme.isChecked = true
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.switchTheme.isChecked = false
            }
        }

        binding.switchTheme.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            mainViewModel.saveThemeSetting(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}