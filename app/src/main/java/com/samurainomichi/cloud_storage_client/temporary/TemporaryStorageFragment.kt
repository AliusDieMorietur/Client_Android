package com.samurainomichi.cloud_storage_client.temporary

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.samurainomichi.cloud_storage_client.R

class TemporaryStorageFragment : Fragment() {
    private lateinit var viewModel: TemporaryStorageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(TemporaryStorageViewModel::class.java)
        return inflater.inflate(R.layout.temporary_storage_fragment, container, false)
    }
}