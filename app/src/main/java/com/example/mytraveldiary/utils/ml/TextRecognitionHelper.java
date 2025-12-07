package com.example.mytraveldiary.utils.ml;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ML Kit Text Recognition (OCR) Helper
 * Extract text from receipts, tickets, signs, documents
 * Use Cases:
 * - Scan receipts to auto-add expenses
 * - Extract text from travel documents
 * - Read signs and menus in foreign languages
 * - Auto-detect dates and amounts from receipts
 */
public class TextRecognitionHelper {
    private static final String TAG = "TextRecognitionHelper";
    private final TextRecognizer recognizer;

    public interface TextRecognitionCallback {
        void onSuccess(RecognizedTextResult result);
        void onFailure(String error);
    }

    public static class RecognizedTextResult {
        private final String fullText;
        private final List<TextBlock> blocks;
        private final List<String> amounts;      // Detected prices/amounts
        private final List<String> dates;        // Detected dates
        private final List<String> locations;    // Detected location names

        public RecognizedTextResult(String fullText, List<TextBlock> blocks) {
            this.fullText = fullText;
            this.blocks = blocks;
            this.amounts = extractAmounts(fullText);
            this.dates = extractDates(fullText);
            this.locations = new ArrayList<>();
        }

        public String getFullText() {
            return fullText;
        }

        public List<TextBlock> getBlocks() {
            return blocks;
        }

        public List<String> getAmounts() {
            return amounts;
        }

        public List<String> getDates() {
            return dates;
        }

        public List<String> getLocations() {
            return locations;
        }

        // Extract monetary amounts from text
        private List<String> extractAmounts(String text) {
            List<String> amounts = new ArrayList<>();
            // Patterns for different currency formats
            Pattern[] patterns = {
                    Pattern.compile("\\$\\s*\\d+(?:\\.\\d{2})?"),           // $12.34
                    Pattern.compile("\\d+(?:\\.\\d{2})?\\s*USD"),           // 12.34 USD
                    Pattern.compile("€\\s*\\d+(?:[.,]\\d{2})?"),            // €12.34
                    Pattern.compile("£\\s*\\d+(?:\\.\\d{2})?"),             // £12.34
                    Pattern.compile("¥\\s*\\d+"),                           // ¥1234
                    Pattern.compile("\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?")  // 1,234.56
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    amounts.add(matcher.group());
                }
            }
            return amounts;
        }

        // Extract dates from text
        private List<String> extractDates(String text) {
            List<String> dates = new ArrayList<>();
            // Common date patterns
            Pattern[] patterns = {
                    Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{2,4}"),          // 12/31/2024
                    Pattern.compile("\\d{1,2}-\\d{1,2}-\\d{2,4}"),          // 12-31-2024
                    Pattern.compile("\\d{4}-\\d{2}-\\d{2}"),                // 2024-12-31
                    Pattern.compile("\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{2,4}") // 31 Dec 2024
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    dates.add(matcher.group());
                }
            }
            return dates;
        }
    }

    public static class TextBlock {
        private final String text;
        private final float confidence;

        public TextBlock(String text, float confidence) {
            this.text = text;
            this.confidence = confidence;
        }

        public String getText() {
            return text;
        }

        public float getConfidence() {
            return confidence;
        }
    }

    public TextRecognitionHelper() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    /**
     * Recognize text from image URI
     */
    public void recognizeText(Context context, Uri imageUri, TextRecognitionCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            processImage(image, callback);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load image from URI", e);
            callback.onFailure("Failed to load image: " + e.getMessage());
        }
    }

    /**
     * Recognize text from Bitmap
     */
    public void recognizeText(Bitmap bitmap, TextRecognitionCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        processImage(image, callback);
    }

    private void processImage(InputImage image, TextRecognitionCallback callback) {
        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String fullText = visionText.getText();
                    List<TextBlock> blocks = new ArrayList<>();

                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        blocks.add(new TextBlock(
                                block.getText(),
                                1.0f // ML Kit doesn't provide confidence for text blocks
                        ));
                    }

                    RecognizedTextResult result = new RecognizedTextResult(fullText, blocks);
                    callback.onSuccess(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    callback.onFailure("Recognition failed: " + e.getMessage());
                });
    }

    /**
     * Extract expense data from receipt text
     * Returns: [amount, date, description]
     */
    public static String[] parseReceiptData(RecognizedTextResult result) {
        String[] data = new String[3];

        // Get the largest amount (likely the total)
        if (!result.getAmounts().isEmpty()) {
            String maxAmount = result.getAmounts().get(0);
            float max = parseAmount(maxAmount);
            for (String amount : result.getAmounts()) {
                float value = parseAmount(amount);
                if (value > max) {
                    max = value;
                    maxAmount = amount;
                }
            }
            data[0] = maxAmount;
        }

        // Get the first date found
        if (!result.getDates().isEmpty()) {
            data[1] = result.getDates().get(0);
        }

        // Use first few words as description
        String text = result.getFullText();
        if (text.length() > 50) {
            data[2] = text.substring(0, 50) + "...";
        } else {
            data[2] = text;
        }

        return data;
    }

    private static float parseAmount(String amountStr) {
        try {
            // Remove currency symbols and commas
            String cleaned = amountStr.replaceAll("[^0-9.]", "");
            return Float.parseFloat(cleaned);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }

    /**
     * Close the recognizer to free resources
     */
    public void close() {
        if (recognizer != null) {
            recognizer.close();
        }
    }
}

