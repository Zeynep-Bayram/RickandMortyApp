package com.example.rickandmortyapp

import android.app.Application
import com.example.rickandmortyapp.api.RetrofitInstance

class RickAndMortyApp : Application() {
    override fun onCreate() {
        RetrofitInstance.installAppWideSsl()
        super.onCreate()
    }
}
