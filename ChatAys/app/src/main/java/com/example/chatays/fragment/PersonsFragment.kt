package com.example.chatays.fragment

import android.Manifest
import android.content.Context
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatays.R
import com.example.chatays.adapter.ContactsAdapter
import com.example.chatays.databinding.FragmentPersonsBinding
import com.example.chatays.model.entities.User
import com.example.chatays.utils.ContactsHelper
import com.example.chatays.viewModel.SignUpViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonsFragment : Fragment() {

    private  var _binding:FragmentPersonsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ContactsAdapter
    private val viewModel: SignUpViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding=FragmentPersonsBinding.inflate(inflater,container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter= ContactsAdapter(onItemClick = {
            val action = PersonsFragmentDirections.personToChat(
                username = it.username,
                imageUrl = it.imageUrl ?: ""
            )
            findNavController().navigate(action)
        })

        viewModel.userList.observe(viewLifecycleOwner) { updatedList ->
            adapter.setData(updatedList)
        }

        binding.rvContacts.setHasFixedSize(true)
        binding.rvContacts.adapter=adapter
        binding.rvContacts.layoutManager= LinearLayoutManager(requireContext())

        viewModel.fetchContactsUsingApp(requireContext())

        viewModel.contactsUsingApp.observe(viewLifecycleOwner) { contacts ->

            Log.d("AdapterTest", "Gelen kişi sayısı: ${contacts.size}")
            adapter.setData(contacts)
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
