package com.example.mytraveldiary;

import android.content.Context;
import android.widget.Toast;

import java.util.*;

public class AppData {
    private static AppData instance;
    private static DataPersistenceHelper persistenceHelper;

    // --- Account Management ---
    private final Map<String, UserAccount> accounts = new HashMap<>();
    private UserAccount currentUser = null;

    // --- Per-user data ---
    private UserProfile profile;
    private final Map<String, List<Trip>> userTrips = new HashMap<>();

    private AppData() {
        // Constructor stays empty - data will be loaded via initialize()
    }

    public static synchronized AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public void initialize(Context context) {
        if (persistenceHelper == null) {
            persistenceHelper = new DataPersistenceHelper(context);
            loadData();

            // If no data loaded (first launch), seed sample data
            if (accounts.isEmpty()) {
                seedDefaultAccount();
            }
        }
    }

    private void seedDefaultAccount() {
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

        saveData();
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
        saveData();
        return true;
    }

    public boolean login(String email, String password) {
        UserAccount acc = accounts.get(email);
        if (acc != null && acc.getPassword().equals(password)) {
            currentUser = acc;
            profile = new UserProfile(acc.getName(), acc.getEmail(), "No favorites yet");
            userTrips.putIfAbsent(email, new ArrayList<>());
            saveData();
            return true;
        }
        return false;
    }


    public void logout(Context ctx) {
        currentUser = null;
        profile = null;
        saveData();
        Toast.makeText(ctx, "Logged out.", Toast.LENGTH_SHORT).show();
    }

    public void deleteAccount(Context ctx) {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            accounts.remove(email);
            userTrips.remove(email);
            currentUser = null;
            profile = null;
            saveData();
            Toast.makeText(ctx, "Account deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearCache(Context ctx) {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            // Clear all trips and their data
            List<Trip> trips = userTrips.get(email);
            if (trips != null) {
                trips.clear();
            }
            // Reset profile to basic info (keep name and email, clear favorites)
            if (profile != null) {
                String name = profile.getName();
                String userEmail = profile.getEmail();
                profile = new UserProfile(name, userEmail, "");
                profile.setProfileImageUri(null);
            }

            // Regenerate sample data (Tokyo and Rome trips)
            seedSampleDataForCurrentUser();

            saveData();
            Toast.makeText(ctx, "Cache cleared and sample data restored", Toast.LENGTH_SHORT).show();
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
        if (newProfile != null) {
            this.profile = newProfile;
            saveData();
        }
    }

    // ==============================
    //  TRIPS & EXPENSES
    // ==============================
    public List<Trip> getTrips() {
        if (currentUser == null) return new ArrayList<>();
        List<Trip> originalTrips = userTrips.getOrDefault(currentUser.getEmail(), new ArrayList<>());
        // Create a copy to avoid modifying the original list
        List<Trip> trips = new ArrayList<>(originalTrips);
        // Sort by favorite first, then maintain original order
        trips.sort((t1, t2) -> Boolean.compare(t2.isFavorite(), t1.isFavorite()));
        return trips;
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
        saveData();
    }

    public void removeTrip(String tripId) {
        if (currentUser == null) return;
        List<Trip> trips = userTrips.get(currentUser.getEmail());
        if (trips != null) {
            trips.removeIf(t -> t.getId().equals(tripId));
            saveData();
        }
    }

    public void addExpense(String tripId, Expense expense) {
        if (tripId == null || expense == null || currentUser == null) return;
        for (Trip t : getTrips()) {
            if (t.getId().equals(tripId)) {
                t.addExpense(expense);
                saveData();
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

    public int getTotalTrips() {
        return getTrips().size();
    }

    // Helper method to trigger save from external code (like fragments)
    public void triggerSave() {
        saveData();
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
                "android.resource://com.example.mytraveldiary/drawable/tokyo_japan"
        );
        japan.setLocation(35.6762, 139.6503); // Tokyo coordinates
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Sushi dinner", 45.5, ExpenseCategory.Food, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Metro pass", 15.0, ExpenseCategory.Transport, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Hotel stay", 300.0, ExpenseCategory.Accommodation, new Date()));
        japan.addExpense(new Expense(UUID.randomUUID().toString(),
                "Anime figurines", 120.0, ExpenseCategory.Shopping, new Date()));

        // Add sample itinerary for Tokyo
        japan.generateItineraryTemplate(5);
        japan.addActivityToDay(1, "2:00 PM", "Plane landed at Narita Airport");
        japan.addActivityToDay(1, "4:30 PM", "Check in to hotel in Shibuya");
        japan.addActivityToDay(1, "7:00 PM", "Dinner at local ramen shop");
        japan.addActivityToDay(2, "9:00 AM", "Visit Senso-ji Temple in Asakusa");
        japan.addActivityToDay(2, "12:00 PM", "Lunch at Tsukiji Outer Market");
        japan.addActivityToDay(2, "3:00 PM", "Explore Akihabara electronics district");
        japan.addActivityToDay(2, "6:00 PM", "Dinner at conveyor belt sushi");
        japan.addActivityToDay(3, "10:00 AM", "Day trip to Mount Fuji");
        japan.addActivityToDay(3, "8:00 PM", "Return to Tokyo, relax at hotel");
        japan.addActivityToDay(4, "8:00 AM", "Visit Meiji Shrine");
        japan.addActivityToDay(4, "11:00 AM", "Shopping in Harajuku");
        japan.addActivityToDay(4, "2:00 PM", "Visit teamLab Borderless Museum");
        japan.addActivityToDay(4, "7:00 PM", "Dinner in Shinjuku");
        japan.addActivityToDay(5, "10:00 AM", "Last minute souvenir shopping");
        japan.addActivityToDay(5, "1:00 PM", "Checkout from hotel");
        japan.addActivityToDay(5, "3:00 PM", "Flight back home");

        // Add sample diary entries for Tokyo
        japan.addDiaryEntry("First day in Tokyo was amazing! The city is so vibrant and clean. Can't wait to explore more.");
        japan.addDiaryEntry("Visited Senso-ji Temple today - absolutely breathtaking architecture. The street food at Nakamise was delicious!");
        japan.addDiaryEntry("Mount Fuji day trip was the highlight of the trip. Clear skies and stunning views!");
        japan.addDiaryEntry("TeamLab Museum was mind-blowing. Digital art at its finest. Highly recommend!");

        Trip italy = new Trip(
                UUID.randomUUID().toString(),
                "Rome, Italy",
                "2025-04-02",
                "2025-04-09",
                "android.resource://com.example.mytraveldiary/drawable/rome_italy"
        );
        italy.setLocation(41.9028, 12.4964); // Rome coordinates
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Pizza lunch", 25.0, ExpenseCategory.Food, new Date()));
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Taxi from airport", 40.0, ExpenseCategory.Transport, new Date()));
        italy.addExpense(new Expense(UUID.randomUUID().toString(),
                "Colosseum tickets", 60.0, ExpenseCategory.Entertainment, new Date()));

        // Add sample itinerary for Rome
        italy.generateItineraryTemplate(4);
        italy.addActivityToDay(1, "11:00 AM", "Arrived in Rome, taxi to hotel");
        italy.addActivityToDay(1, "2:00 PM", "Lunch at authentic Italian trattoria");
        italy.addActivityToDay(1, "4:00 PM", "Walking tour of Trastevere neighborhood");
        italy.addActivityToDay(1, "8:00 PM", "Dinner with view of the Tiber River");
        italy.addActivityToDay(2, "9:00 AM", "Visit the Colosseum - skip the line tour");
        italy.addActivityToDay(2, "12:00 PM", "Explore Roman Forum and Palatine Hill");
        italy.addActivityToDay(2, "3:00 PM", "Gelato break near Trevi Fountain");
        italy.addActivityToDay(2, "5:00 PM", "Throw coin in Trevi Fountain");
        italy.addActivityToDay(2, "7:00 PM", "Dinner in Piazza Navona");
        italy.addActivityToDay(3, "8:00 AM", "Early visit to Vatican Museums");
        italy.addActivityToDay(3, "11:00 AM", "Sistine Chapel - absolutely stunning!");
        italy.addActivityToDay(3, "1:00 PM", "St. Peter's Basilica and Square");
        italy.addActivityToDay(3, "6:00 PM", "Sunset at Spanish Steps");
        italy.addActivityToDay(4, "10:00 AM", "Last cappuccino at favorite café");
        italy.addActivityToDay(4, "12:00 PM", "Shopping for souvenirs");
        italy.addActivityToDay(4, "3:00 PM", "Departure to airport");

        // Add sample diary entries for Rome
        italy.addDiaryEntry("Rome is a living museum! Every corner has history. The pizza here is unlike anything I've had before.");
        italy.addDiaryEntry("The Colosseum was breathtaking. Standing where gladiators once fought gave me chills. History comes alive here!");
        italy.addDiaryEntry("Vatican Museums were overwhelming in the best way. The Sistine Chapel left me speechless. Michelangelo was a genius.");
        italy.addDiaryEntry("Last day in Rome. Already planning my return trip. This city has stolen my heart!");

        trips.add(japan);
        trips.add(italy);
    }

    // ==============================
    //  INNER TRIP CLASS
    // ==============================
    public static class DayActivity implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private String time;
        private String activity;

        public DayActivity(String time, String activity) {
            this.time = time;
            this.activity = activity;
        }

        public String getTime() { return time; }
        public String getActivity() { return activity; }
        public void setTime(String time) { this.time = time; }
        public void setActivity(String activity) { this.activity = activity; }

        // Convert time string to comparable integer (minutes since midnight)
        public int getTimeInMinutes() {
            try {
                String timeUpper = time.toUpperCase().trim();
                boolean isPM = timeUpper.contains("PM");
                boolean isAM = timeUpper.contains("AM");

                // Remove AM/PM and clean up
                String timePart = timeUpper.replace("AM", "").replace("PM", "").trim();

                // Split by : or space
                String[] parts = timePart.split(":");
                if (parts.length < 2) {
                    // Try splitting by space
                    parts = timePart.split(" ");
                }

                if (parts.length >= 2) {
                    int hours = Integer.parseInt(parts[0].trim());
                    int minutes = Integer.parseInt(parts[1].trim());

                    // Convert to 24-hour format
                    if (isPM && hours != 12) {
                        hours += 12;
                    } else if (isAM && hours == 12) {
                        hours = 0;
                    }

                    return hours * 60 + minutes;
                }
            } catch (Exception e) {
                // If parsing fails, return a default value
            }
            return 9999; // Default to end of day if can't parse
        }
    }

    public static class ItineraryDay implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private int dayNumber;
        private final List<DayActivity> activities = new ArrayList<>();

        public ItineraryDay(int dayNumber) {
            this.dayNumber = dayNumber;
        }

        public int getDayNumber() { return dayNumber; }

        public List<DayActivity> getActivities() {
            // Sort activities by time before returning
            List<DayActivity> sortedActivities = new ArrayList<>(activities);
            sortedActivities.sort((a1, a2) -> Integer.compare(a1.getTimeInMinutes(), a2.getTimeInMinutes()));
            return sortedActivities;
        }

        public void addActivity(String time, String activity) {
            activities.add(new DayActivity(time, activity));
            // Activities will be sorted when getActivities() is called
        }

        public boolean hasActivities() {
            return !activities.isEmpty();
        }
    }

    public static class Trip implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final String id;
        private final String destination;
        private final String startDate;
        private final String endDate;
        private final String image;
        private final List<Expense> expenses = new ArrayList<>();
        private boolean isFavorite = false;
        // --- Location fields ---
        private double latitude = 0.0;
        private double longitude = 0.0;
        // --- New fields ---
        private final List<ItineraryDay> itineraryDays = new ArrayList<>();
        private final List<String> diaryEntries = new ArrayList<>();
        private final List<String> photos = new ArrayList<>();

        public List<ItineraryDay> getItineraryDays() { return itineraryDays; }
        public List<String> getDiaryEntries() { return diaryEntries; }
        public List<String> getPhotos() { return photos; }

        public void generateItineraryTemplate(int totalDays) {
            itineraryDays.clear();
            for (int i = 1; i <= totalDays; i++) {
                itineraryDays.add(new ItineraryDay(i));
            }
        }

        public ItineraryDay getDay(int dayNumber) {
            for (ItineraryDay day : itineraryDays) {
                if (day.getDayNumber() == dayNumber) {
                    return day;
                }
            }
            return null;
        }

        public void addActivityToDay(int dayNumber, String time, String activity) {
            ItineraryDay day = getDay(dayNumber);
            if (day != null) {
                day.addActivity(time, activity);
            }
        }

        public void addDiaryEntry(String entry) { diaryEntries.add(entry); }
        public void addPhoto(String uri) { photos.add(uri); }

        public boolean isFavorite() { return isFavorite; }
        public void setFavorite(boolean favorite) { isFavorite = favorite; }
        public void toggleFavorite() { isFavorite = !isFavorite; }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public void setLocation(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }
        public boolean hasLocation() {
            return latitude != 0.0 || longitude != 0.0;
        }


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

    // ==============================
    //  DATA PERSISTENCE
    // ==============================
    public void saveData() {
        if (persistenceHelper == null) return;

        persistenceHelper.saveAccounts(accounts);
        persistenceHelper.saveUserTrips(userTrips);
        persistenceHelper.saveCurrentUserEmail(currentUser != null ? currentUser.getEmail() : null);
    }

    private void loadData() {
        if (persistenceHelper == null) return;

        // Load accounts
        Map<String, UserAccount> loadedAccounts = persistenceHelper.loadAccounts();
        if (loadedAccounts != null && !loadedAccounts.isEmpty()) {
            accounts.clear();
            accounts.putAll(loadedAccounts);
        }

        // Load user trips
        Map<String, List<Trip>> loadedTrips = persistenceHelper.loadUserTrips();
        if (loadedTrips != null && !loadedTrips.isEmpty()) {
            userTrips.clear();
            userTrips.putAll(loadedTrips);
        }

        // Load current user
        String currentUserEmail = persistenceHelper.loadCurrentUserEmail();
        if (currentUserEmail != null && accounts.containsKey(currentUserEmail)) {
            currentUser = accounts.get(currentUserEmail);
            if (currentUser != null) {
                profile = new UserProfile(currentUser.getName(), currentUser.getEmail(), "No favorites yet");
            }
        }
    }
}