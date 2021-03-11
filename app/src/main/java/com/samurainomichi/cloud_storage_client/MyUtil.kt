package com.samurainomichi.cloud_storage_client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

fun checkPermission(context: Context, permission: String): Boolean =
        (ContextCompat.checkSelfPermission(
            context,
            permission) == PackageManager.PERMISSION_GRANTED)

fun openDirectory(context: Context) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    context.startActivity(intent)
}