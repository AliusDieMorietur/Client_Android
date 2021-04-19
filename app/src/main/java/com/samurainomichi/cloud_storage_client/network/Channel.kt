package com.samurainomichi.cloud_storage_client.network

import com.samurainomichi.cloud_storage_client.util.Observable
import com.samurainomichi.cloud_storage_client.model.Args
import com.samurainomichi.cloud_storage_client.model.CallIdResult
import com.samurainomichi.cloud_storage_client.model.Message
import com.samurainomichi.cloud_storage_client.model.MessageResult
import com.samurainomichi.cloud_storage_client.util.moshiDefault
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.CompletableDeferred

open class Channel {
    val idMap: MutableMap<Int, (message: String) -> Unit> = mutableMapOf()
    var id = 0
    val onSend = Observable<String>()

    fun receiveMessage(message: String) {
        onMessage(message)
    }

    private fun onMessage(message: String) {
        @Suppress("BlockingMethodInNonBlockingContext")
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
            println(msg)
            val res = adapter.fromJson(msg)
            res?.let {
                when {
                    it.error != null -> {
                        deferred.completeExceptionally(Exception(it.error.message))
                    }
                    it.result != null || ignoreResult -> {
                        deferred.complete(it.result?: true as T)
                    }
                    else -> {
                        deferred.completeExceptionally(Exception("Unexpected error."))
                    }
                }
            }
        }

        onSend.invoke(Message(id, action, args).toString())

        if(!waitForResult)
            return true as T

        return deferred.await()
    }
}