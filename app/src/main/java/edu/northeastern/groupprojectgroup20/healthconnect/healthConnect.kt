package edu.northeastern.groupprojectgroup20.healthconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED


class HealthConnect(private val context: Context) {
    companion object {
        private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    }

    fun checkHealthConnectAvailability() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)

        when (availabilityStatus) {
            SDK_AVAILABLE -> {
                // Health Connect is available on the device
                useHealthConnect()
            }

            SDK_UNAVAILABLE -> {
                // Health Connect is not available on the device
                handleUnavailability()
            }

            SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                // Health Connect availability is unknown
                promptUserToUpdateHealthConnect()
            }
        }
    }

    private fun handleUnavailability() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")

        }
        context.startActivity(intent)
    }

    // Optionally redirect to package installer to find a provider, for example:
    private fun promptUserToUpdateHealthConnect() {
        val uriString = "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"
        context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = Uri.parse(uriString)
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }
        )
    }

    private fun useHealthConnect() {
        //obatin a HealthConnectClient instance
        val healthConnectClient = HealthConnectClient.getOrCreate(context, HEALTH_CONNECT_PACKAGE)
        // Use the client to access Health Connect APIs
        // read heart_rate and step data

    }
}