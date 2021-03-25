package com.samurainomichi.cloud_storage_client.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import kotlin.random.Random


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

    private val WEB_SOCKET_URL: String = "ws://${ip}"

    private val idMap: MutableMap<Int, (message: String) -> Unit> = mutableMapOf()
    private var id = Random.nextInt()

    private val client: WebSocketClient =
            object : WebSocketClient(URI(WEB_SOCKET_URL)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    onConnectionOpen.postValue(true)
                    Log.i("WS_CONNECTION", "Connection opened")
                }

                override fun onMessage(message: String?) {
                    Log.i("WS_CONNECTION", "Message: $message")


                    if (message == null)
                        return

                    moshi.adapter(GeneralResult::class.java).fromJson(message)?.callId?.let {
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

    fun tmpAvailableFilesAsync(token: String): Deferred<List<String>> {
        id++
        val deferred = CompletableDeferred<List<String>>()

        idMap[id] = { message ->
            val res = moshi.adapter(StringListResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("${it.error.code}"))
                    }
                    it.result != null -> {
                        deferred.complete(it.result)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: List is null"))
                    }
                }
            }
        }
        client.send(WebSocketMessageFactory.tmpAvailableFiles(id, token, StorageName.tmp))

        return deferred
    }

    fun tmpDownloadFilesAsync(token: String, fileList: List<String>): Deferred<Boolean> {
        id++
        val deferred = CompletableDeferred<Boolean>()

        idMap[id] = { message ->
            deferred.complete(true)
        }

        client.send(WebSocketMessageFactory.tmpDownload(id, token, fileList))

        return deferred
    }

    fun tmpUploadFilesGetTokenAsync(fileNames: List<String>): Deferred<String> {
        id++
        val deferred = CompletableDeferred<String>()

        idMap[id] = { message ->
            val res = moshi.adapter(StringResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("Error: ${it.error}"))
                    }
                    it.result != null -> {
                        deferred.complete(it.result)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: unexpected error (tmpUploadFiles)"))
                    }
                }
            }
        }

        val m = WebSocketMessageFactory.tmpUpload(id, fileNames)
        client.send(m)
        return deferred
    }

    fun authLoginAsync(username: String, password: String): Deferred<String> {
        id++
        val deferred = CompletableDeferred<String>()

        idMap[id] = { message ->
            val res = moshi.adapter(StringResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("Error: ${it.error}"))
                    }
                    it.result != null -> {
                        deferred.complete(it.result)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: unexpected error (authLogin)"))
                    }
                }
            }
        }

        val m = WebSocketMessageFactory.authUser(id, User(username, password))
        client.send(m)
        return deferred
    }

    fun authRestoreSessionAsync(token: String): Deferred<String> {
        id++
        val deferred = CompletableDeferred<String>()

        idMap[id] = { message ->
            val res = moshi.adapter(StringResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("Error: ${it.error}"))
                    }
                    it.result != null -> {
                        deferred.complete(it.result)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: unexpected error (authRestoreSession)"))
                    }
                }
            }
        }

        val m = WebSocketMessageFactory.authRestoreSession(id, token)
        client.send(m)
        return deferred
    }

    fun authLogoutAsync(): Deferred<Boolean> {
        id++
        val deferred = CompletableDeferred<Boolean>()

        idMap[id] = { message ->
            val res = moshi.adapter(GeneralResult::class.java).fromJson(message)
            res?.let {
                if (it.error != null) {
                    deferred.completeExceptionally(Exception("Error: ${it.error.message}"))
                }

                deferred.complete(true)
            }
        }

        val m = WebSocketMessageFactory.authLogout(id)
        client.send(m)
        return deferred
    }

    fun sendBuffer(buffer: ByteBuffer) {
        client.send(buffer)
    }
}

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

object StorageName {
    val tmp = "tmp"
    val pmt = "pmt"
}

class WebSocketMessageFactory {
    companion object {
        private fun create(callId: Int, msg: String, args: Args): String =
                "{\n" +
                        "    \"callId\": \"$callId\",\n" +
                        "    \"msg\": \"$msg\",\n" +
                        "    \"args\": $args\n" +
                        "}"

        fun tmpAvailableFiles(callId: Int, token: String, storageName: String): String =
            create(
                callId,
                "availableFiles",
                Args(token = token, storage = storageName)
            )

        fun tmpDownload(callId: Int, token: String, fileList: List<String>?): String =
            create(
                callId,
                "download",
                Args(token = token, fileList = fileList, storage = StorageName.tmp)
            )

        fun tmpUpload(callId: Int, fileList: List<String>?): String =
            create(
                callId,
                "upload",
                Args(fileList = fileList, storage = StorageName.tmp)
            )

        fun authUser(callId: Int, user: User): String =
            create(
                callId,
                "authUser",
                Args(user = user)
            )

        fun authRestoreSession(callId: Int, token: String): String =
            create(
                callId,
                "restoreSession",
                Args(token = token)
            )

        fun authLogout(callId: Int): String =
            create(
                callId,
                "logOut",
                Args()
            )
    }

}

class Session(val id: Int, val userid: Int, val ip: String, val token: String)
class User(val login: String, val password: String)
class Error(val message: String?, val code: String?)

class GeneralResult(val callId: Int, val error: Error?)
class StringListResult(val result: List<String>?, val error: Error?)
class StringResult(val result: String?, val error: Error?)
class RestoreSessionResult(val result: Session?, val error: Error?)
