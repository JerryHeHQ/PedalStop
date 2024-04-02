package com.example.pedalstop

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private var currentAuthUser = invalidUser
    fun setCurrentAuthUser(user: User) {
        currentAuthUser = user
    }
}