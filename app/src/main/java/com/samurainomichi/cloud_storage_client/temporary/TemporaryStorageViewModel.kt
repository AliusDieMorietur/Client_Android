package com.samurainomichi.cloud_storage_client.temporary

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samurainomichi.cloud_storage_client.network.WSConnection
import com.samurainomichi.cloud_storage_client.readFileFromStorage
import com.samurainomichi.cloud_storage_client.readFileNames
import com.samurainomichi.cloud_storage_client.saveFileToStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class TemporaryStorageViewModel : ViewModel() {
    val connection: WSConnection = WSConnection.getInstance("")

    val checkedFilesList: MutableLiveData<List<String>> = MutableLiveData(listOf())

    private val _createdToken = MutableLiveData<String>()
    val createdToken: LiveData<String>
        get() = _createdToken

    private val _isOnPageDownload = MutableLiveData(true)
    val isOnPageDownload: LiveData<Boolean>
        get() = _isOnPageDownload

    var filesUriToUpload = mutableListOf<Uri>()

    private val filesToDownload = mutableListOf<String>()
    private var downloadIterator = filesToDownload.iterator()

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
                val list: List<String> = connection.tmpAvailableFilesAsync(token).await()
                checkedFilesList.postValue(list)
            }
            catch (e: Exception) {
                Log.i("qwerq", e.toString())
            }
        }
    }

    fun downloadFiles(token: String, list: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                filesToDownload.clear()
                filesToDownload.addAll(list)
                downloadIterator = filesToDownload.iterator()
                connection.tmpDownloadFilesAsync(token, list).await()
            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
            }
        }
    }

    fun setUriList(list: List<Uri>) {
        filesUriToUpload.clear()
        filesUriToUpload.addAll(list)
    }

    fun uploadFiles(context: Context) {
        if(filesUriToUpload.isEmpty())
            return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val names = readFileNames(filesUriToUpload, context)
                val token = connection.tmpUploadFilesGetTokenAsync(names).await()

                for(uri in filesUriToUpload) {
                    val file = readFileFromStorage(uri, context)
                    connection.sendBuffer(file)
                }
                _createdToken.postValue(token)

            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
            }
        }
    }

    fun onBuffer(buffer: ByteBuffer, directoryUri: String, context: Context) {
        if(!downloadIterator.hasNext()) {
            return
        }

        val name = downloadIterator.next()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                saveFileToStorage(buffer, name, directoryUri, context)
            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
            }
        }
    }


}