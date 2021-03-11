package com.samurainomichi.cloud_storage_client

import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

fun saveFilesToStorage(buffers: List<ByteBuffer>, names: List<String>) {
    if(!isMediaAvailable())
        return

    if(true) {
        val downloadsFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.i("qwerq", downloadsFile.path)
        var f: File
        for(i in buffers.indices) {
            
            f = File(downloadsFile, names[i])
            val fc: FileChannel = FileOutputStream(f).channel
            fc.write(buffers[i])
            fc.close()
        }
    }
}

private fun isMediaAvailable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state
}