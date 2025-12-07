package com.example.mytraveldiary.services.location;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.ui.activities.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Bound Service for real-time location tracking during trips
 * Demonstrates: Bound Services, Location Services, Foreground Service
 */
public class LocationTrackingService extends Service {
    private static final String CHANNEL_ID = "location_tracking_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long FASTEST_INTERVAL = 5000; // 5 seconds

    private final IBinder binder = new LocalBinder();
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private LocationUpdateListener listener;

    public interface LocationUpdateListener {
        void onLocationUpdated(Location location);
    }

    public class LocalBinder extends Binder {
        public LocationTrackingService getService() {
            return LocationTrackingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                    if (listener != null) {
                        listener.onLocationUpdated(location);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.listener = listener;
    }

    // MEMORY LEAK FIX: Provide method to clear listener
    public void clearLocationUpdateListener() {
        this.listener = null;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        )
        .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
        .build();

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracking your location during trips");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Travel Diary")
            .setContentText("Tracking your location...")
            .setSmallIcon(R.drawable.ic_map_marker)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        // MEMORY LEAK FIX: Clear listener to prevent Activity/Fragment leak
        clearLocationUpdateListener();
    }
}
