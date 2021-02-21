package com.samurainomichi.cloud_storage_client.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.samurainomichi.cloud_storage_client.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}