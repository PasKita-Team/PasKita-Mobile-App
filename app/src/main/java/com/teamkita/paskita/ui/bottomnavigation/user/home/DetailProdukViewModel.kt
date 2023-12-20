package com.teamkita.paskita.ui.bottomnavigation.user.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetailProdukViewModel : ViewModel() {

    var number = 1

    val currentNumber: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

}