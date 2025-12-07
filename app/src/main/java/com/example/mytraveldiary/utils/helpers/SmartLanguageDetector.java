package com.example.mytraveldiary.utils.helpers;

import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.IdentifiedLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Smart Language Detector using Firebase ML Kit
 * Detects when user types in a different language than current app setting
 * and suggests switching languages
 */
public class SmartLanguageDetector {

    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int MIN_WORDS_FOR_DETECTION = 3; // Minimum words to trigger detection
    private static final Pattern USD_PATTERN = Pattern.compile("\\$\\d+|USD|usd|\\d+\\s*USD");
    private static final Pattern VND_PATTERN = Pattern.compile("₫|VND|vnd|đồng|dong");

    private final LanguageIdentifier languageIdentifier;
    private final Context context;

    public interface LanguageSuggestionListener {
        void onSuggestLanguageChange(String detectedLanguage, String suggestedLanguage, String reason);
    }

    public SmartLanguageDetector(Context context) {
        this.context = context;

        // Configure language identifier with confidence threshold
        LanguageIdentificationOptions options = new LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(CONFIDENCE_THRESHOLD)
                .build();

        this.languageIdentifier = LanguageIdentification.getClient(options);
    }

    /**
     * Analyze text and suggest language change if needed
     * @param text The text to analyze
     * @param currentLanguage Current app language ("en" or "vi")
     * @param listener Callback for suggestion
     */
    public void analyzeText(String text, String currentLanguage, LanguageSuggestionListener listener) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String trimmedText = text.trim();

        // Check if text is long enough (at least 2 sentences or 3+ words)
        if (!isTextLongEnough(trimmedText)) {
            return;
        }

        // Check for currency patterns first (faster than ML detection)
        String currencyDetection = detectCurrency(trimmedText, currentLanguage);
        if (currencyDetection != null) {
            listener.onSuggestLanguageChange(
                currencyDetection,
                currencyDetection,
                "Currency mismatch detected"
            );
            return;
        }

        // Use ML Kit to detect language
        languageIdentifier.identifyLanguage(trimmedText)
                .addOnSuccessListener(languageCode -> {
                    if (!languageCode.equals("und")) { // "und" means undetermined
                        handleDetectedLanguage(languageCode, currentLanguage, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    // Silently fail - language detection is optional
                    android.util.Log.d("SmartLanguageDetector", "Failed to detect language", e);
                });
    }

    /**
     * Analyze text with possible languages and their confidence scores
     */
    public void analyzeTextWithConfidence(String text, String currentLanguage, LanguageSuggestionListener listener) {
        if (text == null || text.trim().isEmpty() || !isTextLongEnough(text.trim())) {
            return;
        }

        String trimmedText = text.trim();

        // Check currency first
        String currencyDetection = detectCurrency(trimmedText, currentLanguage);
        if (currencyDetection != null) {
            listener.onSuggestLanguageChange(
                currencyDetection,
                currencyDetection,
                "Currency pattern detected"
            );
            return;
        }

        // Get possible languages with confidence
        languageIdentifier.identifyPossibleLanguages(trimmedText)
                .addOnSuccessListener(identifiedLanguages -> {
                    if (!identifiedLanguages.isEmpty()) {
                        IdentifiedLanguage topLanguage = identifiedLanguages.get(0);
                        String detectedLang = topLanguage.getLanguageTag();
                        float confidence = topLanguage.getConfidence();

                        android.util.Log.d("SmartLanguageDetector",
                            "Detected: " + detectedLang + " (confidence: " + confidence + ")");

                        if (confidence >= CONFIDENCE_THRESHOLD) {
                            handleDetectedLanguage(detectedLang, currentLanguage, listener);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.d("SmartLanguageDetector", "Failed to detect language", e);
                });
    }

    /**
     * Check if text contains currency patterns
     */
    private String detectCurrency(String text, String currentLanguage) {
        boolean hasUSD = USD_PATTERN.matcher(text).find();
        boolean hasVND = VND_PATTERN.matcher(text).find();

        if (currentLanguage.equals("vi") && hasUSD) {
            return "en"; // Suggest English
        } else if (currentLanguage.equals("en") && hasVND) {
            return "vi"; // Suggest Vietnamese
        }

        return null;
    }

    /**
     * Check if text is long enough for detection
     * At least 3 words or 2 sentences
     */
    private boolean isTextLongEnough(String text) {
        // Check for 2+ sentences (contains period, question mark, or exclamation)
        long sentenceCount = text.chars().filter(ch -> ch == '.' || ch == '?' || ch == '!').count();
        if (sentenceCount >= 2) {
            return true;
        }

        // Check for 3+ words
        String[] words = text.split("\\s+");
        return words.length >= MIN_WORDS_FOR_DETECTION;
    }

    /**
     * Handle detected language and suggest change if mismatch
     */
    private void handleDetectedLanguage(String detectedLang, String currentLanguage, LanguageSuggestionListener listener) {
        // Normalize language codes
        String normalizedDetected = normalizeLanguageCode(detectedLang);
        String normalizedCurrent = normalizeLanguageCode(currentLanguage);

        if (!normalizedDetected.equals(normalizedCurrent) && !normalizedDetected.equals("und")) {
            String suggestedLanguage = normalizedDetected;
            String reason = "Text appears to be in " + getLanguageName(normalizedDetected);
            listener.onSuggestLanguageChange(normalizedDetected, suggestedLanguage, reason);
        }
    }

    /**
     * Normalize language code to "en" or "vi"
     */
    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null) return "en";

        String lower = languageCode.toLowerCase();
        if (lower.startsWith("vi")) {
            return "vi";
        } else if (lower.startsWith("en")) {
            return "en";
        }
        return lower;
    }

    /**
     * Get readable language name
     */
    private String getLanguageName(String languageCode) {
        switch (languageCode) {
            case "en":
                return "English";
            case "vi":
                return "Vietnamese";
            default:
                return languageCode;
        }
    }

    /**
     * Close the language identifier to free resources
     */
    public void close() {
        if (languageIdentifier != null) {
            languageIdentifier.close();
        }
    }

    /**
     * Create a text watcher for EditText that automatically detects language
     */
    public static android.text.TextWatcher createSmartTextWatcher(
            EditText editText,
            Context context,
            String currentLanguage,
            LanguageSuggestionListener listener) {

        final SmartLanguageDetector detector = new SmartLanguageDetector(context);
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final Runnable[] pendingAnalysis = {null};

        return new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Cancel pending analysis
                if (pendingAnalysis[0] != null) {
                    handler.removeCallbacks(pendingAnalysis[0]);
                }

                // Schedule new analysis with debounce (wait for user to stop typing)
                pendingAnalysis[0] = () -> {
                    String text = s.toString();
                    detector.analyzeTextWithConfidence(text, currentLanguage, listener);
                };

                // Debounce: wait 1.5 seconds after user stops typing
                handler.postDelayed(pendingAnalysis[0], 1500);
            }
        };
    }
}

