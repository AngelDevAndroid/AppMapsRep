package com.example.appmaps.ui.uis.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.core.net.toUri
import com.example.appmaps.R
import com.example.appmaps.databinding.MenuTripAcceptFmtBinding
import com.example.appmaps.ui.models.Booking
import com.example.appmaps.ui.models.DriverModel
import com.example.appmaps.ui.utils_code.ReuseCode
import com.example.appmaps.ui.utils_provider.BookingProvider
import com.example.appmaps.ui.utils_provider.ClientProvider
import com.example.appmaps.ui.utils_provider.DriverProvider
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_provider.GeoProvider

class MenuTripAcceptFmt: BottomSheetDialogFragment(), View.OnClickListener {

    // Views
    private var _bindInf: MenuTripAcceptFmtBinding? = null
    private val bindInf get() = _bindInf!!

    // Objects
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private val driverProvider = DriverProvider()
    private val clientProvider = ClientProvider()

    private var booking: Booking? = null

    private var getData: Booking? = null
    private var checkCancel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         initObjects()
        _bindInf = MenuTripAcceptFmtBinding.inflate(inflater, container, false)
        return _bindInf?.root
    }

    companion object {
        const val TAG = "MENU_BSD"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initObjects() {
        //getData = arguments?.getParcelable<Booking>("object_booking")
        Log.d("LG_REQTRIP","$getData")
        getBookingTrip()
    }

    private fun initViews() {
        bindInf.ivPhoneClient.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ib_profile -> {

            }
        }
    }

    // Draw route and add marker client
    private fun getBookingTrip() {
        bookProvider.getBookingToTripModal().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.size() > 0) {
                    booking = query.documents[0].toObject(Booking::class.java)
                    getDataClientAndBook(
                        booking?.idDriver?: "",
                        booking?.origin?: "",
                        booking?.destination?: "")
                }else{
                    Log.d("LG_FIRESTORE", "ERROR -> ${query.size()}")
                }
            }else{
                Log.d("LG_FIRESTORE", "ERROR -> ${query}")
            }
        }.addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "ERROR -> ${error.message}")
        }
    }

    private fun getDataClientAndBook(idDriver: String, originTrip: String, destination: String) {
        driverProvider.getDataDriver(idDriver).addOnSuccessListener { document ->
             if (document.exists()) {
                 val driver = document.toObject(DriverModel::class.java)
                 bindInf.tvNameClient.text = driver?.nameUser
                 bindInf.tvOrigin.text = originTrip
                 bindInf.tvDestination.text = destination

                 bindInf.ivPhoneClient.setOnClickListener(this)
                 bindInf.ivPhoneClient.setOnClickListener {
                     openCallDial(driver?.numUser?: "")
                 }
             }
        }
    }

    // To call client
    private fun openCallDial(numClient: String) {
        if (numClient.isNotEmpty()) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = "tel:$numClient".toUri()
                startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 1)
            }
        }else{
            ReuseCode.msgToast(requireContext(), "Telèfono no disponible!", true)
        }
    }


    // If not accept trip it cancel in 30s only
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!checkCancel) {

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindInf = null  // Prevent memory leaks
    }
}