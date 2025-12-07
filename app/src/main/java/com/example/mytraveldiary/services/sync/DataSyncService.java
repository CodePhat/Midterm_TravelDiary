package com.example.mytraveldiary.services.sync;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.mytraveldiary.data.database.AppDatabase;
import com.example.mytraveldiary.data.database.entities.TripEntity;
import com.example.mytraveldiary.data.database.entities.UserEntity;
import com.example.mytraveldiary.data.firebase.sync.FirebaseSyncManager;

import java.util.List;

/**
 * Intent Service for background data synchronization with Firebase
 * Demonstrates: Intent Services, Background Processing, Cloud Sync
 */
public class DataSyncService extends IntentService {
    private static final String TAG = "DataSyncService";
    public static final String ACTION_SYNC_TRIPS = "com.example.mytraveldiary.SYNC_TRIPS";
    public static final String ACTION_SYNC_TO_CLOUD = "com.example.mytraveldiary.SYNC_TO_CLOUD";
    public static final String ACTION_SYNC_FROM_CLOUD = "com.example.mytraveldiary.SYNC_FROM_CLOUD";
    public static final String ACTION_SYNC_COMPLETE = "com.example.mytraveldiary.SYNC_COMPLETE";
    public static final String EXTRA_SYNC_STATUS = "sync_status";
    public static final String EXTRA_SYNC_MESSAGE = "sync_message";

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_CURRENT_USER = "current_user_email";

    public DataSyncService() {
        super("DataSyncService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        if (ACTION_SYNC_TRIPS.equals(action)) {
            syncTripsData();
        } else if (ACTION_SYNC_TO_CLOUD.equals(action)) {
            syncToCloud();
        } else if (ACTION_SYNC_FROM_CLOUD.equals(action)) {
            syncFromCloud();
        }
    }

    private void syncTripsData() {
        Log.d(TAG, "Starting local data synchronization...");

        try {
            AppDatabase db = AppDatabase.getInstance(this);
            List<TripEntity> trips = db.tripDao().getAllTripsSync();

            // Simulate network delay
            Thread.sleep(2000);

            Log.d(TAG, "Synced " + trips.size() + " trips locally");

            // Broadcast sync completion
            Intent broadcastIntent = new Intent(ACTION_SYNC_COMPLETE);
            broadcastIntent.putExtra(EXTRA_SYNC_STATUS, true);
            broadcastIntent.putExtra(EXTRA_SYNC_MESSAGE, "Local sync completed");
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        } catch (Exception e) {
            Log.e(TAG, "Local sync failed", e);
            broadcastSyncFailure("Local sync failed: " + e.getMessage());
        }
    }

    private void syncToCloud() {
        Log.d(TAG, "Starting cloud sync (upload)...");

        try {
            // Get current user
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                Log.w(TAG, "No user logged in, skipping cloud sync");
                broadcastSyncFailure("No user logged in");
                return;
            }

            // Sync to Firebase
            FirebaseSyncManager syncManager = FirebaseSyncManager.getInstance(this);
            syncManager.performFullSync(userEmail, new FirebaseSyncManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Cloud sync completed successfully");
                    Intent broadcastIntent = new Intent(ACTION_SYNC_COMPLETE);
                    broadcastIntent.putExtra(EXTRA_SYNC_STATUS, true);
                    broadcastIntent.putExtra(EXTRA_SYNC_MESSAGE, "Data synced to cloud");
                    LocalBroadcastManager.getInstance(DataSyncService.this).sendBroadcast(broadcastIntent);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Cloud sync failed", e);
                    broadcastSyncFailure("Cloud sync failed: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Cloud sync error", e);
            broadcastSyncFailure("Cloud sync error: " + e.getMessage());
        }
    }

    private void syncFromCloud() {
        Log.d(TAG, "Starting cloud sync (download)...");

        try {
            String userEmail = getCurrentUserEmail();
            if (userEmail == null) {
                Log.w(TAG, "No user logged in, skipping cloud sync");
                broadcastSyncFailure("No user logged in");
                return;
            }

            FirebaseSyncManager syncManager = FirebaseSyncManager.getInstance(this);
            syncManager.syncTripsFromCloud(userEmail, new FirebaseSyncManager.SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Downloaded data from cloud successfully");
                    Intent broadcastIntent = new Intent(ACTION_SYNC_COMPLETE);
                    broadcastIntent.putExtra(EXTRA_SYNC_STATUS, true);
                    broadcastIntent.putExtra(EXTRA_SYNC_MESSAGE, "Data synced from cloud");
                    LocalBroadcastManager.getInstance(DataSyncService.this).sendBroadcast(broadcastIntent);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to download from cloud", e);
                    broadcastSyncFailure("Download failed: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Cloud sync error", e);
            broadcastSyncFailure("Cloud sync error: " + e.getMessage());
        }
    }

    private String getCurrentUserEmail() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString(KEY_CURRENT_USER, null);

        if (email == null) {
            // Fallback: get first user from database
            AppDatabase db = AppDatabase.getInstance(this);
            UserEntity user = db.userDao().getCurrentUser();
            if (user != null) {
                email = user.getEmail();
            }
        }

        return email;
    }

    private void broadcastSyncFailure(String message) {
        Intent broadcastIntent = new Intent(ACTION_SYNC_COMPLETE);
        broadcastIntent.putExtra(EXTRA_SYNC_STATUS, false);
        broadcastIntent.putExtra(EXTRA_SYNC_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}
