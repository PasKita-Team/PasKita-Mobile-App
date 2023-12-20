package com.teamkita.paskita.ui.bottomnavigation.user.keranjang

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.arlib.floatingsearchview.FloatingSearchView
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.databinding.ActivityKeranjangBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.user.adapter.KeranjangProdukAdapter
import com.teamkita.paskita.ui.bottomnavigation.user.favorite.FavoriteProdukActivity

class KeranjangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKeranjangBinding
    private lateinit var viewModel: KeranjangViewModel

    protected val RESULT_SPEECH = 1
    private val ID_BahasaIndonesia = "id"
    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeranjangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[KeranjangViewModel::class.java]

        binding.rvKeranjang.layoutManager = LinearLayoutManager(this)
        viewModel.fetchProdukList()

        viewModel.getProdukListLiveData().observe(this) { listProduk ->
            setDataProduk(listProduk)
        }

        setupAction()
        setupSearchBar()

    }

    private fun setupSearchBar() {
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

    private fun setupAction() {

        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
            finish()
        }

        binding.searchBar.setOnQueryChangeListener(FloatingSearchView.OnQueryChangeListener { oldQuery, newQuery ->

            if (newQuery.isNullOrEmpty()) {
                viewModel.getProdukListLiveData().observe(this) { listProduk ->
                    setDataProduk(listProduk)
                }
            } else {
                viewModel.searchProduk(newQuery).observe(this) { listProduk ->
                    setDataProduk(listProduk)
                }
            }

        })
    }

    private fun setDataProduk(produk: List<Produk>) {
        val produkAdapter = KeranjangProdukAdapter(this)
        produkAdapter.submitList(produk)
        binding.rvKeranjang.adapter = produkAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RESULT_SPEECH -> if (resultCode == Activity.RESULT_OK && data != null) {
                val text: ArrayList<String>? = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.get(0)?.let { firstResult ->
                    binding.searchBar.setSearchText(firstResult)
                    viewModel.searchProduk(firstResult).observe(this) { listProduk ->
                        setDataProduk(listProduk)
                    }
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
}