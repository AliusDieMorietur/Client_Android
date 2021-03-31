package com.samurainomichi.cloud_storage_client.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer


abstract class WSConnection(ip: String) {
    companion object {
        private lateinit var INSTANCE: WSConnection

        fun getInstance(ip: String = ""): WSConnection {
            if(!::INSTANCE.isInitialized)
                INSTANCE = object : WSConnection(ip) {}


            return INSTANCE
        }

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    }

    val onConnectionOpen = MutableLiveData(false)
    val onConnectionClosed = MutableLiveData(false)
    val onBufferReceived: MutableLiveData<ByteBuffer> = MutableLiveData()

    private val webSocketUrl: String = "ws://${ip}"

    private val idMap: MutableMap<Int, (message: String) -> Unit> = mutableMapOf()
    private var id = 0

    private val client: WebSocketClient =
            object : WebSocketClient(URI(webSocketUrl)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    onConnectionOpen.postValue(true)
                    Log.i("WS_CONNECTION", "Connection opened")
                }

                override fun onMessage(message: String?) {
                    Log.i("WS_CONNECTION", "Message: $message")


                    if (message == null)
                        return

                    moshi.adapter(CallIdResult::class.java).fromJson(message)?.callId?.let {
                        idMap[it]?.invoke(message)
                        idMap.remove(it)
                    }
                }

                override fun onMessage(bytes: ByteBuffer?) {
                    bytes?.let {
                        onBufferReceived.postValue(it)
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.i("WS_CONNECTION", "Connection closed")
                    onConnectionClosed.postValue(true)
                }

                override fun onError(ex: Exception?) {
                    Log.i("WS_CONNECTION", "Error: ${ex.toString()}")
                }
            }

    fun connect() {
        if(!client.connection.isOpen)
            client.connect()
    }

    fun connectBlocking() {
        if(!client.connection.isOpen)
            client.connectBlocking()
    }

    fun reconnect() {
        client.reconnect()
    }

    fun tmpAvailableFilesAsync(token: String): Deferred<List<String>> =
        sendMessageAndGetResultAsync(
            "availableFiles",
            Args(token = token, storage = StorageName.tmp)
        )

    fun tmpDownloadFilesAsync(token: String, fileList: List<String>): Deferred<Boolean> =
        sendMessageAndGetResultAsync(
            "download",
            Args(token = token, storage = StorageName.tmp, fileList = fileList),
            ignoreResult = true
        )

    fun tmpUploadFilesGetTokenAsync(fileNames: List<String>): Deferred<String> =
        sendMessageAndGetResultAsync(
            "upload",
            Args(fileList = fileNames, storage = StorageName.tmp)
        )
    fun authLoginAsync(username: String, password: String): Deferred<String> =
        sendMessageAndGetResultAsync(
            "authUser",
            Args(user = User(username, password))
        )

    fun authRestoreSessionAsync(token: String): Deferred<String> =
        sendMessageAndGetResultAsync(
            "restoreSession",
            Args(token = token)
        )

    fun authLogoutAsync(): Deferred<Boolean> =
        sendMessageAndGetResultAsync(
            "logOut",
            Args(),
            ignoreResult = true
        )

    fun pmtAvailableFilesAsync(): Deferred<List<Structure>> =
        sendMessageAndGetResultAsync(
            "availableFiles",
            Args(storage = StorageName.pmt)
        )
    private inline fun <reified T> sendMessageAndGetResultAsync(action: String, args: Args, ignoreResult: Boolean = false): Deferred<T> {
        val deferred = CompletableDeferred<T>()

        val type = Types.newParameterizedType(MessageResult::class.java, T::class.java)
        val adapter: JsonAdapter<MessageResult<T>> = moshi.adapter(type)

        id++

        idMap[id] = { msg ->
            val res = adapter.fromJson(msg)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception(it.error.message))
                    }
                    it.result != null || ignoreResult -> {
                        deferred.complete(it.result?: true as T)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Unexpected error."))
                    }
                }
            }
        }

        client.send(createMessage(id, action, args))
        return deferred
    }

    fun sendBuffer(buffer: ByteBuffer) {
        client.send(buffer)
    }

    private fun createMessage(callId: Int, msg: String, args: Args): String =
        "{\n" +
            "    \"callId\": \"$callId\",\n" +
            "    \"msg\": \"$msg\",\n" +
            "    \"args\": $args\n" +
            "}"
}

object StorageName {
    const val tmp = "tmp"
    const val pmt = "pmt"
}

class Structure(
    val name: String,
    @Json(name = "childs") val children: List<Structure>?,
    val capacity: Int
)

class Args(
    val storage: String? = null,
    val token: String? = null,
    val fileList: List<String>? = null,
    val currentPath: String? = null,
    val action: String? = null,
    val changes: List<List<String>>? = null,
    val user: User? = null,

    ) {
    override fun toString(): String = WSConnection.moshi.adapter(Args::class.java).toJson(this)
}

class User(val login: String, val password: String)
class Error(val message: String?, val code: String?)

class CallIdResult(val callId: Int)
class MessageResult<T>(val result: T?, val error: Error?)