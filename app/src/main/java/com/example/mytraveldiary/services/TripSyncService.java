package com.example.mytraveldiary.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.mytraveldiary.broadcasts.TripBroadcastReceiver;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.utils.helpers.AlarmHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background service for syncing trip data and managing background tasks
 */
public class TripSyncService extends Service {

    private static final String TAG = "TripSyncService";
    public static final String ACTION_SYNC_DATA = "com.example.mytraveldiary.ACTION_SYNC_DATA";
    public static final String ACTION_RESCHEDULE_ALARMS = "com.example.mytraveldiary.ACTION_RESCHEDULE_ALARMS";

    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "TripSyncService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            switch (action) {
                case ACTION_SYNC_DATA:
                    performDataSync();
                    break;

                case ACTION_RESCHEDULE_ALARMS:
                    rescheduleAllAlarms();
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    private void performDataSync() {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Starting data sync...");

                // Simulate data sync operation
                Thread.sleep(2000);

                // Save data to persistent storage
                AppData.getInstance().triggerSave();

                Log.d(TAG, "Data sync completed");

                // Send broadcast that sync is complete
                Intent broadcastIntent = new Intent(TripBroadcastReceiver.ACTION_DATA_SYNCED);
                sendBroadcast(broadcastIntent);

                // Stop service after sync
                mainHandler.post(() -> stopSelf());

            } catch (Exception e) {
                Log.e(TAG, "Error during data sync", e);
                mainHandler.post(() -> stopSelf());
            }
        });
    }

    private void rescheduleAllAlarms() {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Rescheduling all trip alarms...");

                // Get all trips and reschedule their alarms
                AppData.getInstance().getTrips().forEach(trip -> {
                    if (trip.hasLocation()) {
                        AlarmHelper.scheduleTripReminder(
                            getApplicationContext(),
                            trip.getId(),
                            trip.getDestination(),
                            trip.getStartDate()
                        );
                    }
                });

                Log.d(TAG, "All alarms rescheduled");
                mainHandler.post(() -> stopSelf());

            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling alarms", e);
                mainHandler.post(() -> stopSelf());
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is a started service, not bound
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
        Log.d(TAG, "TripSyncService destroyed");
    }
}
