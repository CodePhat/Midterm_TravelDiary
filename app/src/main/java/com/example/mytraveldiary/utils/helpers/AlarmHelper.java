package com.example.mytraveldiary.utils.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.mytraveldiary.broadcasts.AlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class for managing trip reminder alarms
 */
public class AlarmHelper {

    private static final String TAG = "AlarmHelper";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /**
     * Schedule a trip reminder alarm
     * Sets alarm for 1 day before the trip start date at 9:00 AM
     */
    public static void scheduleTripReminder(Context context, String tripId,
                                           String destination, String startDate) {
        try {
            Date tripDate = DATE_FORMAT.parse(startDate);
            if (tripDate == null) {
                Log.e(TAG, "Failed to parse trip date: " + startDate);
                return;
            }

            // Set alarm for 1 day before trip at 9:00 AM
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTime(tripDate);
            alarmTime.add(Calendar.DAY_OF_MONTH, -1);
            alarmTime.set(Calendar.HOUR_OF_DAY, 9);
            alarmTime.set(Calendar.MINUTE, 0);
            alarmTime.set(Calendar.SECOND, 0);

            // Only schedule if alarm time is in the future
            if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d(TAG, "Trip date is in the past or too close, skipping alarm");
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_TRIP_REMINDER);
            intent.putExtra(AlarmReceiver.EXTRA_TRIP_ID, tripId);
            intent.putExtra(AlarmReceiver.EXTRA_TRIP_DESTINATION, destination);
            intent.putExtra(AlarmReceiver.EXTRA_TRIP_DATE, startDate);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                tripId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.getTimeInMillis(),
                    pendingIntent
                );
            }

            Log.d(TAG, "Alarm scheduled for trip: " + destination + " at " + alarmTime.getTime());

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + startDate, e);
        }
    }

    /**
     * Cancel a trip reminder alarm
     */
    public static void cancelTripReminder(Context context, String tripId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_TRIP_REMINDER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            tripId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Alarm cancelled for trip: " + tripId);
        }
    }

    /**
     * Schedule a repeating daily alarm for data sync
     */
    public static void scheduleDailySync(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DAILY_SYNC);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set alarm for daily at midnight
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );

        Log.d(TAG, "Daily sync alarm scheduled");
    }

    /**
     * Schedule a custom trip reminder with specific time and message
     */
    public static void scheduleCustomTripReminder(Context context, String tripId,
                                                  String message, long alarmTimeMillis) {
        // Only schedule if alarm time is in the future
        if (alarmTimeMillis <= System.currentTimeMillis()) {
            Log.d(TAG, "Alarm time is in the past, skipping");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_TRIP_REMINDER);
        intent.putExtra(AlarmReceiver.EXTRA_TRIP_ID, tripId);
        intent.putExtra(AlarmReceiver.EXTRA_TRIP_DESTINATION, message);
        intent.putExtra("CUSTOM_MESSAGE", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            tripId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule exact alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTimeMillis,
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTimeMillis,
                pendingIntent
            );
        }

        Log.d(TAG, "Custom alarm scheduled with message: " + message);
    }
}
