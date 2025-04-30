package com.example.appmaps.ui.uis.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appmaps.R
import com.example.appmaps.databinding.ShowDetailHistoryFmtBinding
import com.example.appmaps.ui.models.DriverModel
import com.example.appmaps.ui.models.HistoryTripModel
import com.example.appmaps.ui.utils_code.RelativeTime
import com.example.appmaps.ui.utils_code.ReuseCode
import com.example.appmaps.ui.utils_provider.DriverProvider
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_provider.HistoryProvider

class ShowDetailHistoryFmt : Fragment() {

    // Vars
    private var param1: String? = null
    private var param2: String? = null
    private var getArgId: String? = null

    // Objects
    private val authProvider = FrbAuthProviders()
    private val driverProvider = DriverProvider()

    private val histProvider = HistoryProvider()

    var driver: DriverModel? = null
    var history: HistoryTripModel? = null

    // View
    private var _bindDetHist: ShowDetailHistoryFmtBinding? = null
    private val bindDetHist get() = _bindDetHist!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getArgId = arguments?.getString("idHistDoc")
        Log.d("GET_DATA->", getArgId ?: "")
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
        initObjects()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _bindDetHist = ShowDetailHistoryFmtBinding.inflate(
            inflater,
            container,
            false
        )
        initViews()
        return _bindDetHist?.root
    }

    private fun initObjects() {
    }

    private fun initViews() {
        setToolBar()
        getHistoryById(getArgId?: "")
    }

    private fun setToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(bindDetHist.tbProf)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindDetHist.tbProf.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getDataDriver(idDriver: String) {
        driverProvider.getDataDriver(idDriver).addOnSuccessListener { document ->
            if (document.exists()) {

                driver = document.toObject(DriverModel::class.java)

                Glide.with(this)
                    .load(driver?.imgUser)
                    .placeholder(R.drawable.ic_login)
                    .error(R.drawable.ic_login)
                    .into(bindDetHist.ivDetHist)

                (activity as AppCompatActivity).title = StringBuilder()
                            .append("Conductor: ").append(driver?.nameUser)

                bindDetHist.tvEmail.text = driver?.emailUser
            }else{
                ReuseCode.msgToast(
                    requireContext(),
                    "No se encontro el conductor!",
                    true
                )
            }
        }
    }

    // Get detail history trip
    private fun getHistoryById(idHistDoc: String) {
        histProvider.getHistoryClientById(idHistDoc).addOnSuccessListener { document ->
            if (document.exists()) {
                history = document.toObject(HistoryTripModel::class.java)
                bindDetHist.tvOrigin.text = history?.origin
                bindDetHist.tvDestination.text = history?.destination
                bindDetHist.tvTimeDistance.text = StringBuilder()
                    .append(history?.time)
                    .append(" Min")
                    .append(" - ")
                    .append(history?.km)
                    .append(" Km")
                bindDetHist.tvPriceTrip.text =    history?.price.toString()
                bindDetHist.tvDateTrip.text =
                   RelativeTime.getTimeAgo(
                        history?.timeStamp ?: 0, context
                    )
                bindDetHist.tvRatingDriv.text =   history?.ratingToDriver.toString()
                bindDetHist.tvRatingClient.text = history?.ratingToClient.toString()

                Log.d("LG_HIST", history?.destination.toString())

                getDataDriver(history?.idDriver?: "")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindDetHist = null
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowDetailHistoryFmt().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                    //putString(ARG_PARAM2, param2)
                }
            }
    }
}