package com.example.pedalstop.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedalstop.data.MainViewModel
import com.example.pedalstop.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private var _binding : FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    private fun initAdapter(binding: FragmentSearchBinding) {
        val postRowAdapter = PostRowAdapter(requireContext(), viewModel)
        binding.searchRecyclerView.adapter = postRowAdapter
        viewModel.observeAllPosts().observe(viewLifecycleOwner) {
            postRowAdapter.submitList(it)
        }
//        viewModel.searchPosts.observe(viewLifecycleOwner, Observer {
//            postRowAdapter.submitList(it)
//        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(binding.searchRecyclerView.context)
        initAdapter(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}