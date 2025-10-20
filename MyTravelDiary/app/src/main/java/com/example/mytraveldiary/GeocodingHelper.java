package com.example.mytraveldiary;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeocodingHelper {

    private static final String TAG = "GeocodingHelper";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeocodingCallback {
        void onLocationFound(double latitude, double longitude, String displayName);
        void onLocationNotFound();
        void onError(String error);
    }

    public static void geocodeLocation(String locationName, GeocodingCallback callback) {
        executor.execute(() -> {
            try {
                // Encode the location name for URL
                String encodedLocation = URLEncoder.encode(locationName, "UTF-8");
                String urlString = NOMINATIM_URL + encodedLocation + "&limit=1";

                // Create connection
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "MyTravelDiary-Android");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Read response
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse JSON response
                    JSONArray results = new JSONArray(response.toString());
                    if (results.length() > 0) {
                        JSONObject firstResult = results.getJSONObject(0);
                        double lat = firstResult.getDouble("lat");
                        double lon = firstResult.getDouble("lon");
                        String displayName = firstResult.getString("display_name");

                        // Call callback on main thread
                        mainHandler.post(() -> callback.onLocationFound(lat, lon, displayName));
                    } else {
                        mainHandler.post(callback::onLocationNotFound);
                    }
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + responseCode));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Geocoding error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
}
