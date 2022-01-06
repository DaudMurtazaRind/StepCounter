package com.example.myapplication.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.database.DatabaseClient
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.databinding.FragmentSettingsBinding
import com.example.myapplication.models.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saveProfile()
    }

    private fun saveProfile() {
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etName.text.toString()
            val dob = binding.etDob.text.toString()
            val profile = Profile(0, name, dob)
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                DatabaseClient.getInstance(requireContext()).appDatabase.stepsDao().insertProfile(profile)
            }
        }
    }
}