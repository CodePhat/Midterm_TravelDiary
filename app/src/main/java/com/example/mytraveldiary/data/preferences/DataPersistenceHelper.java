package com.example.mytraveldiary.data.preferences;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mytraveldiary.data.models.UserAccount;
import com.example.mytraveldiary.data.repository.AppData;

public class DataPersistenceHelper {
    private static final String TAG = "DataPersistence";
    private static final String ACCOUNTS_FILE = "accounts.dat";
    private static final String USER_TRIPS_FILE = "user_trips.dat";
    private static final String CURRENT_USER_FILE = "current_user.dat";
    private static final String USER_PROFILES_FILE = "user_profiles.dat";

    private final Context context;

    public DataPersistenceHelper(Context context) {
        this.context = context;
    }

    public void saveAccounts(Map<String, UserAccount> accounts) {
        File file = new File(context.getFilesDir(), ACCOUNTS_FILE);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(accounts);
            Log.d(TAG, "Accounts saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving accounts", e);
        }
    }

    // Load accounts
    @SuppressWarnings("unchecked")
    public Map<String, UserAccount> loadAccounts() {
        File file = new File(context.getFilesDir(), ACCOUNTS_FILE);
        if (!file.exists()) {
            Log.d(TAG, "Accounts file does not exist");
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<String, UserAccount> accounts = (Map<String, UserAccount>) ois.readObject();
            Log.d(TAG, "Accounts loaded successfully: " + accounts.size() + " accounts");
            return accounts;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading accounts", e);
            return new HashMap<>();
        }
    }

    // Save user trips
    public void saveUserTrips(Map<String, List<AppData.Trip>> userTrips) {
        File file = new File(context.getFilesDir(), USER_TRIPS_FILE);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userTrips);
            Log.d(TAG, "User trips saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving user trips", e);
        }
    }

    // Load user trips
    @SuppressWarnings("unchecked")
    public Map<String, List<AppData.Trip>> loadUserTrips() {
        File file = new File(context.getFilesDir(), USER_TRIPS_FILE);
        if (!file.exists()) {
            Log.d(TAG, "User trips file does not exist");
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<String, List<AppData.Trip>> userTrips = (Map<String, List<AppData.Trip>>) ois.readObject();
            Log.d(TAG, "User trips loaded successfully");
            return userTrips;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading user trips", e);
            return new HashMap<>();
        }
    }

    // Save current user email
    public void saveCurrentUserEmail(String email) {
        File file = new File(context.getFilesDir(), CURRENT_USER_FILE);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(email);
            Log.d(TAG, "Current user email saved: " + email);
        } catch (IOException e) {
            Log.e(TAG, "Error saving current user email", e);
        }
    }

    // Load current user email
    public String loadCurrentUserEmail() {
        File file = new File(context.getFilesDir(), CURRENT_USER_FILE);
        if (!file.exists()) {
            Log.d(TAG, "Current user file does not exist");
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            String email = (String) ois.readObject();
            Log.d(TAG, "Current user email loaded: " + email);
            return email;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading current user email", e);
            return null;
        }
    }

    // Save user profiles (email -> UserProfile mapping)
    public void saveUserProfiles(Map<String, com.example.mytraveldiary.data.models.UserProfile> userProfiles) {
        File file = new File(context.getFilesDir(), USER_PROFILES_FILE);
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(userProfiles);
            Log.d(TAG, "User profiles saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving user profiles", e);
        }
    }

    // Load user profiles
    @SuppressWarnings("unchecked")
    public Map<String, com.example.mytraveldiary.data.models.UserProfile> loadUserProfiles() {
        File file = new File(context.getFilesDir(), USER_PROFILES_FILE);
        if (!file.exists()) {
            Log.d(TAG, "User profiles file does not exist");
            return new HashMap<>();
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<String, com.example.mytraveldiary.data.models.UserProfile> userProfiles =
                (Map<String, com.example.mytraveldiary.data.models.UserProfile>) ois.readObject();
            Log.d(TAG, "User profiles loaded successfully: " + userProfiles.size() + " profiles");
            return userProfiles;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading user profiles", e);
            return new HashMap<>();
        }
    }

    // Clear all data
    public void clearAll() {
        deleteFile(ACCOUNTS_FILE);
        deleteFile(USER_TRIPS_FILE);
        deleteFile(CURRENT_USER_FILE);
        deleteFile(USER_PROFILES_FILE);
        Log.d(TAG, "All data files cleared");
    }

    private void deleteFile(String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
    }
}
