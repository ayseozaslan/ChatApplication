package com.example.chatays.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatays.R
import com.example.chatays.databinding.FragmentGoogleLoginBinding
import com.example.chatays.viewModel.AuthViewModel
import com.example.chatays.viewModel.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleLoginFragment : Fragment() {

    private lateinit var binding: FragmentGoogleLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()
    private val requestPermissionLauncher=
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                Toast.makeText(requireContext(),"Rehber izni verildi",Toast.LENGTH_SHORT).show()
            }
        }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("GoogleLogin", "resultCode: ${result.resultCode}, data: ${result.data}")

            if (result.resultCode == Activity.RESULT_OK) {
                // GoogleSignInAccount alınır
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.let {
                        googleSignIn(it) // Firebase giriş ve Firestore kaydı burada yapılacak
                    }
                } catch (e: ApiException) {
                    Log.e("GoogleLogin", "Google Sign-In Hatası: ${e.statusCode}")
                    Toast.makeText(requireContext(), "Google Sign-In Hatası", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Giriş iptal edildi", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_google_login, container, false)

        checkContactsPermission()
        binding.googleloginfragment = this

        binding.loginGoogle.setOnClickListener {
            Log.d("GoogleLogin", "Buton tıklandı")
            val signInIntent = viewModel.getSignInIntent()
            signInLauncher.launch(signInIntent)
        }

        observeViewModel()

        return binding.root
    }
    private fun checkContactsPermission(){
        if(ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)

        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), "Hata: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // GoogleSignInAccount ile Firebase giriş ve Firestore kaydı
    private fun googleSignIn(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = Firebase.auth.currentUser
                    firebaseUser?.let {
                        // Firestore kaydı
                        signUpViewModel.saveGoogleUser(it)
                        val username = it.displayName ?: ""
                        val email = it.email ?: ""
                        val imageUrl = it.photoUrl?.toString() ?: ""
                        val action = GoogleLoginFragmentDirections.googleloginToSettings(
                            imageUrl, username, email
                        )
                        findNavController().navigate(action)
                    }
                } else {
                    Toast.makeText(requireContext(), "Firebase Auth Başarısız", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun navigateToLogin(){
        findNavController().navigate(R.id.google_to_login)
    }

}
