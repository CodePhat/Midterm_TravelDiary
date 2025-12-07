package com.example.mytraveldiary.utils.helpers;

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

    // Base URLs with placeholders; we'll append query and language parameters.
    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";
    private static final String NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s";

    // Force results to be in a predictable language. We use English here to
    // make searching across countries easier even when their local script is different.
    // If you want to prefer Vietnamese UI text, you could switch this to "vi" or
    // combine it with the app's current language.
    private static final String DEFAULT_LANGUAGE_PARAM = "&accept-language=en";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeocodingCallback {
        void onLocationFound(double latitude, double longitude, String displayName);
        void onLocationNotFound();
        void onError(String error);
    }

    public interface ReverseGeocodingCallback {
        void onLocationFound(String displayName);
        void onError(String error);
    }

    public interface AutocompleteCallback {
        void onSuggestionsFound(java.util.List<LocationSuggestion> suggestions);
        void onError(String error);
    }

    public static class LocationSuggestion {
        public final String displayName;
        public final double latitude;
        public final double longitude;

        public LocationSuggestion(String displayName, double latitude, double longitude) {
            this.displayName = displayName;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void geocodeLocation(String locationName, GeocodingCallback callback) {
        executor.execute(() -> {
            try {
                // Encode the location name for URL
                String encodedLocation = URLEncoder.encode(locationName, "UTF-8");
                // Add language hint so we always get English-friendly names
                String urlString = NOMINATIM_SEARCH_URL + encodedLocation + "&limit=1" + DEFAULT_LANGUAGE_PARAM;

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

    // Reverse geocode: get location name from coordinates
    public static void reverseGeocode(double latitude, double longitude, ReverseGeocodingCallback callback) {
        executor.execute(() -> {
            try {
                String urlString = String.format(NOMINATIM_REVERSE_URL, latitude, longitude)
                        + DEFAULT_LANGUAGE_PARAM;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "MyTravelDiary-Android");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

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

                    JSONObject result = new JSONObject(response.toString());
                    String displayName = result.getString("display_name");

                    mainHandler.post(() -> callback.onLocationFound(displayName));
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + responseCode));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Reverse geocoding error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Get autocomplete suggestions for location search
    public static void getLocationSuggestions(String query, AutocompleteCallback callback) {
        if (query == null || query.trim().length() < 2) {
            mainHandler.post(() -> callback.onSuggestionsFound(new java.util.ArrayList<>()));
            return;
        }

        final String normalizedQuery = query.trim().toLowerCase();

        executor.execute(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                // Slightly higher limit to have candidates to rank, we'll trim later
                String urlString = NOMINATIM_SEARCH_URL + encodedQuery + "&limit=10" + DEFAULT_LANGUAGE_PARAM;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "MyTravelDiary-Android");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

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

                    JSONArray results = new JSONArray(response.toString());
                    java.util.List<ScoredSuggestion> scored = new java.util.ArrayList<>();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);
                        String displayName = item.getString("display_name");
                        double lat = item.getDouble("lat");
                        double lon = item.getDouble("lon");
                        double importance = item.optDouble("importance", 0.0);

                        double score = computeMatchScore(normalizedQuery, displayName.toLowerCase(), importance);
                        scored.add(new ScoredSuggestion(new LocationSuggestion(displayName, lat, lon), score));
                    }

                    // Sort best matches first
                    scored.sort((a, b) -> Double.compare(b.score, a.score));

                    // Convert back to plain suggestions, keep top 5 for UI clarity
                    java.util.List<LocationSuggestion> suggestions = new java.util.ArrayList<>();
                    int max = Math.min(5, scored.size());
                    for (int i = 0; i < max; i++) {
                        suggestions.add(scored.get(i).suggestion);
                    }

                    mainHandler.post(() -> callback.onSuggestionsFound(suggestions));
                } else {
                    mainHandler.post(() -> callback.onError("Server returned code: " + responseCode));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Autocomplete error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private static class ScoredSuggestion {
        final LocationSuggestion suggestion;
        final double score;

        ScoredSuggestion(LocationSuggestion suggestion, double score) {
            this.suggestion = suggestion;
            this.score = score;
        }
    }

    private static double computeMatchScore(String query, String name, double importance) {
        double score = 0.0;

        // Strong boost if the name starts with the query (e.g., "la" -> "Los Angeles")
        if (name.startsWith(query)) {
            score += 5.0;
        } else if (name.contains(" " + query)) {
            // If query appears as a separate word inside the name
            score += 3.0;
        } else if (name.contains(query)) {
            score += 1.0;
        }

        // Add scaled importance (Nominatim's importance is 0..1-ish)
        score += importance * 2.0;

        // For very short queries, slightly penalize very long, overly specific names
        if (query.length() <= 3 && name.length() > 40) {
            score -= 1.0;
        }

        return score;
    }
}
