package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData

interface AuthDataSource {
    fun getAuthState(): LiveData<Boolean>
}