package com.example.mytraveldiary.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

/**
 * Broadcast Receiver for handling notification actions
 * Demonstrates: Broadcast Receivers, Custom Broadcasts
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION_DISMISS = "com.example.mytraveldiary.ACTION_DISMISS";
    public static final String ACTION_VIEW = "com.example.mytraveldiary.ACTION_VIEW";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        switch (action) {
            case ACTION_DISMISS:
                int notificationId = intent.getIntExtra("notification_id", -1);
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId);
                    Toast.makeText(context, "Reminder dismissed", Toast.LENGTH_SHORT).show();
                }
                break;

            case ACTION_VIEW:
                // Handle view action
                Toast.makeText(context, "Opening trip details", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
