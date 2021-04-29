package com.samurainomichi.cloud_storage_client.temporary

import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.tabs.TabLayout
import com.samurainomichi.cloud_storage_client.databinding.TemporaryStorageFragmentBinding


class TemporaryStorageFragment : Fragment() {
    private lateinit var viewModel: TemporaryStorageViewModel
    private lateinit var binding: TemporaryStorageFragmentBinding

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
                viewModel.selectTab(tab?.position ?: 0)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        binding.btnCheckTmp.setOnClickListener {
            viewModel.checkAvailableFiles(binding.editTokenTmp.text.toString())
            binding.editTokenTmp.clearFocus()
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
                adapter.list.filter { name -> adapter.checkedCards.contains(name) }
            )
        }

        viewModel.repository.ldOnBufferReceived.observe(viewLifecycleOwner) {
            it?.let {
                val path = preferences.getString("download_path", null)

                path?.let { p ->
                    viewModel.onBuffer(it, p, requireContext())
                }
            }
        }

        binding.btnPasteTmp.setOnClickListener {
            pasteTokenFromClipboard()
        }

        binding.btnSelectAllTmp.setOnClickListener {
            adapter.checkAll()
        }

        binding.btnTmpChoose.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "*/*"

            @Suppress("DEPRECATION")
            startActivityForResult(intent, 33)
        }

        binding.btnTmpUpload.setOnClickListener {
            viewModel.uploadFiles(requireContext())
        }

        binding.btnCopyTmp.setOnClickListener {
            copyTokenToClipboard()
        }

        viewModel.availableFilesList.observe(viewLifecycleOwner) {
            adapter.list = it
        }

        viewModel.filesDownloadResult.observe(viewLifecycleOwner) {
            if(it.isSuccess)
                Toast.makeText(requireContext(), "${it.getOrNull()} files downloaded", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(requireContext(), "Download failed", Toast.LENGTH_SHORT).show()
        }

        viewModel.filesUploadResult.observe(viewLifecycleOwner) {
            if(it.isSuccess)
                Toast.makeText(requireContext(), "${it.getOrNull()} files uploaded", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
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

    private fun copyTokenToClipboard() {
        val clipboard = getSystemService(requireContext(), ClipboardManager::class.java) as ClipboardManager
        val token = binding.textViewTmpTokenUpload.text.toString()
        val clip: ClipData = ClipData.newPlainText("simple text", token)
        clipboard.setPrimaryClip(clip)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 33) {
            if (data != null) {
                val clipData = data.clipData

                val list = mutableListOf<Uri>()
                if(clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val path = clipData.getItemAt(i)
                        list.add(path.uri)
                    }
                    viewModel.setUriList(list, requireContext())
                }
                else if(data.data != null) {
                    viewModel.setUriList(listOf(data.data!!), requireContext())
                }
            }
        }
    }
}