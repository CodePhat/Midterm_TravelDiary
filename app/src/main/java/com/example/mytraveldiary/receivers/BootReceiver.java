package com.example.mytraveldiary.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mytraveldiary.receivers.AlarmReceiver;

/**
 * Broadcast Receiver for device boot
 * Re-schedules alarms after device reboot
 * Demonstrates: System Broadcasts, Boot Receiver
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted - rescheduling alarms");

            // Re-schedule trip reminders
            AlarmReceiver.rescheduleAllAlarms(context);
        }
    }
}
