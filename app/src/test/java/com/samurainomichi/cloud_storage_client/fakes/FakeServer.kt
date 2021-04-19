package com.samurainomichi.cloud_storage_client.fakes

import com.samurainomichi.cloud_storage_client.model.Error
import com.samurainomichi.cloud_storage_client.model.Message
import com.samurainomichi.cloud_storage_client.util.moshiDefault

class FakeServer() {
    companion object {
        private val adapter = moshiDefault.adapter(Message::class.java)!!

        private fun formResult(callId: Int, res: String) = "{" +
            "\"callId\": \"$callId\"," +
            "\"result\": $res}"

        private fun formError(callId: Int, error: Error? = null) = "{" +
            "\"callId\": \"$callId\"," +
            "\"error\": ${moshiDefault.adapter(Error::class.java).toJson(error)}}"

        fun getResult(msg: String): String {
            val message = adapter.fromJson(msg)!!

            var answer = ""
            when (message.msg) {
                "getInt" -> answer = formResult(message.callId, "13")
                "getList" -> answer = formResult(message.callId, "[\"one\", \"two\"]")
                "getError" -> answer = formError(message.callId, Error("test error", "1"))
            }
            return answer
        }
    }
}