package com.example.appmaps.ui.models

import com.google.android.gms.maps.model.LatLng

// Model
data class DriverLocationModel(
    var id: String? = null,
    var posCoord: LatLng? = null
)
