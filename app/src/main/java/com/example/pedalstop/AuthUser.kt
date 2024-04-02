package com.example.pedalstop

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Abstract representation of a User
data class User (private val nullableName: String?,
                 private val nullableEmail: String?,
                 val uid: String) {
    val name: String = nullableName ?: "User logged out"
    val email: String = nullableEmail ?: "User logged out"
}

const val invalidUserUid = "-1"
val invalidUser = User(null, null, invalidUserUid)

fun User.isInvalid(): Boolean {
    return uid == invalidUserUid
}

class AuthUser(private val registry: ActivityResultRegistry) :
    DefaultLifecycleObserver,
    FirebaseAuth.AuthStateListener {

    companion object { private const val TAG = "AuthUser" }

    private var liveUser = MutableLiveData<User>().apply { this.postValue(invalidUser) }
    private var pendingLogin = false
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    fun observeUser(): LiveData<User> { return liveUser }
    private fun user(): FirebaseUser? { return Firebase.auth.currentUser }

    // Listen to FirebaseAuth state to change views on login and logout
    init { Firebase.auth.addAuthStateListener(this) }
    override fun onAuthStateChanged(p0: FirebaseAuth) {
        Log.d(TAG, "onAuthStateChanged | user is null? ${p0.currentUser == null}")
        val firebaseUser = p0.currentUser
        if (firebaseUser == null) {
            Log.d(TAG, "onAuthStateChanged | login")
            liveUser.postValue(invalidUser)
            login()
        } else {
            val user = User(firebaseUser.displayName, firebaseUser.email, firebaseUser.uid)
            liveUser.postValue(user)
        }
    }

    // Use provided Firebase login UI
    private fun login() {
        if (user() == null && !pendingLogin) {
            Log.d(TAG, "login | user null")
            pendingLogin = true
            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }
    fun logout() {
        if(user() == null) { return }
        Firebase.auth.signOut()
    }

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = registry.register(
            "key",
            owner,
            FirebaseAuthUIActivityResultContract()
        ) { result ->
            Log.d(TAG, "onCreate | sign in result ${result.resultCode}")
            pendingLogin = false
        }
    }
}