package com.samurainomichi.cloud_storage_client.model

import com.samurainomichi.cloud_storage_client.util.moshiDefault
import com.squareup.moshi.Json

class Structure(
    val name: String,
    @Json(name = "childs") val children: List<Structure>?,
    val capacity: Int
)

data class Message(
    val callId: Int,
    val msg: String,
    val args: Args? = null
) {
    override fun toString(): String = moshiDefault.adapter(Message::class.java).toJson(this)
}

data class Args(
    val storage: String? = null,
    val token: String? = null,
    val fileList: List<String>? = null,
    val currentPath: String? = null,
    val action: String? = null,
    val changes: List<List<String>>? = null,
    val user: User? = null,

    ) {
    override fun toString(): String = moshiDefault.adapter(Args::class.java).toJson(this)
}

data class User(val login: String, val password: String)
data class Error(val message: String?, val code: String?)
data class CallIdResult(val callId: Int)
data class MessageResult<T>(val result: T?, val error: Error?)