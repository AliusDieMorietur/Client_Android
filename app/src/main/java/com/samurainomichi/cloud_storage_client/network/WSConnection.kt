package com.samurainomichi.cloud_storage_client.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.FileOutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.random.Random


abstract class WSConnection(private val ip: String) {
    companion object {
        private lateinit var INSTANCE: WSConnection

        fun getInstance(ip: String): WSConnection {
            if(!::INSTANCE.isInitialized)
                INSTANCE = object : WSConnection(ip) {}

            return INSTANCE
        }

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    }

    private val WEB_SOCKET_URL: String = "ws://${ip}"

    private val buffers: MutableList<ByteBuffer> = mutableListOf()
    private val idMap: MutableMap<Int, (message: String) -> Unit> = mutableMapOf()
    private var id = Random.nextInt()

    val client: WebSocketClient =
            object : WebSocketClient(URI(WEB_SOCKET_URL)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
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
                    if(bytes == null)
                        return

                    buffers.add(bytes)
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.i("WS_CONNECTION", "Connection closed")
                }

                override fun onError(ex: Exception?) {
                    Log.i("WS_CONNECTION", "Error: ${ex.toString()}")
                }
            }

    init {
        client.connect()
    }

    fun availableFilesTempAsync(token: String): Deferred<List<String>> {
        id++
        val deferred = CompletableDeferred<List<String>>()

        idMap[id] = { message ->
            val res = moshi.adapter(AvailableFilesResult::class.java).fromJson(message)
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
        client.send(WebSocketMessageFactory.availableFilesMessage(id, token, StorageName.Tmp))

        return deferred;
    }

    fun downloadFilesTempAsync(token: String, fileList: List<String>): Deferred<List<ByteBuffer>> {
        id++
        val deferred = CompletableDeferred<List<ByteBuffer>>()

        buffers.clear()

        idMap[id] = { message ->
            val res = moshi.adapter(DownloadResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("Error: ${it.error.message}"))
                    }
                    it.result != null -> {
                        deferred.complete(buffers)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: List is null"))
                    }
                }
            }
        }

        client.send(WebSocketMessageFactory.downloadMessageTemp(id, token, fileList))

        return deferred
    }

    fun uploadFilesTempAsync(buffers: List<ByteBuffer>, fileNames: List<String>): Deferred<String> {
        id++
        val deferred = CompletableDeferred<String>()

        idMap[id] = { message ->
            val res = moshi.adapter(UploadResult::class.java).fromJson(message)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception("Error: ${it.error}"))
                    }
                    it.result != null -> {
                        deferred.complete(it.result)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Exception: Token is null"))
                    }
                }
            }
        }
        buffers.forEach {
            client.send(it)
        }
        val m = WebSocketMessageFactory.uploadMessageTemp(id, fileNames)
        client.send(m)
        return deferred
    }
}

class Args(
        val storageName: String? = null,
        val token: String? = null,
        val fileList: List<String>? = null,
        val currentPath: String? = null,
        val action: String? = null,
        val changes: List<List<String>>? = null,

) {
    fun toJson(): String = WSConnection.moshi.adapter(Args::class.java).toJson(this)
}

enum class StorageName(name: String) {
    Tmp("tmp"),
    Pmt("pmt")
}

class WebSocketMessageFactory {
    companion object {
        private fun create(callId: Int, msg: String, args: String): String =
                "{\n" +
                        "    \"callId\": \"$callId\",\n" +
                        "    \"msg\": \"$msg\",\n" +
                        "    \"args\": $args\n" +
                        "}"

        fun availableFilesMessage(callId: Int, token: String, storageName: StorageName): String =
                create(
                        callId,
                        "availableFiles",
                        Args(token = token, storageName = storageName.name).toJson()
                )

        fun downloadMessageTemp(callId: Int, token: String, fileList: List<String>?): String =
                create(
                        callId,
                        "download",
                        Args(token = token, fileList = fileList, storageName = StorageName.Tmp.name).toJson()
                )

        fun downloadMessagePerm(callId: Int, token: String, fileList: List<String>?): String =
                create(
                        callId,
                        "download",
                        Args(token = token, fileList = fileList).toJson()
                )

        fun uploadMessageTemp(callId: Int, fileList: List<String>?): String =
                create(
                        callId,
                        "upload",
                        Args(fileList = fileList, storageName = StorageName.Tmp.name).toJson()
                )

        fun uploadMessagePerm(callId: Int, fileList: List<String>?): String =
                create(
                        callId,
                        "upload",
                        Args(fileList = fileList).toJson()
                )


    }

}

class Error(val message: String?, val code: String?)
class AvailableFilesResult(val callId: Int, val result: List<String>?, val error: Error?)
class DownloadResult(val callId: Int, val result: List<String>?, val error: Error?)
class UploadResult(val callId: Int, val result: String?, val error: Error?)
class GeneralResult(val callId: Int)