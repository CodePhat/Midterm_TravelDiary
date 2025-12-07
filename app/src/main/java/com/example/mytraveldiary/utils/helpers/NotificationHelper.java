package com.example.mytraveldiary.utils.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.ui.activities.MainActivity;

/**
 * Helper class for managing notifications
 */
public class NotificationHelper {

    private static final String CHANNEL_TRIP_REMINDERS = "trip_reminders";
    private static final String CHANNEL_EXPENSE_ALERTS = "expense_alerts";
    private static final String CHANNEL_GENERAL = "general";

    public static final int NOTIFICATION_TRIP_REMINDER = 1001;
    public static final int NOTIFICATION_EXPENSE_ALERT = 1002;
    public static final int NOTIFICATION_DATA_SYNC = 1003;

    /**
     * Create notification channels (required for Android O and above)
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);

            // Trip Reminders Channel
            NotificationChannel tripChannel = new NotificationChannel(
                CHANNEL_TRIP_REMINDERS,
                "Trip Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            tripChannel.setDescription("Notifications for upcoming trips");
            tripChannel.enableVibration(true);
            notificationManager.createNotificationChannel(tripChannel);

            // Expense Alerts Channel
            NotificationChannel expenseChannel = new NotificationChannel(
                CHANNEL_EXPENSE_ALERTS,
                "Expense Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            expenseChannel.setDescription("Notifications for expense tracking");
            notificationManager.createNotificationChannel(expenseChannel);

            // General Channel
            NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription("General app notifications");
            notificationManager.createNotificationChannel(generalChannel);
        }
    }

    /**
     * Show trip reminder notification
     */
    public static void showTripReminderNotification(Context context, String tripId,
                                                     String destination, String date) {
        // Intent to open app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("tripId", tripId);
        intent.putExtra("openTrip", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            tripId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TRIP_REMINDERS)
            .setSmallIcon(R.drawable.ic_travel)
            .setContentTitle("Upcoming Trip: " + destination)
            .setContentText("Your trip to " + destination + " starts on " + date)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Don't forget to pack your bags and check your itinerary for " + destination + "!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_itinerary, "View Itinerary", pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_TRIP_REMINDER + tripId.hashCode(), builder.build());
    }

    /**
     * Show expense alert notification
     */
    public static void showExpenseAlertNotification(Context context, String tripDestination,
                                                     double totalExpenses, double budget) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String message;
        if (budget > 0 && totalExpenses >= budget * 0.8) {
            message = String.format("You've spent $%.2f out of $%.2f budget (%.0f%%)",
                totalExpenses, budget, (totalExpenses / budget) * 100);
        } else {
            message = String.format("Total expenses: $%.2f", totalExpenses);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_EXPENSE_ALERTS)
            .setSmallIcon(R.drawable.ic_money)
            .setContentTitle("Expense Alert: " + tripDestination)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_EXPENSE_ALERT, builder.build());
    }

    /**
     * Show data sync notification
     */
    public static void showDataSyncNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_dashboard)
            .setContentTitle("Travel Diary")
            .setContentText("Your trip data has been synced")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_DATA_SYNC, builder.build());
    }

    /**
     * Cancel a notification
     */
    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel all notifications
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }
}
