package com.example.mytraveldiary.utils.helpers;

import android.os.Handler;
import android.os.Looper;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiHelper {

    private static final String MODEL_NAME = "models/gemini-1.5-flash-latest";

    private final Client genaiClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public GeminiHelper() {
        String apiKey = "AIzaSyBIWCxyl-c9zCJ018lUXlLOBMh8f7Iwmq4";

        // Set API key as system property - Gemini SDK reads from GOOGLE_API_KEY
        System.setProperty("GOOGLE_API_KEY", apiKey);

        // Create client - it will read API key from system property
        genaiClient = new Client();
    }

    public void askQuestion(String prompt, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                GenerateContentResponse response = genaiClient.models.generateContent(
                        MODEL_NAME,
                        prompt,
                        null
                );

                String text = response.text();
                mainHandler.post(() -> callback.onSuccess(text != null && !text.isEmpty()
                        ? text
                        : "(Empty response)"));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(
                        e.getMessage() != null ? e.getMessage() : "Unknown error"));
            }
        });
    }

    // Convenience helpers reuse askQuestion
    public void getTravelSuggestion(String destination, GeminiCallback callback) {
        String prompt = "As a travel expert, give me 3 quick travel tips for visiting " + destination +
                ". Keep it brief and practical (max 150 words).";
        askQuestion(prompt, callback);
    }

    public void getItinerarySuggestion(String destination, int days, GeminiCallback callback) {
        String prompt = "Create a brief " + days + "-day itinerary for " + destination +
                ". List key activities per day (max 200 words).";
        askQuestion(prompt, callback);
    }

    public void getBudgetEstimate(String destination, int days, GeminiCallback callback) {
        String prompt = "Estimate daily budget for " + days + " days in " + destination +
                ". Include accommodation, food, transport, and activities (max 150 words).";
        askQuestion(prompt, callback);
    }

    // MEMORY LEAK FIX: Cleanup method to shut down executor and prevent thread leak
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
