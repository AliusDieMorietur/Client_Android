package com.samurainomichi.cloud_storage_client.unit

import com.samurainomichi.cloud_storage_client.fakes.FakeServer
import com.samurainomichi.cloud_storage_client.network.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.lang.Exception

class ChannelTest {
    companion object {
        private val channel = object : Channel() {}
        init {
            val fakeServer = FakeServer()
            channel.onSend.observe {
                val answer = fakeServer.getResult(it)
                channel.receiveMessage(answer)
            }
        }
    }

    @Test
    fun getInt(): Unit = runBlocking {
        val i: Int = channel.sendMessageAndGetResult("getInt")
        assertEquals(13, i)
    }

    @Test
    fun getStringList(): Unit = runBlocking {
        val list: List<String> = channel.sendMessageAndGetResult("getList")
        assertEquals(2, list.size)
        assertEquals("one", list[0])
        assertEquals("two", list[1])
    }

    @Test
    fun getError(): Unit = runBlocking {
        try {
            channel.sendMessageAndGetResult<Int>("getError")
            fail("Should throw test error")
        }
        catch (e: Exception) {
            assertEquals("test error", e.message)
        }
    }
}