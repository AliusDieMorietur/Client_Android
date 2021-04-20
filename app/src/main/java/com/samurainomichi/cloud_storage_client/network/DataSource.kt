package com.samurainomichi.cloud_storage_client.network

import com.samurainomichi.cloud_storage_client.util.Observable
import java.nio.ByteBuffer

interface DataSource {
    val onConnectionClosed: Observable<Boolean>
    val onConnectionOpened: Observable<Boolean>
    val onBufferReceived: Observable<ByteBuffer>
    val onMessageReceived: Observable<String>

    fun connect()
    fun connectBlocking()

    fun sendBuffer(byteBuffer: ByteBuffer)
    fun sendMessage(message: String)
}