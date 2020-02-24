package com.inspirecoding.firebaseauthdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import com.inspirecoding.firebaseauthdemo.repository.UserRepository
import com.inspirecoding.firebaseauthdemo.repository.implementation.UserRepositoryImpl
import kotlinx.android.synthetic.main.activity_register.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private val TAG = "RegisterActivity"
class RegisterActivity : AppCompatActivity()
{
    private lateinit var firebaseViewModel: FirebaseViewModel

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseViewModel = ViewModelProvider(this)
            .get(FirebaseViewModel::class.java)

        btn_register_login.setOnClickListener {
            if(validateName() && validateEmail() && validatePassword())
            {
                firebaseViewModel.registerUserFromAuthWithEmailAndPassword(
                    tiet_register_name.text.toString(),
                    tiet_register_email.text.toString(),
                    tiet_register_password.text.toString(),
                    this
                )
            }
        }

        firebaseViewModel.toast.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                firebaseViewModel.onToastShown()
            }
        })

        firebaseViewModel.spinner.observe(this, Observer { value ->
            value.let {show ->
                spinner_register.visibility = if (show) View.VISIBLE else View.GONE
                Log.i(TAG, "$show")
            }
        })
    }


    private fun validateName(): Boolean
    {
        val name = tiet_register_name.text.toString().trim()

        return if(name.length < 6)
        {
            tiet_register_name.error = "Use at least 5 characters"
            false
        }
        else
        {
            true
        }
    }
    private fun validateEmail(): Boolean
    {
        val email = tiet_register_email.text.toString().trim()

        return if(!email.contains("@") && !email.contains("."))
        {
            tiet_register_email.error = "Enter a valid email"
            false
        }
        else if (email.length < 6)
        {
            tiet_register_email.error = "Use at least 5 characters"
            false
        }
        else
        {
            true
        }
    }
    private fun validatePassword(): Boolean
    {
        val password = tiet_register_password.text.toString().trim()

        return if(password.length < 6)
        {
            tiet_register_password.error = "Use at least 5 characters"
            false
        }
        else
        {
            true
        }
    }
}
