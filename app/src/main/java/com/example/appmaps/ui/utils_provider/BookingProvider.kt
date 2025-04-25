package com.example.appmaps.ui.utils_provider

import android.util.Log
import com.example.appmaps.ui.models.Booking
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Provider
class BookingProvider {

    val db = Firebase.firestore.collection("Bookings")
    val authProvider = FrbAuthProviders()

    // Create data trip frbs
    fun createBookingTrip(booking: Booking): Task<Void> {
        return db.document(authProvider.getIdFrb()).set(booking).addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }

    // Get trip data frbs
    fun getBooking(): DocumentReference {
        return db.document(authProvider.getIdFrb())
    }

    // Remove data trip frbs
    fun removeBook(): Task<Void> {
        return db.document(authProvider.getIdFrb()).delete().addOnFailureListener { exp ->
            Log.d("LG_FIRESTORE", "ERROR -> ${exp.message}")
        }
    }
}