package com.udacity.project4.utils

import android.Manifest.permission.*
import android.os.Build
import androidx.annotation.RequiresApi

sealed class Permission(vararg val permissions: String) {
    // Individual permissions
    @RequiresApi(Build.VERSION_CODES.Q)
    object BackgroundLocation : Permission(ACCESS_BACKGROUND_LOCATION)
    // Grouped permissions
    object ForegroundLocation : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)


    companion object {
        fun from(permission: String) = when (permission) {
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> ForegroundLocation
            ACCESS_BACKGROUND_LOCATION -> BackgroundLocation
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}