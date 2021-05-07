package com.samurainomichi.cloud_storage_client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.samurainomichi.cloud_storage_client.login.LoginResult
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import com.samurainomichi.cloud_storage_client.network.WebSocketDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.exceptions.WebsocketNotConnectedException

class MainActivityViewModel: ViewModel() {
    private lateinit var repository: ConnectionRepository
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _connectionResult = MutableLiveData<Boolean>()
    val connectionResult: LiveData<Boolean> = _connectionResult

    fun connectToServer(ip: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository = ConnectionRepository.getInstance(WebSocketDataSource(ip))
            try {
                repository.connectBlocking()
                _connectionResult.postValue(true)
            }
            catch (e: WebsocketNotConnectedException) {
                _connectionResult.postValue(false)
            }
        }
    }

    fun loginWithToken(token: String?) {
        if(token == null) {
            _loginResult.postValue(LoginResult(error = R.string.session_not_restored))
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val t = repository.authRestoreSession(token)
                _loginResult.postValue(LoginResult(success = t))
            } catch (e: Exception) {
                _loginResult.postValue(LoginResult(error = R.string.session_not_restored))
            }
        }
    }
}