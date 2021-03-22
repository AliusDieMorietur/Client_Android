package com.samurainomichi.cloud_storage_client.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.samurainomichi.cloud_storage_client.R
import com.samurainomichi.cloud_storage_client.network.WSConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val connection = WSConnection.getInstance()

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val authToken = connection.authLoginAsync(username, password).await()
                _loginResult.postValue(LoginResult(success = authToken))
            }

            catch (e: Exception) {
                _loginResult.postValue(LoginResult(error = R.string.login_failed))
            }
        }
    }

    fun loginWithToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = connection.authRestoreSessionAsync(token).await()
                _loginResult.postValue(LoginResult(success = token))
            }
            catch (e: Exception) {
                _loginResult.postValue(LoginResult(error = R.string.session_not_restored))
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.isNotBlank()
    }
}