package com.samurainomichi.cloud_storage_client.temporary

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import com.samurainomichi.cloud_storage_client.readFileFromStorage
import com.samurainomichi.cloud_storage_client.readFileNames
import com.samurainomichi.cloud_storage_client.saveFileToStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class TemporaryStorageViewModel : ViewModel() {
    val repository = ConnectionRepository.getInstance()

    val availableFilesList: MutableLiveData<List<String>> = MutableLiveData(listOf())

    private val _filesDownloadResult: MutableLiveData<Result<Int>> = MutableLiveData()
    val filesDownloadResult: LiveData<Result<Int>>
        get() = _filesDownloadResult

    private val _filesUploadResult: MutableLiveData<Result<Int>> = MutableLiveData()
    val filesUploadResult: LiveData<Result<Int>>
        get() = _filesUploadResult

    private val _createdToken = MutableLiveData<String>()
    val createdToken: LiveData<String>
        get() = _createdToken

    private val _isOnPageDownload = MutableLiveData(true)
    val isOnPageDownload: LiveData<Boolean>
        get() = _isOnPageDownload

    private val filesToUpload = MutableLiveData<List<String>>()
    val stringFilesToUpload: LiveData<String> = Transformations.map(filesToUpload) {
        it.mapIndexed {i, item -> "${i+1}. $item"}.joinToString("\n", "Chosen files:\n")
    }

    private val _onBufferReceived = MutableLiveData<ByteBuffer>()
    val onBufferReceived: LiveData<ByteBuffer>
        get() = _onBufferReceived

    private var filesUriToUpload = mutableListOf<Uri>()
    private val filesToDownload = mutableListOf<String>()
    private var downloadIterator = filesToDownload.iterator()
    init {
        repository.onBufferReceived.observe { _onBufferReceived.postValue(it) }
    }

    fun selectTab(index: Int) {
        _isOnPageDownload.value = when(index) {
            0 -> true
            1 -> false
            else -> true
        }
    }

    fun checkAvailableFiles(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: List<String> = repository.tmpAvailableFiles(token)
                availableFilesList.postValue(list)
            }
            catch (e: Exception) {
                Log.i("qwerq", e.toString())
            }
        }
    }

    fun downloadFiles(token: String, list: List<String>) {
        if(list.isEmpty()) {
            _filesDownloadResult.postValue(Result.failure(Exception("Files aren't chosen")))
            return
        }

        filesToDownload.clear()
        filesToDownload.addAll(list)
        downloadIterator = filesToDownload.iterator()
        viewModelScope.launch(Dispatchers.IO) {
            try {

                repository.tmpDownloadFiles(token, list)
            }
            catch (e: Exception) {
                _filesDownloadResult.postValue(Result.failure(e))
            }
        }
    }

    fun setUriList(list: List<Uri>, context: Context) {
        filesUriToUpload.clear()
        filesUriToUpload.addAll(list)
        viewModelScope.launch(Dispatchers.IO) {
            val names = readFileNames(filesUriToUpload, context)
            filesToUpload.postValue(names)
        }

    }

    fun uploadFiles(context: Context) {
        if(filesUriToUpload.isEmpty())
            return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val names = readFileNames(filesUriToUpload, context)
                repository.tmpUploadFilesStart(names)
                for(uri in filesUriToUpload) {
                    val file = readFileFromStorage(uri, context)
                    repository.sendBuffer(file)
                }
                val token = repository.tmpUploadFilesEnd(names)
                _createdToken.postValue(token)
                _filesUploadResult.postValue(Result.success(filesUriToUpload.size))

            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
                _filesUploadResult.postValue(Result.failure(e))
            }
        }
    }

    fun onBuffer(buffer: ByteBuffer, context: Context) {
        if(!downloadIterator.hasNext()) {
            return
        }

        val name = downloadIterator.next()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                saveFileToStorage(buffer, name, context)

                if(!downloadIterator.hasNext()) {
                    _filesDownloadResult.postValue(Result.success(filesToDownload.size))
                }
            }
            catch (e: Exception) {
                Log.e("qwerq", e.message.toString())
                _filesDownloadResult.postValue(Result.failure(e))
            }
        }
    }
}