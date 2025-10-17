package com.example.mytraveldiary;

import android.content.Context;
import android.widget.Toast;

import java.util.*;

public class AppData {
    private static AppData instance;

    // --- Account Management ---
    private final Map<String, UserAccount> accounts = new HashMap<>();
    private UserAccount currentUser = null;

    // --- Per-user data ---
    private UserProfile profile;
    private final Map<String, List<Trip>> userTrips = new HashMap<>();

    private AppData() {
        // Seed one default account for demo purposes
        String name = "Traveler";
        String email = "traveler@example.com";
        String password = "123456";

        UserAccount acc = new UserAccount(name, email, password);
        accounts.put(email, acc);
        userTrips.put(email, new ArrayList<>());

        // Prepopulate sample data (but don't log in automatically)
        currentUser = acc; // temporarily for seeding
        seedSampleDataForCurrentUser();
        currentUser = null; // reset to require login
    }


    public static synchronized AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    // ==============================
    //  ACCOUNT MANAGEMENT
    // ==============================
    public boolean signup(String name, String email, String password) {
        if (accounts.containsKey(email)) {
            return false; // email already exists
        }
        UserAccount newAcc = new UserAccount(name, email, password);
        accounts.put(email, newAcc);
        currentUser = newAcc;

        profile = new UserProfile(name, email, "No favorites yet");
        userTrips.put(email, new ArrayList<>());
        return true;
    }

    public boolean login(String email, String password) {
        UserAccount acc = accounts.get(email);
        if (acc != null && acc.getPassword().equals(password)) {
            currentUser = acc;
            profile = new UserProfile(acc.getName(), acc.getEmail(), "No favorites yet");
            userTrips.putIfAbsent(email, new ArrayList<>());
            return true;
        }
        return false;
    }


    public void logout(Context ctx) {
        currentUser = null;
        profile = null;
        Toast.makeText(ctx, "Logged out.", Toast.LENGTH_SHORT).show();
    }

    public void deleteAccount(Context ctx) {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            accounts.remove(email);
            userTrips.remove(email);
            currentUser = null;
            profile = null;
            Toast.makeText(ctx, "Account deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    public UserAccount getCurrentUser() {
        return currentUser;
    }

    // ==============================
    //  PROFILE
    // ==============================
    public UserProfile getProfile() {
        if (profile == null && currentUser != null) {
            profile = new UserProfile(currentUser.getName(), currentUser.getEmail(), "No favorites yet");
        }
        return profile;
    }

    public void updateProfile(UserProfile newProfile) {
        if (newProfile != null) this.profile = newProfile;
    }

    // ==============================
    //  TRIPS & EXPENSES
    // ==============================
    public List<Trip> getTrips() {
        if (currentUser == null) return new ArrayList<>();
        return userTrips.getOrDefault(currentUser.getEmail(), new ArrayList<>());
    }



    public Trip getTrip(String id) {
        for (Trip t : getTrips()) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    public void addTrip(Trip trip) {
        if (currentUser == null || trip == null) return;
        userTrips.computeIfAbsent(currentUser.getEmail(), k -> new ArrayList<>()).add(0, trip);
    }

    public void removeTrip(String tripId) {
        if (currentUser == null) return;
        List<Trip> trips = userTrips.get(currentUser.getEmail());
        if (trips != null) trips.removeIf(t -> t.getId().equals(tripId));
    }

    public void addExpense(String tripId, Expense expense) {
        if (tripId == null || expense == null || currentUser == null) return;
        for (Trip t : getTrips()) {
            if (t.getId().equals(tripId)) {
                t.addExpense(expense);
                return;
            }
        }
    }

    // --- Expense summary for dashboard ---
    public Map<String, Float> getExpenseData() {
        Map<String, Float> totals = new HashMap<>();
        for (Trip trip : getTrips()) {
            for (Expense e : trip.getExpenses()) {
                String cat = e.getCategory().name();
                float current = totals.getOrDefault(cat, 0f);
                totals.put(cat, current + (float) e.getAmount());
            }
        }
        return totals;
    }

    // ==============================
    //  SAMPLE DATA (for demo)
    // ==============================
    private void seedSampleDataForCurrentUser() {
        if (currentUser == null) return;

        List<Trip> trips = userTrips.get(currentUser.getEmail());
        if (trips == null) {
            trips = new ArrayList<>();
            userTrips.put(currentUser.getEmail(), trips);
        }

        Trip japan = new Trip(
                UUID.randomUUID().toString(),
                "Tokyo, Japan",
                "2025-03-10",
                "2025-03-18",
                "https://images.unsplash.com/photo-1505069611822-69b4d1a3d07e"
        );
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Sushi dinner", 45.5, ExpenseCategory.Food, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Metro pass", 15.0, ExpenseCategory.Transport, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Hotel stay", 300.0, ExpenseCategory.Accommodation, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Anime figurines", 120.0, ExpenseCategory.Shopping, new Date()));

        Trip italy = new Trip(
                UUID.randomUUID().toString(),
                "Rome, Italy",
                "2025-04-02",
                "2025-04-09",
                "https://images.unsplash.com/photo-1563393351-c75aa2d8e9a5"
        );
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Pizza lunch", 25.0, ExpenseCategory.Food, new Date()));
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Taxi from airport", 40.0, ExpenseCategory.Transport, new Date()));
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Colosseum tickets", 60.0, ExpenseCategory.Entertainment, new Date()));

        trips.add(japan);
        trips.add(italy);
    }

    // ==============================
    //  INNER TRIP CLASS
    // ==============================
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