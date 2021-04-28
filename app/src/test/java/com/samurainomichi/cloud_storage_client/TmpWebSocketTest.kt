package com.samurainomichi.cloud_storage_client

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
        private val repository = ConnectionRepository.getInstance(WebSocketDataSource(ip))
        private var token: String = ""
        private var authToken: String = ""
    }

    @Test
    fun t0_connect() {
        repository.connectBlocking()
        println("Connected")
    }

    @Test
    fun t1_auth() = runBlocking {
        repository.authCreateUser("testUser", "testPassword")
        authToken = repository.authLogin("testUser", "testPassword")
        println("Logged in")
        println("Auth token: $authToken")
        assertEquals(32, authToken.length)
    }

    @Test
    fun t2_upload() = runBlocking {
        repository.tmpUploadFilesStart(listOf("File1", "File2"))
        val buffer = ByteBuffer.wrap(ByteArray(30) { i -> (i % 16).toByte() })
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        token = repository.tmpUploadFilesEnd(listOf("File1", "File2"))

        assertEquals(32, token.length)
        println("Files uploaded")
        println("Token: $token")
    }

    @Test
    fun t3_download() = runBlocking {
        val size = CompletableDeferred<Int>()
        repository.onBufferReceived.observe {
            println("got it")
            size.complete(it.array().size)
        }
        repository.tmpDownloadFiles(token, listOf("File2"))

        assertEquals(30, size.await())
        println("File downloaded")
    }

    @Test
    fun t4_availableFiles() = runBlocking {
        val list = repository.tmpAvailableFiles(token)
        println()
        assertEquals("File1", list[0])
        assertEquals("File2", list[1])
    }

    @Test
    fun t5_availableFilesWrongToken() = runBlocking {
        try {
            repository.tmpAvailableFiles("12345678901234567890123456789012")
            fail("Exception 'No such token' expected")
        }
        catch (e: Exception) {
            assertEquals("No such token", e.message)
        }
    }

    @Test
    fun t6_downloadWrongToken() = runBlocking {
        try {
            repository.tmpDownloadFiles("Definitely wrong token.", listOf())
            fail("Exception 'Invalid token' expected")
        }
        catch (e: Exception) {
            assertEquals("Invalid token", e.message)
        }
    }

    @Test
    fun tl0_logOut() = runBlocking {
        repository.authLogout()
        println("Logged out")
    }

    @Test
    fun tl1_authWrongUsername() = runBlocking {
        try {
            repository.authLogin("NoWayItExists", "123456")
            fail("Exception 'No such user' expected")
        }
        catch (e: Exception) {
            assertEquals("User doesn't exist", e.message)
        }
    }

    @Test
    fun tl2_authWrongPassword() = runBlocking {
        try {
            repository.authLogin("admin", "123456")
            fail("Exception 'Wrong password' expected")
        }
        catch (e: Exception) {
            assertEquals("Username and/or password is incorrect", e.message)
        }
    }

    @Test
    fun tl3_authWithToken() = runBlocking {
        repository.authRestoreSession(authToken)
        println("Session restored")
    }

    @Test
    fun tl4_deleteUser() = runBlocking {
        repository.authDeleteUser()
        println("Logged out")

        try {
            repository.authRestoreSession(authToken)
            fail("Session should not be restored")
        }
        catch (e: Exception) {
            assertEquals("Session was not restored", e.message)
        }

    }
}