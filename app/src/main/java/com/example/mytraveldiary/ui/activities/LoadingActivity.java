package com.example.mytraveldiary.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytraveldiary.utils.LocaleHelper;

public class LoadingActivity extends AppCompatActivity {
    public static final String EXTRA_LANG = "extra_lang";
    private Handler loadingHandler;  // MEMORY LEAK FIX: Proper Handler cleanup

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        // ANR FIX: Use applyLocale (no I/O) instead of setLocale (with I/O)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No layout needed - just a brief transition

        String lang = getIntent().getStringExtra(EXTRA_LANG);
        if (lang == null) lang = LocaleHelper.getLanguage(this);

        // Small delay to show loading feel, then apply locale and relaunch main activity
        final String finalLang = lang;
        loadingHandler = new Handler(Looper.getMainLooper());
        loadingHandler.postDelayed(() -> {
            // MEMORY LEAK FIX: Check if Activity is still valid
            if (!isFinishing() && !isDestroyed()) {
                LocaleHelper.setLocale(LoadingActivity.this, finalLang);

                Intent i = new Intent(LoadingActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        }, 300);
    }

    @Override
    protected void onDestroy() {
        // MEMORY LEAK FIX: Clean up Handler
        if (loadingHandler != null) {
            loadingHandler.removeCallbacksAndMessages(null);
            loadingHandler = null;
        }
        super.onDestroy();
    }
}

