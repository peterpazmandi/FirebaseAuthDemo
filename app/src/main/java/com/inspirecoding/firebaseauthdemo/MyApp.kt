package com.inspirecoding.firebaseauthdemo

import android.app.Application
import com.inspirecoding.firebaseauthdemo.module.firebaseViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp: Application()
{
    override fun onCreate()
    {
        super.onCreate()

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(firebaseViewModelModule))
        }
    }
}