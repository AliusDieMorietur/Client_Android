package com.samurainomichi.cloud_storage_client

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.samurainomichi.cloud_storage_client.network.WSConnection
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runners.MethodSorters
import java.lang.Exception
import java.nio.ByteBuffer

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TmpWebSocketTest {
    companion object {
        private const val ip = "192.168.1.148:7000"
        private val connection = WSConnection.getInstance(ip)
        private var token: String = ""
    }

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun t0_connect() {
        connection.connectBlocking()
        println("Connected")
    }

    @Test
    fun t1_auth() = runBlocking {
        val authToken = connection.authLoginAsync("admin", "admin").await()
        println("Logged in")
        println("Auth token: $authToken")
        assertEquals(32, authToken.length)
    }

    @Test
    fun t2_upload() = runBlocking {
        token = connection.tmpUploadFilesGetTokenAsync(listOf("File1", "File2")).await()
        val buffer = ByteBuffer.wrap(ByteArray(30) { i -> (i % 16).toByte() })
        connection.sendBuffer(buffer.asReadOnlyBuffer())
        connection.sendBuffer(buffer.asReadOnlyBuffer())

        assertEquals(32, token.length)
        println("Files uploaded")
        println("Token: $token")
    }

    @Test
    fun t3_download() = runBlocking {
        connection.tmpDownloadFilesAsync(token, listOf("File2")).await()
        val size = CompletableDeferred<Int>()
        connection.onBufferReceived.observeForever {
            size.complete(it.array().size)
        }

        assertEquals(30, size.await())
        println("File downloaded")
    }

    @Test
    fun t4_availableFiles() = runBlocking {
        val list = connection.tmpAvailableFilesAsync(token).await()
        println()
        assertEquals("File1", list[0])
        assertEquals("File2", list[1])
    }

    @Test
    fun t5_availableFilesWrongToken() = runBlocking {
        connection.connectBlocking()
        try {
            val list = connection.tmpAvailableFilesAsync("12345678901234567890123456789012").await()
            fail("Exception 'No such token' expected")
        }
        catch (e: Exception) {
            assertEquals("No such token", e.message)
        }
    }

    @Test
    fun t6_downloadWrongToken() = runBlocking {
        try {
            connection.tmpDownloadFilesAsync("Definitely wrong token.", listOf()).await()
            fail("Exception 'No such token' expected")
        }
        catch (e: Exception) {
            assertEquals("Invalid token", e.message)
        }
    }

    @Test
    fun tl0_logOut() = runBlocking {
        connection.authLogoutAsync().await()
        println("Logged out")
    }

    @Test
    fun tl1_authWrongUsername() = runBlocking {
        try {
            val authToken = connection.authLoginAsync("NoWayItExists", "123456").await()
            fail("Exception 'No such user' expected")
        }
        catch (e: Exception) {
            assertEquals("User with login <NoWayItExists> doesn't exist", e.message)
        }

    }

    @Test
    fun tl2_authWrongPassword() = runBlocking {
        try {
            val authToken = connection.authLoginAsync("admin", "123456").await()
            fail("Exception 'Wrong password' expected")
        }
        catch (e: Exception) {
            assertEquals("Username and/or password is incorrect", e.message)
        }
    }
}