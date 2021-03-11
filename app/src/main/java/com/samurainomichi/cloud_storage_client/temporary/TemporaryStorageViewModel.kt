package com.samurainomichi.cloud_storage_client.temporary

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samurainomichi.cloud_storage_client.network.WSConnection
import com.samurainomichi.cloud_storage_client.saveFilesToStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class TemporaryStorageViewModel : ViewModel() {
    private val connection: WSConnection = WSConnection.getInstance("")

    val fileList: MutableLiveData<List<String>> = MutableLiveData(listOf())

    private val _isOnPageDownload = MutableLiveData(true)
    val isOnPageDownload: LiveData<Boolean>
        get() = _isOnPageDownload

    fun selectTab(index: Int) {
        _isOnPageDownload.value = when(index) {
            0 -> true
            1 -> false
            else -> true
        }
    }

    fun checkFiles(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: List<String> = connection.availableFilesTempAsync(token).await()
                fileList.postValue(list)
            }
            catch (e: Exception) {
                Log.i("qwerq", e.toString())
            }
        }
    }

    fun downloadFiles(token: String, list: List<String>, stringUri: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val buffers: List<ByteBuffer> = connection.downloadFilesTempAsync(token, list).await()
                saveFilesToStorage(buffers, list, stringUri, context)
            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
            }
        }
    }


}