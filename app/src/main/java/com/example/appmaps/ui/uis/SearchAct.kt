package com.example.appmaps.ui.uis

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActSearchBinding
import com.example.appmaps.ui.models.Booking
import com.example.appmaps.ui.utils_provider.BookingProvider
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_code.GeoProvider
import com.example.appmaps.ui.utils_code.ReutiliceCode
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener
import java.util.Date

class SearchAct : AppCompatActivity() {

    // Views
    lateinit var bindSearch: ActSearchBinding

    // Vars
    private var originExtra: String? = null
    private var destinationExtra: String? = null

    private var originExtraLat: Double? = null
    private var originExtraLng: Double? = null

    private var destExtraLatLat: Double? = null
    private var destExtraLatLng: Double? = null

    private var getDistExtra: Double? = null
    private var getTimeExtra: Double? = null

    private var originLat: LatLng? = null
    private var destinationLng: LatLng? = null

    // -------->

    // Objects
    lateinit var bundle: Bundle
    private val geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private var listenerBooking: ListenerRegistration? = null

    private var radius = 10.0
    private var idDriver = ""
    private var isDriverFound = false
    private var driverLoc: LatLng? = null
    private val limitRadius = 20

    private val bookingProvider = BookingProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bindSearch = ActSearchBinding.inflate(layoutInflater)
        setContentView(bindSearch.root)

        var getExtrasX = intent.extras

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (getExtrasX != null) {
            getExtrasAct(getExtrasX)
        }
    }

    private fun getExtrasAct(bundle: Bundle) {
        val ifIsNull = 0.0

        originExtra = bundle.getString("origin")
        destinationExtra = bundle.getString("destination")

        originExtraLat = bundle.getDouble("origin_lat", 0.0)
        originExtraLng = bundle.getDouble("origin_lng", 0.0)

        destExtraLatLat = bundle.getDouble("destination_lat", 0.0)
        destExtraLatLng = bundle.getDouble("destination_lng", 0.0)

        getDistExtra = bundle.getDouble("distance_trip", 0.0)
        getTimeExtra = bundle.getDouble("time_trip", 0.0)

        originLat = LatLng(originExtraLat?: ifIsNull, originExtraLng?: ifIsNull)
        destinationLng = LatLng(destExtraLatLat?: ifIsNull, destExtraLatLng?: ifIsNull)

        Log.d("LG_ROUTE ->", "$originLat")
        Log.d("LG_ROUTE ->", "$destinationLng")

        getClosesDriver()
        checkIfDriverAccept()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getClosesDriver() {

        if (originLat == null) return

            geoProvider.getNearbyDrivers(originLat!!, 10.0)
                .addGeoQueryEventListener(object: GeoQueryEventListener {

                    // Start search of driver
                    override fun onKeyEntered(documentID: String, location: GeoPoint) {
                        if (!isDriverFound) {
                            isDriverFound = true
                            idDriver = documentID
                            driverLoc = LatLng(location.latitude, location.longitude)
                            bindSearch.tvSrhDriver.text = "Conductor encontrado\nEsperando respuesta..."
                            createBooking(documentID)
                        }
                        Log.d("DG_NEARBY", "onKeyEntered -> $idDriver")
                    }

                    override fun onGeoQueryError(exception: Exception) {
                        Log.d("DG_NEARBY", "onGeoQueryError")
                    }

                    // Finish search of driver
                    override fun onGeoQueryReady() {
                        if (!isDriverFound) {
                            radius += radius
                            if (radius > limitRadius) {
                                bindSearch.tvSrhDriver.text = "No se encontro ningun conductor"
                                return
                            }else{
                                getClosesDriver()
                            }
                        }
                        Log.d("DG_NEARBY", "onGeoQueryReady")
                    }

                    override fun onKeyExited(documentID: String) {
                        Log.d("DG_NEARBY", "onKeyExited")
                    }

                    override fun onKeyMoved(documentID: String, location: GeoPoint) {
                        Log.d("DG_NEARBY", "onKeyMoved")
                    }
                })
    }

    private fun createBooking(idDriver: String) {

        val booking = Booking(
            id = null,
            idClient = authProvider.getIdFrb(),
            idDriver = idDriver,
            status = "create",
            destination = destinationExtra,
            origin = originExtra,
            time = getTimeExtra,
            km = getDistExtra,
            originLat = originExtraLat,
            originLng = originExtraLng,
            destinationLat = destExtraLatLat,
            destinationLng = destExtraLatLng,
            price = 100.0,
            timeStamp = Date().time
        )

        bookingProvider.createBookingTrip(booking).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                ReutiliceCode.msgToast(this, "Datos del viaje creado", false)
            }else{
                ReutiliceCode.msgToast(this, "Datos del viaje no creado", false)
            }
        }
    }

    // Check if driver accept trip
    private fun checkIfDriverAccept() {

        listenerBooking = bookingProvider.getBooking().addSnapshotListener { snapshot, error ->
            val getError = error?.message
            if (getError != null) {
                Log.d("DG_ACCEPT", "error -> $getError")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val book = snapshot.toObject(Booking::class.java)

                when(book?.status) {
                    "accept" -> {
                        listenerBooking?.remove()
                        goToMapTripAccept(MapTripAcceptAct::class.java, 0)
                        ReutiliceCode.msgToast(this,"Viaje aceptado",true)
                    }
                    "cancel" -> {
                        listenerBooking?.remove()
                        goToMapTripAccept(MapsAct::class.java, 1)
                        ReutiliceCode.msgToast(this, "Viaje cancelado!", true)
                    }
                }
            }
        }
    }

    private fun goToMapTripAccept(navCls: Class<*>, type: Int) {

        val intent = Intent(this, navCls)
        when(type) {
            0 -> {

            }
            1 -> {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerBooking?.remove()
    }
}