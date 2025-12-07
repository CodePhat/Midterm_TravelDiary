package com.example.mytraveldiary.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Manager class for handling app language settings
 * Supports English and Vietnamese
 */
public class LanguageManager {
    private static final String PREFS_NAME = "language_prefs";
    private static final String KEY_LANGUAGE = "selected_language";

    // Language codes
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_VIETNAMESE = "vi";

    private final Context context;
    private final SharedPreferences prefs;

    public LanguageManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Set the app language
     * @param languageCode Language code (e.g., "en", "vi")
     * @return Wrapped context with new locale (use this context for Activities)
     */
    public Context setLanguage(String languageCode) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
        return updateResources(languageCode);
    }

    /**
     * Get the currently selected language
     * @return Language code
     */
    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }

    /**
     * Apply the saved language to the app
     * @return Wrapped context with the saved locale
     */
    public Context applyLanguage() {
        String languageCode = getLanguage();
        return updateResources(languageCode);
    }

    /**
     * Update app resources with the selected language
     * IMPORTANT: Returns wrapped context that must be used for proper locale handling
     * @return Context wrapped with new locale configuration
     */
    private Context updateResources(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        // CRITICAL FIX: Must return the wrapped context
        Context newContext = context.createConfigurationContext(config);

        // Also update the configuration for legacy support
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        return newContext;
    }

    /**
     * Get display name for a language code
     */
    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_VIETNAMESE:
                return "Tiếng Việt";
            case LANGUAGE_ENGLISH:
            default:
                return "English";
        }
    }

    /**
     * Get all supported languages
     */
    public static String[] getSupportedLanguages() {
        return new String[]{LANGUAGE_ENGLISH, LANGUAGE_VIETNAMESE};
    }

    /**
     * Get display names for all supported languages
     */
    public static String[] getSupportedLanguageNames() {
        return new String[]{"English", "Tiếng Việt"};
    }
}
