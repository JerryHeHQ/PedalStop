package com.example.pedalstop.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.pedalstop.R
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private var _binding : FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentAuthUser = viewModel.getCurrentAuthUser()
        val textUID = "UID: ${currentAuthUser.uid}"
        val textName = "Name: ${currentAuthUser.name}"
        val textEmail = "Email: ${currentAuthUser.email}"
        binding.settingsUID.text = textUID
        binding.settingsName.text = textName
        binding.settingsEmail.text = textEmail
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}