package com.inspirecoding.firebaseauthdemo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity()
{
    val firebaseViewModel: FirebaseViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login_login.setOnClickListener {
            if(validateEmail(tiet_login_email) && validatePassword())
            {
                firebaseViewModel.logInUserFromAuthWithEmailAndPassword(
                    tiet_login_email.text.toString(),
                    tiet_login_password.text.toString(),
                    this
                )
            }
        }

        tv_login_regsiternow.setOnClickListener {
            startRegisterActivity()
        }
    }

    fun startRegisterActivity()
    {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun validateEmail(view: View): Boolean
    {
        val email = (view as TextInputEditText).text.toString().trim()

        return if(!email.contains("@") && !email.contains("."))
        {
            view.error = "Enter a valid email"
            false
        }
        else if (email.length < 6)
        {
            view.error = "Use at least 5 characters"
            false
        }
        else
        {
            true
        }
    }
    private fun validatePassword(): Boolean
    {
        val password = tiet_login_password.text.toString().trim()

        return if(password.length < 6)
        {
            tiet_login_password.error = "Use at least 5 characters"
            false
        }
        else
        {
            true
        }
    }
}
