package com.udacity.project4.locationreminders.data.auth

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.data.AuthDataSource

class AuthRepository : AuthDataSource {
    private val auth = FirebaseAuth.getInstance()

    override fun getAuthState(): LiveData<Boolean> {
        return AuthLiveData(auth)
    }
}