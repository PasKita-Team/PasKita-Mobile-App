package com.teamkita.paskita.ui.bottomnavigation.user.produk

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView.OnQueryChangeListener
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityProdukBinding
import com.teamkita.paskita.preferences.SettingPreferences
import com.teamkita.paskita.preferences.dataStore
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.ThemeViewModelSetting
import com.teamkita.paskita.ui.bottomnavigation.ViewModelFactoryThemeSetting
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.ProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.favorite.FavoriteProdukActivity
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.DaftarPesanActivity


class ProdukActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProdukBinding
    private lateinit var listProduk: ArrayList<Produk>
    private val db = Firebase.firestore

    protected val RESULT_SPEECH = 1
    private val ID_BahasaIndonesia = "id"
    private val PERMISSION_REQUEST_CODE = 123

    var selectedCategory: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvProduk.layoutManager = GridLayoutManager(this, 2)
        listProduk = arrayListOf()

        setupSearchBar()
        setupAction()
    }

    private fun setupAction() {

        val kategori = arrayOf("Semua Kategori", "Makanan", "Minuman", "Kerajinan", "Sambal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategori)
        binding.spKategori.adapter = adapter

        val kategoriPilih = intent.getStringExtra("kategori")
        val kategoriIndex = kategori.indexOf(kategoriPilih)
        binding.spKategori.setSelection(kategoriIndex)

        binding.spKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = kategori[position]
                val searchQuery = binding.searchBar.query

                // Perform search based on both search query and selected category
                if (searchQuery.isNullOrEmpty()){
                    tampilProdukByKategori(selectedCategory)
                }else{
                    searchProduk(searchQuery, selectedCategory)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        binding.ivPesan.setOnClickListener {
            val intent = Intent(this, DaftarPesanActivity::class.java)
            startActivity(intent)

            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(this, NotifikasiActivity::class.java)
            startActivity(intent)

            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }
    }
    private fun setupSearchBar() {
        val searchQuery = intent.getStringExtra("cari")
        binding.searchBar.setSearchText(searchQuery)
        if (searchQuery.equals("")){
            tampilProduk()
        }else{
            searchProduk(searchQuery, selectedCategory)
        }

        binding.searchBar.setOnQueryChangeListener(OnQueryChangeListener { oldQuery, newQuery ->

            if (newQuery.isNullOrEmpty()){
                tampilProdukByKategori(selectedCategory)
            }else{
                searchProduk(newQuery, selectedCategory)
            }

        })

        binding.searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fav -> {
                    val intent = Intent(this, FavoriteProdukActivity::class.java)
                    startActivity(intent)
                }
                R.id.mic -> {
                    val permission = ContextCompat.checkSelfPermission(
                        applicationContext!!,
                        Manifest.permission.RECORD_AUDIO
                    )

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
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
                                applicationContext,
                                "Maaf, Perangkat Anda Tidak Mendukung Speech To Text",
                                Toast.LENGTH_SHORT
                            ).show()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

    }

    private fun tampilProduk() {
        listProduk.clear()
        val produkCollection = db.collection("produk")

        produkCollection.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (data in snapshot.documents) {
                        val produk: Produk? = data.toObject(Produk::class.java)
                        produk?.let { listProduk.add(it) }
                    }
                    binding.rvProduk.adapter = ProdukAdapter(listProduk)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilProdukByKategori(category: String?) {
        listProduk.clear()
        val produkCollection = db.collection("produk")
        val query = produkCollection.whereEqualTo("kategori_produk", category)

        query.get()
            .addOnSuccessListener { snapshot ->
                if (category.equals("Semua Kategori")){
                    tampilProduk()
                }else{
                    if (!snapshot.isEmpty) {
                        for (data in snapshot.documents) {
                            val produk: Produk? = data.toObject(Produk::class.java)
                            if (produk != null){
                                produk.let { listProduk.add(it) }
                            }

                        }
                        binding.rvProduk.adapter = ProdukAdapter(listProduk)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchProduk(searchQuery: String?, category: String?) {
        searchQuery?.let { query ->
            listProduk.clear()
            val produkCollection = db.collection("produk")

            produkCollection.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        for (data in snapshot.documents) {
                            val produk: Produk? = data.toObject(Produk::class.java)
                            val namaProduk = produk?.nama_produk
                            val kategoriProduk = produk?.kategori_produk

                            // Check if the product matches the search query and category
                            if (!namaProduk.isNullOrBlank() && query.isNotEmpty() &&
                                namaProduk!!.contains(query, ignoreCase = true) &&
                                (category == "Semua Kategori" || kategoriProduk == category)
                            ) {
                                produk?.let { listProduk.add(it) }
                            }
                        }
                        binding.rvProduk.adapter = ProdukAdapter(listProduk)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(applicationContext, exception.toString(), Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            // Handle null searchQuery here if needed
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.get(0)?.let { firstResult ->
                    binding.searchBar.setSearchText(firstResult)
                    searchProduk(firstResult, selectedCategory)
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
                    applicationContext,
                    "Izin telah diberikan, Anda dapat menggunakan mikrofon",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(applicationContext, "Izin mikrofon ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val pref = SettingPreferences.getInstance(applicationContext.dataStore)
        val mainViewModel = ViewModelProvider(this, ViewModelFactoryThemeSetting(pref))[ThemeViewModelSetting::class.java]

        mainViewModel.getThemeSettings().observe(this) { isDarkModeActive: Boolean ->
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

}