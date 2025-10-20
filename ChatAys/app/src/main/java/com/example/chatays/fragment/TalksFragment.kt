package com.example.chatays.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatays.R
import com.example.chatays.adapter.SohbetlerAdapter
import com.example.chatays.adapter.TalksAdapter
import com.example.chatays.databinding.FragmentTalksBinding
import com.example.chatays.viewModel.SignUpViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TalksFragment : Fragment() {

    private  var _binding: FragmentTalksBinding? =  null
    private val binding get() = _binding!!

    private lateinit var adapter: SohbetlerAdapter
    private  val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding=DataBindingUtil.inflate(inflater,R.layout.fragment_talks,container,false)

        binding.talksfragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {

        }

        adapter=SohbetlerAdapter(onItemClick = {
            val action = TalksFragmentDirections.talksToChat(
                username = it.username,
                imageUrl = it.imageUrl ?: ""
            )
            findNavController().navigate(action)
        })
        binding.apply {
            rvTalks.setHasFixedSize(true)
            rvTalks.adapter=adapter
            rvTalks.layoutManager= LinearLayoutManager(requireContext())
        }
        val currentUser = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        Log.d("Check", "currentUser displayName=$currentUser")

        viewModel.listenChats(currentUser)

        viewModel.chat.observe(viewLifecycleOwner) { list ->
            adapter.setData(list)
            adapter.notifyDataSetChanged()
        }

    }

    fun fabToClicked(){
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
            val navController = findNavController()

            if (navController.currentDestination?.id != R.id.personsFragment) {
                navController.navigate(R.id.personsFragment)
            }
            bottomNav.menu.findItem(R.id.personsFragment).isChecked = true

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding= null
    }


}