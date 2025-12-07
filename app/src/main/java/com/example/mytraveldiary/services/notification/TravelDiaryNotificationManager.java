package com.example.mytraveldiary.services.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.receivers.DiaryReminderReceiver;
import com.example.mytraveldiary.ui.activities.MainActivity;

import java.util.Calendar;

/**
 * Notification Manager for Travel Diary specific notifications
 * Features:
 * - Daily diary entry reminders at custom time
 * - Location-based arrival notifications
 * - Photo attachment reminders
 * - Offline sync reminders
 */
public class TravelDiaryNotificationManager {

    // Notification Channels
    private static final String CHANNEL_DIARY_REMINDERS = "diary_reminders";
    private static final String CHANNEL_LOCATION_ALERTS = "location_alerts";
    private static final String CHANNEL_PHOTO_PROMPTS = "photo_prompts";
    private static final String CHANNEL_SYNC_ALERTS = "sync_alerts";

    // Notification IDs
    private static final int NOTIFICATION_DAILY_DIARY = 2001;
    private static final int NOTIFICATION_LOCATION_ARRIVAL = 2002;
    private static final int NOTIFICATION_PHOTO_REMINDER = 2003;
    private static final int NOTIFICATION_OFFLINE_SYNC = 2004;

    // SharedPreferences
    private static final String PREFS_NAME = "TravelDiaryNotifications";
    private static final String PREF_REMINDER_HOUR = "reminder_hour";
    private static final String PREF_REMINDER_MINUTE = "reminder_minute";
    private static final String PREF_REMINDERS_ENABLED = "reminders_enabled";

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final SharedPreferences prefs;

    public TravelDiaryNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        createNotificationChannels();
    }

    /**
     * Create notification channels for Android O+
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // Diary Reminders Channel (HIGH priority - user wants to remember)
            NotificationChannel diaryChannel = new NotificationChannel(
                CHANNEL_DIARY_REMINDERS,
                "Diary Entry Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            diaryChannel.setDescription("Daily reminders to write diary entries");
            diaryChannel.enableVibration(true);
            diaryChannel.setShowBadge(true);

            // Location Alerts Channel (HIGH priority - time-sensitive)
            NotificationChannel locationChannel = new NotificationChannel(
                CHANNEL_LOCATION_ALERTS,
                "Location Arrivals",
                NotificationManager.IMPORTANCE_HIGH
            );
            locationChannel.setDescription("Notifications when you arrive at destinations");
            locationChannel.enableVibration(true);

            // Photo Prompts Channel (DEFAULT priority)
            NotificationChannel photoChannel = new NotificationChannel(
                CHANNEL_PHOTO_PROMPTS,
                "Photo Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            photoChannel.setDescription("Reminders to attach photos to entries");

            // Sync Alerts Channel (LOW priority)
            NotificationChannel syncChannel = new NotificationChannel(
                CHANNEL_SYNC_ALERTS,
                "Sync Notifications",
                NotificationManager.IMPORTANCE_LOW
            );
            syncChannel.setDescription("Notifications about data synchronization");

            manager.createNotificationChannel(diaryChannel);
            manager.createNotificationChannel(locationChannel);
            manager.createNotificationChannel(photoChannel);
            manager.createNotificationChannel(syncChannel);
        }
    }

    // ==================== DAILY DIARY REMINDERS ====================

    /**
     * Show daily diary entry reminder
     * "Don't forget to record your memories from today in Paris!"
     */
    public void showDailyDiaryReminder(String currentLocation) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_diary_entry", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_DAILY_DIARY,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Action: Write Entry
        Intent writeIntent = new Intent(context, MainActivity.class);
        writeIntent.putExtra("action", "write_entry");
        PendingIntent writePendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_DAILY_DIARY + 1,
            writeIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Time to Record Your Memories! 📝";
        String message = currentLocation != null && !currentLocation.isEmpty()
            ? "Don't forget to record your memories from today in " + currentLocation + "!"
            : "Don't forget to record your travel memories from today!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DIARY_REMINDERS)
            .setSmallIcon(R.drawable.ic_diary)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message + "\n\nWhat was the highlight of your day?"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_add, "Write Entry", writePendingIntent);

        notificationManager.notify(NOTIFICATION_DAILY_DIARY, builder.build());
    }

    /**
     * Schedule daily diary reminder at custom time
     */
    public void scheduleDailyDiaryReminder(int hour, int minute) {
        // Save preferences
        prefs.edit()
            .putInt(PREF_REMINDER_HOUR, hour)
            .putInt(PREF_REMINDER_MINUTE, minute)
            .putBoolean(PREF_REMINDERS_ENABLED, true)
            .apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, DiaryReminderReceiver.class);
        intent.setAction(DiaryReminderReceiver.ACTION_DAILY_REMINDER);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_DAILY_DIARY,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, schedule for tomorrow
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
        }
    }

    /**
     * Schedule daily reminder with default time (9 PM)
     */
    public void scheduleDailyDiaryReminder() {
        scheduleDailyDiaryReminder(21, 0); // 9:00 PM
    }

    /**
     * Cancel daily diary reminder
     */
    public void cancelDailyDiaryReminder() {
        prefs.edit().putBoolean(PREF_REMINDERS_ENABLED, false).apply();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DiaryReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_DAILY_DIARY,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    // ==================== LOCATION-BASED NOTIFICATIONS ====================

    /**
     * Show notification when user arrives at a location
     * "You've arrived at Hoan Kiem Lake. Want to log this moment?"
     */
    public void showLocationArrivalNotification(String locationName, double latitude, double longitude) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("location_name", locationName);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("action", "log_location");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_LOCATION_ARRIVAL,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Action: Log Now
        Intent logIntent = new Intent(context, MainActivity.class);
        logIntent.putExtra("action", "quick_log");
        logIntent.putExtra("location_name", locationName);
        PendingIntent logPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_LOCATION_ARRIVAL + 1,
            logIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        String title = "📍 You've Arrived!";
        String message = "You've arrived at " + locationName + ". Want to log this moment?";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LOCATION_ALERTS)
            .setSmallIcon(R.drawable.ic_map_marker)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message + "\n\nCapture your first impressions while they're fresh!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_add, "Log Now", logPendingIntent)
            .addAction(R.drawable.ic_dashboard, "Later", pendingIntent);

        notificationManager.notify(NOTIFICATION_LOCATION_ARRIVAL, builder.build());
    }

    /**
     * Show notification when user is leaving a location
     * "Would you like to save some notes before leaving?"
     */
    public void showLocationDepartureNotification(String locationName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("action", "add_departure_notes");
        intent.putExtra("location_name", locationName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_LOCATION_ARRIVAL + 10,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "Leaving " + locationName + "?";
        String message = "Would you like to save some notes before leaving?";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LOCATION_ALERTS)
            .setSmallIcon(R.drawable.ic_map_marker)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_add, "Add Notes", pendingIntent);

        notificationManager.notify(NOTIFICATION_LOCATION_ARRIVAL + 10, builder.build());
    }

    // ==================== PHOTO ATTACHMENT REMINDERS ====================

    /**
     * Remind user to attach photos from today
     * "You took 10 photos today. Would you like to attach them to today's entry?"
     */
    public void showPhotoAttachmentReminder(int photoCount) {
        if (photoCount == 0) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("action", "attach_photos");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_PHOTO_REMINDER,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "Don't Forget Your Photos! 📸";
        String message = "You took " + photoCount + " photo" + (photoCount > 1 ? "s" : "")
            + " today. Would you like to attach them to today's entry?";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PHOTO_PROMPTS)
            .setSmallIcon(R.drawable.ic_photo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_photo, "Attach Photos", pendingIntent);

        notificationManager.notify(NOTIFICATION_PHOTO_REMINDER, builder.build());
    }

    // ==================== OFFLINE SYNC NOTIFICATIONS ====================

    /**
     * Remind user to sync offline entries
     * "You recorded entries offline. Sync now?"
     */
    public void showOfflineSyncReminder(int offlineEntryCount) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("action", "sync_offline_data");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_OFFLINE_SYNC,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "Sync Your Offline Entries";
        String message = "You have " + offlineEntryCount + " offline entr"
            + (offlineEntryCount > 1 ? "ies" : "y") + " waiting to sync.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SYNC_ALERTS)
            .setSmallIcon(R.drawable.ic_cloud)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_cloud, "Sync Now", pendingIntent);

        notificationManager.notify(NOTIFICATION_OFFLINE_SYNC, builder.build());
    }

    /**
     * Show sync completion notification
     */
    public void showSyncCompletedNotification() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_OFFLINE_SYNC + 1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SYNC_ALERTS)
            .setSmallIcon(R.drawable.ic_cloud)
            .setContentTitle("Sync Complete ✓")
            .setContentText("Your travel memories are safely backed up!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_OFFLINE_SYNC + 1, builder.build());
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if daily reminders are enabled
     */
    public boolean areDailyRemindersEnabled() {
        return prefs.getBoolean(PREF_REMINDERS_ENABLED, false);
    }

    /**
     * Get reminder hour
     */
    public int getReminderHour() {
        return prefs.getInt(PREF_REMINDER_HOUR, 21); // Default 9 PM
    }

    /**
     * Get reminder minute
     */
    public int getReminderMinute() {
        return prefs.getInt(PREF_REMINDER_MINUTE, 0);
    }

    /**
     * Cancel all notifications
     */
    public void cancelAll() {
        notificationManager.cancelAll();
    }
}
