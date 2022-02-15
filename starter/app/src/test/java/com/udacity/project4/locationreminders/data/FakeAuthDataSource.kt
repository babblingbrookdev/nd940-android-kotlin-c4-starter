package com.udacity.project4.locationreminders.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.locationreminders.data.auth.AuthLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAuthDataSource : AuthDataSource {

    private val auth = MutableLiveData<Boolean>(true)
    override fun getAuthState(): LiveData<Boolean> {
       return auth
    }
}