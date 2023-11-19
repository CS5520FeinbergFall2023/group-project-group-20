package edu.northeastern.groupprojectgroup20.healthconnect;

import android.health.connect.ReadRecordsRequest;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.health.connect.client.records.StepsRecord;
import androidx.health.connect.client.response.ReadRecordsResponse;

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
    private TextView totalSteps, totalDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_connect);

        healthConnect = new HealthConnect(this);
        totalSteps = findViewById(R.id.total_steps);
        totalDistance = findViewById(R.id.total_distance);

//        ZonedDateTime now = ZonedDateTime.now();
//        ZonedDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
//        ZonedDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfYesterday = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfYesterday = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        Instant startTime = startOfYesterday.toInstant();
        ;
        Instant endTime = endOfYesterday.toInstant();

        CompletableFuture<ReadRecordsResponse<StepsRecord>> future = healthConnect.readStepsByTimeRange(startTime, endTime);
        future.thenAcceptAsync(response -> {
            List<StepsRecord> stepsRecords = response.getRecords();
            long totalStepsCount = 0;
            for (StepsRecord stepsRecord : stepsRecords) {
                totalStepsCount += stepsRecord.getCount();
            }

            final long finalTotalStepsCount = totalStepsCount;

            runOnUiThread(() -> {
                totalSteps.setText(String.format(Locale.getDefault(), "%d", finalTotalStepsCount));
            });
        }, executor);


    }


}
