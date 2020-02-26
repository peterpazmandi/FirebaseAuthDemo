package com.inspirecoding.firebaseauthdemo.repository

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.firebaseauthdemo.model.User
import com.inspirecoding.firebaseauthdemo.utils.Result
import kotlinx.coroutines.CoroutineScope

interface UserRepository
{
    suspend fun registerUserFromAuthWithEmailAndPassword(email: String, password: String, context: Context): Result<FirebaseUser?>
    suspend fun createUserInFirestore(user: User): Result<Void?>

    suspend fun checkUserLoggedIn(): FirebaseUser?
}