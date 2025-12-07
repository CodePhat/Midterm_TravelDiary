package com.example.mytraveldiary.utils.ml;

import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Smart Reply Helper (Template-Based)
 * Generate diary entry suggestions and responses using templates
 *
 * Note: ML Kit Smart Reply is not available in standalone ML Kit.
 * This implementation uses intelligent template matching instead.
 *
 * Use Cases:
 * - Suggest diary entry continuations
 * - Quick reply templates for travel notes
 * - Context-aware suggestions
 */
public class SmartReplyHelper {
    private static final String TAG = "SmartReplyHelper";

    public interface SmartReplyCallback {
        void onSuccess(List<String> suggestions);
        void onFailure(String error);
    }

    public SmartReplyHelper() {
        // Template-based, no initialization needed
    }

    /**
     * Generate smart reply suggestions based on conversation history
     * Uses intelligent template matching based on keywords
     */
    public void generateReplies(List<String> messages, SmartReplyCallback callback) {
        if (messages == null || messages.isEmpty()) {
            callback.onFailure("No messages provided");
            return;
        }

        // Get the last message
        String lastMessage = messages.get(messages.size() - 1).toLowerCase();

        // Generate context-aware suggestions
        List<String> suggestions = getContextAwareSuggestions(lastMessage);

        if (suggestions.isEmpty()) {
            suggestions = getDefaultSuggestions();
        }

        callback.onSuccess(suggestions);
    }

    /**
     * Generate diary entry suggestions based on trip context
     */
    public void generateDiarySuggestions(String destination, String activity, SmartReplyCallback callback) {
        List<String> suggestions = new ArrayList<>();

        // Add context-specific suggestions
        if (destination != null && !destination.isEmpty()) {
            suggestions.add("Excited to explore " + destination + "! ✨");
            suggestions.add(destination + " is even better than I imagined!");
        }

        if (activity != null && !activity.isEmpty()) {
            suggestions.add("Can't wait to " + activity + "! 🎉");
            suggestions.add("Today's " + activity + " was unforgettable!");
        }

        // Add generic suggestions
        suggestions.addAll(getDefaultSuggestions());

        callback.onSuccess(suggestions);
    }

    /**
     * Get context-aware suggestions based on message content
     */
    private List<String> getContextAwareSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();

        // Food-related
        if (text.contains("food") || text.contains("restaurant") || text.contains("ate") ||
            text.contains("meal") || text.contains("dinner") || text.contains("lunch")) {
            suggestions.add("The food was absolutely delicious! 🍽️");
            suggestions.add("Best meal I've had in a long time!");
            suggestions.add("Can't wait to come back and try more dishes!");
        }

        // Beach/Ocean related
        else if (text.contains("beach") || text.contains("ocean") || text.contains("sea") ||
                 text.contains("sand") || text.contains("swimming")) {
            suggestions.add("The beach was absolutely beautiful! 🏖️");
            suggestions.add("Perfect weather for relaxing by the water!");
            suggestions.add("Crystal clear water and amazing views!");
        }

        // Mountain/Hiking related
        else if (text.contains("mountain") || text.contains("hiking") || text.contains("climb") ||
                 text.contains("trail") || text.contains("summit")) {
            suggestions.add("The view from the top was breathtaking! ⛰️");
            suggestions.add("Challenging but so worth the effort!");
            suggestions.add("Amazing scenery throughout the entire hike!");
        }

        // Museum/Culture related
        else if (text.contains("museum") || text.contains("art") || text.contains("history") ||
                 text.contains("culture") || text.contains("exhibit")) {
            suggestions.add("Learned so much about the local culture! 🏛️");
            suggestions.add("The exhibits were absolutely fascinating!");
            suggestions.add("A must-visit for anyone interested in history!");
        }

        // City/Urban exploration
        else if (text.contains("city") || text.contains("town") || text.contains("street") ||
                 text.contains("building") || text.contains("shopping")) {
            suggestions.add("Love exploring the vibrant city streets! 🏙️");
            suggestions.add("So much to see and do here!");
            suggestions.add("The architecture is absolutely stunning!");
        }

        // Nature/Wildlife
        else if (text.contains("nature") || text.contains("wildlife") || text.contains("animal") ||
                 text.contains("forest") || text.contains("park")) {
            suggestions.add("The natural beauty here is incredible! 🌿");
            suggestions.add("Saw some amazing wildlife today!");
            suggestions.add("Nature at its finest!");
        }

        return suggestions;
    }

    /**
     * Get default suggestions when no specific context matches
     */
    private List<String> getDefaultSuggestions() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("Today was amazing! ✨");
        suggestions.add("Such a memorable experience!");
        suggestions.add("Can't wait for tomorrow's adventure!");
        suggestions.add("Best day of the trip so far!");
        return suggestions;
    }

    /**
     * Get pre-defined travel diary prompts (fallback if Smart Reply unavailable)
     */
    public static List<String> getDefaultDiaryPrompts(String context) {
        List<String> prompts = new ArrayList<>();

        if (context.toLowerCase().contains("food") || context.toLowerCase().contains("restaurant")) {
            prompts.add("The food was amazing! 🍽️");
            prompts.add("Best meal of the trip so far!");
            prompts.add("Can't wait to come back here!");
        } else if (context.toLowerCase().contains("beach") || context.toLowerCase().contains("ocean")) {
            prompts.add("The beach was beautiful! 🏖️");
            prompts.add("Perfect weather for relaxing!");
            prompts.add("The water was crystal clear!");
        } else if (context.toLowerCase().contains("mountain") || context.toLowerCase().contains("hiking")) {
            prompts.add("The view from the top was breathtaking! ⛰️");
            prompts.add("Challenging but worth it!");
            prompts.add("Great workout with amazing scenery!");
        } else if (context.toLowerCase().contains("museum") || context.toLowerCase().contains("culture")) {
            prompts.add("Learned so much about the local culture! 🏛️");
            prompts.add("The exhibits were fascinating!");
            prompts.add("A must-visit for history lovers!");
        } else {
            // Generic travel prompts
            prompts.add("Today was amazing! ✨");
            prompts.add("Best day of the trip!");
            prompts.add("Can't wait for tomorrow's adventure!");
            prompts.add("Such a memorable experience!");
        }

        return prompts;
    }

    /**
     * Generate quick note templates for different trip types
     */
    public static List<String> getQuickNoteTemplates(String tripCategory) {
        List<String> templates = new ArrayList<>();

        switch (tripCategory.toLowerCase()) {
            case "beach vacation":
            case "beach":
                templates.add("🏖️ Relaxing day at the beach");
                templates.add("🌊 Perfect weather for swimming");
                templates.add("🌅 Beautiful sunset views");
                break;

            case "food tour":
            case "food":
                templates.add("🍽️ Tried amazing local cuisine");
                templates.add("🍜 Best [dish name] ever!");
                templates.add("👨‍🍳 Great restaurant recommendation");
                break;

            case "mountain adventure":
            case "hiking":
                templates.add("⛰️ Reached the summit!");
                templates.add("🥾 Challenging but rewarding hike");
                templates.add("📸 Incredible mountain views");
                break;

            case "city exploration":
            case "urban":
                templates.add("🏙️ Explored the downtown area");
                templates.add("🚶 Walking tour of historic sites");
                templates.add("🛍️ Great shopping districts");
                break;

            case "cultural tour":
            case "museum":
                templates.add("🏛️ Fascinating museum visit");
                templates.add("🎭 Attended local cultural event");
                templates.add("📚 Learned about local history");
                break;

            default:
                templates.add("✨ Great day exploring");
                templates.add("📸 Captured some amazing moments");
                templates.add("🗺️ Discovered hidden gems");
                break;
        }

        return templates;
    }

    /**
     * Close resources (template-based system has nothing to close)
     */
    public void close() {
        // No resources to close in template-based system
    }
}

