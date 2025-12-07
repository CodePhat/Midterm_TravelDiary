package com.example.mytraveldiary.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LocaleHelper {
    private static final String PREFS = "app_prefs";
    private static final String KEY_LANG = "pref_language";
    private static final String DEFAULT = "en";

    public static String getLanguage(Context context) {
        // CRASH FIX: Handle null context gracefully
        if (context == null) {
            return DEFAULT;
        }

        try {
            // In attachBaseContext, getApplicationContext() returns null
            // Use the context parameter directly, but prefer ApplicationContext when available
            Context appContext = context.getApplicationContext();
            if (appContext == null) {
                appContext = context; // Use provided context if ApplicationContext not ready
            }
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            return prefs.getString(KEY_LANG, DEFAULT);
        } catch (Exception e) {
            // Fallback to default if anything fails
            return DEFAULT;
        }
    }

    public static void persistLanguage(Context context, String language) {
        // CRASH FIX: Handle null context
        if (context == null) {
            return;
        }

        try {
            // Handle null ApplicationContext
            Context appContext = context.getApplicationContext();
            if (appContext == null) {
                appContext = context;
            }
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                   .edit()
                   .putString(KEY_LANG, language)
                   .apply();
        } catch (Exception e) {
            // Silently fail if preferences can't be saved
        }
    }

    // MEMORY LEAK & ANR FIX: Apply locale without persisting (for attachBaseContext)
    public static Context applyLocale(Context context, String language) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        } else {
            return updateResourcesLegacy(context, language);
        }
    }

    // Set locale AND persist it (for when user changes language)
    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);
        return applyLocale(context, language);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale);

        return context.createConfigurationContext(config);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        config.setLayoutDirection(locale);

        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);
        return context;
    }
}

