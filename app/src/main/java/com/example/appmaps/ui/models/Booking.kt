package com.example.appmaps.ui.models

// Model
data class Booking(
    var id: String? = null,
    var idClient: String? = null,
    var idDriver: String? = null,
    var origin: String? = null,
    var destination: String? = null,
    var status: String? = null,
    var time: Double? = null,
    var km: Double? = null,
    var originLat: Double? = null,
    var originLng: Double? = null,
    var destinationLat: Double? = null,
    var destinationLng: Double? = null,
    var price: Double? = null
)
