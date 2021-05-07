package com.samurainomichi.cloud_storage_client

import android.annotation.TargetApi
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

fun saveFileToStorage(buffer: ByteBuffer, fullName: String, context: Context) {
    val nameArray: List<String> = fullName.split(".")
    val mimeType: String = MimeTypeMap.getSingleton().getMimeTypeFromExtension(nameArray.last())!!
    val filename: String = nameArray.dropLast(1).joinToString("")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        saveFileQ(buffer, filename, mimeType, context)
    }
    else
        saveFileLegacy(buffer, filename, mimeType)
}

@TargetApi(29)
fun saveFileQ(buffer: ByteBuffer, name: String, mimeType: String, context: Context) {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, name)
        put(MediaStore.Downloads.MIME_TYPE, mimeType)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        val os: OutputStream = context.contentResolver.openOutputStream(it)!!
        os.write(buffer.array())
        os.close()

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(it, values, null, null)
    } ?: throw RuntimeException("MediaStore failed for some reason")
}

@Suppress("DEPRECATION")
fun saveFileLegacy(buffer: ByteBuffer, name: String, mimeType: String) {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "$name.$mimeType"
    )
    val os: OutputStream = file.outputStream()
    os.write(buffer.array())
    os.close()
}

fun readFileFromStorage(uri: Uri, context: Context): ByteBuffer {
    var buffer: ByteBuffer? = null

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        buffer = ByteBuffer.wrap(inputStream.readBytes())
    }

    if(buffer == null) {
        throw IOException("Couldn't read file or it's name")
    }

    return buffer!!
}

fun readFileNames(uriList: List<Uri>, context: Context): List<String> {
    val names = mutableListOf<String>()

    for (uri in uriList) {
        context.contentResolver.query(uri, null, null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            names.add(cursor.getString(nameIndex))
        }
    }

    return names
}


private fun isMediaAvailable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state
}