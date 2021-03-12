package com.samurainomichi.cloud_storage_client.temporary

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.samurainomichi.cloud_storage_client.databinding.TemporaryStorageFragmentBinding

class TemporaryStorageFragment : Fragment() {
    private lateinit var viewModel: TemporaryStorageViewModel
    private lateinit var binding: TemporaryStorageFragmentBinding
    private var downloadPending: Boolean = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this).get(TemporaryStorageViewModel::class.java)
        binding = TemporaryStorageFragmentBinding.inflate(layoutInflater)
        val adapter = TemporaryStorageFilesAdapter()
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding.viewModel = viewModel
        binding.recyclerViewDownloadTmp.adapter = adapter

        binding.tabLayoutTmp.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectTab(tab?.position?: 0)
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        binding.btnCheckTmp.setOnClickListener {
            viewModel.checkFiles(binding.editTokenTmp.text.toString())
        }

        val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if(uri == null)
                return@registerForActivityResult

            requireContext().contentResolver
                    .takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val path = uri.toString()
            with(preferences.edit()) {
                putString("download_path", path)
                apply()
            }
        }

        binding.btnDownloadTmp.setOnClickListener {
            val path = preferences.getString("download_path", null)

            if(path == null) {
                openDocumentTree.launch(null)
                return@setOnClickListener
            }

            viewModel.downloadFiles(
                    binding.editTokenTmp.text.toString(),
                    adapter.checkedCards.toList(),
                    path,
                    requireContext()
            )
        }

        binding.btnPasteTmp.setOnClickListener {
            pasteTokenFromClipboard()
        }

        viewModel.fileList.observe(viewLifecycleOwner) {
            adapter.list = it
        }

        pasteTokenFromClipboard()
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun pasteTokenFromClipboard() {
        val clipboard = getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager

        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN) == true) {
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text

            if (text != null && text.length == 32) {
                binding.editTokenTmp.setText(text)
            }
        }
    }
}