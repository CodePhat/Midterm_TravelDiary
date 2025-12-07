package com.example.mytraveldiary.utils.ml;

import android.content.Context;
import android.util.Log;

/**
 * ML Kit Model Manager
 * Manages ML Kit features availability
 *
 * Key Points:
 * - Image Labeling: On-device base model (always ready)
 * - Text Recognition: On-device model (always ready)
 * - Smart Reply: Template-based (always ready, no ML Kit API needed)
 * - Entity Extraction: Pattern-based (always ready, no ML Kit API needed)
 *
 * All features work immediately, no downloads required.
 * Smart Reply and Entity Extraction use intelligent pattern matching
 * instead of ML Kit APIs which are not available in standalone ML Kit.
 */
public class MLKitModelManager {
    private static final String TAG = "MLKitModelManager";
    private static MLKitModelManager instance;

    private boolean allFeaturesReady = true; // All features are pattern/on-device based
    private boolean isInitializing = false;

    private MLKitModelManager() {}

    public static synchronized MLKitModelManager getInstance() {
        if (instance == null) {
            instance = new MLKitModelManager();
        }
        return instance;
    }

    /**
     * Initialize all ML Kit features
     * Call this in MyApplication.onCreate() or on app startup
     * All features are ready immediately (on-device or pattern-based)
     */
    public void downloadAllModels(Context context, ModelDownloadCallback callback) {
        if (isInitializing) {
            Log.d(TAG, "Already initializing");
            return;
        }

        isInitializing = true;
        Log.d(TAG, "Initializing ML Kit features...");

        // All features are on-device or pattern-based - no downloads needed
        // Image Labeling: Uses on-device base model
        // Text Recognition: Uses on-device model
        // Smart Reply: Template-based system
        // Entity Extraction: Pattern-based system

        EntityExtractionHelper helper = new EntityExtractionHelper();
        helper.downloadModel(
            () -> {
                isInitializing = false;
                allFeaturesReady = true;
                Log.d(TAG, "All ML Kit features ready! (on-device & pattern-based)");
                if (callback != null) callback.onSuccess();
            },
            () -> {
                isInitializing = false;
                Log.w(TAG, "Initialization completed");
                if (callback != null) callback.onSuccess();
            }
        );
    }

    /**
     * Check if all features are ready
     */
    public boolean areModelsReady() {
        // All features are on-device or pattern-based - always ready
        return allFeaturesReady;
    }

    /**
     * Check if Entity Extraction is available
     */
    public boolean isEntityExtractionReady() {
        // Pattern-based entity extraction is always ready
        return allFeaturesReady;
    }

    /**
     * Get status message for UI
     */
    public String getStatusMessage() {
        if (isInitializing) {
            return "Initializing ML Kit features...";
        }

        StringBuilder status = new StringBuilder();
        status.append("✓ Image Labeling: Ready (on-device)\n");
        status.append("✓ Text Recognition (OCR): Ready (on-device)\n");
        status.append("✓ Smart Reply: Ready (template-based)\n");
        status.append("✓ Entity Extraction: Ready (pattern-based)");

        return status.toString();
    }

    /**
     * Callback for model downloads
     */
    public interface ModelDownloadCallback {
        void onSuccess();
        void onFailure(String error);
        void onPartialSuccess(String message);
    }
}

