package com.samurainomichi.cloud_storage_client.cloudstorage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samurainomichi.cloud_storage_client.model.Structure
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CloudStorageViewModel : ViewModel() {
    private val repository = ConnectionRepository.getInstance()
    private val _structures = MutableLiveData<List<Structure>>()
    val structures: LiveData<List<Structure>>
        get() = _structures

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _structures.postValue(repository.pmtAvailableStructure())
        }
        repository.onStructureUpdated.observe {
           _structures.value = it
        }
    }
}