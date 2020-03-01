package com.inspirecoding.firebaseauthdemo.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import android.util.Base64

private val TAG = "SplashScreen"
class SplashActivity : AppCompatActivity()
{
    private val firebaseViewModel: FirebaseViewModel by inject()
    private var currentFirebaseUser: FirebaseUser? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        printKeyHash()

        coroutineScope.launch {
            delay(3_000)
            currentFirebaseUser = firebaseViewModel.checkUserLoggedIn()

            if (currentFirebaseUser == null)
            {
                startActivity(RegisterActivity())
            }
            else
            {
                currentFirebaseUser?.let { firebaseUser ->
                    Log.i(TAG, firebaseUser.uid)
                    firebaseViewModel.getUserFromFirestore(
                            firebaseUser.uid,
                    this@SplashActivity)
                    startActivity(MainActivity())
                }
            }
        }
    }

    private fun startActivity(activity: Activity)
    {
        val intent = Intent(this@SplashActivity, activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun printKeyHash()
    {
        try
        {
            val info = packageManager.getPackageInfo("com.pazpeti.runningapp", PackageManager.GET_SIGNATURES)
            for(signature in info.signatures)
            {
                val messageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(signature.toByteArray())
                Log.i("printKeyHash_1", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT))
            }
        }
        catch (exception: PackageManager.NameNotFoundException)
        {
            Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
        }
        catch (exception: NoSuchAlgorithmException)
        {
            Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
