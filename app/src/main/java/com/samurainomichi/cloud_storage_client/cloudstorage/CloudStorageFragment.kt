package com.samurainomichi.cloud_storage_client.cloudstorage

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.samurainomichi.cloud_storage_client.R

class CloudStorageFragment : Fragment() {

    private lateinit var viewModel: CloudStorageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(CloudStorageViewModel::class.java)

        return inflater.inflate(R.layout.cloud_storage_fragment, container, false)
    }
}