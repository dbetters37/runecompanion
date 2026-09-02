package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.theme.OsrsPetTheme
import com.example.viewmodel.PetViewModel

class MainActivity : ComponentActivity() {

    private val petViewModel: PetViewModel by viewModels()

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    petViewModel.onScreenTurnedOff()
                    petViewModel.onAppBackgrounded()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    petViewModel.checkInactivitySleep()
                    // Do NOT call onAppForegrounded here: user may unlock phone into another app.
                    // onResume() will handle onAppForegrounded when MainActivity actually enters the foreground.
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissionsIfNeeded()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(screenStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenStateReceiver, filter)
        }

        setContent {
            OsrsPetTheme {
                MainScreen(viewModel = petViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        petViewModel.onAppForegrounded()
    }

    override fun onResume() {
        super.onResume()
        petViewModel.checkInactivitySleep()
        petViewModel.onAppForegrounded()
    }

    override fun onPause() {
        super.onPause()
        petViewModel.onAppBackgrounded()
    }

    override fun onStop() {
        super.onStop()
        petViewModel.onAppBackgrounded()
    }

    override fun onDestroy() {
        super.onDestroy()
        petViewModel.onAppBackgrounded()
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {}
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
