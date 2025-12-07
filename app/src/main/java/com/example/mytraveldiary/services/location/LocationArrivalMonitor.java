package com.example.mytraveldiary.services.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.example.mytraveldiary.receivers.DiaryReminderReceiver;
import com.example.mytraveldiary.services.notification.TravelDiaryNotificationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Monitor for detecting location arrivals and departures
 * Triggers notifications when user arrives at or leaves significant locations
 */
public class LocationArrivalMonitor {
    private static final String TAG = "LocationArrivalMonitor";

    // Distance thresholds
    private static final float ARRIVAL_RADIUS_METERS = 100; // Within 100m = arrived
    private static final float DEPARTURE_RADIUS_METERS = 500; // Beyond 500m = departed

    // Time thresholds
    private static final long MIN_STAY_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final long DEPARTURE_CHECK_INTERVAL_MS = 10 * 60 * 1000; // 10 minutes

    private final Context context;
    private final TravelDiaryNotificationManager notificationManager;
    private final SharedPreferences prefs;

    // Track monitored locations
    private final Map<String, MonitoredLocation> monitoredLocations;
    private String currentLocationName;
    private long arrivalTime;
    private boolean hasNotifiedArrival;

    private static class MonitoredLocation {
        String name;
        double latitude;
        double longitude;
        long lastVisitTime;

        MonitoredLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.lastVisitTime = 0;
        }
    }

    public LocationArrivalMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = new TravelDiaryNotificationManager(context);
        this.prefs = context.getSharedPreferences("LocationMonitor", Context.MODE_PRIVATE);
        this.monitoredLocations = new HashMap<>();
        this.currentLocationName = null;
        this.arrivalTime = 0;
        this.hasNotifiedArrival = false;
    }

    /**
     * Add a location to monitor (e.g., from user's trip itinerary)
     */
    public void addMonitoredLocation(String name, double latitude, double longitude) {
        monitoredLocations.put(name, new MonitoredLocation(name, latitude, longitude));
        Log.d(TAG, "Added monitored location: " + name);
    }

    /**
     * Remove a location from monitoring
     */
    public void removeMonitoredLocation(String name) {
        monitoredLocations.remove(name);
        Log.d(TAG, "Removed monitored location: " + name);
    }

    /**
     * Update with current location
     * Called periodically from LocationTrackingService
     */
    public void onLocationUpdate(Location currentLocation) {
        if (currentLocation == null) return;

        // Check if user has arrived at any monitored location
        for (MonitoredLocation monitored : monitoredLocations.values()) {
            float[] distance = new float[1];
            Location.distanceBetween(
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                monitored.latitude,
                monitored.longitude,
                distance
            );

            // Check for arrival
            if (distance[0] <= ARRIVAL_RADIUS_METERS) {
                handleArrival(monitored, currentLocation);
                return;
            }
        }

        // Check for departure if we're currently at a location
        if (currentLocationName != null) {
            checkDeparture(currentLocation);
        }
    }

    /**
     * Handle arrival at a location
     */
    private void handleArrival(MonitoredLocation location, Location currentLocation) {
        long currentTime = System.currentTimeMillis();

        // If this is a new arrival (not the same location or enough time has passed)
        if (!location.name.equals(currentLocationName) ||
            (currentTime - location.lastVisitTime > DEPARTURE_CHECK_INTERVAL_MS)) {

            currentLocationName = location.name;
            arrivalTime = currentTime;
            hasNotifiedArrival = false;

            // Wait a bit to confirm arrival (not just passing through)
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (location.name.equals(currentLocationName) && !hasNotifiedArrival) {
                    // User has stayed for MIN_STAY_DURATION - confirm arrival
                    notificationManager.showLocationArrivalNotification(
                        location.name,
                        location.latitude,
                        location.longitude
                    );

                    // Update last visited location for diary reminders
                    DiaryReminderReceiver.updateLastVisitedLocation(context, location.name);

                    location.lastVisitTime = currentTime;
                    hasNotifiedArrival = true;

                    Log.d(TAG, "User arrived at: " + location.name);
                }
            }, MIN_STAY_DURATION_MS);
        }
    }

    /**
     * Check if user has departed from current location
     */
    private void checkDeparture(Location currentLocation) {
        if (currentLocationName == null) return;

        MonitoredLocation currentMonitored = monitoredLocations.get(currentLocationName);
        if (currentMonitored == null) return;

        float[] distance = new float[1];
        Location.distanceBetween(
            currentLocation.getLatitude(),
            currentLocation.getLongitude(),
            currentMonitored.latitude,
            currentMonitored.longitude,
            distance
        );

        // If user has moved beyond departure radius
        if (distance[0] > DEPARTURE_RADIUS_METERS) {
            long stayDuration = System.currentTimeMillis() - arrivalTime;

            // Only notify if they stayed for a meaningful amount of time
            if (stayDuration > MIN_STAY_DURATION_MS) {
                notificationManager.showLocationDepartureNotification(currentLocationName);
                Log.d(TAG, "User departed from: " + currentLocationName);
            }

            // Clear current location
            currentLocationName = null;
            arrivalTime = 0;
            hasNotifiedArrival = false;
        }
    }

    /**
     * Add locations from trip itinerary
     * Call this when user creates a trip with destinations
     */
    public void addTripDestinations(java.util.List<TripDestination> destinations) {
        for (TripDestination dest : destinations) {
            addMonitoredLocation(dest.name, dest.latitude, dest.longitude);
        }
    }

    /**
     * Simple destination class
     */
    public static class TripDestination {
        public String name;
        public double latitude;
        public double longitude;

        public TripDestination(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Clear all monitored locations
     */
    public void clearAll() {
        monitoredLocations.clear();
        currentLocationName = null;
        arrivalTime = 0;
        hasNotifiedArrival = false;
        Log.d(TAG, "Cleared all monitored locations");
    }
}
