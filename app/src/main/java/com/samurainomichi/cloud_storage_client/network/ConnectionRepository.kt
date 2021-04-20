package com.samurainomichi.cloud_storage_client.network

import com.samurainomichi.cloud_storage_client.model.Args
import com.samurainomichi.cloud_storage_client.model.User
import com.samurainomichi.cloud_storage_client.util.StorageName
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

    val onBufferReceived = dataSource.onBufferReceived
    val onConnectionOpened = dataSource.onConnectionOpened
    val onConnectionClosed = dataSource.onConnectionClosed

    init {
        onSend.observe { dataSource.sendMessage(it) }
        dataSource.onMessageReceived.observe { receiveMessage(it) }
    }

    fun sendBuffer(byteBuffer: ByteBuffer) = dataSource.sendBuffer(byteBuffer)
    fun connect() = dataSource.connect()
    fun connectBlocking() = dataSource.connectBlocking()


    suspend fun tmpAvailableFiles(token: String): List<String> =
        sendMessageAndGetResult(
            "availableFiles",
            Args(token = token, storage = StorageName.tmp)
        )

    suspend fun tmpDownloadFiles(token: String, fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "download",
            Args(token = token, storage = StorageName.tmp, fileList = fileList),
            ignoreResult = true,
        )
    }

    suspend fun tmpUploadFilesGetToken(fileNames: List<String>): String =
        sendMessageAndGetResult(
            "upload",
            Args(fileList = fileNames, storage = StorageName.tmp)
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

    suspend fun pmtAvailableFiles(): Unit =
        sendMessageAndGetResult(
            "availableFiles",
            Args(storage = StorageName.pmt)
        )
}