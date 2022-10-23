package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthenticationViewModel(app: Application) : ViewModel() {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}