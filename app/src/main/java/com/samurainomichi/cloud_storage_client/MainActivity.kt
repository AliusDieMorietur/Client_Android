package com.samurainomichi.cloud_storage_client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

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

    }
}