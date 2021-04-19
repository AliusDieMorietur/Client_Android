package com.samurainomichi.cloud_storage_client

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import com.samurainomichi.cloud_storage_client.network.WebSocketDataSource
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
        private val connection = ConnectionRepository.getInstance(WebSocketDataSource(ip))
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
        val authToken = connection.authLogin("admin", "admin")
        println("Logged in")
        println("Auth token: $authToken")
        assertEquals(32, authToken.length)
    }

    @Test
    fun t2_upload() = runBlocking {
        token = connection.tmpUploadFilesGetToken(listOf("File1", "File2"))
        val buffer = ByteBuffer.wrap(ByteArray(30) { i -> (i % 16).toByte() })
        connection.sendBuffer(buffer.asReadOnlyBuffer())
        connection.sendBuffer(buffer.asReadOnlyBuffer())

        assertEquals(32, token.length)
        println("Files uploaded")
        println("Token: $token")
    }

    @Test
    fun t3_download() = runBlocking {
        val size = CompletableDeferred<Int>()
        connection.onBufferReceived.observe {
            println("got it")
            size.complete(it.array().size)
        }
        connection.tmpDownloadFiles(token, listOf("File2"))

        assertEquals(30, size.await())
        println("File downloaded")
    }

    @Test
    fun t4_availableFiles() = runBlocking {
        val list = connection.tmpAvailableFiles(token)
        println()
        assertEquals("File1", list[0])
        assertEquals("File2", list[1])
    }

    @Test
    fun t5_availableFilesWrongToken() = runBlocking {
        connection.connectBlocking()
        try {
            connection.tmpAvailableFiles("12345678901234567890123456789012")
            fail("Exception 'No such token' expected")
        }
        catch (e: Exception) {
            assertEquals("No such token", e.message)
        }
    }

    @Test
    fun t6_downloadWrongToken() = runBlocking {
        try {
            connection.tmpDownloadFiles("Definitely wrong token.", listOf())
            fail("Exception 'Invalid token' expected")
        }
        catch (e: Exception) {
            assertEquals("Invalid token", e.message)
        }
    }

    @Test
    fun tl0_logOut() = runBlocking {
        connection.authLogout()
        println("Logged out")
    }

    @Test
    fun tl1_authWrongUsername() = runBlocking {
        try {
            connection.authLogin("NoWayItExists", "123456")
            fail("Exception 'No such user' expected")
        }
        catch (e: Exception) {
            assertEquals("User with login <NoWayItExists> doesn't exist", e.message)
        }

    }

    @Test
    fun tl2_authWrongPassword() = runBlocking {
        try {
            connection.authLogin("admin", "123456")
            fail("Exception 'Wrong password' expected")
        }
        catch (e: Exception) {
            assertEquals("Username and/or password is incorrect", e.message)
        }
    }
}