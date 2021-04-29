package com.samurainomichi.cloud_storage_client.cloudstorage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import com.samurainomichi.cloud_storage_client.R
import com.samurainomichi.cloud_storage_client.databinding.CloudStorageFragmentBinding
import com.samurainomichi.cloud_storage_client.model.Structure
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CloudStorageFragment : Fragment() {

    private lateinit var viewModel: CloudStorageViewModel
    private lateinit var adapter: CloudStorageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CloudStorageViewModel::class.java)
        val binding = CloudStorageFragmentBinding.inflate(inflater)
        adapter = CloudStorageAdapter()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, adapter.callback)

        viewModel.structures.observe(viewLifecycleOwner) {
            adapter.root = it
        }

        binding.recyclerView.adapter = adapter
        binding.lifecycleOwner = this
        return binding.root
    }
}