package com.inspirecoding.firebaseauthdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity()
{
    private val firebaseViewModel: FirebaseViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseViewModel.toast.observe(this, Observer { message ->
            message?.let {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                firebaseViewModel.onToastShown()
            }
        })
    }
}
