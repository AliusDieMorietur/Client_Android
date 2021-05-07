package com.samurainomichi.cloud_storage_client.network

import com.samurainomichi.cloud_storage_client.util.Observable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class WebSocketDataSource (
    ip: String,
): DataSource {

    private val webSocketUrl: String = "ws://${ip}"

    private val client: WebSocketClient =
        object : WebSocketClient(URI(webSocketUrl)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                onConnectionOpened.invoke(true)
            }

            override fun onMessage(message: String?) {
                if (message == null)
                    return

                onMessageReceived.invoke(message)
            }

            override fun onMessage(bytes: ByteBuffer?) {
                println("buf")
                bytes?.let {
                    onBufferReceived.invoke(it)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                onConnectionClosed.invoke(true)
            }

            override fun onError(ex: Exception?) {

            }
        }

    override val onConnectionClosed: Observable<Boolean> = Observable()
    override val onConnectionOpened: Observable<Boolean> = Observable()
    override val onBufferReceived: Observable<ByteBuffer> = Observable()
    override val onMessageReceived: Observable<String> = Observable()

    override fun connect() {
        if(!client.connection.isOpen)
            client.connect()
    }

    override fun connectBlocking() {
        if(!client.connection.isOpen)
            client.connectBlocking(2, TimeUnit.SECONDS)
    }

    override fun sendBuffer(byteBuffer: ByteBuffer) {
        client.send(byteBuffer)
    }

    override fun sendMessage(message: String) {
        client.send(message)
    }


}