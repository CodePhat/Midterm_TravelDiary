package com.example.mytraveldiary.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mytraveldiary.services.TripSyncService;
import com.example.mytraveldiary.utils.helpers.NotificationHelper;

/**
 * BroadcastReceiver for handling alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_TRIP_REMINDER = "com.example.mytraveldiary.ACTION_TRIP_REMINDER";
    public static final String ACTION_DAILY_SYNC = "com.example.mytraveldiary.ACTION_DAILY_SYNC";

    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_TRIP_DESTINATION = "trip_destination";
    public static final String EXTRA_TRIP_DATE = "trip_date";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Received alarm action: " + action);

        switch (action) {
            case ACTION_TRIP_REMINDER:
                handleTripReminder(context, intent);
                break;

            case ACTION_DAILY_SYNC:
                handleDailySync(context);
                break;
        }
    }

    private void handleTripReminder(Context context, Intent intent) {
        String tripId = intent.getStringExtra(EXTRA_TRIP_ID);
        String destination = intent.getStringExtra(EXTRA_TRIP_DESTINATION);
        String date = intent.getStringExtra(EXTRA_TRIP_DATE);

        Log.d(TAG, "Trip reminder for: " + destination);

        // Show notification
        NotificationHelper.showTripReminderNotification(context, tripId, destination, date);
    }

    private void handleDailySync(Context context) {
        Log.d(TAG, "Daily sync triggered");

        // Start sync service
        Intent serviceIntent = new Intent(context, TripSyncService.class);
        serviceIntent.setAction(TripSyncService.ACTION_SYNC_DATA);
        context.startService(serviceIntent);
    }
}
