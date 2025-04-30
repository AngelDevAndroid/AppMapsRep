package com.example.appmaps.ui.uis.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appmaps.R
import com.example.appmaps.databinding.ShowProfileClientFmtBinding
import com.example.appmaps.ui.models.ClientModel
import com.example.appmaps.ui.utils_code.ReuseCode
import com.example.appmaps.ui.utils_provider.ClientProvider
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class ShowProfileClientFmt : Fragment(), View.OnClickListener {

    private var param1: String? = null
    private var param2: String? = null

    // Objects
    private val authProvider = FrbAuthProviders()
    private val clientProvider = ClientProvider()

    private var _bindProfile: ShowProfileClientFmtBinding? = null
    private val bindProfile get() = _bindProfile!!

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        _bindProfile = ShowProfileClientFmtBinding.inflate(
            inflater,
            container,
            false
        )
        initViews()
        return _bindProfile?.root
    }

    private fun initObjects() {
        getDataClient()
    }

    private fun initViews() {
        setToolBar()
        bindProfile.btnUpdateInfo.setOnClickListener(this)
        bindProfile.ivProfile.setOnClickListener(this)
    }

    private fun setToolBar() {
        (activity as AppCompatActivity).setSupportActionBar(bindProfile.tbProf)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).title = "Datos del cliente"

        bindProfile.tbProf.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getDataClient() {
        clientProvider.getDataClient(authProvider.getIdFrb()).addOnSuccessListener { document ->
            if (document.exists()) {

                val client = document.toObject(ClientModel::class.java)

                Glide.with(this)
                    .load(client?.imgUser)
                    .placeholder(R.drawable.ic_login)
                    .error(R.drawable.ic_login)
                    .into(bindProfile.ivProfile)

                bindProfile.tvEmail.text = client?.emailUser
                bindProfile.etNameClient.setText(client?.nameUser)
                bindProfile.etIdUser.setText(client?.idUser)
                bindProfile.etEmailUser.setText(client?.emailUser)
                bindProfile.etPassUser.setText(client?.passwUser)
            }
        }
    }

    // Update info driver
    private fun getDataUpdate() {

        val idClient = bindProfile.etIdUser.text.toString()
        val nameClient = bindProfile.etNameClient.text.toString()
        val emailClient = bindProfile.etEmailUser.text.toString()
        val passClient = bindProfile.etPassUser.text.toString()

        var urlImageFile: String? = null
        var obClient: ClientModel? = null

        if (imageFile?.exists() == true) {
            // Upload image
            clientProvider
                .uploadFileStorage(authProvider.getIdFrb(),imageFile?: File(""))
                                .addOnSuccessListener { tSnapshot ->
                    if (tSnapshot.task.isSuccessful) {
                        Log.d("LG_STORAGE", "img uploaded")

                        // Get url image
                        clientProvider.getImageUrlStorage().addOnSuccessListener { getUrl ->

                            //if (getUrl.path.isNullOrEmpty()) {
                                val urlImage = getUrl.toString()
                                urlImageFile = urlImage

                            obClient =  setObjClient(
                                        idClient,
                                        nameClient,
                                null,
                                        emailClient,
                                        passClient,
                                urlImageFile?: ""
                            )
                            updateDataClient(obClient?: ClientModel())
                            Log.d("LG_STORAGE", urlImageFile!!)
                           // }else{
                            Log.d("LG_STORAGE", "No se pudo obtener la uri")
                            //}
                        }

                    }else{
                        Log.d("LG_STORAGE", "img not uploaded")
                    }
                }
        }else{

            obClient = setObjClient(
                idClient,
                nameClient,
                null,
                emailClient,
                passClient,
                null
            )
            updateDataClient(obClient?: ClientModel())
        }
    }

    private fun setObjClient(
                             idClientParam: String?,
                             nameClientParam: String,
                             numClientParam: String?,
                             emailParam: String,
                             passParam: String,
                             imgParam: String?): ClientModel {

        val objClient = ClientModel(
                    authProvider.getIdFrb(),
                    nameClientParam,
            null,
                    emailParam,
                    passParam,
                    imgParam
        )
        return objClient
    }

    private fun updateDataClient(obClient: ClientModel) {
        clientProvider.updateInfoClient(obClient?: ClientModel())
                   .addOnCompleteListener { update ->
            if (update.isSuccessful) {
                ReuseCode.msgToast(
                    requireActivity(),
                    "Datos actualizados.",
                    true
                )
            }else{
                ReuseCode.msgToast(
                    requireActivity(),
                    "Error al actualizar.",
                    true
                )
            }
        }
    }

    private fun selectImage() {
        ImagePicker.Companion.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
            //.start()
    }

    private val startImageForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            val resultCode = result.resultCode
            val data = result.data

            if (resultCode == Activity.RESULT_OK) {
                val fileUri = data?.data!!

                imageFile = File(fileUri.path ?: "")
                bindProfile.ivProfile.setImageURI(fileUri)

            } else if (resultCode == com.github.dhaval2404.imagepicker.ImagePicker.Companion.RESULT_ERROR) {

                ReuseCode.msgToast(
                    requireContext(),
                    com.github.dhaval2404.imagepicker.ImagePicker.Companion.getError(data),
                    true
                )

            } else {
                ReuseCode.msgToast(
                    requireContext(),
                    "Tarea cancelada!",
                    true
                )

            }
        }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowProfileClientFmt().apply {
                arguments = Bundle().apply {
                    //putString(com.angandroid.appmapsdriver.ui.fragments.ARG_PARAM1, param1)
                    //putString(com.angandroid.appmapsdriver.ui.fragments.ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.btn_update_info -> {
                getDataUpdate()
            }
            R.id.iv_profile -> {
                selectImage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindProfile = null
    }
}