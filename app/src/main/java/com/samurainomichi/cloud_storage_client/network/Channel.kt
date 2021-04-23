package com.samurainomichi.cloud_storage_client.network

import com.samurainomichi.cloud_storage_client.model.*
import com.samurainomichi.cloud_storage_client.util.Observable
import com.samurainomichi.cloud_storage_client.util.moshiDefault
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.*

open class Channel {
    val idMap: MutableMap<Int, (message: String) -> Unit> = mutableMapOf()
    var id = 0
    val onSend = Observable<String>()

    private val job = Job()
    val channelScope = CoroutineScope(job)

    fun receiveMessage(message: String) {
        onMessage(message)
    }

    private fun onMessage(message: String) {
        @Suppress("BlockingMethodInNonBlockingContext")
        println(message)
        moshiDefault.adapter(CallIdResult::class.java).fromJson(message)?.callId?.let {
            idMap[it]?.invoke(message)
            idMap.remove(it)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend inline fun <reified T> sendMessageAndGetResult(
        action: String, args: Args? = null, ignoreResult: Boolean = false, waitForResult: Boolean = true
    ): T {
        val deferred = CompletableDeferred<T>()

        val type = Types.newParameterizedType(MessageResult::class.java, T::class.java)
        val adapter: JsonAdapter<MessageResult<T>> = moshiDefault.adapter(type)

        id++

        idMap[id] = { msg ->
            val er = moshiDefault.adapter(ErrorResult::class.java).fromJson(msg)
            when {
                er?.error != null -> deferred.completeExceptionally(Exception(er.error.message))
                ignoreResult -> deferred.complete(true as T)
                else -> {
                    val res = adapter.fromJson(msg)
                    res?.result?.let {
                        deferred.complete(it)
                    }?: deferred.completeExceptionally(Exception("Unexpected error."))
                }
            }
        }

        onSend.invoke(Message(id, action, args).toString())

        if(!waitForResult)
            return true as T

        channelScope.launch {
            delay(2000)
            deferred.completeExceptionally(Exception("No answer from server."))
        }

        return deferred.await()
    }

    fun interruptPending() {
        idMap.forEach {
            it.value.invoke("{\"callId\":${it.key}, error: {\"message\":\"Pending interrupted\", \"code\":501}}")
        }
    }
}