package com.example.mytraveldiary.services.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.receivers.NotificationActionReceiver;
import com.example.mytraveldiary.ui.activities.MainActivity;

/**
 * Notification Manager for various app notifications
 * Demonstrates: Notification Channels, Notification Styles, Tap Actions, Action Buttons
 */
public class TripNotificationManager {
    private static final String CHANNEL_TRIP_REMINDERS = "trip_reminders";
    private static final String CHANNEL_TRIP_UPDATES = "trip_updates";
    private static final String CHANNEL_GENERAL = "general";

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public TripNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(this.context);
        createNotificationChannels();
    }

    /**
     * Create notification channels (required for Android O+)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Trip Reminders Channel
            NotificationChannel reminderChannel = new NotificationChannel(
                CHANNEL_TRIP_REMINDERS,
                "Trip Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            reminderChannel.setDescription("Reminders for upcoming trips");
            reminderChannel.enableVibration(true);
            reminderChannel.setShowBadge(true);

            // Trip Updates Channel
            NotificationChannel updateChannel = new NotificationChannel(
                CHANNEL_TRIP_UPDATES,
                "Trip Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            updateChannel.setDescription("Updates about your trips");
            updateChannel.setShowBadge(true);

            // General Channel
            NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription("General app notifications");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(reminderChannel);
            manager.createNotificationChannel(updateChannel);
            manager.createNotificationChannel(generalChannel);
        }
    }

    /**
     * Show trip reminder notification
     */
    public void showTripReminder(int notificationId, String tripTitle, String message, long tripId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("trip_id", tripId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // View Trip Action
        Intent viewIntent = new Intent(context, MainActivity.class);
        viewIntent.putExtra("trip_id", tripId);
        viewIntent.putExtra("action", "view");
        PendingIntent viewPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 1000,
            viewIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        // Dismiss Action
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction(NotificationActionReceiver.ACTION_DISMISS);
        dismissIntent.putExtra("notification_id", notificationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2000,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TRIP_REMINDERS)
            .setSmallIcon(R.drawable.ic_travel)
            .setContentTitle(tripTitle)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_dashboard, "View Trip", viewPendingIntent)
            .addAction(R.drawable.ic_add, "Dismiss", dismissPendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Show expense summary notification
     */
    public void showExpenseSummary(int notificationId, String tripTitle, double totalExpenses, int expenseCount) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        String message = String.format("You have spent $%.2f across %d expenses", totalExpenses, expenseCount);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TRIP_UPDATES)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(tripTitle + " - Expense Summary")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Show simple notification
     */
    public void showSimpleNotification(int notificationId, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Show progress notification (for sync/upload operations)
     */
    public void showProgressNotification(int notificationId, String title, int progress, int max) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle(title)
            .setProgress(max, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Cancel notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
