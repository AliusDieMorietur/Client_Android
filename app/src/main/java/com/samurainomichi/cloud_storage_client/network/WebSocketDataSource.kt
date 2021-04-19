package com.samurainomichi.cloud_storage_client.network

import android.util.Log
import com.samurainomichi.cloud_storage_client.util.Observable
import com.samurainomichi.cloud_storage_client.util.StorageName
import com.samurainomichi.cloud_storage_client.model.Args
import com.samurainomichi.cloud_storage_client.model.User
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class WebSocketDataSource (
    ip: String,
): Channel(), DataSource {

    private val webSocketUrl: String = "ws://${ip}"

    private val client: WebSocketClient =
        object : WebSocketClient(URI(webSocketUrl)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("WS_CONNECTION", "Connection opened")
                onConnectionOpened.invoke(true)
            }

            override fun onMessage(message: String?) {
                Log.i("WS_CONNECTION", "Message: $message")

                if (message == null)
                    return

                receiveMessage(message)
            }

            override fun onMessage(bytes: ByteBuffer?) {
                println("buf")
                bytes?.let {
                    onBufferReceived.invoke(it)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i("WS_CONNECTION", "Connection closed")
                onConnectionClosed.invoke(true)
            }

            override fun onError(ex: Exception?) {
                Log.i("WS_CONNECTION", "Error: ${ex.toString()}")
            }
        }

    init {
        onSend.observe { client.send(it) }
    }

    override val onConnectionClosed: Observable<Boolean> = Observable()
    override val onConnectionOpened: Observable<Boolean> = Observable()
    override val onBufferReceived: Observable<ByteBuffer> = Observable()

    override fun connect() {
        if(!client.connection.isOpen)
            client.connect()
    }

    override fun connectBlocking() {
        if(!client.connection.isOpen)
            client.connectBlocking()
    }

    override fun sendBuffer(byteBuffer: ByteBuffer) {
        client.send(byteBuffer)
    }

    override suspend fun tmpAvailableFiles(token: String): List<String> =
        sendMessageAndGetResult(
            "availableFiles",
            Args(token = token, storage = StorageName.tmp)
        )

    override suspend fun tmpDownloadFiles(token: String, fileList: List<String>) {
        sendMessageAndGetResult<Boolean>(
            "download",
            Args(token = token, storage = StorageName.tmp, fileList = fileList),
            ignoreResult = true,
        )
    }

    override suspend fun tmpUploadFilesGetToken(fileNames: List<String>): String =
        sendMessageAndGetResult(
            "upload",
            Args(fileList = fileNames, storage = StorageName.tmp)
        )

    override suspend fun authLogin(username: String, password: String): String =
        sendMessageAndGetResult(
            "authUser",
            Args(user = User(username, password))
        )

    override suspend fun authRestoreSession(token: String): String =
        sendMessageAndGetResult(
            "restoreSession",
            Args(token = token)
        )

    override suspend fun authLogout() {
        sendMessageAndGetResult<Boolean>(
            "logOut",
            Args(),
            ignoreResult = true
        )
    }

    override suspend fun pmtAvailableFiles(): Unit =
        sendMessageAndGetResult(
            "availableFiles",
            Args(storage = StorageName.pmt)
        )
}