package com.inspirecoding.firebaseauthdemo.module

import com.inspirecoding.firebaseauthdemo.repository.FirebaseViewModel
import com.inspirecoding.firebaseauthdemo.repository.implementation.UserRepositoryImpl
import org.koin.dsl.module

val firebaseViewModelModule = module {
    single { FirebaseViewModel(UserRepositoryImpl()) }
}