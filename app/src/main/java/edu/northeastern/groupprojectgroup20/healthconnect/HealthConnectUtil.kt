package edu.northeastern.groupprojectgroup20.healthconnect

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object HealthConnectUtil {

    fun checkPermissions(context: Context, onResult: (Boolean) -> Unit) {
        val healthConnect = HealthConnect(context)
        GlobalScope.launch(Dispatchers.Main) {
//            val hasPermissions = healthConnect.hasAllPermissions()
//            onResult(hasPermissions)
            try {
                val hasPermissions = healthConnect.hasAllPermissions()
                onResult(hasPermissions)
            } catch (e: IllegalStateException) {
                // Handle the exception here, for example, you can log the error message
                Log.e("HealthConnectUtil", "Service not available: ${e.message}")
                onResult(false)
            }
        }
    }
}