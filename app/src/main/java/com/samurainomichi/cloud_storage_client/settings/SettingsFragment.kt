package com.samurainomichi.cloud_storage_client.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.samurainomichi.cloud_storage_client.R
import com.samurainomichi.cloud_storage_client.login.LoginActivity
import com.samurainomichi.cloud_storage_client.network.Connection
import kotlinx.coroutines.runBlocking

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val prefIp = findPreference<EditTextPreference>("server_ip")
        prefIp?.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val prefPath = findPreference<Preference>("download_path")
        prefPath?.summary = preferences.getString("download_path", null)?.let { Uri.parse(it).path } ?: "Not set"

        val openDocumentTree = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if(uri == null)
                return@registerForActivityResult
            
            requireContext().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val path = uri.toString()
            with(preferences.edit()) {
                putString("download_path", path)
                apply()
            }
            prefPath?.summary = preferences.getString("download_path", null)?.let { Uri.parse(it).path } ?: "Not set"
        }

        prefPath?.setOnPreferenceClickListener {
            openDocumentTree.launch(null)
            true
        }

        val prefLogout = findPreference<Preference>("logout")
        prefLogout?.setOnPreferenceClickListener {
            with(preferences.edit()) {
                putString("auth_token", null)
                apply()
            }

            @Suppress("DeferredResultUnused")
            runBlocking {
                Connection.getInstance().authLogout()
            }

            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
            )
            requireActivity().finish()
            true
        }

    }
}
