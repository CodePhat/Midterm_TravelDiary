package com.example.mytraveldiary.utils.constants;

/**
 * Application-wide constants
 */
public class Constants {
    // Database
    public static final String DATABASE_NAME = "travel_diary_db";
    public static final int DATABASE_VERSION = 1;

    // Shared Preferences
    public static final String PREFS_NAME = "TravelDiaryPrefs";
    public static final String PREF_THEME_MODE = "theme_mode";
    public static final String PREF_CURRENT_USER = "current_user";

    // API Keys (In production, use BuildConfig or secure storage)
    public static final String WEATHER_API_KEY = "your_openweather_api_key";
    public static final String GOOGLE_MAPS_API_KEY = "your_google_maps_api_key";
    public static final String MAPBOX_API_KEY = "your_mapbox_api_key";

    // Notification IDs
    public static final int NOTIFICATION_ID_TRIP_REMINDER = 1001;
    public static final int NOTIFICATION_ID_SYNC = 1002;
    public static final int NOTIFICATION_ID_BACKUP = 1003;

    // Intent Extras
    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_USER_EMAIL = "user_email";

    // Work Manager
    public static final String WORK_BACKUP = "data_backup_work";
    public static final String WORK_SYNC = "data_sync_work";

    // Permissions Request Codes
    public static final int PERMISSION_LOCATION = 100;
    public static final int PERMISSION_CAMERA = 101;
    public static final int PERMISSION_STORAGE = 102;

    // Image Picker
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_REQUEST = 2;

    private Constants() {
        // Prevent instantiation
    }
}
