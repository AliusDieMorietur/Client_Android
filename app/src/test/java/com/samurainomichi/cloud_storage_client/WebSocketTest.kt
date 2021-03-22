package com.samurainomichi.cloud_storage_client

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.samurainomichi.cloud_storage_client.network.WSConnection
import kotlinx.coroutines.*
import org.junit.Test

import org.junit.Rule
import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class WebSocketTest {
    // val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun availableFilesTmp() {
        val connection = WSConnection.getInstance("127.0.0.1:7000")
        connection.client.reconnectBlocking()
        runBlocking {
            val files = connection.tmpAvailableFilesAsync("BjttL0uqyRxtRmAYCFvL03OBt36mTqt")
            try {
                println(files.await())
            }
            catch (e: Exception) {
                println(e.message)
            }

            runBlocking {
                repeat(5) {
                    delay(1000)
                }
            }
        }
    }

    @Test
    fun downloadTemp() {
        val connection = WSConnection.getInstance("127.0.0.1:7000")
        connection.client.connectBlocking()
        runBlocking {
            val files = connection.tmpDownloadFilesAsync("cEOGcJ1Ip33AD1IEWvry3NAQlDym5CYh", listOf("contract.pdf"))
            try {
                println(files.await())
                println("files loaded.")
            }
            catch (e: Exception) {
                println(e.message)
            }

            runBlocking {
                repeat(5) {
                    delay(1000)
                }
            }
        }
    }

    @Test
    fun uploadTemp() {
        val connection = WSConnection.getInstance("127.0.0.1:7000")
        connection.client.connectBlocking()
        val byteBuffer = ByteBuffer.allocate(100000)
        val fc: FileChannel = FileInputStream("F:\\Test\\contract.pdf").channel
        fc.read(byteBuffer)
        runBlocking {
            val token = connection.uploadFilesTempAsync(listOf(byteBuffer), listOf("contract.pdf"))
            try {
                println(token.await())
                println("files uploaded.")
            }
            catch (e: Exception) {
                println(e.message)
            }

            runBlocking {
                repeat(5) {
                    delay(1000)
                }
            }
        }
    }
}