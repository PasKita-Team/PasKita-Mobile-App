package com.teamkita.paskita.ui.bottomnavigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.teamkita.paskita.R

import com.teamkita.paskita.databinding.FragmentProfileBinding
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

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.cvSelamatDatang.visibility = View.GONE

            val userEmail = currentUser.email
            binding.tvEmailUser.text = userEmail

            val userName = userEmail?.substringBefore("@")
            binding.tvNamaUser.text = userName

        }else{
            binding.cvSelamatDatang.visibility = View.VISIBLE
            binding.tvKeluar.isEnabled = false
            binding.switchTheme.isEnabled = false
            binding.ivPesan.isEnabled = false
            binding.ivNotif.isEnabled = false
            binding.ivPhotoUser.isEnabled = false
        }

        binding.tvKeluar.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, BottomNavigation::class.java))
            requireActivity().finish()
        }

        binding.btnMasuk.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        binding.btnDaftar.setOnClickListener {
            startActivity(Intent(activity, RegisterActivity::class.java))
        }

        binding.signinGoogle.setOnClickListener {
            showLoading(true)
            signGoogle()
        }


        return binding.root
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
                    Log.d("Profile Fragment", "signInWithCredential:success")
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

    private fun showLoading(state: Boolean) { binding.progressBar.visibility = if (state) View.VISIBLE else View.GONE }

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