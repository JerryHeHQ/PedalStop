package com.example.pedalstop.screens

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.databinding.FragmentPostsBinding
import com.example.pedalstop.databinding.FragmentSavedBinding

class SavedFragment : Fragment() {
    private var _binding : FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private fun initAdapter(binding: FragmentSavedBinding) {
        val postRowAdapter = PostRowAdapter(requireContext(), viewModel)
        binding.savedRecyclerView.adapter = postRowAdapter
        viewModel.visiblePosts.observe(viewLifecycleOwner, Observer {
            Log.d("BRUH", "submitList")
            postRowAdapter.submitList(it.filter { postData ->
                viewModel.isFavorited(postData.firestoreID)
            })
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSavedBinding.bind(view)
        binding.savedRecyclerView.layoutManager = LinearLayoutManager(binding.savedRecyclerView.context)
        initAdapter(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}