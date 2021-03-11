package com.samurainomichi.cloud_storage_client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.samurainomichi.cloud_storage_client.network.WSConnection

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            setOnNavigationItemSelectedListener { item ->
                if(selectedItemId == item.itemId)
                    return@setOnNavigationItemSelectedListener false
                when(item.itemId) {
                    R.id.menu_item_storage -> {
                        navController.navigate(R.id.action_global_cloudStorageFragment)
                        true
                    }
                    R.id.menu_item_temporary -> {
                        navController.navigate(R.id.action_global_temporaryStorageFragment)
                        true
                    }
                    R.id.menu_item_settings -> {
                        navController.navigate(R.id.action_global_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val serverIp = preferences.getString("server_ip", null) ?: "192.168.1.148:7000"
        WSConnection.getInstance(serverIp)
    }
}