package com.example.mytraveldiary.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.mytraveldiary.services.sync.DataSyncService;

/**
 * Broadcast Receiver for network connectivity changes
 * Triggers data sync when network becomes available
 * Demonstrates: System Broadcasts, Network Monitoring
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            Log.d(TAG, "Network connectivity changed: " + (isConnected ? "Connected" : "Disconnected"));

            if (isConnected) {
                // Trigger data sync when network is available
                Intent syncIntent = new Intent(context, DataSyncService.class);
                syncIntent.setAction(DataSyncService.ACTION_SYNC_TRIPS);
                context.startService(syncIntent);

                // Send custom broadcast
                Intent customIntent = new Intent("com.example.mytraveldiary.NETWORK_AVAILABLE");
                context.sendBroadcast(customIntent);
            }
        }
    }
}
