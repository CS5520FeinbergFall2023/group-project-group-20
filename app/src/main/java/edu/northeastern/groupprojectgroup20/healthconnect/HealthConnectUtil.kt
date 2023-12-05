package edu.northeastern.groupprojectgroup20.healthconnect

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object HealthConnectUtil {

    fun checkPermissions(context: Context, onResult: (Boolean) -> Unit) {
        val healthConnect = HealthConnect(context)
        GlobalScope.launch(Dispatchers.Main) {
            val hasPermissions = healthConnect.hasAllPermissions()
            onResult(hasPermissions)
        }
    }
}