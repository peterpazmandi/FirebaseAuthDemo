package com.inspirecoding.firebaseauthdemo.repository.implementation

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.facebook.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.inspirecoding.firebaseauthdemo.extension.await
import com.inspirecoding.firebaseauthdemo.model.User
import com.inspirecoding.firebaseauthdemo.repository.UserRepository
import com.inspirecoding.firebaseauthdemo.utils.Result
import java.lang.Exception
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import kotlinx.coroutines.*
import java.util.*


private val TAG = "UserRepositoryImpl"
class UserRepositoryImpl : UserRepository
{
    //CONST
    private val USER_COLLECTION_NAME = "users"

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val userCollection = firestoreInstance.collection(USER_COLLECTION_NAME)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun registerUserFromAuthWithEmailAndPassword(email: String, password: String, context: Context): Result<FirebaseUser?>
    {
        try
        {
            return when(val resultDocumentSnapshot = firebaseAuth.createUserWithEmailAndPassword(email, password).await())
            {
                is Result.Success -> {
                    Log.i(TAG, "Result.Success")
                    val firebaseUser = resultDocumentSnapshot.data.user
                    Result.Success(firebaseUser)
                }
                is Result.Error -> {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Error(resultDocumentSnapshot.exception)
                }
                is Result.Canceled ->  {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Canceled(resultDocumentSnapshot.exception)
                }
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }

    override suspend fun checkUserLoggedIn(): FirebaseUser?
    {
        return firebaseAuth.currentUser
    }

    override suspend fun createUserInFirestore(user: User): Result<Void?>
    {
        return try
        {
            userCollection.document(user.id).set(user).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
}