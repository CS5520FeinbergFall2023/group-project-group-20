package edu.northeastern.groupprojectgroup20.healthconnect;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.health.connect.client.records.ExerciseSessionRecord;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord;
import androidx.health.connect.client.response.ReadRecordsResponse;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class HealthDataFetchWorker extends Worker {
    private HealthConnect healthConnect;
    private ZonedDateTime lastFetchTime;
    private long finalTotalStepsCount;
    private double finalTotalCaloriesBurned;
    private long finalTotalSleepTime;
    private double finalTotalExerciseSession;
    private HealtData healthDataToUpdate;
    private DatabaseReference databaseRef;

    public HealthDataFetchWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        finalTotalStepsCount = 0;
        finalTotalCaloriesBurned = 0;
        finalTotalExerciseSession = 0;
        finalTotalSleepTime = 0;
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public Result doWork() {
        // Add code to retrieve data from Health Connect
        // For example, readStepsByTimeRange(), readCaloriesByTimeRange(), etc.

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startTime = now.minusHours(1);

        fetchHealthData(startTime, now);
        updateHealthDataToFirebase(healthDataToUpdate);
        lastFetchTime = ZonedDateTime.now();
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void fetchHealthData(ZonedDateTime startTime, ZonedDateTime endTime) {
        //Fetch fitness data from health connect
        HealthConnect healthConnect = new HealthConnect(getApplicationContext());
        retrieveStepsData(startTime, endTime);
        retrieveCaloriesData(startTime, endTime);
        retrieveSleepData(startTime, endTime);
        retrieveExerciseSessionData(startTime, endTime);
        healthDataToUpdate = new HealtData(finalTotalStepsCount,
                finalTotalCaloriesBurned, finalTotalExerciseSession, finalTotalSleepTime, ZonedDateTime.now());
    }

    private void updateHealthDataToFirebase(HealtData healthDataToUpdate) {
        //Update fitness data in the firebase database
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String date = healthDataToUpdate.getTimeStamp().format(formatter);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String UserID = firebaseUser.getUid();

//        databaseRef.child("History").child(UserID).child(date).setValue(healthDataToUpdate);
        databaseRef.child("History").child(UserID).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // data exists, add the newest data to the existing data
                    HealtData existingData = snapshot.getValue(HealtData.class);
                    HealtData newData = new HealtData(existingData.getSteps() + healthDataToUpdate.getSteps(),
                            existingData.getCalories() + healthDataToUpdate.getCalories(),
                            existingData.getExercise() + healthDataToUpdate.getExercise(),
                            existingData.getSleep() + healthDataToUpdate.getSleep(),
                            ZonedDateTime.now());
                    databaseRef.child("History").child(UserID).child(date).setValue(healthDataToUpdate);
                } else {
                    // data doesn't exist, do something else
                    databaseRef.child("History").child(UserID).child(date).setValue(healthDataToUpdate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("HealthDataFetchWorker", "updateHealthDataToFirebase failed");
            }
        });
    }

    /**
     * Retrieves steps data from Health Connect
     *
     * @param startTime
     * @param endTime
     */
    public void retrieveStepsData(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            Instant start = startTime.toInstant();
            Instant end = endTime.toInstant();
            CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(start, end);
            future.exceptionally(ex -> {
                // Handle the exception here
                System.err.println("Error reading steps data: " + ex.getMessage());
                return null;
            });
            future.thenAcceptAsync(response -> {
                Log.d("HealthConnectActivity", "retrieveStepsData called");
                if (response != null) {
                    List<StepsRecord> stepsRecords = response.getRecords();
                    long totalStepsCount = 0;
                    for (StepsRecord stepsRecord : stepsRecords) {
                        totalStepsCount += stepsRecord.getCount();
                    }
                    finalTotalStepsCount = totalStepsCount;
                }
            });
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            System.err.println("Error in retrieveAndDisplayStepsData: " + e.getMessage());
        }
    }

    /**
     * Retrieves calories data from Health Connect
     *
     * @param startTime
     * @param endTime
     */
    public void retrieveCaloriesData(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            Instant start = startTime.toInstant();
            Instant end = endTime.toInstant();
            CompletableFuture<ReadRecordsResponse<TotalCaloriesBurnedRecord>> future = healthConnect.readCaloriesByTimeRange(start, end);
            future.thenAcceptAsync(response -> {
                List<TotalCaloriesBurnedRecord> caloriesRecords = response.getRecords();

                Log.d("HealthConnectActivity", "retrieveCaloriesData called");
                double totalCaloriesCount = 0;

                for (TotalCaloriesBurnedRecord caloriesRecord : caloriesRecords) {
                    totalCaloriesCount += caloriesRecord.getEnergy().getCalories();
                    // log the calories data
                    Log.d("Calories", "Calories: " + caloriesRecord.getEnergy().getCalories());
                    System.out.println("Calories: " + caloriesRecord.getEnergy().getCalories());
                    System.out.println("nice");
                }

                finalTotalCaloriesBurned = totalCaloriesCount / 1000;

            });
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            System.err.println("Error in retrieveAndDisplayStepsData: " + e.getMessage());
        }
    }

    /**
     * Retrieves sleep data from Health Connect
     *
     * @param startTime
     * @param endTime
     */
    public void retrieveSleepData(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
            Log.d("HealthConnectActivity", "retrieveSleepData called");
            Instant start = startTime.toInstant();
            Instant end = endTime.toInstant();

            //Retrieve calories data from yesterday
            CompletableFuture<Duration> future = healthConnect.readTotalSleepByTimeRange(start, end);
            future.thenAcceptAsync(response -> {
                long total_sleep = response.toMinutes();

                Log.d("HealthConnectActivity", "retrieveAndDisplayTotalSleepData start");

                finalTotalSleepTime = total_sleep;

            });
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            System.err.println("Error in retrieveAndDisplaySleepData: " + e.getMessage());
        }
    }

    /**
     * Retrieves exercise session data from Health Connect
     *
     * @param startTime
     * @param endTime
     */
    public void retrieveExerciseSessionData(ZonedDateTime startTime, ZonedDateTime endTime) {
        try {
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

                finalTotalExerciseSession = totalExcerciseSession;
            });
        } catch (Exception e) {
            // Handle any other exceptions that might occur
            System.err.println("Error in retrieveAndDisplaySleepData: " + e.getMessage());
        }
    }


}
