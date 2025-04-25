package com.example.appmaps.ui.utils_provider

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class FrbAuthProviders() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register new client
    fun registerUser(email: String, password: String): Task<AuthResult>{
        return auth.createUserWithEmailAndPassword(email, password)
    }

    // Login client
    fun loginUser(email: String, password: String): Task<AuthResult>{
        return auth.signInWithEmailAndPassword(email, password)
    }

    // Get id to the client auth
    fun getIdFrb(): String {
        return auth.currentUser?.uid ?: ""
    }
}