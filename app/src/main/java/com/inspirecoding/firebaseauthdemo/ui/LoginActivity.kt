package com.inspirecoding.firebaseauthdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.inspirecoding.firebaseauthdemo.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login_login.setOnClickListener {

        }
    }
}
