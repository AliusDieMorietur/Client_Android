package com.samurainomichi.cloud_storage_client

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

fun saveFileToStorage(buffer: ByteBuffer, name: String, stringUri: String, context: Context) {
    if (!isMediaAvailable())
        return

    val df = DocumentFile.fromTreeUri(context, Uri.parse(stringUri))

    val nameArray: List<String> = name.split(".")
    val mimeType: String = MimeTypeMap.getSingleton().getMimeTypeFromExtension(nameArray.last())!!
    val filename: String = nameArray.dropLast(1).joinToString("")
    val f = df?.createFile(mimeType, filename)!!

    val os: OutputStream = context.contentResolver.openOutputStream(f.uri)!!
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