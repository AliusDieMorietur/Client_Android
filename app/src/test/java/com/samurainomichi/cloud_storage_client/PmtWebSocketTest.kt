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
class PmtWebSocketTest {
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
    fun t10_auth() = runBlocking {
        repository.authCreateUser("testUser", "testPassword")
        authToken = repository.authLogin("testUser", "testPassword")
        println("Logged in")
        println("Auth token: $authToken")
        assertEquals(32, authToken.length)
    }

    @Test
    fun t20_newFolder() = runBlocking {
        repository.pmtNewFolder("foo/")
        repository.pmtNewFolder("bar/")
        repository.pmtNewFolder("foo/second/")

        val structure = repository.pmtAvailableStructure()
        assertEquals(2, structure.size)
        assertEquals("second", structure.find { it.name == "foo" }?.children?.get(0)?.name)
    }

    @Test
    fun t21_newFolderSameName() = runBlocking {
        repository.pmtNewFolder("foo/")

        val structure = repository.pmtAvailableStructure()
        assertEquals(2, structure.size)
    }

    @Test
    fun t30_upload() = runBlocking {
        repository.pmtUploadFilesStart(listOf("bar/File1", "bar/File2"))
        val buffer = ByteBuffer.wrap(ByteArray(25) { i -> (i % 16).toByte() })
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        repository.sendBuffer(buffer.asReadOnlyBuffer())
        repository.pmtUploadFilesEnd(listOf("bar/File1", "bar/File2"))

        val structure = repository.pmtAvailableStructure()
        val bar = structure.find { it.name == "bar" }!!

        assertEquals(2, bar.children?.size)
        assertEquals(50, bar.capacity)
    }

    @Test
    fun t40_rename() = runBlocking {
        repository.pmtRenameFile("bar/File1", "bar/newName")
        val structure = repository.pmtAvailableStructure()
        val foo = structure.find { it.name == "bar" }!!

        assertEquals(null, foo.children?.find { it.name == "File1" }?.name)
        assertEquals("newName", foo.children?.find { it.name == "newName" }?.name)
        assertEquals("File2", foo.children?.find { it.name == "File2" }?.name)
    }

    @Test
    fun t41_renameSameName() = runBlocking {
        try {
            repository.pmtRenameFile("bar/File2", "bar/newName")
            fail()
        }
        catch (e: Exception) {
            assertEquals("NOT_IMPLEMENTED", e.message)
        }

    }

    @Test
    fun t50_availableFiles() = runBlocking {
        val structure = repository.pmtAvailableStructure()
        assertEquals(50, structure.sumBy { it.capacity })
    }

    @Test
    fun tl0_deleteUser() = runBlocking {
        repository.authDeleteUser()
        println("Logged out")
    }
}