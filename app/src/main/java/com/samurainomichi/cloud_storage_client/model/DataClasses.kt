package com.samurainomichi.cloud_storage_client.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val nonstrict = Json {ignoreUnknownKeys = true}

@Serializable
data class Structure(
    val name: String,
    val children: List<Structure>? = null,
    val capacity: Int
)

@Serializable
data class Message(
    val callId: Int,
    val msg: String,
    val args: Args? = null
) {
//    override fun toString(): String = moshiDefault.adapter(Message::class.java).toJson(this)
    override fun toString(): String = Json.encodeToString(this)
}

@Serializable
data class Args(
    val storage: String? = null,
    val token: String? = null,
    val fileList: List<String>? = null,
    val currentPath: String? = null,
    val action: String? = null,
    val changes: List<List<String>>? = null,
    val user: User? = null,
    val name: String? = null,
    val newName: String? = null,
    ) {
//    override fun toString(): String = moshiDefault.adapter(Args::class.java).toJson(this)
    override fun toString(): String = Json.encodeToString(this)
}

@Serializable
data class User(val login: String, val password: String)
@Serializable
data class Error(val message: String?, val code: Int?)

@Serializable
data class CallIdResult(val callId: Int)
@Serializable
data class MessageResult<T>(val result: T?)
@Serializable
data class ErrorResult(val error: Error? = null)