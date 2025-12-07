package com.example.mytraveldiary.utils.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ML Kit Image Labeling Helper
 * Automatically detects objects, landmarks, and activities in trip photos
 * Use Cases:
 * - Auto-tag trip photos (beach, mountain, food, etc.)
 * - Suggest trip categories based on images
 * - Search trips by image content
 */
public class ImageLabelingHelper {
    private static final String TAG = "ImageLabelingHelper";
    private final ImageLabeler labeler;

    public interface ImageLabelCallback {
        void onSuccess(List<ImageLabelResult> labels);
        void onFailure(String error);
    }

    public static class ImageLabelResult {
        private final String text;
        private final float confidence;
        private final int index;

        public ImageLabelResult(String text, float confidence, int index) {
            this.text = text;
            this.confidence = confidence;
            this.index = index;
        }

        public String getText() {
            return text;
        }

        public float getConfidence() {
            return confidence;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return text + " (" + Math.round(confidence * 100) + "%)";
        }
    }

    public ImageLabelingHelper() {
        // Configure labeler to use ON-DEVICE model (no download needed)
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f) // Only return labels with 70%+ confidence
                .build();

        labeler = ImageLabeling.getClient(options);
        // Note: Default labeler uses on-device base model, no download required
    }

    /**
     * Label image from URI (for trip photos)
     */
    public void labelImage(Context context, Uri imageUri, ImageLabelCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            processImage(image, callback);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load image from URI", e);
            callback.onFailure("Failed to load image: " + e.getMessage());
        }
    }

    /**
     * Label image from Bitmap
     */
    public void labelImage(Bitmap bitmap, ImageLabelCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        processImage(image, callback);
    }

    private void processImage(InputImage image, ImageLabelCallback callback) {
        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    List<ImageLabelResult> results = new ArrayList<>();
                    for (ImageLabel label : labels) {
                        results.add(new ImageLabelResult(
                                label.getText(),
                                label.getConfidence(),
                                label.getIndex()
                        ));
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image labeling failed", e);
                    callback.onFailure("Labeling failed: " + e.getMessage());
                });
    }

    /**
     * Get suggested trip category based on image labels
     */
    public static String suggestTripCategory(List<ImageLabelResult> labels) {
        for (ImageLabelResult label : labels) {
            String text = label.getText().toLowerCase();

            // Beach/Ocean
            if (text.contains("beach") || text.contains("ocean") || text.contains("sea") ||
                text.contains("sand") || text.contains("wave")) {
                return "Beach Vacation";
            }

            // Mountain/Hiking
            if (text.contains("mountain") || text.contains("hiking") || text.contains("trail") ||
                text.contains("peak") || text.contains("summit")) {
                return "Mountain Adventure";
            }

            // Food/Restaurant
            if (text.contains("food") || text.contains("restaurant") || text.contains("cuisine") ||
                text.contains("meal") || text.contains("dish")) {
                return "Food Tour";
            }

            // Architecture/City
            if (text.contains("building") || text.contains("architecture") || text.contains("city") ||
                text.contains("urban") || text.contains("skyscraper")) {
                return "City Exploration";
            }

            // Nature/Wildlife
            if (text.contains("nature") || text.contains("wildlife") || text.contains("animal") ||
                text.contains("forest") || text.contains("tree")) {
                return "Nature & Wildlife";
            }

            // Cultural/Museum
            if (text.contains("museum") || text.contains("art") || text.contains("culture") ||
                text.contains("monument") || text.contains("temple")) {
                return "Cultural Tour";
            }
        }

        return "General Travel"; // Default category
    }

    /**
     * Generate auto-tags for trip from image labels
     */
    public static List<String> generateAutoTags(List<ImageLabelResult> labels) {
        List<String> tags = new ArrayList<>();
        for (ImageLabelResult label : labels) {
            if (label.getConfidence() > 0.8f) { // High confidence labels become tags
                tags.add(label.getText());
            }
        }
        return tags;
    }

    /**
     * Close the labeler to free resources
     */
    public void close() {
        if (labeler != null) {
            labeler.close();
        }
    }
}

