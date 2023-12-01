package edu.northeastern.groupprojectgroup20.healthconnect;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.health.connect.ReadRecordsRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord;
import androidx.health.connect.client.records.ExerciseSessionRecord;
import androidx.health.connect.client.records.SleepSessionRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.health.connect.client.units.Energy;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.northeastern.groupprojectgroup20.R;

public class HealthConnectActivity extends AppCompatActivity {
    private HealthConnect healthConnect;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private TextView totalSteps;
    private TextView totalCalories, totalSleep, total_exercise_session;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private long sensorStepsCount = 0; // Save sensor steps

    private static final int MY_PERMISSIONS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_connect);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACTIVITY_RECOGNITION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_CODE);
        }



        healthConnect = new HealthConnect(this);

        totalSteps = findViewById(R.id.total_steps);
        totalCalories = findViewById(R.id.total_calories_burned);
        totalSleep = findViewById(R.id.sleep_session);
        total_exercise_session = findViewById(R.id.exercise_session);

        // Set Today's start and end time
//        ZonedDateTime now = ZonedDateTime.now();
//        ZonedDateTime startOfYesterday = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
//        ZonedDateTime endOfYesterday = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);


        // Set Yesterday's start and end time
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfYesterday = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        Instant startTime = startOfYesterday.toInstant();
        Instant endTime = endOfYesterday.toInstant();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(stepSensorEventListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);

        retrieveAndDisplayStepsData(startOfYesterday, endOfYesterday);
        retrieveAndDisplayCaloriesData(startOfYesterday, endOfYesterday);
        retrieveAndDisplaySleepData(startOfYesterday, endOfYesterday);
        retrieveAndDisplayExerciseSessionData(startOfYesterday, endOfYesterday);


        // Retrieve steps data from yesterday
//        CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(startTime, endTime);
//        future.thenAcceptAsync(response -> {
//            List<StepsRecord> stepsRecords = response.getRecords();
//            long totalStepsCount = 0;
//            for (StepsRecord stepsRecord : stepsRecords) {
//                totalStepsCount += stepsRecord.getCount();
//            }
//
//            final long finalTotalStepsCount = totalStepsCount;
//
//            runOnUiThread(() -> {
//                totalSteps.setText(String.format(Locale.getDefault(), "%d", finalTotalStepsCount));
//            });
//        }, executor);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sensorManager.registerListener(stepSensorEventListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private final SensorEventListener stepSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                sensorStepsCount = (long) event.values[0];
                runOnUiThread(() -> {
                    TextView sensorStepsTextView = findViewById(R.id.sensor_steps);
                    sensorStepsTextView.setText("Sensor Steps: " + sensorStepsCount);
                });
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Don't delete this, but don't do this either
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(stepSensorEventListener);
    }

    // test commit

    /**
     * Retrieve and display steps data on a given time range
     * @param startTime
     * @param endTime
     */
    public void retrieveAndDisplayStepsData(ZonedDateTime startTime, ZonedDateTime endTime) {
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();
        CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(start, end);
        future.thenAcceptAsync(response -> {
            List<StepsRecord> stepsRecords = response.getRecords();
            long totalStepsCount = 0;
            for (StepsRecord stepsRecord : stepsRecords) {
                totalStepsCount += stepsRecord.getCount();
//                Log.d("Steps", "Steps: " + stepsRecord.getCount());
            }

            final long finalTotalStepsCount = totalStepsCount;

            runOnUiThread(() -> {
                totalSteps.setText(String.format(Locale.getDefault(), "%d", finalTotalStepsCount));
            });
        }, executor);
    }

    public void retrieveAndDisplayCaloriesData(ZonedDateTime startTime, ZonedDateTime endTime) {
        Log.d("HealthConnectActivity", "retrieveAndDisplayCaloriesData called");
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();
        //Retrieve calories data from yesterday
        CompletableFuture<ReadRecordsResponse<TotalCaloriesBurnedRecord>> future = healthConnect.readCaloriesByTimeRange(start, end);
        future.thenAcceptAsync(response -> {
            List<TotalCaloriesBurnedRecord> caloriesRecords = response.getRecords();

            Log.d("HealthConnectActivity", "retrieveAndDisplayCaloriesData start");
            double totalCaloriesCount = 0;
            if (caloriesRecords.isEmpty()) {
                runOnUiThread(() -> {
                    totalCalories.setText("No calories data available for the specified time range");
                });
                return;
            }

            for (TotalCaloriesBurnedRecord caloriesRecord : caloriesRecords) {
                totalCaloriesCount += caloriesRecord.getEnergy().getCalories();
                // log the calories data
            }

            final double finalTotalCaloriesCount = totalCaloriesCount / 1000;

            runOnUiThread(() -> {
                totalCalories.setText(String.format(Locale.getDefault(), "%.2f", finalTotalCaloriesCount));
            });
        }, executor).exceptionally(e -> {
            Log.e("HealthConnectActivity", "Error retrieving calories data", e);
            return null;
        });
    }

    /**
     * Retrieve and display sleep data on a given time range
     * @param startTime
     * @param endTime
     */
    public void retrieveAndDisplaySleepData(ZonedDateTime startTime, ZonedDateTime endTime) {
        Log.d("HealthConnectActivity", "retrieveAndDisplaySleepData called");
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();

        //Retrieve calories data from yesterday
        CompletableFuture<Duration> future = healthConnect.readTotalSleepByTimeRange(start, end);
        future.thenAcceptAsync(response -> {
            long total_sleep = response.toMinutes();

            Log.d("HealthConnectActivity", "retrieveAndDisplayTotalSleepData start");

            final double final_total_sleep = total_sleep;

            runOnUiThread(() -> {
                totalSleep.setText(String.format(Locale.getDefault(), "%.2f", final_total_sleep));
            });
        }, executor).exceptionally(e -> {
            Log.e("HealthConnectActivity", "Error retrieving sleep data", e);
            return null;
        });
    }

    /**
     * Retrieve and display exercise session data on a given time range
     * @param startTime
     * @param endTime
     */
    public void retrieveAndDisplayExerciseSessionData(ZonedDateTime startTime, ZonedDateTime endTime) {
        Instant start = startTime.toInstant();
        Instant end = endTime.toInstant();
        CompletableFuture<ReadRecordsResponse<ExerciseSessionRecord>> future = healthConnect.readExerciseSessionByTimeRange(start, end);
        future.thenAcceptAsync(response -> {
            List<ExerciseSessionRecord> stepsRecords = response.getRecords();
            double totalExcerciseSession = 0;
            // TODO: get the duration of the exercise session
            for (ExerciseSessionRecord exerciseSessionRecord : stepsRecords) {
                Instant startTimeOfSession = exerciseSessionRecord.getStartTime();
                Instant endTimeOfSession = exerciseSessionRecord.getEndTime();
                long duration = Duration.between(startTimeOfSession, endTimeOfSession).toMinutes();
                totalExcerciseSession += duration;
            }

            final double finalTotalExerciseSession = totalExcerciseSession;

            runOnUiThread(() -> {
                total_exercise_session.setText(String.format(Locale.getDefault(), "%.2f", finalTotalExerciseSession));
            });
        }, executor);
    }
}
