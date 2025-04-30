package com.example.appmaps.ui.utils_provider

import android.content.Context
import android.util.Log
import com.example.appmaps.ui.utils_code.ReuseCode
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.runBlocking

class PushNotifProvider {

    var fcm =  FirebaseMessaging.getInstance()

    fun getTokenFcm() {
        fcm.token.addOnCompleteListener { token ->
            if (token.isSuccessful) {
                val tokenX = token.result
                ReuseCode.getTokenUtils = tokenX
            }
        }.addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "error -> ${error.message}")
        }
    }
}