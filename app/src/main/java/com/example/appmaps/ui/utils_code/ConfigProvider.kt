package com.example.appmaps.ui.utils_code

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.okhttp.internal.DiskLruCache.Snapshot

class ConfigProvider {

    val db = Firebase.firestore.collection("Config")

    fun getPrices(): Task<DocumentSnapshot> {
        return db.document("prices").get().addOnFailureListener { exp ->
            Log.d("FIREBASE", "Error -> ${exp.message}")
        }
    }
}