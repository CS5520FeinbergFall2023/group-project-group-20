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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.northeastern.groupprojectgroup20.data.model.GameData;
import edu.northeastern.groupprojectgroup20.data.model.HealthData;

public class HealthDataFetchWorker extends Worker {
    private HealthConnect healthConnect;
    private ZonedDateTime lastFetchTime;
    private long finalDailyStepsCount, finalTotalStepsCount;
    private double finalDailyCaloriesBurned, finalTotalCaloriesBurned;
    private long finalDailySleepTime, finalTotalSleepTime;
    private double finalDailyExerciseSession, finalTotalExerciseSession;
    private HealthData healthDataToUpdate;
    private GameData gameDataToUpdate;
    private DatabaseReference databaseRef;

    public HealthDataFetchWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        finalDailyStepsCount = 0;
        finalDailyCaloriesBurned = 0;
        finalDailyExerciseSession = 0;
        finalDailySleepTime = 0;
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public Result doWork() {

        resetWorkerState();
        // Add code to retrieve data from Health Connect
        // For example, readStepsByTimeRange(), readCaloriesByTimeRange(), etc.

        healthConnect = new HealthConnect(getApplicationContext());
        // check if health connect is connected
        if (healthConnect == null) {
            Log.d("HealthDataFetchWorker", "healthConnect is null");
        } else {
            Log.d("HealthDataFetchWorker", "healthConnect is not null");
        }


        // Set Today's start and end time
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        fetchHealthData(startTime, endTime);
        fetchAccumulatedHealthData();

        lastFetchTime = ZonedDateTime.now();
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void resetWorkerState() {
        finalDailyStepsCount = 0;
        finalDailyCaloriesBurned = 0;
        finalDailyExerciseSession = 0;
        finalDailySleepTime = 0;
        // Reset any other relevant variables here
    }

    private void fetchHealthData(ZonedDateTime startTime, ZonedDateTime endTime) {
        //Fetch fitness data from health connect


        Log.d("HealthDataFetchWorker", "fetchHealthData called");

        // Wait for all the futures to complete
        // Call the methods to retrieve fitness data
        CompletableFuture<Void> stepsFuture = retrieveStepsData(startTime, endTime);
        CompletableFuture<Void> caloriesFuture = retrieveCaloriesData(startTime, endTime);
        CompletableFuture<Void> sleepFuture = retrieveSleepData(startTime, endTime);
        CompletableFuture<Void> exerciseFuture = retrieveExerciseSessionData(startTime, endTime);

        // Wait for all the futures to complete
        CompletableFuture.allOf(stepsFuture, caloriesFuture, sleepFuture, exerciseFuture)
                .thenRun(() -> {
                    // After all futures complete, update the health data
                    healthDataToUpdate = new HealthData(finalDailyStepsCount,
                            finalDailyCaloriesBurned, finalDailyExerciseSession, finalDailySleepTime, ZonedDateTime.now());

                    if (healthDataToUpdate != null) {
                        updateHealthDataToFirebase(healthDataToUpdate);
                    } else {
                        Log.e("HealthDataFetchWorker", "healthDataToUpdate is null");
                    }
                }).join();  // Join here to block the worker thread until all futures are complete
        //log all the data
        Log.d("HealthDataFetchWorker", "fetchHealthData called");
        Log.d("HealthDataFetchWorker", "Steps: " + finalDailyStepsCount);
        Log.d("HealthDataFetchWorker", "Calories: " + finalDailyCaloriesBurned);
        Log.d("HealthDataFetchWorker", "Sleep: " + finalDailySleepTime);
        Log.d("HealthDataFetchWorker", "Exercise: " + finalDailyExerciseSession);

    }

    private void fetchAccumulatedHealthData() {
        //Fetch fitness data from health connect

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String UserID = firebaseUser.getUid();

        ZonedDateTime now = ZonedDateTime.now();
        Log.d("HealthDataFetchWorker", "fetchAccumulatedHealthData called");


        final String[] registerDate = new String[1];
        databaseRef.child("Register Users").child(UserID).child("registerDate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                registerDate[0] = dataSnapshot.getValue(String.class);
                // Use registerDate here...
                Log.d("HealthDataFetchWorker", "registerDate: " + registerDate[0]);

                if (registerDate[0] != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(registerDate[0], formatter);
                    ZonedDateTime registerDateTime = localDateTime.atZone(ZoneId.systemDefault());

                    CompletableFuture<Void> stepsFuture = retrieveTotalStepsData(registerDateTime, now);
                    CompletableFuture<Void> caloriesFuture = retrieveTotalCaloriesData(registerDateTime, now);
                    CompletableFuture<Void> sleepFuture = retrieveTotalSleepData(registerDateTime, now);
                    CompletableFuture<Void> exerciseFuture = retrieveTotalExerciseSessionData(registerDateTime, now);

                    // Wait for all the futures to complete
                    CompletableFuture.allOf(stepsFuture, caloriesFuture, sleepFuture, exerciseFuture)
                            .thenRun(() -> {

                                // After all futures complete, update the health data
                                GameData gameDataToUpdate = new GameData(finalTotalStepsCount,
                                        finalTotalCaloriesBurned, finalTotalExerciseSession, finalTotalSleepTime, ZonedDateTime.now());

                                if (gameDataToUpdate != null) {
                                    updateGameDataToFirebase(gameDataToUpdate);
                                } else {
                                    Log.e("HealthDataFetchWorker", "healthDataToUpdate is null");
                                }
                            }).join();  // Join here to block the worker thread until all futures are complete
                    //log all the data
                    Log.d("HealthDataFetchWorker", "fetchHealthData called");
                    Log.d("HealthDataFetchWorker", "Steps: " + finalDailyStepsCount);
                    Log.d("HealthDataFetchWorker", "Calories: " + finalDailyCaloriesBurned);
                    Log.d("HealthDataFetchWorker", "Sleep: " + finalDailySleepTime);
                    Log.d("HealthDataFetchWorker", "Exercise: " + finalDailyExerciseSession);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error here...
            }
        });

    }

    private void updateHealthDataToFirebase(HealthData healthDataToUpdate) {
        //Update fitness data in the firebase database
        String date = healthDataToUpdate.getLastUpdateTime();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String UserID = firebaseUser.getUid();

        // Check if UserID exists under History
        databaseRef.child("History").child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // UserID doesn't exist under History, create a new child with UserID
                    databaseRef.child("History").child(UserID).setValue("");
                }
                // Now UserID exists under History, proceed to upload health data
                databaseRef.child("History").child(UserID).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("HealthDataFetchWorker", "updateHealthDataToFirebase called");
//                        if (snapshot.exists()) {
//                            // data exists, add the newest data to the existing data
//                            HealthData existingData = snapshot.getValue(HealthData.class);
//                            HealthData newData = new HealthData(existingData.getSteps() + healthDataToUpdate.getSteps(),
//                                    existingData.getCalories() + healthDataToUpdate.getCalories(),
//                                    existingData.getExercise() + healthDataToUpdate.getExercise(),
//                                    existingData.getSleep() + healthDataToUpdate.getSleep(),
//                                    ZonedDateTime.now());
//                            databaseRef.child("History").child(UserID).child(date).setValue(newData);
//                        } else {
//                            // data doesn't exist, do something else
//                            databaseRef.child("History").child(UserID).child(date).setValue(healthDataToUpdate);
//                        }
                        databaseRef.child("History").child(UserID).child(date).setValue(healthDataToUpdate);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("HealthDataFetchWorker", "updateHealthDataToFirebase failed");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("HealthDataFetchWorker", "updateHealthDataToFirebase failed");
            }
        });
    }

    // Update Game Data to the firebase database

    private void updateGameDataToFirebase(GameData gameDataToUpdate) {
        //Update fitness data in the firebase database
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        String date = healthDataToUpdate.getTimeStamp().format(formatter);

        String date = gameDataToUpdate.getLastUpdateTime();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String UserID = firebaseUser.getUid();

        // Check if UserID exists under History
        databaseRef.child("Game Data").child(UserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("HealthDataFetchWorker", "updateGameDataToFirebase called");
//                if (snapshot.exists()) {
//                    //If Game data exists, add the newest data to the existing data
//                    GameData existingData = snapshot.getValue(GameData.class);
//                    GameData newData = new GameData(existingData.getTotalAccumulatedSteps() + gameDataToUpdate.getTotalAccumulatedSteps(),
//                            existingData.getTotalAccumulatedCalories() + gameDataToUpdate.getTotalAccumulatedCalories(),
//                            existingData.getTotalAccumulatedExercise() + gameDataToUpdate.getTotalAccumulatedExercise(),
//                            existingData.getTotalAccumulatedSleep() + gameDataToUpdate.getTotalAccumulatedSleep(),
//                            ZonedDateTime.now());
//                    databaseRef.child("Game Data").child(UserID).setValue(newData);
//                    Log.d("HealthDataFetchWorker", "updateGameDataToFirebase called, update existing data");
//                } else {
//                    // If data doesn't exist, upload the newest data
//                    databaseRef.child("Game Data").child(UserID).setValue(gameDataToUpdate);
//                    Log.d("HealthDataFetchWorker", "updateGameDataToFirebase called, upload the first data");
//
//                }

                databaseRef.child("Game Data").child(UserID).setValue(gameDataToUpdate);
            }
            // Now UserID exists under History, proceed to upload health data
//                databaseRef.child("Game Data").child(UserID).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            //If Game data exists, add the newest data to the existing data
//                            GameData existingData = snapshot.getValue(GameData.class);
//                            GameData newData = new GameData(existingData.getTotalAccumulatedSteps() + gameDataToUpdate.getTotalAccumulatedSteps(),
//                                    existingData.getTotalAccumulatedCalories() + gameDataToUpdate.getTotalAccumulatedCalories(),
//                                    existingData.getTotalAccumulatedExercise() + gameDataToUpdate.getTotalAccumulatedExercise(),
//                                    existingData.getTotalAccumulatedSleep() + gameDataToUpdate.getTotalAccumulatedSleep(),
//                                    ZonedDateTime.now());
//                            databaseRef.child("Game Data").child(UserID).child(date).setValue(newData);
//                            Log.d("HealthDataFetchWorker", "updateGameDataToFirebase called, update existing data");
//                        } else {
//                            // If data doesn't exist, upload the newest data
//                            databaseRef.child("Game Data").child(UserID).child(date).setValue(gameDataToUpdate);
//                            Log.d("HealthDataFetchWorker", "updateGameDataToFirebase called, upload the first data");
//
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Log.d("HealthDataFetchWorker", "updateGameDataToFirebase failed");
//                    }
//                });
//            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("HealthDataFetchWorker", "updateHealthDataToFirebase failed");
            }
        });
    }

    /**
     * Retrieve steps data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */

    public CompletableFuture<Void> retrieveStepsData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(start, end);

                ReadRecordsResponse<StepsRecord> response = future.join(); // Blocking call to wait for the future to complete

                Log.d("HealthDataFetchWorker", "retrieveStepsData called");
                if (response != null) {
                    List<StepsRecord> stepsRecords = response.getRecords();
                    long totalStepsCount = 0;
                    for (StepsRecord stepsRecord : stepsRecords) {
                        totalStepsCount += stepsRecord.getCount();
                    }
                    finalDailyStepsCount = totalStepsCount;
                }
            } catch (Exception e) {
                // Handle any other exceptions that might occur
                throw new RuntimeException("Error in retrieveStepsData: " + e.getMessage());

            }
        });
    }

    /**
     * Retrieve calories data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveCaloriesData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<TotalCaloriesBurnedRecord>> future = healthConnect.readCaloriesByTimeRange(start, end);

                ReadRecordsResponse<TotalCaloriesBurnedRecord> response = future.join(); // Wait for the future to complete

                Log.d("HealthConnectActivity", "retrieveCaloriesData called");
                if (response != null) {
                    List<TotalCaloriesBurnedRecord> caloriesRecords = response.getRecords();
                    double totalCaloriesCount = 0;

                    for (TotalCaloriesBurnedRecord caloriesRecord : caloriesRecords) {
                        totalCaloriesCount += caloriesRecord.getEnergy().getCalories();
                        // log the calories data
//                        Log.d("Calories", "Calories: " + caloriesRecord.getEnergy().getCalories());
                    }

                    finalDailyCaloriesBurned = totalCaloriesCount / 1000;
                }
            } catch (Exception e) {
                // Handle any other exceptions that might occur
                System.err.println("Error in retrieveCaloriesData: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieve sleep data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveSleepData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Log.d("HealthConnectActivity", "retrieveSleepData called");
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();

                CompletableFuture<Duration> future = healthConnect.readTotalSleepByTimeRange(start, end);
                Duration totalSleepDuration = future.join(); // Wait for the future to complete

                Log.d("HealthConnectActivity", "retrieveAndDisplayTotalSleepData start");
                finalDailySleepTime = totalSleepDuration.toMinutes();

            } catch (Exception e) {
                System.err.println("Error in retrieveSleepData: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieve exercise session data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */

    public CompletableFuture<Void> retrieveExerciseSessionData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<ExerciseSessionRecord>> future = healthConnect.readExerciseSessionByTimeRange(start, end);

                ReadRecordsResponse<ExerciseSessionRecord> response = future.join(); // Wait for the future to complete

                double totalExerciseSession = 0;
                for (ExerciseSessionRecord exerciseSessionRecord : response.getRecords()) {
                    Instant startTimeOfSession = exerciseSessionRecord.getStartTime();
                    Instant endTimeOfSession = exerciseSessionRecord.getEndTime();
                    long duration = Duration.between(startTimeOfSession, endTimeOfSession).toMinutes();
                    totalExerciseSession += duration;
                }

                finalDailyExerciseSession = totalExerciseSession;

            } catch (Exception e) {
                System.err.println("Error in retrieveExerciseSessionData: " + e.getMessage());
            }
        });
    }


    // Final accumulated data

    /**
     * Retrieve Accumulated steps data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveTotalStepsData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(start, end);

                ReadRecordsResponse<StepsRecord> response = future.join(); // Blocking call to wait for the future to complete

                Log.d("HealthDataFetchWorker", "retrieveStepsData called");
                if (response != null) {
                    List<StepsRecord> stepsRecords = response.getRecords();
                    long totalStepsCount = 0;
                    for (StepsRecord stepsRecord : stepsRecords) {
                        totalStepsCount += stepsRecord.getCount();
                    }
                    finalTotalStepsCount = totalStepsCount;
                }
            } catch (Exception e) {
                // Handle any other exceptions that might occur
                System.err.println("Error in retrieveStepsData: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieve Accumulated calories data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveTotalCaloriesData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<TotalCaloriesBurnedRecord>> future = healthConnect.readCaloriesByTimeRange(start, end);

                ReadRecordsResponse<TotalCaloriesBurnedRecord> response = future.join(); // Wait for the future to complete

                Log.d("HealthConnectActivity", "retrieveCaloriesData called");
                if (response != null) {
                    List<TotalCaloriesBurnedRecord> caloriesRecords = response.getRecords();
                    double totalCaloriesCount = 0;

                    for (TotalCaloriesBurnedRecord caloriesRecord : caloriesRecords) {
                        totalCaloriesCount += caloriesRecord.getEnergy().getCalories();
                        // log the calories data
//                        Log.d("Calories", "Calories: " + caloriesRecord.getEnergy().getCalories());
                    }

                    finalTotalCaloriesBurned = totalCaloriesCount / 1000;
                }
            } catch (Exception e) {
                // Handle any other exceptions that might occur
                System.err.println("Error in retrieveCaloriesData: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieve Accumulated sleep data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveTotalSleepData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Log.d("HealthConnectActivity", "retrieveSleepData called");
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();

                CompletableFuture<Duration> future = healthConnect.readTotalSleepByTimeRange(start, end);
                Duration totalSleepDuration = future.join(); // Wait for the future to complete

                Log.d("HealthConnectActivity", "retrieveAndDisplayTotalSleepData start");
                finalTotalSleepTime = totalSleepDuration.toMinutes();

            } catch (Exception e) {
                System.err.println("Error in retrieveSleepData: " + e.getMessage());
            }
        });
    }

    /**
     * Retrieve Accumulated exercise session data from Health Connect
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public CompletableFuture<Void> retrieveTotalExerciseSessionData(ZonedDateTime startTime, ZonedDateTime endTime) {
        return CompletableFuture.runAsync(() -> {
            try {
                Instant start = startTime.toInstant();
                Instant end = endTime.toInstant();
                CompletableFuture<ReadRecordsResponse<ExerciseSessionRecord>> future = healthConnect.readExerciseSessionByTimeRange(start, end);

                ReadRecordsResponse<ExerciseSessionRecord> response = future.join(); // Wait for the future to complete

                double totalExerciseSession = 0;
                for (ExerciseSessionRecord exerciseSessionRecord : response.getRecords()) {
                    Instant startTimeOfSession = exerciseSessionRecord.getStartTime();
                    Instant endTimeOfSession = exerciseSessionRecord.getEndTime();
                    long duration = Duration.between(startTimeOfSession, endTimeOfSession).toMinutes();
                    totalExerciseSession += duration;
                }

                finalTotalExerciseSession = totalExerciseSession;

            } catch (Exception e) {
                System.err.println("Error in retrieveExerciseSessionData: " + e.getMessage());
            }
        });
    }


}