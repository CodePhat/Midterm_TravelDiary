package com.example.mytraveldiary.utils.helpers;

import android.util.Log;
import android.widget.TextView;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.HashMap;
import java.util.Map;

public class MachineTranslationHelper {

    private static final String TAG = "MachineTranslation";
    private static final Map<String, Translator> translators = new HashMap<>();
    // PERFORMANCE: Cache translations to avoid redundant API calls
    private static final Map<String, String> translationCache = new HashMap<>();

    public interface TranslationCallback {
        void onTranslated(String translatedText);
        void onError(String error);
    }

    private static Translator getTranslator(String sourceLang, String targetLang) {
        String key = sourceLang + "-" + targetLang;
        if (translators.containsKey(key)) {
            return translators.get(key);
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();
        Translator translator = Translation.getClient(options);
        translators.put(key, translator);
        return translator;
    }

    public static void translate(String text, String targetLang, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            callback.onTranslated("");
            return;
        }

        // For now, we assume source is always English
        String sourceLang = TranslateLanguage.ENGLISH;

        // PERFORMANCE: Check cache first
        String cacheKey = sourceLang + "-" + targetLang + ":" + text;
        if (translationCache.containsKey(cacheKey)) {
            String cachedTranslation = translationCache.get(cacheKey);
            Log.d(TAG, "Using cached translation for: " + text);
            callback.onTranslated(cachedTranslation);
            return;
        }

        Translator translator = getTranslator(sourceLang, targetLang);

        // IMPROVEMENT: Allow download over mobile data for better UX
        DownloadConditions conditions = new DownloadConditions.Builder()
                .build(); // Removed requireWifi() to allow mobile data downloads

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> translator.translate(text)
                        .addOnSuccessListener(translatedText -> {
                            // PERFORMANCE: Cache the translation
                            translationCache.put(cacheKey, translatedText);
                            callback.onTranslated(translatedText);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Translation failed", e);
                            callback.onError(e.getMessage());
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Model download failed", e);
                    callback.onError("Failed to download translation model: " + e.getMessage());
                });
    }

    public static void translateAndSetText(TextView textView, String text, String targetLang) {
        if (textView == null || text == null) return;

        // Don't translate if target is English (or source)
        if (targetLang.equals(TranslateLanguage.ENGLISH)) {
            textView.setText(text);
            return;
        }

        translate(text, targetLang, new TranslationCallback() {
            @Override
            public void onTranslated(String translatedText) {
                textView.setText(translatedText);
            }

            @Override
            public void onError(String error) {
                // Fallback to original text on error
                textView.setText(text);
            }
        });
    }

    public static void releaseAll() {
        for (Translator translator : translators.values()) {
            translator.close();
        }
        translators.clear();
        translationCache.clear(); // Also clear cache
        Log.d(TAG, "All translators released and cache cleared");
    }

    /**
     * Clear translation cache (useful when language changes)
     */
    public static void clearCache() {
        translationCache.clear();
        Log.d(TAG, "Translation cache cleared");
    }
}

