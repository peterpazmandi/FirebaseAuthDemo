package com.inspirecoding.firebaseauthdemo.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_forgotpassword.view.*
import org.koin.android.ext.android.inject

private val TAG = "LoginActivity"
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

        iv_login_facebook.setOnClickListener {
            firebaseViewModel.signInWithFacebook(this)
        }

        iv_login_google.setOnClickListener {
            firebaseViewModel.signInWithGoogle(this)
        }

        tv_login_regsiternow.setOnClickListener {
            startRegisterActivity()
        }

        tv_login_forgotPassword.setOnClickListener {
            startForgotPasswordDialog()
        }

        firebaseViewModel.toast.observe(this, Observer {message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                firebaseViewModel.onToastShown()
            }
        })
    }

    fun startRegisterActivity()
    {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }


    private fun startForgotPasswordDialog()
    {
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_forgotpassword,
            null) // 1
        val builder = AlertDialog.Builder(this).setView(dialogView) // 2
        val dialog: AlertDialog = builder.show() // 3

        dialogView.btn_forgotpassword_send.setOnClickListener { // 4
            if(validateEmail(dialogView.tiet_forgotpassword_email)) // 5
            {
                firebaseViewModel.sendPasswordResetEmail(
                    dialogView.tiet_forgotpassword_email.text.toString(),
                    this
                )
                dialog.dismiss() // 5
            }
        }

        dialogView.btn_forgotpassword_cancel.setOnClickListener { // 6
            dialog.dismiss() // 7
        }

        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 8
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        firebaseViewModel.onActivityResult(requestCode, resultCode, data, this)
    }
}
