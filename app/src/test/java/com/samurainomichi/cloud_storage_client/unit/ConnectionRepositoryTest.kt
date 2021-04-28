package com.samurainomichi.cloud_storage_client.unit
import com.samurainomichi.cloud_storage_client.fakes.FakeServer
import com.samurainomichi.cloud_storage_client.network.ConnectionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.Exception
import java.nio.ByteBuffer

class ConnectionRepositoryTest {
    companion object {
        private val fakeServer = FakeServer()
        val repository = ConnectionRepository.getInstance(fakeServer.getDataSource())
    }

    @Test
    fun tmpAvailableFiles() = runBlocking {
        val list = repository.tmpAvailableFiles("123321")
        assertEquals("file1", list[0])
        assertEquals("file2", list[1])
    }

    @Test
    fun tmpAvailableFilesWrongToken() = runBlocking {
        try {
            repository.tmpAvailableFiles("123321042")
            fail("Exception 'No such token' expected")
        }
        catch (e: Exception) {
            assertEquals("No such token", e.message)
        }
    }

    @Test
    fun auth() = runBlocking {
        val authToken = repository.authLogin("admin", "12345")
        assertEquals("789789789", authToken)
    }

    @Test
    fun authWrongPassword() = runBlocking {
        try {
            repository.authLogin("admin", "123456")
            fail("Exception 'Wrong password' expected")
        }
        catch (e: Exception) {
            assertEquals("Username and/or password is incorrect", e.message)
        }
    }

    @Test
    fun upload_download() = runBlocking {
        // Upload
        repository.tmpUploadFilesStart(listOf("File1", "File2"))
        val buffer = ByteBuffer.wrap(ByteArray(30) { i -> (i % 16).toByte() })
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        val token = repository.tmpUploadFilesEnd(listOf("File1", "File2"))


        assertEquals("buffersToken", token)
        println("Files uploaded")

        // Download
        repository.tmpDownloadFiles(token, listOf())
        val size = CompletableDeferred<Int>()
        repository.onBufferReceived.observe {
            println("Got it")
            size.complete(it.remaining())
        }
        repository.tmpDownloadFiles(token, listOf())

        assertEquals(30, size.await())
        println("File downloaded")
    }
}