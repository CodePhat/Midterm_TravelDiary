package com.example.mytraveldiary.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mytraveldiary.services.notification.TravelDiaryNotificationManager;

/**
 * Broadcast Receiver for daily diary reminders
 * Handles:
 * - Daily reminder alarms
 * - Boot completed (to reschedule alarms)
 */
public class DiaryReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "DiaryReminderReceiver";
    public static final String ACTION_DAILY_REMINDER = "com.example.mytraveldiary.DAILY_DIARY_REMINDER";

    private static final String PREFS_NAME = "TravelDiaryPrefs";
    private static final String PREF_LAST_VISITED_LOCATION = "last_visited_location";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_DAILY_REMINDER.equals(action)) {
            Log.d(TAG, "Daily diary reminder triggered");
            handleDailyReminder(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "Boot completed - rescheduling alarms");
            rescheduleAlarms(context);
        }
    }

    /**
     * Handle daily reminder
     */
    private void handleDailyReminder(Context context) {
        TravelDiaryNotificationManager notificationManager =
            new TravelDiaryNotificationManager(context);

        // Get last visited location from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastLocation = prefs.getString(PREF_LAST_VISITED_LOCATION, null);

        // Show daily reminder
        notificationManager.showDailyDiaryReminder(lastLocation);

        Log.d(TAG, "Showed daily diary reminder for location: " + lastLocation);
    }

    /**
     * Reschedule alarms after device boot
     */
    private void rescheduleAlarms(Context context) {
        TravelDiaryNotificationManager notificationManager =
            new TravelDiaryNotificationManager(context);

        // Reschedule daily reminder if it was enabled
        if (notificationManager.areDailyRemindersEnabled()) {
            int hour = notificationManager.getReminderHour();
            int minute = notificationManager.getReminderMinute();
            notificationManager.scheduleDailyDiaryReminder(hour, minute);
            Log.d(TAG, "Rescheduled daily reminder for " + hour + ":" + minute);
        }
    }

    /**
     * Update last visited location (called from activities/services)
     */
    public static void updateLastVisitedLocation(Context context, String locationName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LAST_VISITED_LOCATION, locationName).apply();
        Log.d(TAG, "Updated last visited location: " + locationName);
    }
}
