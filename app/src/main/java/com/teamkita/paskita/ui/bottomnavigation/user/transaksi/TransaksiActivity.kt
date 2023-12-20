package com.teamkita.paskita.ui.bottomnavigation.user.transaksi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.teamkita.paskita.R
import com.teamkita.paskita.data.Produk
import com.teamkita.paskita.data.model.CostPostageFee
import com.teamkita.paskita.data.model.ResultData
import com.teamkita.paskita.data.model.city.CityResult
import com.teamkita.paskita.data.model.cost.CostRajaOngkir
import com.teamkita.paskita.databinding.ActivityTransaksiBinding
import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.util.localID
import me.abhinay.input.CurrencySymbols

class TransaksiActivity : AppCompatActivity(){

    private lateinit var binding: ActivityTransaksiBinding
    private lateinit var listCity: List<CityResult?>

    private val viewModel: TransaksiViewModel by lazy {
        ViewModelProvider(this).get(TransaksiViewModel::class.java)
    }

    private val cekOngkirAdapter: CekOngkirAdapter by lazy { CekOngkirAdapter(this) }
    val listPostageFee = arrayListOf<CostPostageFee>()
    private var courierName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        viewModel.currentNumber.observe(this, Observer {
            binding.tvJumlah.text = it.toString()
        })

        incrementButton()
        decrementButton()

        setupAction()
        setupData()

        loadCities()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            val intent = Intent(this, BottomNavigation::class.java)
            startActivity(intent)
        }

        binding.btnBayar.setOnClickListener {
            val alamat_lengkap = binding.etALamatLengkap.text.toString()
            val kota = binding.destinationAutoCompleteTV.text.toString()
            val provinsi = binding.etProvinsi.text.toString()
            val kurir = binding.courierAutoCompleteTV.text.toString()
            val produk = intent.getParcelableExtra<Produk>("produk") as Produk
            val totalBayar = binding.tvTotalHarga.text.toString()
            val jumlah = binding.tvJumlah.text.toString()
            val catatanProduk = binding.etCatataProduk.text.toString()
            val intent = Intent(this, PembayaranActivity::class.java)
            intent.putExtra("totalBayar", totalBayar)
            intent.putExtra("jumlah", jumlah)
            intent.putExtra("produk", produk)
            intent.putExtra("alamat_lengkap", alamat_lengkap)
            intent.putExtra("kota", kota)
            intent.putExtra("provinsi", provinsi)
            intent.putExtra("kurir", kurir)
            intent.putExtra("catatanProduk", catatanProduk)
            startActivity(intent)
        }

    }

    private fun calculateTotal(harga: Double) {
        val number = viewModel.currentNumber.value ?: 1
        val numberAsDouble = number.toDouble()
        val totalHarga = numberAsDouble * (harga * 100)

        viewModel.getSelectedValue().observe(this) { ongkirValue ->
            val ongkirRegex = Regex("\\d+(\\.\\d+)?")
            val matchResultOngkir = ongkirRegex.find(ongkirValue ?: "")
            val hargaOngkir = matchResultOngkir?.value?.toDoubleOrNull() ?: 0.0

            val totalHargaOngkir = totalHarga + (hargaOngkir * 100)
            binding.tvTotalHarga.setCurrency(CurrencySymbols.INDONESIA)
            binding.tvTotalHarga.setDecimals(false)
            binding.tvTotalHarga.setSeparator(".")
            binding.tvTotalHarga.setText(totalHargaOngkir.toString())
        }
    }

    private fun loadCities() {
        val listCity = viewModel.getCities()
        listCity.observe(this) {
            when (it) {
                is ResultData.Success -> {
                    showLoading(false)
                    initSpinner(it.data?.rajaOngkir?.results ?: return@observe)
                }
                is ResultData.Loading -> showLoading(true)
                is ResultData.Failed -> showErrorMessage(it.message.toString())
                is ResultData.Exception -> showErrorMessage(it.message.toString())
            }
        }
    }

    private fun initSpinner(cityResults: List<CityResult?>) {
        listCity = cityResults
        val cities = mutableListOf<String>()
        val couriers = resources.getStringArray(R.array.list_courier)
        for (i in cityResults.indices) cities.add(cityResults[i]?.cityName ?: "")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cities)
        val courierAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, couriers)

        binding.destinationAutoCompleteTV.setAdapter(cityAdapter)
        binding.courierAutoCompleteTV.setAdapter(courierAdapter)
        initClick()
    }

    private fun initClick() {
        binding.btnCekOngkir.setOnClickListener { checkedPostageFee() }
    }

    private fun checkedPostageFee() {
        val alamatToko= intent.getStringExtra("alamat_toko")
        val produk = intent.getParcelableExtra<Produk>("produk") as Produk
        val strOriginCity = alamatToko
        val strDestinationCity = binding.destinationAutoCompleteTV.text.toString()
        val strCourier = binding.courierAutoCompleteTV.text.toString()
        val strWeight = produk.berat_produk

        if (strDestinationCity.isNotEmpty() && strCourier.isNotEmpty()) {
            val originCity = checkedForm(strOriginCity.toString())
            val destinationCity = checkedForm(strDestinationCity)
            val courier = strCourier.lowercase(localID())
            val weight = strWeight?.toInt()
            val idOriginCity = originCity?.cityId ?: ""
            val idDestinationCity = destinationCity?.cityId ?: ""

            if (weight != null) {
                checkedCost(idOriginCity, idDestinationCity, weight, courier)
            }
        } else {
            Toast.makeText(this, "Ada data yang masih kosong!!",Toast.LENGTH_SHORT).show()
        }

        initView(strOriginCity, strDestinationCity, courierName)
    }

    private fun checkedForm(selectedCity: String): CityResult? {
        var city: CityResult? = null
        val dataCity = listCity.filter {
            it?.cityName?.contains(selectedCity) ?: false
        }

        for (i in dataCity.indices) {
            val matchedCity = dataCity[i]?.cityName ?: ""
            if (selectedCity == matchedCity) city = dataCity[i]
        }
        return city
    }

    private fun checkedCost(origin: String, destination: String, weight: Int, courier: String) {
        val costData = viewModel.getCost(origin, destination, weight, courier)
        costData.observe(this) {
            when (it) {
                is ResultData.Success -> {
                    showLoading(false)
                    setupAdapter(it.data?.rajaOngkir)
                }
                is ResultData.Loading -> showLoading(true)
                is ResultData.Failed -> showErrorMessage(it.message.toString())
                is ResultData.Exception -> showErrorMessage(it.message.toString())
            }
        }
    }

    private fun setupAdapter(rajaOngkir: CostRajaOngkir?) {
        listPostageFee.clear()
        for (i in rajaOngkir?.results?.indices ?: return) {
            val costs = rajaOngkir.results[i].costs
            for (j in costs?.indices ?: return) {
                val cost = rajaOngkir.results[i].costs?.get(j)?.cost
                for (k in cost?.indices ?: return) {
                    val code = rajaOngkir.results[i].code
                    val name = rajaOngkir.results[i].name
                    val service = costs[j].service
                    val description = costs[j].description
                    val value = cost[k].value
                    val etd = cost[k].etd
                    listPostageFee.add(CostPostageFee(code, name, service, description, etd, value))
                }
            }
        }

        setupCekOngkirAdapter(listPostageFee)

    }

    private fun setupCekOngkirAdapter(listPostageFee: ArrayList<CostPostageFee>) {
        cekOngkirAdapter.submitList(listPostageFee)
        binding.rvCekOngkir.apply {
            layoutManager = LinearLayoutManager(this@TransaksiActivity)
            setHasFixedSize(true)
            adapter = cekOngkirAdapter
        }
    }

    private fun initView(originCity: String?, destinationCity: String?, courierName: String?) {
        val strTransportationLine = "$originCity - $destinationCity"
        binding.transportationLineTV.text = strTransportationLine
    }

    private fun showErrorMessage(message: String) {
        showLoading(false)
        Toast.makeText(this, message.toString(),Toast.LENGTH_SHORT).show()
    }


    private fun incrementButton(){
        binding.ivTambah.setOnClickListener {
            viewModel.currentNumber.value = ++viewModel.number
        }
    }

    private fun decrementButton(){
        binding.ivKurang.setOnClickListener {
            if (viewModel.number > 1){
                viewModel.currentNumber.value = --viewModel.number
            }
        }
    }

    private fun setupData() {
        val namaToko= intent.getStringExtra("nama_toko")
        val namaProduk= intent.getStringExtra("nama_produk")
        val hargaProduk= intent.getStringExtra("harga")
        val totalHargaProduk= intent.getStringExtra("total_harga")
        val jumlah= intent.getStringExtra("jumlah")
        val urlFotoProduk= intent.getStringExtra("url_foto_produk")

        val jumlahToInt = jumlah?.toIntOrNull() ?: 0
        viewModel.number = jumlahToInt

        val hargaProdukString = hargaProduk
        val hargaRegex = Regex("\\d+(\\.\\d+)?")
        val matchResult = hargaRegex.find(hargaProdukString ?: "")

        val totalhargaProdukString = totalHargaProduk
        val totalhargaRegex = Regex("\\d+(\\.\\d+)?")
        val totalmatchResult = totalhargaRegex.find(totalhargaProdukString ?: "")

        val harga = matchResult?.value?.toDoubleOrNull() ?: 0.0
        val totalharga = totalmatchResult?.value?.toDoubleOrNull() ?: 0.0
        calculateTotal(totalharga)

        binding.tvHargaProduk.text = hargaProdukString
        binding.tvTotalHarga.setText(totalHargaProduk)

        viewModel.currentNumber.observe(this, Observer { number ->
            val numberAsDouble = number.toDouble()
            val totalHarga = numberAsDouble * (harga * 100)

            binding.tvTotalHarga.setCurrency(CurrencySymbols.INDONESIA)
            binding.tvTotalHarga.setDecimals(false)
            binding.tvTotalHarga.setSeparator(".")
            binding.tvTotalHarga.setText(totalHarga.toString())

            viewModel.getSelectedValue().observe(this) { ongkirValue ->
                val ongkirRegex = Regex("\\d+(\\.\\d+)?")
                val matchResultOngkir = ongkirRegex.find(ongkirValue ?: "")
                val hargaOngkir = matchResultOngkir?.value?.toDoubleOrNull() ?: 0.0

                val totalHargaOngkir = totalHarga + (hargaOngkir * 100)
                binding.tvTotalHarga.setCurrency(CurrencySymbols.INDONESIA)
                binding.tvTotalHarga.setDecimals(false)
                binding.tvTotalHarga.setSeparator(".")
                binding.tvTotalHarga.setText(totalHargaOngkir.toString())
            }

        })
        binding.tvNamaToko.text = namaToko
        binding.tvNamaProduk.text = namaProduk
        binding.tvJumlah.text = jumlah
        Glide.with(this)
            .load(urlFotoProduk)
            .into(binding.ivImage)
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }
}