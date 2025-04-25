package com.example.appmaps.ui.uis

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActTripInfoBinding
import com.example.appmaps.ui.models.Prices
import com.example.appmaps.ui.utils_provider.ConfigProvider
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class TripInfoAct : AppCompatActivity(),
    OnMapReadyCallback, Listener, View.OnClickListener, DirectionUtil.DirectionCallBack  {

    // Views
    lateinit var bindTrip: ActTripInfoBinding
    lateinit var bundle: Bundle

    // Vars
    private var originExtra: String? = null
    private var destinationExtra: String? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var originExtraLat: Double? = null
    private var originExtraLng: Double? = null
    private var destExtraLatLat: Double? = null
    private var destExtraLatLng: Double? = null

    private var distanceExtra = 0.0
    private var timeExtra = 0.0

    private var wayPoints: ArrayList<LatLng> = arrayListOf()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestinate: Marker? = null

    private var confProvider = ConfigProvider()

    // Objects
    private var gMap: GoogleMap? = null
    private var ewlLocation: EasyWayLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        bindTrip = ActTripInfoBinding.inflate(layoutInflater)
        setContentView(bindTrip.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initMapAndLocCurrent()
    }

    private fun initViews() {
        setSupportActionBar(bindTrip.tbBack)
        bindTrip.tbBack.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindTrip.btnConfirmTrip.setOnClickListener(this)

        bundle = intent.extras!!

        val ifIsNull = 0.0

        originExtra = bundle.getString("origin")
        destinationExtra = bundle.getString("destination")

        originExtraLat = bundle.getDouble("origin_lat", 0.0)
        originExtraLng = bundle.getDouble("origin_lng", 0.0)

        destExtraLatLat = bundle.getDouble("destination_lat", 0.0)
        destExtraLatLng = bundle.getDouble("destination_lng", 0.0)

        originLatLng = LatLng(originExtraLat?: ifIsNull, originExtraLng?: ifIsNull)
        destinationLatLng = LatLng(destExtraLatLat?: ifIsNull, destExtraLatLng?: ifIsNull)

        bindTrip.tvOrigin.text = originExtra
        bindTrip.tvDestination.text = destinationExtra

        Log.d("LG_ROUTE", "$originLatLng")
        Log.d("LG_ROUTE", "$destinationLatLng")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // Handle back press
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initMapAndLocCurrent() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fmt_cont_trip) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        ewlLocation = EasyWayLocation(this, locRequest, false, false, this)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        gMap = map
        gMap?.uiSettings?.isZoomControlsEnabled = true

        gMap?.moveCamera(
            CameraUpdateFactory
                .newCameraPosition(CameraPosition.builder().target(originLatLng!!).zoom(14f).build()))

        gMap?.isMyLocationEnabled = true

        easyDrawRoute()
        addOriginMarker()
        addDestinationMarker()
    }

    private fun easyDrawRoute() {

        val ifIsNull = LatLng(0.0, 0.0)

        wayPoints.add(originLatLng?: ifIsNull)
        wayPoints.add(destinationLatLng?: ifIsNull)
        directUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng?: ifIsNull)
            .setWayPoints(wayPoints)
            .setGoogleMap(gMap!!)
            .setPolyLinePrimaryColor(R.color.green_route)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng?: ifIsNull)
            .build()

        directUtil.initPath()
    }

    private fun addOriginMarker() {
        markerOrigin = gMap?.addMarker(MarkerOptions()
            .position(originLatLng!!)
            .title("Recoger aqui")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location)))
    }

    private fun addDestinationMarker() {
        markerDestinate = gMap?.addMarker(MarkerOptions()
            .position(destinationLatLng!!)
            .title("Ir aquì")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_marker)))
    }

    // Get time and distance trip
    @SuppressLint("SetTextI18n")
    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directUtil.drawPath(WAY_POINT_TAG)

        distanceExtra = polyLineDetailsArray[1].time.toDouble() // Seconds
        timeExtra = polyLineDetailsArray[1].distance.toDouble() // Metros

        // Convert of meters to km and seconds to minutes
        timeExtra = if (timeExtra < 60.0) 60.0 else timeExtra
        distanceExtra = if (distanceExtra < 1000.0) 1000.0 else distanceExtra

        timeExtra /= 60 // MIN
        distanceExtra /= 1000 // KN

        val timeReduceDec = String.format("%.2f", timeExtra)
        val distanceReduceDec = String.format("%.2f", distanceExtra)

        getPrices(distanceExtra, timeExtra)
        bindTrip.tvTimeDistance.text = "$timeReduceDec mins - $distanceReduceDec km"
    }

    // Get price of trip
    private fun getPrices(distance: Double, time: Double) {
         confProvider.getPrices().addOnSuccessListener { doc ->
             if (doc.exists()) {
                 val prices = doc.toObject(Prices::class.java)?: Prices()

                 val totalDistance = (distance * prices.km!!) ?: 0.0
                 val totalTime = (time * prices.min!!)?: 0.0

                 var total = totalDistance + totalTime
                 total = if (total < 5.74) prices.minValue!! else total

                 var minTotal = total - prices.difference!!
                 var maxTotal = total + prices.difference

                 val minTotalDec = String.format("%.2f", minTotal)
                 val maxTotalDec = String.format("%.2f", maxTotal)

                 bindTrip.tvPriceTrip.text = "MXN $minTotalDec a $maxTotalDec Pesos"
             }
         }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_confirm_trip -> {
                passDataActivity()
                /*if (checkETEmpty()) {
                    passDataActivity()
                }else{
                    ReutiliceCode.msgToast(this, "Debe ingresar origen y destino!", true)
                }*/
            }
        }
    }

    private fun passDataActivity() {

        val intent = Intent(this, SearchAct::class.java)

        intent.putExtra("origin", originExtra)
        intent.putExtra("destination", destinationExtra)

        intent.putExtra("origin_lat", originExtraLat)
        intent.putExtra("origin_lng", originExtraLng)

        intent.putExtra("destination_lat", destExtraLatLat)
        intent.putExtra("destination_lng", destExtraLatLng)

        intent.putExtra("distance_trip", distanceExtra)
        intent.putExtra("time_trip", timeExtra)

        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ewlLocation?.endUpdates()
    }
}