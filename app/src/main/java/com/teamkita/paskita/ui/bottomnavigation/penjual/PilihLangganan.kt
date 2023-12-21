package com.teamkita.paskita.ui.bottomnavigation.penjual

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.teamkita.paskita.databinding.ActivityPilihLanggananBinding
import com.teamkita.paskita.ui.bottomnavigation.OnDialogCloseListener
import com.teamkita.paskita.ui.bottomnavigation.user.transaksi.PembayaranActivity

class PilihLangganan : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "PilihLangganan"

        fun newInstance(): PilihLangganan {
            return PilihLangganan()
        }
    }

    private lateinit var binding: ActivityPilihLanggananBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityPilihLanggananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
    }

    private fun setupAction() {
        binding.ivBack.setOnClickListener {
            dismiss()
        }

        binding.btn1.setOnClickListener {
            val harga = "Rp26.900"
            val paket = "1 Bulan"
            val intent = Intent(requireActivity(), PembayaranLanggananActivity::class.java)
            intent.putExtra("harga", harga)
            intent.putExtra("paket", paket)
            startActivity(intent)
            requireActivity().finish()
            dismiss()
        }

        binding.btn2.setOnClickListener {
            val harga = "Rp63.900"
            val paket = "3 Bulan"
            val intent = Intent(requireActivity(), PembayaranLanggananActivity::class.java)
            intent.putExtra("harga", harga)
            intent.putExtra("paket", paket)
            startActivity(intent)
            requireActivity().finish()
            dismiss()
        }

        binding.btn3.setOnClickListener {
            val harga = "Rp111.900"
            val paket = "6 Bulan"
            val intent = Intent(requireActivity(), PembayaranLanggananActivity::class.java)
            intent.putExtra("harga", harga)
            intent.putExtra("paket", paket)
            startActivity(intent)
            requireActivity().finish()
            dismiss()
        }

        binding.btn4.setOnClickListener {
            val harga = "Rp199.900"
            val paket = "1 Tahun"
            val intent = Intent(requireActivity(), PembayaranLanggananActivity::class.java)
            intent.putExtra("harga", harga)
            intent.putExtra("paket", paket)
            startActivity(intent)
            requireActivity().finish()
            dismiss()
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val activity = requireActivity()
        if (activity is OnDialogCloseListener) {
            (activity as OnDialogCloseListener).onDialogClose(dialog)
        }
    }
}