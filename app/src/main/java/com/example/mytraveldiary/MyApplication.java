package com.example.mytraveldiary;

import android.app.Application;
import android.content.Context;

import androidx.work.WorkManager;

import com.example.mytraveldiary.utils.LocaleHelper;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // CRASH FIX: Initialize WorkManager manually since it's disabled in manifest
        // This prevents crash when LeakCanary tries to use WorkManager
        try {
            // Use fully qualified name to avoid import conflict with android.content.res.Configuration
            androidx.work.Configuration workManagerConfig = new androidx.work.Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .build();
            WorkManager.initialize(this, workManagerConfig);
        } catch (Exception e) {
            android.util.Log.e("MyApplication", "WorkManager initialization failed", e);
        }

        // ML KIT: Initialize ML Kit features
        try {
            com.example.mytraveldiary.utils.ml.MLKitManager.getInstance().initialize(this);
            android.util.Log.d("MyApplication", "ML Kit initialized successfully");
        } catch (Exception e) {
            android.util.Log.e("MyApplication", "ML Kit initialization failed", e);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        // CRASH FIX: Handle null context gracefully in attachBaseContext
        // At this early stage, ApplicationContext is not yet available
        try {
            String language = LocaleHelper.getLanguage(base);
            super.attachBaseContext(LocaleHelper.applyLocale(base, language));
        } catch (Exception e) {
            // Fallback: if locale retrieval fails, use default
            super.attachBaseContext(LocaleHelper.applyLocale(base, "en"));
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Re-apply saved locale when configuration changes (no need to persist again)
        LocaleHelper.applyLocale(this, LocaleHelper.getLanguage(this));
    }
}

