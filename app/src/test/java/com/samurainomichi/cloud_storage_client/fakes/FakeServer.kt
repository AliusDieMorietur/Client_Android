package com.samurainomichi.cloud_storage_client.fakes

import com.samurainomichi.cloud_storage_client.model.Error
import com.samurainomichi.cloud_storage_client.model.Message
import com.samurainomichi.cloud_storage_client.model.User
import com.samurainomichi.cloud_storage_client.network.DataSource
import com.samurainomichi.cloud_storage_client.util.Observable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer

class FakeServer {
    private fun formResult(callId: Int, res: String) = "{" +
        "\"callId\": $callId," +
        "\"result\": $res}"

    private fun formError(callId: Int, error: Error? = null) = "{" +
        "\"callId\": \"$callId\"," +
        "\"error\": ${Json.encodeToString(error)}}"

    val buffers = mutableListOf<ByteBuffer>()
    var sendBuffer = false

    fun getResult(msg: String): String {
        val message: Message = Json.decodeFromString(msg)

        var answer = formError(message.callId, Error("No answer", 0))

        when (message.msg) {
            "getInt" -> answer = formResult(message.callId, "13")
            "getList" -> answer = formResult(message.callId, "[\"one\", \"two\"]")
            "getError" -> answer = formError(message.callId, Error("test error", 1))

            "tmpAvailableFiles" -> {
                val args = message.args!!
                answer = if (args.token!! == "123321")
                    formResult(message.callId, "[\"file1\", \"file2\"]")
                else
                    formError(message.callId, Error("No such token", 2))
            }

            "authUser" -> {
                val user = message.args!!.user!!
                answer = if (user == User("admin", "12345")) {
                    formResult(message.callId, "\"789789789\"")
                } else
                    formError(message.callId, Error("Username and/or password is incorrect", 3))
            }

            "tmpUploadStart" -> {
                answer = formResult(message.callId, "\"ok\"")
            }

            "tmpUploadEnd" -> {
                answer = formResult(message.callId, "\"buffersToken\"")
            }

            "tmpDownload" -> {
                answer = formResult(message.callId, "true")
                sendBuffer = true
            }
        }
        return answer
    }

    fun getDataSource() = object : DataSource {
        override val onConnectionClosed: Observable<Boolean> = Observable()
        override val onConnectionOpened: Observable<Boolean> = Observable()
        override val onBufferReceived: Observable<ByteBuffer> = Observable()
        override val onMessageReceived: Observable<String> = Observable()

        override fun connect() {
            println("fake connected")
        }

        override fun connectBlocking() {
            println("fake connected")
        }

        override fun sendBuffer(byteBuffer: ByteBuffer) {
            buffers += byteBuffer
        }

        override fun sendMessage(message: String) {
            val answer = getResult(message)
            onMessageReceived.invoke(answer)

            if(sendBuffer) {
                onBufferReceived.invoke(buffers[0])
                sendBuffer = false
            }
        }

    }
}