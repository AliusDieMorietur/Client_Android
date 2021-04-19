package com.samurainomichi.cloud_storage_client.util
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object StorageName {
    const val tmp = "tmp"
    const val pmt = "pmt"
}

val moshiDefault: Moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()