package com.example.mytraveldiary;

import android.content.Context;
import android.widget.Toast;
import java.util.*;

public class AppData {
    private static AppData instance;

    private UserProfile profile;
    private static final List<Trip> trips = new ArrayList<>();

    // --- Singleton ---
    private AppData() {
        profile = new UserProfile("Traveler", "traveler@example.com", "Japan, Italy");
    }

    public static synchronized AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }

    // --- Profile ---
    public UserProfile getProfile() {
        if (profile == null) {
            profile = new UserProfile("Traveler", "traveler@example.com", "Japan, Italy");
        }
        return profile;
    }

    public void updateProfile(UserProfile newProfile) {
        if (newProfile != null) this.profile = newProfile;
    }

    // --- Expense Summary (Dashboard) ---
    public Map<String, Float> getExpenseData() {
        Map<String, Float> totals = new HashMap<>();

        for (Trip trip : trips) {
            for (Expense e : trip.getExpenses()) {
                String cat = e.getCategory().name();
                float current = totals.getOrDefault(cat, 0f);
                totals.put(cat, current + (float) e.getAmount());
            }
        }
        return totals;
    }

    // --- Trip Management ---
    public List<Trip> getTrips() {
        return trips;
    }

    public Trip getTrip(String id) {
        for (Trip t : trips) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public void addTrip(Trip trip) {
        if (trip != null) trips.add(0, trip);
    }

    public void removeTrip(String tripId) {
        trips.removeIf(t -> t.getId().equals(tripId));
    }

    // ✅ Add expense to a trip (this is what AddExpenseDialog calls)
    public void addExpense(String tripId, Expense expense) {
        if (tripId == null || expense == null) return;
        for (Trip t : trips) {
            if (t.getId().equals(tripId)) {
                t.addExpense(expense);
                return;
            }
        }
    }

    // --- Account Actions ---
    public void logout(Context ctx) {
        Toast.makeText(ctx.getApplicationContext(), "Switched account.", Toast.LENGTH_SHORT).show();
    }

    public void deleteAccount(Context ctx) {
        Toast.makeText(ctx.getApplicationContext(), "Account deleted.", Toast.LENGTH_SHORT).show();
        profile = null;
        trips.clear();
    }

    // --- Inner Trip class ---
    public static class Trip {
        private final String id;
        private final String destination;
        private final String startDate;
        private final String endDate;
        private final String image;
        private final List<Expense> expenses = new ArrayList<>();

        public Trip(String id, String destination, String startDate, String endDate, String image) {
            this.id = id;
            this.destination = destination;
            this.startDate = startDate;
            this.endDate = endDate;
            this.image = image;
        }

        public String getId() { return id; }
        public String getDestination() { return destination; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getImage() { return image; }

        public List<Expense> getExpenses() { return expenses; }

        // ✅ Simple per-trip expense add
        public void addExpense(Expense expense) {
            if (expense != null) expenses.add(expense);
        }

        public Map<String, Double> getExpenseSummary() {
            Map<String, Double> map = new HashMap<>();
            for (Expense e : expenses) {
                String cat = e.getCategory().name();
                map.put(cat, map.getOrDefault(cat, 0.0) + e.getAmount());
            }
            return map;
        }

        public String getDateRange() {
            return startDate + " - " + endDate;
        }
    }
}
