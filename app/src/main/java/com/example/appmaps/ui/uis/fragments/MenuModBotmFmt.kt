package com.example.appmaps.ui.uis.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.appmaps.R
import com.example.appmaps.databinding.MenuModbotFmtBinding
import com.example.appmaps.ui.models.Booking
import com.example.appmaps.ui.models.ClientModel
import com.example.appmaps.ui.uis.activities.ContinerMenuFmts
import com.example.appmaps.ui.uis.activities.MapsAct
import com.example.appmaps.ui.utils_provider.GeoProvider
import com.example.appmaps.ui.utils_provider.BookingProvider
import com.example.appmaps.ui.utils_provider.ClientProvider
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MenuModBotmFmt: BottomSheetDialogFragment(), View.OnClickListener {

    // Views
    private var _bindInf: MenuModbotFmtBinding? = null
    private val bindInf get() = _bindInf!!

    // Objects
    private var geoProvider = GeoProvider()
    private val authProvider = FrbAuthProviders()
    private val bookProvider = BookingProvider()
    private val clientProvider = ClientProvider()

    private var getData: Booking? = null
    private var checkCancel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initObjects()
        _bindInf = MenuModbotFmtBinding.inflate(inflater, container, false)
        return _bindInf?.root
    }

    companion object {
        const val TAG = "MENU_BSD"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        getDataClient()
    }

    private fun initObjects() {
        //getData = arguments?.getParcelable<Booking>("object_booking")
        Log.d("LG_REQTRIP","$getData")
    }

    private fun initViews() {
        bindInf.ibProfile.setOnClickListener(this)
        bindInf.ibHistory.setOnClickListener(this)
        bindInf.ibCloseSesion.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ib_profile -> {
                goToFragmentsOption(ContinerMenuFmts::class.java, 0)
            }
            R.id.ib_history -> {
                goToFragmentsOption(ContinerMenuFmts::class.java, 1)
            }
            R.id.ib_close_sesion -> {
                goToMainLogin(MapsAct::class.java)
            }
        }
    }

    private fun getDataClient() {
        clientProvider.getDataClient(authProvider.getIdFrb()).addOnSuccessListener { document ->
            if (document.exists()) {
                val client = document.toObject(ClientModel::class.java)
                bindInf.tvNameDriver.text = StringBuilder()
                    .append("Cliente: ")
                    .append("${client?.nameUser}")
            }
        }
    }

    private fun goToFragmentsOption(navCls: Class<*>, typeFmt: Int) {
        val intent = Intent(requireContext(), navCls)
        intent.putExtra("kTypeFmt", typeFmt)
        startActivity(intent)
    }

    private fun goToMainLogin(navCls: Class<*>) {
        //authProvider.logOut()
        val intent = Intent(requireContext(), navCls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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