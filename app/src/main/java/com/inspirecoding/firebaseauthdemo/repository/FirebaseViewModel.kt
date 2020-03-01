package com.inspirecoding.firebaseauthdemo.repository

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.inspirecoding.firebaseauthdemo.repository.UserRepository
import com.inspirecoding.firebaseauthdemo.model.User
import kotlinx.coroutines.*
import com.inspirecoding.firebaseauthdemo.utils.Result
import com.inspirecoding.firebaseauthdemo.R
import com.inspirecoding.firebaseauthdemo.extension.await
import com.inspirecoding.firebaseauthdemo.repository.implementation.UserRepositoryImpl
import com.inspirecoding.firebaseauthdemo.ui.LoginActivity
import com.inspirecoding.firebaseauthdemo.ui.MainActivity
import com.inspirecoding.firebaseauthdemo.ui.RegisterActivity
import java.lang.Exception
import java.util.*


private val TAG = "FirebaseViewModel"
class FirebaseViewModel(var userRepository: UserRepository): ViewModel()
{
    private var callbackManager: CallbackManager? = null

    private lateinit var googleSingInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _currentUserMLD = MutableLiveData<User>(User())
    val currentUserLD: LiveData<User>
        get() = _currentUserMLD

    //Email
    fun registerUserFromAuthWithEmailAndPassword(name: String, email: String, password: String, activity: Activity)
    {
        launchDataLoad {
            viewModelScope.launch {
                when(val result = userRepository.registerUserFromAuthWithEmailAndPassword(email, password, activity.applicationContext))
                {
                    is Result.Success -> {
                        Log.e(TAG, "Result.Success")
                        result.data?.let {firebaseUser ->
                            createUserInFirestore(createUserObject(firebaseUser, name), activity)
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${result.exception.message}")
                        _toast.value = result.exception.message
                    }
                    is Result.Canceled -> {
                        Log.e(TAG, "${result.exception!!.message}")
                        _toast.value = activity.getString(R.string.request_canceled)
                    }
                }
            }
        }
    }
    private suspend fun createUserInFirestore(user: User, activity: Activity)
    {
        Log.d(TAG, "Result - ${user.name}")
        when(val result = userRepository.createUserInFirestore(user))
        {
            is Result.Success -> {
                Log.d(TAG, activity::class.java.simpleName)
                when(activity)
                {
                    is RegisterActivity -> {
                        _toast.value = activity.getString(R.string.registration_successful)
                    }
                    is LoginActivity -> {
                        Log.d(TAG, "Result - ${user.name}")
                        _toast.value = activity.getString(R.string.login_successful)
                    }
                }
                Log.d(TAG, "Result.Error - ${user.name}")
                _currentUserMLD.value = user
                startMainActivitiy(activity)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    fun logInUserFromAuthWithEmailAndPassword(email: String, password: String, activity: Activity)
    {
        launchDataLoad {
            viewModelScope.launch {
                when (val result = userRepository.logInUserFromAuthWithEmailAndPassword(email, password))
                {
                    is Result.Success -> {
                        Log.i(TAG, "SignIn - Result.Success - User: ${result.data}")
                        result.data?.let { firebaseUser ->
                            _toast.value = activity.getString(R.string.login_successful)
                            getUserFromFirestore(firebaseUser.uid, activity)
                        }
                    }
                    is Result.Error -> {
                        _toast.value = result.exception.message
                    }
                    is Result.Canceled -> {
                        _toast.value = activity.getString(R.string.request_canceled)
                    }
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String, activity: Activity)
    {
        viewModelScope.launch {
            when(val result = userRepository.sendPasswordResetEmail(email))
            {
                is Result.Success -> {
                    _toast.value = "Check email to reset your password!"
                }
                is Result.Error -> {
                    _toast.value = result.exception.message
                }
                is Result.Canceled -> {
                    _toast.value = activity.getString(R.string.request_canceled)
                }
            }
        }
    }

    //Facebook
    fun signInWithFacebook(activity: Activity)
    {
        launchDataLoad {
            callbackManager = CallbackManager.Factory.create()

            LoginManager
                .getInstance()
                .logInWithReadPermissions(
                    activity,
                    Arrays.asList("email", "public_profile"))

            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>
            {
                override fun onSuccess(result: LoginResult?)
                {
                    val credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
                    viewModelScope.launch {
                        when(val result = userRepository.signInWithCredential(credential))
                        {
                            is Result.Success -> {
                                Log.d(TAG, "Result.Success - ${result.data?.user?.uid}")
                                result.data?.user?.let { user ->
                                    val _user = user.displayName?.let {
                                        createUserObject(
                                            user,
                                            it
                                        )
                                    }
                                    _user?.let {
                                        createUserInFirestore(_user, activity)
                                    }
                                }
                            }
                            is Result.Error -> {
                                Log.e(TAG, "Result.Error - ${result.exception.message}")
                                _toast.value = result.exception.message
                            }
                            is Result.Canceled -> {
                                Log.d(TAG, "Result.Canceled")
                                _toast.value = activity.applicationContext.getString(R.string.request_canceled)
                            }
                        }
                    }
                }

                override fun onError(error: FacebookException?)
                {
                    Log.e(TAG, "Result.Error - ${error?.message}")
                    _toast.value = error?.message
                }

                override fun onCancel()
                {
                    Log.d(TAG, "Result.Canceled")
                    _toast.value = activity.applicationContext.getString(R.string.request_canceled)
                }
            })
        }
    }

    //Google
    fun signInWithGoogle(activity: Activity)
    {
        launchDataLoad {
            val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSingInClient = GoogleSignIn.getClient(activity, googleSignInOptions)

            val intent = googleSingInClient.signInIntent
            activity.startActivityForResult(intent, RC_SIGN_IN)
        }
    }
    private fun handleSignInResult (completedTask: Task<GoogleSignInAccount>, activity: Activity)
    {
        viewModelScope.launch {
            try
            {
                val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
                account?.let {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    when(val result = userRepository.signInWithCredential(credential))
                    {
                        is Result.Success -> {
                            Log.d(TAG, "Result.Success - ${result.data?.user?.uid}")
                            result.data?.user?.let {user ->
                                val _user = user.displayName?.let {
                                    createUserObject(
                                        user,
                                        it
                                    )
                                }
                                _user?.let {
                                    createUserInFirestore(_user, activity)
                                }
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Result.Error - ${result.exception.message}")
                            _toast.value = result.exception.message
                        }
                        is Result.Canceled -> {
                            Log.d(TAG, "Result.Canceled")
                            _toast.value = activity.getString(R.string.request_canceled)
                        }
                    }
                }
            }
            catch (exception: Exception)
            {
                Toast.makeText(activity.applicationContext, "Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun getUserFromFirestore(userId: String, activity: Activity)
    {
        when(val result = userRepository.getUserFromFirestore(userId))
        {
            is Result.Success -> {
                val _user = result.data
                Log.d(TAG, "${result.data}")
                _currentUserMLD.value = _user
                startMainActivitiy(activity = activity)
            }
            is Result.Error -> {
                _toast.value = result.exception.message
            }
            is Result.Canceled -> {
                _toast.value = activity.getString(R.string.request_canceled)
            }
        }
    }

    fun checkUserLoggedIn(): FirebaseUser?
    {
        var _firebaseUser: FirebaseUser? = null
        viewModelScope.launch {
            _firebaseUser = userRepository.checkUserLoggedIn()
        }
        return _firebaseUser
    }
    fun logOutUser()
    {
        viewModelScope.launch {
            userRepository.logOutUser()
        }
    }
    fun createUserObject(firebaseUser: FirebaseUser, name: String, profilePicture: String = ""): User
    {
        val currentUser = User(
            id =  firebaseUser.uid,
            name = name,
            profilePicture = profilePicture
        )

        return currentUser
    }
    fun onToastShown()
    {
        _toast.value = null
    }
    private fun launchDataLoad(block: suspend () -> Unit): Job
    {
        return viewModelScope.launch {
            try
            {
                _spinner.value = true
                block()
            }
            catch (error: Throwable)
            {
                _toast.value = error.message
            }
            finally
            {
                _spinner.value = false
            }
        }
    }
    private fun startMainActivitiy(activity: Activity)
    {
        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity)
    {
        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN)
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task, activity)
        }
    }
}