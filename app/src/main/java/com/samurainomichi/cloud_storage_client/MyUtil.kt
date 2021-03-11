package com.samurainomichi.cloud_storage_client

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun checkPermission(context: Context, permission: String): Boolean =
        (ContextCompat.checkSelfPermission(
            context,
            permission) == PackageManager.PERMISSION_GRANTED)

