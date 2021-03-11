package com.samurainomichi.cloud_storage_client

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStream
import java.nio.ByteBuffer

fun saveFilesToStorage(buffers: List<ByteBuffer>, names: List<String>, stringUri: String, context: Context) {
    if (!isMediaAvailable())
        return

    val df = DocumentFile.fromTreeUri(context, Uri.parse(stringUri))

    var namearray: List<String>
    var mimetype: String
    var filename: String
    for (i in buffers.indices) {

        namearray = names[i].split(".")
        mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(namearray.last())!!
        filename = namearray.dropLast(1).joinToString("")
        val f = df?.createFile(mimetype, filename)!!

        val os: OutputStream = context.contentResolver.openOutputStream(f.uri)!!
        os.write(buffers[i].array())
        os.close()
    }
}

private fun isMediaAvailable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state
}