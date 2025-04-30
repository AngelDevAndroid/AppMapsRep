package com.example.appmaps.ui.uis.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.appmaps.R

import com.example.appmaps.databinding.ActContinerMenuFmtsBinding
import com.example.appmaps.ui.uis.fragments.ShowHistoryFmt
import com.example.appmaps.ui.uis.fragments.ShowProfileClientFmt

class ContinerMenuFmts : AppCompatActivity() {

    lateinit var bindContainer: ActContinerMenuFmtsBinding

    // Vars
    private var getTypeFmt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bindContainer = ActContinerMenuFmtsBinding.inflate(layoutInflater)
        setContentView(bindContainer.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getIntentObjects()
        setFragments()
    }

    private fun getIntentObjects() {
        getTypeFmt = intent.getIntExtra("kTypeFmt", -1)
    }

    private fun setFragments() {

        var fmt: Fragment? = null

        when(getTypeFmt){
            0 -> {
                fmt = ShowProfileClientFmt()
            }
            1 -> {
                fmt = ShowHistoryFmt()
            }
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fmt?: Fragment())
            .commit()
    }
}