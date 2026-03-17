package com.example.parkme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.parkme.navigation.AppNavigation
import com.example.parkme.ui.theme.ParkMeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            ParkMeTheme {

                AppNavigation()

            }

        }

    }

}