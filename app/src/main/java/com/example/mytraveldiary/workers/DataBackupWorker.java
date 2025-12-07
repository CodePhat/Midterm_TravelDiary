package com.example.mytraveldiary.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mytraveldiary.data.database.AppDatabase;
import com.example.mytraveldiary.data.database.entities.TripEntity;

import java.util.List;

/**
 * WorkManager Worker for periodic data backup
 * Demonstrates: WorkManager, Background Tasks, Constraints
 */
public class DataBackupWorker extends Worker {
    private static final String TAG = "DataBackupWorker";

    public DataBackupWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting data backup...");

        try {
            // Perform backup operation
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<TripEntity> trips = db.tripDao().getAllTripsSync();

            // Simulate backup to cloud or external storage
            // In real app, this would upload to Firebase/server
            Log.d(TAG, "Backing up " + trips.size() + " trips");

            // Simulate network delay
            Thread.sleep(2000);

            Log.d(TAG, "Backup completed successfully");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Backup failed", e);
            return Result.retry(); // Retry on failure
        }
    }
}
