package edu.northeastern.groupprojectgroup20.healthconnect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

import edu.northeastern.groupprojectgroup20.R;

public class HealthDataService extends Service {

    private static final String CHANNEL_ID = "HealthDataChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        // Schedule your WorkManager job here
        scheduleHealthDataFetch();

        return START_NOT_STICKY;
    }

    private void scheduleHealthDataFetch() {
//        WorkRequest healthDataFetchRequest = new OneTimeWorkRequest.Builder(HealthDataFetchWorker.class).build();
        PeriodicWorkRequest.Builder workBuilder = new PeriodicWorkRequest.Builder(
                HealthDataFetchWorker.class,
                1,
                TimeUnit.HOURS);

        PeriodicWorkRequest request = workBuilder.build();
        WorkManager.getInstance(this).enqueue(request);
    }

    private Notification createNotification() {
        // Create a notification for the foreground service
        // Make sure to follow Android's notification guidelines
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Health Data Sync")
                .setContentText("Syncing health data in progress...")
                .setSmallIcon(R.drawable.ic_profile); // Replace with your app's icon

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "test";
            String description = "this is to test the channel";// Replace with your channel description
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
