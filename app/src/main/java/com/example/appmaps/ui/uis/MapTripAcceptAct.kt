package com.example.appmaps.ui.uis

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActMapTripAcceptBinding
import com.example.appmaps.ui.models.Booking
import com.example.appmaps.ui.models.GeoPointModel
import com.example.appmaps.ui.utils_provider.BookingProvider
import com.example.appmaps.ui.utils_code.CarMoveAnim
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_code.GeoProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class MapTripAcceptAct : AppCompatActivity(),
                         OnMapReadyCallback,
                         Listener,
                         View.OnClickListener,
                         DirectionUtil.DirectionCallBack {

    // View
    private lateinit var bindMapsAccept: ActMapTripAcceptBinding
    //private val modalBooking = FmtRequestTripInf()

    // Objects
    private var gMap: GoogleMap? = null
    lateinit var ewlLocation: EasyWayLocation
    private var setCoordLocation: com.google.android.gms.maps.model.LatLng? = null

    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()

    private var listenBook: ListenerRegistration? = null
    private var listenDrivLoc: ListenerRegistration? = null

    private var posOriginClient: LatLng? = null
    private var posDestinClient: LatLng? = null

    private var driverLoc: LatLng? = null
    private var endLatLng: LatLng? = null

    private lateinit var directionUtil: DirectionUtil
    private var book: Booking? = null

    private var markerLocDriver: Marker? = null
    private var markerOriginClient: Marker? = null
    private var markerDestinateClient: Marker? = null

    // Vars
    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"

    private var isDrivLocFound = false
    private var isBookingLoader = false


    //private lateinit var currentLocation: Location
    //private lateinit var flProviderLocation: FusedLocationProviderClient
    private val permissionCode = 101
    val ifIsNull = LatLng(0.0, 0.0)
    var msgStatusTrip = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        bindMapsAccept = ActMapTripAcceptBinding.inflate(layoutInflater)
        setContentView(bindMapsAccept.root)

        initObjects()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fmt_cont_trip_accept) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        ewlLocation = EasyWayLocation(this, locRequest, false, false, this)
        locPermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun initObjects() {
        //bindMapsAccept.btnStart.setOnClickListener(this)
        //bindMapsAccept.btnFinish.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        /*when(v?.id) {
            R.id.btn_start -> {
                connectDriver()
            }
            R.id.btn_finish -> {
                disconnectDriver()
            }
        }*/
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap?.uiSettings?.isZoomControlsEnabled = true
        //gMap?.isMyLocationEnabled = false

        getBooking()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        when(requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    val locPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        //ewlLocation.startLocation()
                        Log.d("TAG_PERMS", "Permiso aceptado")
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        //ewlLocation.startLocation()
                        Log.d("TAG_PERMS", "Permiso aceptado con limitaciòn")
                    }
                    else -> {
                        Log.d("TAG_PERMS", "Permiso no aceptado")
                    }
                }
            }
        }

    override fun locationOn() {
        TODO("Not yet implemented")
    }

    override fun currentLocation(location: Location) {

    }

    // Draw route and add marker client
    private fun getBooking() {

        // addSnapshotListener -> se ejecuta siempre realtime, se actualiza
        listenBook = bookProvider.getBooking().addSnapshotListener { document, error ->
            if (error?.message != null) {
                Log.d("LG_LISTEN", "${error.message}")
                return@addSnapshotListener
            }

            book = document?.toObject(Booking::class.java)

            if (!isBookingLoader) {
                isBookingLoader = true

                posOriginClient = LatLng(book?.originLat?: 0.0,book?.originLng?: 0.0)
                posDestinClient = LatLng(book?.destinationLat?: 0.0,book?.destinationLng?: 0.0)
                Log.d("TAG_CRD", "${posOriginClient?.latitude}")

                gMap?.moveCamera(
                    CameraUpdateFactory
                        .newCameraPosition(CameraPosition.builder().target(posOriginClient!!).zoom(17f).build()))

                getLocDriver()
                addOriginMarkerClient(originLatLngX = posOriginClient?: ifIsNull)
            }

            when (book?.status) {
                "accept" -> {
                    msgStatusTrip = "Aceptado"
                }
                "started" -> {
                    msgStatusTrip = "Iniciado"
                    startedTrip()
                }
                "finished" -> {
                    msgStatusTrip = "Finalizado"
                    finishedTrip(RatingDriverAct::class.java)
                }
            }

            bindMapsAccept.tvStatusTrip.text = "Estatus del viaje: $msgStatusTrip"
        }
    }

    private fun startedTrip() {
        //addMarkerDriver(driverLoc?: ifIsNull)
        markerOriginClient?.remove()
        addDestinMarkerClient()
        easyDrawRoute(driverLoc?: ifIsNull,posDestinClient?: ifIsNull)
    }

    private fun finishedTrip(navCls: Class<*>) {
        listenDrivLoc?.remove()
        val intent = Intent(this, navCls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getLocDriver() {

        listenDrivLoc = geoProvider.getLocWorking(book?.idDriver?: "")
            .addSnapshotListener { document, error ->
            if (error?.message != null) {
                Log.d("LG_LISTEN", "${error.message}")
                return@addSnapshotListener
            }

            if (document?.exists()!!) {
                val getCoord = document.get("l")
                val gson = Gson()
                val getJsonLoc: String = gson.toJson(getCoord)

                val builder = GsonBuilder().setPrettyPrinting().create()
                val getPosition = builder.fromJson(getJsonLoc, GeoPointModel::class.java)

                Log.d("LG_LISTEN", "$getPosition")

                val lat = getPosition.latitude
                val lng = getPosition.longitude

                driverLoc = LatLng(lat, lng)
                endLatLng = driverLoc
                //markerLocDriver?.remove()

                if (!isDrivLocFound) {
                    isDrivLocFound = true
                    addMarkerDriver(driverLoc?: ifIsNull)
                    easyDrawRoute(driverLoc?: ifIsNull,posDestinClient?: ifIsNull)
                }

                if (endLatLng != null) {
                    CarMoveAnim.carAnim(markerLocDriver!!,endLatLng?: ifIsNull,driverLoc?: ifIsNull)
                }
            }
        }
    }

    private fun addMarkerDriver(originLatLngX: LatLng) {
        markerLocDriver = gMap?.addMarker(MarkerOptions()
            .position(originLatLngX)
            .title("Tu conductor")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_uber_car)))
    }

    private fun addOriginMarkerClient(originLatLngX: LatLng) {
        markerOriginClient = gMap?.addMarker(MarkerOptions()
            .position(originLatLngX)
            .title("Aquì estoy")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinMarkerClient() {
        markerDestinateClient = gMap?.addMarker(MarkerOptions()
            .position(posDestinClient?: ifIsNull)
            .title("Ir aquì")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_marker)))
    }

    private fun easyDrawRoute(originLatLng: LatLng, destinLatLng: LatLng) {

        //wayPoints.clear()
        wayPoints.add(originLatLng?: ifIsNull)
        wayPoints.add(destinLatLng)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng?: ifIsNull)
            .setWayPoints(wayPoints)
            .setGoogleMap(gMap!!)
            .setPolyLinePrimaryColor(R.color.green_route)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinLatLng)
            .build()

        directionUtil.initPath()
    }

    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = createBitmap(70, 150)

        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,100,100)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)

    }

    override fun locationCancelled() {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        //ewlLocation?.startLocation()
    }

    private fun saveLocation() {
        if (setCoordLocation != null) {
            geoProvider.saveLocationWorking(authProvider.getIdFrb(), setCoordLocation!!)
        }
    }

    private fun checkIfDriverIsConnected() {
        geoProvider.getLocIsConnected(authProvider.getIdFrb()).addOnSuccessListener { document ->
            if (document.exists() && document.contains("g")) {
                connectDriver()
            }else{
                disconnectDriver()
            }
        }
    }

    private fun disconnectDriver() {
        ewlLocation.endUpdates()
        if (setCoordLocation != null) {
            //geoProvider.removeLocationOnly(authProvider.getIdFrb())
            geoProvider.delCollLocationAllTree(authProvider.getIdFrb())
            //showBtnConnect()
        }
    }

    // Connect loc current handly
    private fun connectDriver() {
        ewlLocation.endUpdates()
        ewlLocation.startLocation()
        //showBtnDisconnect()
    }

    override fun onPause() {
        super.onPause()
        ewlLocation.endUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation.endUpdates()
        listenBook?.remove()
        listenDrivLoc?.remove()
        //geoProvider.removeLocationOnly(authProvider.getIdFrb())
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)
    }
}