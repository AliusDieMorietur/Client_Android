package com.samurainomichi.cloud_storage_client.network

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.samurainomichi.cloud_storage_client.model.Args
import com.samurainomichi.cloud_storage_client.model.Structure
import com.samurainomichi.cloud_storage_client.model.StructureMessage
import com.samurainomichi.cloud_storage_client.model.User
import com.samurainomichi.cloud_storage_client.util.Observable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.nio.ByteBuffer

class ConnectionRepository private constructor(private val dataSource: DataSource) : Channel() {
    companion object {
        private lateinit var inst: ConnectionRepository
        fun getInstance(dataSource: DataSource? = null): ConnectionRepository {
            if (!::inst.isInitialized) {
                if (dataSource != null)
                    inst = ConnectionRepository(dataSource)
                else
                    throw Exception("Data source must be presented on the first call of getInstance")
            }

            return inst
        }
    }

    private val onBufferReceived = dataSource.onBufferReceived
    private val onConnectionOpened = dataSource.onConnectionOpened
    private val onConnectionClosed = dataSource.onConnectionClosed
    val onStructureUpdated = Observable<List<Structure>>()

    val ldOnBufferReceived = MutableLiveData<ByteBuffer>()
    val ldOnConnectionOpened = MutableLiveData<Boolean>()
    val ldOnConnectionClosed = MutableLiveData<Boolean>()

    init {
        onSend.observe { dataSource.sendMessage(it) }
        dataSource.onMessageReceived.observe { receiveMessage(it) }
        onConnectionClosed.observe {
            interruptPending("Connection closed")
            ldOnConnectionClosed.value = it
        }

        onConnectionOpened.observe { ldOnConnectionOpened.postValue(it) }
        onBufferReceived.observe { ldOnBufferReceived.postValue(it) }

        idMap[-1] = {
            val msg = Json.decodeFromString<StructureMessage>(it)
            onStructureUpdated.invoke(msg.structure)
        }
    }

    fun sendBuffer(byteBuffer: ByteBuffer) = dataSource.sendBuffer(byteBuffer)
    fun connect() = dataSource.connect()
    fun connectBlocking() = dataSource.connectBlocking()


    suspend fun tmpAvailableFiles(token: String): List<String> =
        sendMessageAndGetResult(
            "tmpAvailableFiles",
            Args(token = token)
        )

    suspend fun tmpDownloadFiles(token: String, fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "tmpDownload",
            Args(token = token, fileList = fileList),
            ignoreResult = true,
        )
    }

    suspend fun tmpUploadFilesStart(fileList: List<String>): String =
        sendMessageAndGetResult(
            "tmpUploadStart",
            Args(fileList = fileList)
        )

    suspend fun tmpUploadFilesEnd(fileList: List<String>): String =
        sendMessageAndGetResult(
            "tmpUploadEnd",
            Args(fileList = fileList)
        )

    suspend fun authLogin(username: String, password: String): String =
        sendMessageAndGetResult(
            "authUser",
            Args(user = User(username, password))
        )

    suspend fun authRestoreSession(token: String): String =
        sendMessageAndGetResult(
            "restoreSession",
            Args(token = token)
        )

    suspend fun authLogout() {
        sendMessageAndGetResult<Boolean>(
            "logOut",
            Args(),
            ignoreResult = true
        )
    }

    suspend fun authCreateUser(username: String, password: String) {
        sendMessageAndGetResult<Boolean>(
            "createUser",
            Args(user = User(username, password)),
            ignoreResult = true
        )
    }

    suspend fun authDeleteUser() {
        sendMessageAndGetResult<Boolean>(
            "deleteUser",
            Args(),
            ignoreResult = true
        )
    }

    suspend fun pmtAvailableStructure(): List<Structure> =
        sendMessageAndGetResult(
            "pmtAvailableFiles"
        )

    suspend fun pmtUploadFilesStart(fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "pmtUploadStart",
            Args(fileList = fileList),
            ignoreResult = true
        )
    }

    suspend fun pmtUploadFilesEnd(fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "pmtUploadEnd",
            Args(fileList = fileList),
            ignoreResult = true
        )
    }

    suspend fun pmtDownloadFiles(fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "pmtDownload",
            Args(fileList = fileList),
            ignoreResult = true,
        )
    }

    suspend fun pmtNewFolder(name: String) {
        sendMessageAndGetResult<Boolean>(
            "pmtNewFolder",
            Args(name = name),
            ignoreResult = true,
        )
    }

    suspend fun pmtRenameFile(name: String, newName: String) {
        sendMessageAndGetResult<Boolean>(
            "pmtRenameFile",
            Args(name = name, newName = newName),
            ignoreResult = true,
        )
    }

}