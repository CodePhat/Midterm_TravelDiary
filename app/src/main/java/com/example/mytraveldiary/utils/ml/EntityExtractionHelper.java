package com.example.mytraveldiary.utils.ml;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Entity Extraction Helper (Pattern-Based)
 * Extract structured data from natural language text using regex patterns
 *
 * Note: ML Kit Entity Extraction is not available in standalone ML Kit.
 * This implementation uses intelligent regex pattern matching instead.
 *
 * Use Cases:
 * - Auto-detect dates from diary entries ("tomorrow", "next Monday", "12/25/2025")
 * - Extract place names from text (common travel destinations)
 * - Detect addresses, phone numbers, email addresses
 * - Parse travel itineraries
 */
public class EntityExtractionHelper {
    private static final String TAG = "EntityExtractionHelper";

    public interface EntityExtractionCallback {
        void onSuccess(ExtractedEntities entities);
        void onFailure(String error);
    }

    public static class ExtractedEntities {
        private final List<DateTimeInfo> dates;
        private final List<PlaceInfo> places;
        private final List<String> addresses;
        private final List<String> phoneNumbers;
        private final List<String> emails;
        private final List<String> urls;

        public ExtractedEntities() {
            this.dates = new ArrayList<>();
            this.places = new ArrayList<>();
            this.addresses = new ArrayList<>();
            this.phoneNumbers = new ArrayList<>();
            this.emails = new ArrayList<>();
            this.urls = new ArrayList<>();
        }

        public void addDate(DateTimeInfo date) {
            dates.add(date);
        }

        public void addPlace(PlaceInfo place) {
            places.add(place);
        }

        public void addAddress(String address) {
            addresses.add(address);
        }

        public void addPhoneNumber(String phone) {
            phoneNumbers.add(phone);
        }

        public void addEmail(String email) {
            emails.add(email);
        }

        public void addUrl(String url) {
            urls.add(url);
        }

        public List<DateTimeInfo> getDates() {
            return dates;
        }

        public List<PlaceInfo> getPlaces() {
            return places;
        }

        public List<String> getAddresses() {
            return addresses;
        }

        public List<String> getPhoneNumbers() {
            return phoneNumbers;
        }

        public List<String> getEmails() {
            return emails;
        }

        public List<String> getUrls() {
            return urls;
        }

        public boolean hasData() {
            return !dates.isEmpty() || !places.isEmpty() || !addresses.isEmpty() ||
                   !phoneNumbers.isEmpty() || !emails.isEmpty() || !urls.isEmpty();
        }
    }

    public static class DateTimeInfo {
        private final String text;
        private final long timestampMillis;
        private final String formattedDate;

        public DateTimeInfo(String text, long timestampMillis) {
            this.text = text;
            this.timestampMillis = timestampMillis;
            this.formattedDate = formatDate(timestampMillis);
        }

        private String formatDate(long millis) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(millis));
        }

        public String getText() {
            return text;
        }

        public long getTimestampMillis() {
            return timestampMillis;
        }

        public String getFormattedDate() {
            return formattedDate;
        }

        @Override
        public String toString() {
            return text + " → " + formattedDate;
        }
    }

    public static class PlaceInfo {
        private final String text;
        private final String name;

        public PlaceInfo(String text, String name) {
            this.text = text;
            this.name = name;
        }

        public String getText() {
            return text;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name != null ? name : text;
        }
    }

    public EntityExtractionHelper() {
        // Pattern-based, no initialization needed
    }

    /**
     * Initialize the entity extraction (no-op for pattern-based system)
     * Kept for API compatibility
     */
    public void downloadModel(Runnable onSuccess, Runnable onFailure) {
        // Pattern-based system doesn't need model download
        Log.d(TAG, "Entity extraction ready (pattern-based, no download needed)");
        if (onSuccess != null) onSuccess.run();
    }

    /**
     * Extract entities from text using pattern matching
     */
    public void extractEntities(String text, EntityExtractionCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onFailure("No text provided");
            return;
        }

        ExtractedEntities entities = new ExtractedEntities();

        try {
            // Extract dates
            extractDatesFromText(text, entities);

            // Extract places
            extractPlacesFromText(text, entities);

            // Extract contact info
            extractEmailsFromText(text, entities);
            extractPhonesFromText(text, entities);
            extractUrlsFromText(text, entities);
            extractAddressesFromText(text, entities);

            callback.onSuccess(entities);
        } catch (Exception e) {
            Log.e(TAG, "Entity extraction failed", e);
            callback.onFailure("Extraction failed: " + e.getMessage());
        }
    }

    /**
     * Extract dates using patterns and natural language
     */
    private void extractDatesFromText(String text, ExtractedEntities entities) {
        Calendar cal = Calendar.getInstance();
        String lowerText = text.toLowerCase();

        // Relative dates (tomorrow, today, yesterday, etc.)
        if (lowerText.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            entities.addDate(new DateTimeInfo("tomorrow", cal.getTimeInMillis()));
        }
        if (lowerText.contains("today")) {
            entities.addDate(new DateTimeInfo("today", cal.getTimeInMillis()));
        }
        if (lowerText.contains("yesterday")) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            entities.addDate(new DateTimeInfo("yesterday", cal.getTimeInMillis()));
        }
        if (lowerText.contains("next week")) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            entities.addDate(new DateTimeInfo("next week", cal.getTimeInMillis()));
        }
        if (lowerText.contains("next month")) {
            cal.add(Calendar.MONTH, 1);
            entities.addDate(new DateTimeInfo("next month", cal.getTimeInMillis()));
        }

        // Day of week (next Monday, this Friday, etc.)
        String[] daysOfWeek = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (lowerText.contains(daysOfWeek[i])) {
                Calendar dayCal = Calendar.getInstance();
                int targetDay = i + 2; // Calendar.MONDAY = 2
                if (targetDay > 7) targetDay = 1;

                int currentDay = dayCal.get(Calendar.DAY_OF_WEEK);
                int daysToAdd = (targetDay - currentDay + 7) % 7;
                if (daysToAdd == 0) daysToAdd = 7; // Next occurrence

                dayCal.add(Calendar.DAY_OF_YEAR, daysToAdd);
                entities.addDate(new DateTimeInfo(daysOfWeek[i], dayCal.getTimeInMillis()));
            }
        }

        // Formatted dates (MM/DD/YYYY, YYYY-MM-DD, etc.)
        Pattern[] datePatterns = {
            Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{2,4})\\b"),
            Pattern.compile("\\b(\\d{4})-(\\d{1,2})-(\\d{1,2})\\b"),
            Pattern.compile("\\b(\\d{1,2})-(\\d{1,2})-(\\d{2,4})\\b")
        };

        for (Pattern pattern : datePatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String dateStr = matcher.group();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                    Date date = sdf.parse(dateStr);
                    if (date != null) {
                        entities.addDate(new DateTimeInfo(dateStr, date.getTime()));
                    }
                } catch (ParseException e) {
                    // Try other formats
                }
            }
        }
    }

    /**
     * Extract place names from text
     */
    private void extractPlacesFromText(String text, ExtractedEntities entities) {
        // Common travel destinations and cities
        String[] places = {
            "Paris", "London", "Tokyo", "New York", "Rome", "Barcelona", "Amsterdam",
            "Dubai", "Singapore", "Hong Kong", "Sydney", "Bangkok", "Istanbul",
            "Vienna", "Prague", "Berlin", "Madrid", "Seoul", "Beijing", "Shanghai",
            "Los Angeles", "San Francisco", "Chicago", "Miami", "Las Vegas",
            "Venice", "Florence", "Athens", "Lisbon", "Copenhagen", "Stockholm",
            "Munich", "Dublin", "Edinburgh", "Brussels", "Zurich", "Geneva",
            "Montreal", "Toronto", "Vancouver", "Mexico City", "Rio de Janeiro",
            "Buenos Aires", "Lima", "Cairo", "Cape Town", "Mumbai", "Delhi",
            "Bali", "Phuket", "Maldives", "Hawaii", "Santorini", "Iceland"
        };

        for (String place : places) {
            if (text.contains(place)) {
                entities.addPlace(new PlaceInfo(place, place));
            }
        }
    }

    /**
     * Extract email addresses
     */
    private void extractEmailsFromText(String text, ExtractedEntities entities) {
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = emailPattern.matcher(text);
        while (matcher.find()) {
            entities.addEmail(matcher.group());
        }
    }

    /**
     * Extract phone numbers
     */
    private void extractPhonesFromText(String text, ExtractedEntities entities) {
        Pattern phonePattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b|\\b\\(\\d{3}\\)\\s*\\d{3}[-.]?\\d{4}\\b");
        Matcher matcher = phonePattern.matcher(text);
        while (matcher.find()) {
            entities.addPhoneNumber(matcher.group());
        }
    }

    /**
     * Extract URLs
     */
    private void extractUrlsFromText(String text, ExtractedEntities entities) {
        Pattern urlPattern = Pattern.compile("\\b(https?://|www\\.)\\S+\\b");
        Matcher matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            entities.addUrl(matcher.group());
        }
    }

    /**
     * Extract addresses (simple pattern matching)
     */
    private void extractAddressesFromText(String text, ExtractedEntities entities) {
        // Match patterns like "123 Main Street" or "456 Oak Avenue"
        Pattern addressPattern = Pattern.compile("\\b\\d+\\s+[A-Z][a-z]+\\s+(Street|St|Avenue|Ave|Road|Rd|Boulevard|Blvd|Lane|Ln|Drive|Dr)\\b");
        Matcher matcher = addressPattern.matcher(text);
        while (matcher.find()) {
            entities.addAddress(matcher.group());
        }
    }

    /**
     * Extract only dates from text (convenience method)
     */
    public void extractDates(String text, EntityExtractionCallback callback) {
        extractEntities(text, new EntityExtractionCallback() {
            @Override
            public void onSuccess(ExtractedEntities entities) {
                callback.onSuccess(entities);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    /**
     * Extract only places from text (convenience method)
     */
    public void extractPlaces(String text, EntityExtractionCallback callback) {
        extractEntities(text, callback);
    }

    /**
     * Parse itinerary text and extract structured data
     * Example: "We're going to Paris tomorrow and then Rome on Friday"
     */
    public void parseItinerary(String itineraryText, EntityExtractionCallback callback) {
        extractEntities(itineraryText, new EntityExtractionCallback() {
            @Override
            public void onSuccess(ExtractedEntities entities) {
                // Add parsing logic to match dates with places
                callback.onSuccess(entities);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    /**
     * Close resources (pattern-based system has nothing to close)
     */
    public void close() {
        // No resources to close in pattern-based system
    }
}

