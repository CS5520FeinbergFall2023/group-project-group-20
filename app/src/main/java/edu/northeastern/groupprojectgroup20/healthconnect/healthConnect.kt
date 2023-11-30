package edu.northeastern.groupprojectgroup20.healthconnect

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.time.Duration


class HealthConnect(private val context: Context) {


    // initialize the HealthConnectClient when init
    private val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    companion object {
        private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"
    }

    val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getWritePermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getWritePermission(ExerciseSessionRecord::class)
        )

    init {
        // check the availability of Health Connect on the device
        checkHealthConnectAvailability()
        //check permissions

    }

    /**
     * TODO: Obtains a Changes token for the specified record types.
     */
    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class,
                    StepsRecord::class,
                    TotalCaloriesBurnedRecord::class,
                    SleepSessionRecord::class
                )
            )
        )
    }



    fun checkHealthConnectAvailability(): Boolean {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, HEALTH_CONNECT_PACKAGE)

        return when (availabilityStatus) {
            SDK_AVAILABLE -> true
            SDK_UNAVAILABLE -> {
                handleUnavailability()
                false
            }

            SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                promptUserToUpdateHealthConnect()
                false
            }

            else -> {
                false
            }
        }
    }

    private fun handleUnavailability() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data =
                Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")

        }
        context.startActivity(intent)
    }

    // Optionally redirect to package installer to find a provider, for example:
    private fun promptUserToUpdateHealthConnect() {
        val uriString =
            "market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", context.packageName)
            }
        )
    }

    fun getHealthConnectClientOrNull(): HealthConnectClient? {
        return healthConnectClient
    }

    suspend fun hasAllPermissions(): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(PERMISSIONS)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }



    // read all the steps data given time range and return a list of StepsRecord
    fun readStepsByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<ReadRecordsResponse<StepsRecord>> {
        return GlobalScope.future {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
        }
    }

    // read all the heart rate data given time range and return a list of HeartRateRecord
    fun readHeartRateByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<ReadRecordsResponse<HeartRateRecord>> {
        return GlobalScope.future {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
        }
    }

    // read all the calories data given time range and return a list of TotalCaloriesBurnedRecord
    fun readCaloriesByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<ReadRecordsResponse<TotalCaloriesBurnedRecord>> {
        return GlobalScope.future {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
        }
    }

    //read all the sleep data given time range and return a list of SleepSessionRecord
    fun readSleepByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<ReadRecordsResponse<SleepSessionRecord>> {
        return GlobalScope.future {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
        }
    }

    fun readTotalSleepByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<Duration> {
        return GlobalScope.future {
            val result = AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
            )
            val response = healthConnectClient.aggregate(result)
            response[SleepSessionRecord.SLEEP_DURATION_TOTAL]!!
        }
    }


    //read all the exercise data given time range and return a list of ExerciseSessionRecord
    fun readExerciseSessionByTimeRange(
        startTime: Instant,
        endTime: Instant
    ): CompletableFuture<ReadRecordsResponse<ExerciseSessionRecord>> {
        return GlobalScope.future {
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.Companion.between(startTime, endTime)
                )
            )
        }
    }
}



