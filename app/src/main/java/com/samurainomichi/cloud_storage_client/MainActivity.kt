package com.samurainomichi.cloud_storage_client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.samurainomichi.cloud_storage_client.login.LoginActivity
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import com.samurainomichi.cloud_storage_client.network.WebSocketDataSource
import com.samurainomichi.cloud_storage_client.temporary.TemporaryStorageViewModel
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private val job = Job()
    val coroutineScope = CoroutineScope(job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_loading)

        val viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val serverIp = preferences.getString("server_ip", null) ?: "192.168.1.148:7000"
        val authToken = preferences.getString("auth_token", null)

        viewModel.connectToServer(serverIp)
        viewModel.connectionResult.observe(this) {
            if(it) {
                viewModel.loginWithToken(authToken)
            }
            else {
                Toast.makeText(applicationContext, "Couldn't connect to the server", Toast.LENGTH_SHORT).show()
                navigateToLoginScreen()
            }
        }

        viewModel.loginResult.observe(this) {
            if (it.error != null) {
                Toast.makeText(applicationContext, it.error, Toast.LENGTH_SHORT).show()
                navigateToLoginScreen()
                return@observe
            }

            setContentView(R.layout.activity_main)
            val navController = findNavController(R.id.nav_host_fragment)
            findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
                setOnNavigationItemSelectedListener { item ->
                    if (selectedItemId == item.itemId)
                        return@setOnNavigationItemSelectedListener false
                    when (item.itemId) {
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
    fun navigateToLoginScreen() {
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
    }
}