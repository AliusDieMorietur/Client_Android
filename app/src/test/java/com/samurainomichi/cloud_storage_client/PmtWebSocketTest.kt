package com.samurainomichi.cloud_storage_client
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.samurainomichi.cloud_storage_client.network.Connection
//import com.samurainomichi.cloud_storage_client.network.WebSocketDataSource
//import kotlinx.coroutines.*
//import org.junit.*
//import org.junit.Assert.*
//import org.junit.runners.MethodSorters
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//class PmtWebSocketTest {
//    companion object {
//        private const val ip = "127.0.0.1:8000"
//        private val connection = Connection.getInstance(WebSocketDataSource(ip))
//        private var token: String = ""
//    }
//
//    @Rule
//    @JvmField
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    @Test
//    fun t0_connect() {
//        connection.connectBlocking()
//        println("Connected")
//    }
//
//    @Test
//    fun t1_auth() = runBlocking {
//        val authToken = connection.authLogin("admin", "admin")
//        println("Logged in")
//        println("Auth token: $authToken")
//        assertEquals(32, authToken.length)
//    }
//
//    @Test
//    fun t2_newFolder() = runBlocking {
//
//    }
//
//    @Test
//    fun t2_availableFiles() = runBlocking {
//        val structure = connection.pmtAvailableFiles()
////        println(structure.size)
//    }
//
//    @Test
//    fun tl0_logOut() = runBlocking {
//        connection.authLogout()
//        println("Logged out")
//    }
//}