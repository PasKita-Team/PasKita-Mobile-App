package com.teamkita.paskita.ui.bottomnavigation.user.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamkita.paskita.R
import com.teamkita.paskita.databinding.FragmentProfileBinding

import com.teamkita.paskita.ui.bottomnavigation.BottomNavigation
import com.teamkita.paskita.ui.bottomnavigation.penjual.DaftarPenjual
import com.teamkita.paskita.ui.bottomnavigation.penjual.DashboardPenjual
import com.teamkita.paskita.ui.bottomnavigation.user.favorite.FavoriteProdukActivity
import com.teamkita.paskita.ui.bottomnavigation.user.keranjang.KeranjangActivity
import com.teamkita.paskita.ui.bottomnavigation.user.notifikasi.NotifikasiActivity
import com.teamkita.paskita.ui.bottomnavigation.user.pesan.DaftarPesanActivity
import com.teamkita.paskita.ui.logindanregister.login.LoginActivity
import com.teamkita.paskita.ui.logindanregister.register.RegisterActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = Firebase.auth

        setupAction()

    }

    private fun setupAction() {
        binding.ivPesan.setOnClickListener {
            val intent = Intent(requireContext(), DaftarPesanActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.ivNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifikasiActivity::class.java)
            startActivity(intent)

            activity?.overridePendingTransition(androidx.transition.R.anim.abc_fade_in,androidx.transition.R.anim.abc_fade_out)
        }

        binding.tvKeluar.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, BottomNavigation::class.java))
            requireActivity().finish()
        }

        binding.btnMasuk.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
            requireActivity().finish()
        }

        binding.btnDaftar.setOnClickListener {
            startActivity(Intent(activity, RegisterActivity::class.java))
            requireActivity().finish()
        }

        binding.signinGoogle.setOnClickListener {
            showLoading(true)
            signGoogle()
        }

        binding.tvInfoPribadi.setOnClickListener {
            val informasiPribadi = InformasiPribadi.newInstance()
            informasiPribadi.show(requireFragmentManager(), InformasiPribadi.TAG)
        }

        binding.tvFavorite.setOnClickListener {
            startActivity(Intent(activity, FavoriteProdukActivity::class.java))
        }

        binding.tvBantuan.setOnClickListener {
            val bantuan = Bantuan.newInstance()
            bantuan.show(requireFragmentManager(), Bantuan.TAG)
        }

        binding.tvPengaturan.setOnClickListener {
            val pengaturan = Pengaturan.newInstance()
            pengaturan.show(requireFragmentManager(), Pengaturan.TAG)
        }
        binding.tvTentang.setOnClickListener {
            val tentang = TentangKami.newInstance()
            tentang.show(requireFragmentManager(), TentangKami.TAG)
        }

        binding.btnDaftarPenjual.setOnClickListener {
            val daftarPenjual = DaftarPenjual.newInstance()
            daftarPenjual.show(requireFragmentManager(), DaftarPenjual.TAG)
        }

        binding.btnDashboardPenjual.setOnClickListener {
            startActivity(Intent(activity, DashboardPenjual::class.java))
        }

        binding.ivKeranjang.setOnClickListener {
            val intent = Intent(requireContext(), KeranjangActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateProfileData() {
        showLoading(true)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.cvSelamatDatang.visibility = View.GONE
            val db = FirebaseFirestore.getInstance()
            val userDocument = db.collection("users").document(currentUser.uid)

            userDocument.addSnapshotListener { document, _ ->
                if (document != null && document.exists()) {
                    // Ambil data dari dokumen Firestore
                    val email = document.getString("email")
                    val nama = document.getString("nama")
                    val urlProfile = document.getString("url_profile")
                    val sebagai = document.getString("as")

                    if (sebagai.equals("penjual")) {
                        binding.btnDashboardPenjual.visibility = View.VISIBLE
                        binding.btnDaftarPenjual.visibility = View.GONE
                    }else if (sebagai.equals("penjual premium")) {
                        binding.btnDashboardPenjual.visibility = View.VISIBLE
                        binding.btnDaftarPenjual.visibility = View.GONE
                    } else {
                        binding.btnDaftarPenjual.visibility = View.VISIBLE
                        binding.btnDashboardPenjual.visibility = View.GONE
                    }

                    binding.tvEmailUser.text = email
                    binding.tvNamaUser.text = nama

                    Glide.with(requireContext())
                        .load(urlProfile)
                        .placeholder(R.drawable.tulisan_paskita)
                        .error(R.drawable.baseline_error_24)
                        .into(binding.ivPhotoUser)

                    showLoading(false)
                }
            }
        } else {
            binding.cvSelamatDatang.visibility = View.VISIBLE
            binding.tvKeluar.isEnabled = false
            binding.ivPesan.isEnabled = false
            binding.ivNotif.isEnabled = false
            binding.ivPhotoUser.isEnabled = false

            showLoading(false)
        }
    }

    private fun signGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("Profile Fragment", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("Profile Fragment", "Google sign in failed", e)
            }
        }else{
            showLoading(false)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    showLoading(false)
                    startActivity(Intent(requireActivity(), BottomNavigation::class.java))
                    requireActivity().finish()
                    Toast.makeText(requireContext(), "Login Berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    showLoading(false)
                    Log.w("Profile Fragment", "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()
        updateProfileData()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}