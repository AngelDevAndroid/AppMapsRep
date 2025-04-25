package com.example.appmaps.ui.utils_provider

import android.util.Log

import com.example.appmaps.ui.models.HistoryTrip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Provider
class HistoryProvider {

    val db = Firebase.firestore.collection("Histories")
    val authProvider = FrbAuthProviders()

    fun create(history: HistoryTrip): Task<DocumentReference> {
        return db.add(history).addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }

    // Consulta compuesta , requiere collection indexed
    fun getLastHistory(): Query {
        return db.whereEqualTo("idClient", authProvider.getIdFrb())
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(1)
    }

    fun updateRatingDriver(idDocHist: String, rating: Float): Task<Void> {
        return db.document(idDocHist)
            .update("ratingToDriver", rating).addOnFailureListener { failure ->
                Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
            }
    }

    // Update status trip driver booking
    fun updateStatus(idClient: String, status: String): Task<Void> {
        return db.document(idClient)
                 .update("status", status).addOnFailureListener { failure ->
            Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
        }
    }
}