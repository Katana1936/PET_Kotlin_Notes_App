package com.example.to_doapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsetsController

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.setDecorFitsSystemWindows(false)
    }
}