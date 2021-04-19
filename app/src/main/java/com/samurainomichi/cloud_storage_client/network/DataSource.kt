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

//    suspend fun tmpAvailableFiles(token: String): List<String>
//    suspend fun tmpDownloadFiles(token: String, fileList: List<String>)
//    suspend fun tmpUploadFilesGetToken(fileNames: List<String>): String
//
//    suspend fun authLogin(username: String, password: String): String
//    suspend fun authRestoreSession(token: String): String
//    suspend fun authLogout()
//
//    suspend fun pmtAvailableFiles()
}