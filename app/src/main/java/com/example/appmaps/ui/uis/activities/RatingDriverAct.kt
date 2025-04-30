package com.example.appmaps.ui.uis.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActRatingDriverBinding
import com.example.appmaps.ui.models.HistoryTripModel
import com.example.appmaps.ui.utils_provider.HistoryProvider
import com.example.appmaps.ui.utils_code.ReuseCode
import java.lang.StringBuilder

class RatingDriverAct : AppCompatActivity(), View.OnClickListener {

    lateinit var bindRating: ActRatingDriverBinding
    //private var getExtraPrice = 0.0
    val historyProvider = HistoryProvider()

    var history: HistoryTripModel? = null
    private var setRating = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindRating = ActRatingDriverBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(bindRating.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initObjects()
        getHistory()
    }

    private fun initObjects() {
        //getExtraPrice = intent.getDoubleExtra("getPrice", 0.0)
        //bindRating.tvPrice.text = "$getExtraPrice - Pesos"
        bindRating.btnSaveUpdateRating.setOnClickListener(this)
    }

    private fun getHistory() {
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null) {
                if (query.documents.size > 0) {
                    history = query.documents[0].toObject(HistoryTripModel::class.java)
                    history?.id = query.documents[0].id
                    setDataRating(history)
                    Log.d("LG_FIRESTORE", "history -> ${history}")
                }else{
                    Log.d("LG_FIRESTORE", "history -> ${query.documents.size}")
                    ReuseCode.msgToast(this, "No se encontro el historial! ${query.documents.size}", true)
                }
            }else{
                Log.d("LG_FIRESTORE", "history -> $query")
            }
        }.addOnFailureListener { error ->
            Log.d("LG_FIRESTORE", "history -> ${error.message}")
        }
    }

    // Set data
    private fun setDataRating(history: HistoryTripModel?) {
        bindRating.tvOrigin.text = history?.origin
        bindRating.tvDestination.text = history?.destination

        bindRating.tvInfoDistTime.text = StringBuilder()
            .append("${history?.time}").append("Min")
            .append("-")
            .append("${history?.km}").append("Km")

        bindRating.tvPrice.text = history?.price.toString()

        bindRating.rtgRatingClient.setOnRatingBarChangeListener { ratingBar, value, b ->
            setRating = value
        }
    }

    private fun updateRatingStarts(idDocHist: String) {
        historyProvider.updateRatingDriver(idDocHist, setRating).addOnCompleteListener { resultUp ->
            if (resultUp.isSuccessful) {
                goToMapDriver()
            }else{
                ReuseCode.msgToast(this, "No se pudo calificar!", true)
            }
        }
    }

    private fun goToMapDriver() {
        val intent = Intent(this, MapsAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btn_save_update_rating -> {
                updateRatingStarts(history?.id?: "-1")
            }
        }
    }

    // Life cycle-------------->
    override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    // Life cycle-------------->
}