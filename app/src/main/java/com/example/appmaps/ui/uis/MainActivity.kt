package com.example.appmaps.ui.uis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmaps.R
import com.example.appmaps.databinding.ActivityMainBinding
import com.example.appmaps.ui.utils_provider.FrbAuthProviders
import com.example.appmaps.ui.utils_code.ReutiliceCode

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var bindMainAct: ActivityMainBinding
    private val authProvider = FrbAuthProviders()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bindMainAct = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMainAct.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewsEvents()
    }

    private fun initViewsEvents() {
        bindMainAct.btnLogin.setOnClickListener(this)
        bindMainAct.btnRegister.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.btn_login -> {
                checkCredentialsLogin()
            }
            R.id.btn_register -> {
                navIntent(RegistAct::class.java)
            }
        }
    }

    private fun checkCredentialsLogin() {

        val etNameUser = bindMainAct.etUser.text.toString()
        val etPasswUser = bindMainAct.etPassw.text.toString()

        checkEditEmpty(etNameUser, etPasswUser)

        if (checkEditEmpty(etNameUser, etPasswUser)){

            authProvider.loginUser(etNameUser, etPasswUser).addOnCompleteListener { aResult ->
                if (aResult.isSuccessful) {
                    ReutiliceCode.msgToast(this, "Credenciales correctas.", true)
                    navIntent(MapsAct::class.java)
                }else{
                    ReutiliceCode.msgToast(this, "Credenciales invalidas.", true)
                    Log.d("LG_REG", "${aResult.result}")
                }
            }
        }
    }


    private fun navIntent(navCls: Class<*>) {
        startActivity(Intent(this, navCls))
    }

    // Check edit empty
    private fun checkEditEmpty(name: String, passw: String): Boolean {
        if (name.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese usuario", true)
            return false
        }
        if (passw.isEmpty()){
            ReutiliceCode.msgToast(this, "Ingrese Contraseña", true)
            return false
        }
        return true
    }
}