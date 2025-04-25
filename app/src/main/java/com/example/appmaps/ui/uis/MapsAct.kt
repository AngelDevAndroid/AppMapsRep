package com.example.appmaps.ui.uis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActivityMapsBinding
import com.example.appmaps.ui.models.Booking
import com.example.appmaps.ui.models.DriverLocationModel
import com.example.appmaps.ui.utils_provider.BookingProvider
import com.example.appmaps.ui.utils_code.CarMoveAnim
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_code.GeoProvider
import com.example.appmaps.ui.utils_code.ReutiliceCode
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener

class MapsAct : AppCompatActivity(), OnMapReadyCallback, Listener, View.OnClickListener {

    // View
    private lateinit var bindMapsDriver: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var sessionToken: AutocompleteSessionToken

    // Objects
    private var gMap: GoogleMap? = null
    private var ewlLocation: EasyWayLocation? = null
    private var myCoordLocation: com.google.android.gms.maps.model.LatLng? = null
    private var markerDriver: Marker? = null
    private var geoProvider = GeoProvider()

    // Vars
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private var isLocEnabled = false
    private var lstDriverMarkers = ArrayList<Marker>()
    private var lstDrivLocations = ArrayList<DriverLocationModel>()

    // Objects places
    private var places: PlacesClient? = null
    private var autoCompOrig: AutocompleteSupportFragment? = null
    private var autoCompDest: AutocompleteSupportFragment? = null
    private var originName: String? = null
    private var destName: String? = null

    private var originLatLng: LatLng? = null
    private var destLatLng: LatLng? = null


    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        bindMapsDriver = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(bindMapsDriver.root)

        initObjects()
        //Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.google_maps_key))

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fmt_map_client) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        initLocCurrent()
        startGooglePlacesWithApiPlaces()
        checkToRemoveBooking()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initObjects() {
        bindMapsDriver.btnRequestTrip.setOnClickListener(this)
    }

    val locPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("TAG_PERMS", "Permiso aceptado")
                        ewlLocation?.startLocation()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d("TAG_PERMS", "Permiso aceptado con limitaciòn")
                        ewlLocation?.startLocation()
                    }
                    else -> {
                        Log.d("TAG_PERMS", "Permiso no aceptado")
                    }
                }
            }
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap?.uiSettings?.isZoomControlsEnabled = true
        onCameraMoveToSetLoc()
        gMap?.isMyLocationEnabled = false // true to show point blue my loc
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        when(requestCode){
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    private fun initLocCurrent() {
        val locRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        ewlLocation = EasyWayLocation(this, locRequest, false, false, this)
    }

    private fun getPositionDriver(id: String): Int {
        var position = 0
        for (i in lstDrivLocations.indices) {
            if (id == lstDrivLocations[i].id) {
                position = i
                break
            }
        }
        return position
    }

    // Manage drivers nearby
    private fun getNearbyDrivers() {

        if (myCoordLocation == null) return

        geoProvider.getNearbyDrivers(myCoordLocation!!, 10.0)
            .addGeoQueryEventListener(object: GeoQueryEventListener {

                override fun onKeyEntered(documentID: String, location: GeoPoint) {

                    Log.d("DG_GEO_QY ->", documentID.toString())

                    for (marker in lstDriverMarkers) {
                        if (marker.tag != null){
                            if (marker.tag == documentID) {
                                return
                            }
                        }
                    }

                    // create a new marker to the driver connected
                    val driverLatLng = LatLng(location.latitude, location.longitude)
                    val marker = gMap?.addMarker(
                        MarkerOptions().position(driverLatLng).title("Conductor disponible")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.uber_car))
                    )

                    marker?.tag = documentID
                    lstDriverMarkers.add(marker!!)
                    Log.d("DG_GEO_QY ->", lstDriverMarkers.toString())

                    val driverLoc = DriverLocationModel()
                    driverLoc.id = documentID
                    lstDrivLocations.add(driverLoc)
                }

                // Remove marker of driver disconnected
                override fun onKeyExited(documentID: String) {

                    for (maker in lstDriverMarkers) {
                        if (maker.tag != null) {
                            if (maker.tag == documentID) {
                                maker.remove()
                                lstDriverMarkers.remove(maker)
                                lstDrivLocations.removeAt(getPositionDriver(documentID))
                                return
                            }
                        }
                    }
                    Log.d("DG_GEO_QY ->", "onKeyExited")
                }

                // Each that driver be move listen and update
                override fun onKeyMoved(documentID: String, location: GeoPoint) {

                    val startDriv = LatLng(location.latitude, location.longitude)
                    var endDriv: LatLng? = null

                    for (marker in lstDriverMarkers) {

                        val getPosDriver = getPositionDriver(marker.tag.toString())

                        if (marker.tag != null) {
                            if (marker.tag == documentID) {
                                // Update position driver
                                marker.position = LatLng(location.latitude, location.longitude)

                                if (lstDrivLocations[getPosDriver].posCoord != null) {
                                    endDriv = lstDrivLocations[getPosDriver].posCoord
                                }
                                lstDrivLocations[getPosDriver].posCoord = LatLng(location.latitude, location.longitude)

                                if (endDriv != null) {
                                    CarMoveAnim.carAnim(marker, endDriv, startDriv)
                                }
                            }
                        }
                    }
                    Log.d("DG_GEO_QY", "onKeyMoved")
                }

                override fun onGeoQueryError(exception: Exception) {
                         Log.d("DG_GEO_QY", "onGeoQueryError")
                }

                override fun onGeoQueryReady() {
                         Log.d("DG_GEO_QY", "onGeoQueryReady $myCoordLocation")
                }
            })
    }

    private fun onCameraMoveToSetLoc() {

        gMap?.setOnCameraMoveListener {
            try {
                val geocoder = Geocoder(this)
                originLatLng = gMap?.cameraPosition?.target
                val addressList = geocoder
                    .getFromLocation(originLatLng?.latitude?: 0.0, originLatLng?.longitude?: 0.0, 1)

                if (addressList?.isNotEmpty() == true) {

                    val city = addressList[0]?.locality
                    val country = addressList[0]?.countryName
                    val address = addressList[0]?.getAddressLine(0)

                    originName = "$address $city"
                    autoCompOrig?.setText(originName)
                }else{
                    ReutiliceCode.msgToast(this, "No hay ubicaciòn disponible!", true)
                }

            }catch (e: Exception) {
                Log.d("DG_EX", e.message.toString())
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_request_trip -> {
                if (checkETEmpty()) {
                    passDataActivity()
                }else{
                    ReutiliceCode.msgToast(this, "Debe ingresar origen y destino!", true)
                }
            }
        }
    }

    private fun checkETEmpty(): Boolean {
        return originName?.isNotEmpty()?: false && destName?.isNotEmpty()?: false
    }

    private fun navIntent(navCls: Class<*>) {
        startActivity(Intent(this, navCls))
    }

    private fun passDataActivity() {
        val intent = Intent(this, TripInfoAct::class.java) // TripInfoAct
        intent.putExtra("origin", originName)
        intent.putExtra("destination", destName)
        intent.putExtra("origin_lat", originLatLng?.latitude)
        intent.putExtra("origin_lng", originLatLng?.longitude)
        intent.putExtra("destination_lat", destLatLng?.latitude)
        intent.putExtra("destination_lng", destLatLng?.longitude)
        startActivity(intent)
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) {
        myCoordLocation =
            com.google.android.gms.maps.model.LatLng(location.latitude, location.longitude)

            if (!isLocEnabled) {
                isLocEnabled = true
                gMap?.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(CameraPosition.builder().target(myCoordLocation!!).zoom(15f).build()))

                getNearbyDrivers()
                limitSearch()
            }
    }

    private fun startGooglePlacesWithApiPlaces() {

        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.google_maps_key))
        }

        places = Places.createClient(this)
        instanceAutoCompleteOrigin()
        instanceAutoCompleteDestinate()
    }

    private fun limitSearch() {

        val northSide = SphericalUtil.computeOffset(myCoordLocation, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myCoordLocation, 5000.0, 180.0)

        autoCompOrig?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        autoCompDest?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
    }

    private fun instanceAutoCompleteOrigin() {
        autoCompOrig = supportFragmentManager
            .findFragmentById(R.id.fmt_places_auto_comp_orig) as AutocompleteSupportFragment

        autoCompOrig?.setPlaceFields(
            listOf(Place.Field.ID,
                   Place.Field.NAME,
                   Place.Field.LAT_LNG,
                   Place.Field.ADDRESS)
        )

        autoCompOrig?.setHint("De")
        autoCompOrig?.setCountry("MX")
        autoCompOrig?.setOnPlaceSelectedListener(object: PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                originName = place.name
                originLatLng = place.latLng

                Log.d("LG_PLACES ->", "$originName")
                Log.d("LG_PLACES", "${originLatLng?.latitude}")
            }

            override fun onError(status: Status) {
                Log.d("LG_PLACES", "${status.statusMessage}")
            }
        })
    }

    private fun instanceAutoCompleteDestinate() {
        autoCompDest = supportFragmentManager
            .findFragmentById(R.id.fmt_places_auto_comp_dest) as AutocompleteSupportFragment

        autoCompDest?.setPlaceFields(
            listOf(Place.Field.ID,
                   Place.Field.NAME,
                   Place.Field.LAT_LNG,
                   Place.Field.ADDRESS)
        )

        autoCompDest?.setHint("Ir a...")
        autoCompDest?.setCountry("MX")
        autoCompDest?.setOnPlaceSelectedListener(object: PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                destName = place.name
                destLatLng = place.latLng

                Log.d("LG_PLACES", "$destName")
                Log.d("LG_PLACES", "${destLatLng?.latitude}")
            }

            override fun onError(status: Status) {
                Log.d("LG_PLACES", "${status.statusMessage}")
            }
        })
    }

    // If the status is accept not delete book
    private fun checkToRemoveBooking() {
        bookProvider.getBooking().get().addOnSuccessListener { document ->
            if (document.exists()) {
                val book = document.toObject(Booking::class.java)
                if (book?.status.equals("create") or book?.status.equals("cancel")) {
                    bookProvider.removeBook()
                }
            }
         }
    }

    override fun locationCancelled() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        ewlLocation?.startLocation()
    }

    override fun onPause() {
        super.onPause()
        ewlLocation?.endUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation?.endUpdates()
        //geoProvider.removeLocationOnly(authProvider.getIdFrb())
    }
}