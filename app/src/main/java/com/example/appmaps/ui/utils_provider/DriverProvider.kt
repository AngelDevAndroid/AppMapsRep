package com.example.appmaps.ui.utils_provider

import android.net.Uri
import android.util.Log
import com.example.appmaps.ui.models.ClientModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class DriverProvider {

    val db = Firebase.firestore.collection("Clients")

    // Get data driver
    val dbDrivers = Firebase.firestore.collection("Drivers")

    var storageRef = FirebaseStorage.getInstance().getReference().child("ImgProfileClients")

    fun createUser(clientModel: ClientModel): Task<Void>{
        return db.document(clientModel.idUser?: "").set(clientModel)
    }

    // Get doc driver
    fun getDataDriver(idDriver: String): Task<DocumentSnapshot> {
        return dbDrivers.document(idDriver).get()
    }

    // Update data driver
    fun updateInfoClient(client: ClientModel): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
        map["nameUser"] = client.nameUser?: ""
        map["imgUser"] = client.imgUser?: ""
        map["numUser"] = client.numUser?: ""
        map["emailUser"] = client.emailUser?: ""
        map["passwUser"] = client.passwUser?: ""

        return db.document(client.idUser.toString())
            .update(map).addOnFailureListener { failure ->
                Log.d("LG_FIRESTORE", "ERROR -> ${failure.message}")
            }
    }

    fun uploadFileStorage(idClient: String, filePath: File): StorageTask<UploadTask.TaskSnapshot> {

        val file = Uri.fromFile(filePath)

        val riversRef = storageRef.child("${idClient}.jpg")
        storageRef = riversRef // Reacigne to access to child img
        val uploadTask = riversRef.putFile(file)

        return uploadTask.addOnFailureListener { failure ->
            Log.d("LG_STORAGE", "ERROR -> ${failure.message}")
        }
    }

    fun getImageUrlStorage(): Task<Uri> {
        return storageRef.downloadUrl
            .addOnFailureListener { error ->
                Log.e("LG_STORAGE", "Error al obtener la URL", error)
            }
    }
}