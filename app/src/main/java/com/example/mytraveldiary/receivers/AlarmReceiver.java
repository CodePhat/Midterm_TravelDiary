package com.example.mytraveldiary.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mytraveldiary.data.database.AppDatabase;
import com.example.mytraveldiary.data.database.entities.TripEntity;
import com.example.mytraveldiary.services.notification.TripNotificationManager;

import java.util.Calendar;
import java.util.List;

/**
 * Broadcast Receiver for scheduled alarms
 * Demonstrates: Alarms, Scheduled Tasks, AlarmManager
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final String ACTION_TRIP_REMINDER = "com.example.mytraveldiary.TRIP_REMINDER";
    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_TRIP_TITLE = "trip_title";
    public static final String EXTRA_TRIP_MESSAGE = "trip_message";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_TRIP_REMINDER.equals(intent.getAction())) {
            long tripId = intent.getLongExtra(EXTRA_TRIP_ID, -1);
            String tripTitle = intent.getStringExtra(EXTRA_TRIP_TITLE);
            String message = intent.getStringExtra(EXTRA_TRIP_MESSAGE);

            Log.d(TAG, "Trip reminder triggered for: " + tripTitle);

            // Show notification
            TripNotificationManager notificationManager = new TripNotificationManager(context);
            notificationManager.showTripReminder(
                (int) tripId,
                tripTitle,
                message != null ? message : "Your trip is coming up soon!",
                tripId
            );
        }
    }

    /**
     * Schedule a trip reminder alarm
     */
    public static void scheduleRipReminder(Context context, long tripId, String tripTitle,
                                           long triggerTimeMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIP_REMINDER);
        intent.putExtra(EXTRA_TRIP_ID, tripId);
        intent.putExtra(EXTRA_TRIP_TITLE, tripTitle);
        intent.putExtra(EXTRA_TRIP_MESSAGE, "Your trip to " + tripTitle + " is starting soon!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) tripId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Schedule exact alarm (for important trip reminders)
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            );

            Log.d(TAG, "Scheduled trip reminder for: " + tripTitle);
        }
    }

    /**
     * Schedule a repeating daily reminder
     */
    public static void scheduleDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIP_REMINDER);
        intent.putExtra(EXTRA_TRIP_TITLE, "Daily Reminder");
        intent.putExtra(EXTRA_TRIP_MESSAGE, "Don't forget to update your travel diary!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            9999,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set alarm for 9 AM daily
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (alarmManager != null) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            );

            Log.d(TAG, "Scheduled daily reminder at 9 AM");
        }
    }

    /**
     * Cancel a trip reminder
     */
    public static void cancelTripReminder(Context context, long tripId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIP_REMINDER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) tripId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Cancelled trip reminder for ID: " + tripId);
        }
    }

    /**
     * Re-schedule all trip reminders (called after boot)
     */
    public static void rescheduleAllAlarms(Context context) {
        // This would typically fetch all upcoming trips from database
        // and reschedule their alarms
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<TripEntity> trips = db.tripDao().getAllTripsSync();

            for (TripEntity trip : trips) {
                // Schedule reminder 1 day before trip start
                if (trip.getStartDate() != null) {
                    long tripStartTime = trip.getStartDate().getTime();
                    long reminderTime = tripStartTime - (24 * 60 * 60 * 1000); // 1 day before

                    if (reminderTime > System.currentTimeMillis()) {
                        scheduleRipReminder(context, trip.getId(), trip.getTitle(), reminderTime);
                    }
                }
            }

            Log.d(TAG, "Rescheduled " + trips.size() + " trip reminders");
        });
    }
}
