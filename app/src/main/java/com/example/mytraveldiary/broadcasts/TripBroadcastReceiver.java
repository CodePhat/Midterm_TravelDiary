package com.example.mytraveldiary.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Custom BroadcastReceiver for handling trip-related events
 */
public class TripBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIP_ADDED = "com.example.mytraveldiary.TRIP_ADDED";
    public static final String ACTION_TRIP_UPDATED = "com.example.mytraveldiary.TRIP_UPDATED";
    public static final String ACTION_TRIP_DELETED = "com.example.mytraveldiary.TRIP_DELETED";
    public static final String ACTION_DATA_SYNCED = "com.example.mytraveldiary.DATA_SYNCED";

    public static final String EXTRA_TRIP_DESTINATION = "trip_destination";
    public static final String EXTRA_TRIP_ID = "trip_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        String tripDestination = intent.getStringExtra(EXTRA_TRIP_DESTINATION);

        switch (action) {
            case ACTION_TRIP_ADDED:
                handleTripAdded(context, tripDestination);
                break;

            case ACTION_TRIP_UPDATED:
                handleTripUpdated(context, tripDestination);
                break;

            case ACTION_TRIP_DELETED:
                handleTripDeleted(context, tripDestination);
                break;

            case ACTION_DATA_SYNCED:
                handleDataSynced(context);
                break;

            case Intent.ACTION_BOOT_COMPLETED:
                // Re-schedule alarms after device reboot
                handleBootCompleted(context);
                break;
        }
    }

    private void handleTripAdded(Context context, String destination) {
        // Notify other components that a trip was added
        Toast.makeText(context, "Trip added: " + destination, Toast.LENGTH_SHORT).show();
    }

    private void handleTripUpdated(Context context, String destination) {
        Toast.makeText(context, "Trip updated: " + destination, Toast.LENGTH_SHORT).show();
    }

    private void handleTripDeleted(Context context, String destination) {
        Toast.makeText(context, "Trip deleted: " + destination, Toast.LENGTH_SHORT).show();
    }

    private void handleDataSynced(Context context) {
        Toast.makeText(context, "Data synced successfully", Toast.LENGTH_SHORT).show();
    }

    private void handleBootCompleted(Context context) {
        // Re-schedule trip alarms after device reboot
        Intent serviceIntent = new Intent(context, com.example.mytraveldiary.services.TripSyncService.class);
        serviceIntent.setAction(com.example.mytraveldiary.services.TripSyncService.ACTION_RESCHEDULE_ALARMS);
        context.startService(serviceIntent);
    }
}
