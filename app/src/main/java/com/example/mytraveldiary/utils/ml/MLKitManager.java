package com.example.mytraveldiary.utils.ml;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * ML Kit Manager - Central hub for all ML Kit features
 * Coordinates and manages all ML Kit helpers
 *
 * Features Included:
 * 1. Image Labeling - Auto-tag trip photos
 * 2. Text Recognition (OCR) - Scan receipts and documents
 * 3. Smart Reply - Generate diary entry suggestions
 * 4. Entity Extraction - Parse dates, places, addresses
 *
 * Usage Examples:
 * - Auto-categorize trips based on photo content
 * - Scan receipts to auto-add expenses
 * - Suggest diary entry text
 * - Extract dates and places from text
 */
public class MLKitManager {
    private static final String TAG = "MLKitManager";
    private static MLKitManager instance;

    private ImageLabelingHelper imageLabelingHelper;
    private TextRecognitionHelper textRecognitionHelper;
    private SmartReplyHelper smartReplyHelper;
    private EntityExtractionHelper entityExtractionHelper;

    private MLKitManager() {
        // Private constructor for singleton
    }

    public static synchronized MLKitManager getInstance() {
        if (instance == null) {
            instance = new MLKitManager();
        }
        return instance;
    }

    /**
     * Initialize all ML Kit helpers
     * Call this once at app startup
     */
    public void initialize(Context context) {
        Log.d(TAG, "Initializing ML Kit helpers...");

        imageLabelingHelper = new ImageLabelingHelper();
        textRecognitionHelper = new TextRecognitionHelper();
        smartReplyHelper = new SmartReplyHelper();
        entityExtractionHelper = new EntityExtractionHelper();

        Log.d(TAG, "ML Kit helpers created");

        // Download required models in background
        MLKitModelManager.getInstance().downloadAllModels(context,
            new MLKitModelManager.ModelDownloadCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "All ML Kit models downloaded successfully");
                }

                @Override
                public void onFailure(String error) {
                    Log.w(TAG, "Some ML Kit models failed to download: " + error);
                }

                @Override
                public void onPartialSuccess(String message) {
                    Log.d(TAG, "ML Kit partially ready: " + message);
                }
            });

        Log.d(TAG, "ML Kit initialization complete");
    }

    // ==================== IMAGE LABELING ====================

    /**
     * Analyze trip photo and suggest category
     */
    public void analyzeTripPhoto(Context context, Uri photoUri, TripPhotoAnalysisCallback callback) {
        if (imageLabelingHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }

        imageLabelingHelper.labelImage(context, photoUri, new ImageLabelingHelper.ImageLabelCallback() {
            @Override
            public void onSuccess(List<ImageLabelingHelper.ImageLabelResult> labels) {
                // Generate suggestions based on labels
                String suggestedCategory = ImageLabelingHelper.suggestTripCategory(labels);
                List<String> autoTags = ImageLabelingHelper.generateAutoTags(labels);

                callback.onSuccess(suggestedCategory, autoTags, labels);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    public interface TripPhotoAnalysisCallback {
        void onSuccess(String suggestedCategory, List<String> autoTags, List<ImageLabelingHelper.ImageLabelResult> labels);
        void onFailure(String error);
    }

    // ==================== TEXT RECOGNITION (OCR) ====================

    /**
     * Scan receipt and extract expense data
     */
    public void scanReceipt(Context context, Uri receiptUri, ReceiptScanCallback callback) {
        if (textRecognitionHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }

        textRecognitionHelper.recognizeText(context, receiptUri, new TextRecognitionHelper.TextRecognitionCallback() {
            @Override
            public void onSuccess(TextRecognitionHelper.RecognizedTextResult result) {
                // Parse receipt data
                String[] data = TextRecognitionHelper.parseReceiptData(result);
                String amount = data[0];
                String date = data[1];
                String description = data[2];

                callback.onSuccess(amount, date, description, result);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    public interface ReceiptScanCallback {
        void onSuccess(String amount, String date, String description, TextRecognitionHelper.RecognizedTextResult fullResult);
        void onFailure(String error);
    }

    /**
     * Extract text from any image
     */
    public void extractText(Context context, Uri imageUri, TextRecognitionHelper.TextRecognitionCallback callback) {
        if (textRecognitionHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }
        textRecognitionHelper.recognizeText(context, imageUri, callback);
    }

    // ==================== SMART REPLY ====================

    /**
     * Generate diary entry suggestions
     */
    public void generateDiarySuggestions(List<String> previousEntries, SmartReplyHelper.SmartReplyCallback callback) {
        if (smartReplyHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }
        smartReplyHelper.generateReplies(previousEntries, callback);
    }

    /**
     * Get quick note templates for trip type
     */
    public List<String> getQuickNoteTemplates(String tripCategory) {
        return SmartReplyHelper.getQuickNoteTemplates(tripCategory);
    }

    /**
     * Get default diary prompts based on context
     */
    public List<String> getDiaryPrompts(String context) {
        return SmartReplyHelper.getDefaultDiaryPrompts(context);
    }

    // ==================== ENTITY EXTRACTION ====================

    /**
     * Extract dates and places from diary text
     */
    public void parseItinerary(String text, EntityExtractionHelper.EntityExtractionCallback callback) {
        if (entityExtractionHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }
        entityExtractionHelper.parseItinerary(text, callback);
    }

    /**
     * Extract structured data from any text
     */
    public void extractEntities(String text, EntityExtractionHelper.EntityExtractionCallback callback) {
        if (entityExtractionHelper == null) {
            callback.onFailure("ML Kit not initialized");
            return;
        }
        entityExtractionHelper.extractEntities(text, callback);
    }

    // ==================== COMBINED FEATURES ====================

    /**
     * Smart Trip Creation - Analyzes photo + text to auto-fill trip details
     */
    public void smartTripAnalysis(Context context, Uri photoUri, String description, SmartTripCallback callback) {
        // First, analyze the photo
        analyzeTripPhoto(context, photoUri, new TripPhotoAnalysisCallback() {
            @Override
            public void onSuccess(String suggestedCategory, List<String> autoTags, List<ImageLabelingHelper.ImageLabelResult> labels) {
                // Then, extract entities from description
                if (description != null && !description.trim().isEmpty()) {
                    extractEntities(description, new EntityExtractionHelper.EntityExtractionCallback() {
                        @Override
                        public void onSuccess(EntityExtractionHelper.ExtractedEntities entities) {
                            callback.onSuccess(suggestedCategory, autoTags, entities);
                        }

                        @Override
                        public void onFailure(String error) {
                            // Still return photo analysis even if entity extraction fails
                            callback.onSuccess(suggestedCategory, autoTags, null);
                        }
                    });
                } else {
                    callback.onSuccess(suggestedCategory, autoTags, null);
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    public interface SmartTripCallback {
        void onSuccess(String category, List<String> tags, EntityExtractionHelper.ExtractedEntities entities);
        void onFailure(String error);
    }

    // ==================== RESOURCE MANAGEMENT ====================

    /**
     * Close all ML Kit helpers to free resources
     * Call this when the app is being destroyed
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down ML Kit helpers...");

        if (imageLabelingHelper != null) {
            imageLabelingHelper.close();
            imageLabelingHelper = null;
        }

        if (textRecognitionHelper != null) {
            textRecognitionHelper.close();
            textRecognitionHelper = null;
        }

        if (smartReplyHelper != null) {
            smartReplyHelper.close();
            smartReplyHelper = null;
        }

        if (entityExtractionHelper != null) {
            entityExtractionHelper.close();
            entityExtractionHelper = null;
        }

        Log.d(TAG, "ML Kit shutdown complete");
    }

    /**
     * Check if ML Kit is initialized and ready
     */
    public boolean isInitialized() {
        return imageLabelingHelper != null &&
               textRecognitionHelper != null &&
               smartReplyHelper != null &&
               entityExtractionHelper != null;
    }
}

