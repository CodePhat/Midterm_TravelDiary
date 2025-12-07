package com.example.mytraveldiary.data.firebase.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mytraveldiary.data.firebase.models.FirebaseTrip;
import com.example.mytraveldiary.data.firebase.models.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for Firebase Firestore operations
 * Handles all cloud database interactions
 */
public class FirebaseRepository {
    private static final String TAG = "FirebaseRepository";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_TRIPS = "trips";

    private final FirebaseFirestore db;
    private static FirebaseRepository instance;

    private FirebaseRepository() {
        db = FirebaseFirestore.getInstance();

        // CRITICAL FIX: Enable offline persistence for Firestore
        // This allows the app to work offline and sync when connection is restored
        try {
            com.google.firebase.firestore.FirebaseFirestoreSettings settings =
                    new com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                            .build();
            db.setFirestoreSettings(settings);
            // Note: Offline persistence is now ENABLED BY DEFAULT in newer Firebase versions
            // If you need to explicitly enable/disable, use:
            // db.enableNetwork() or db.disableNetwork()
            Log.d(TAG, "Firestore configured successfully (offline persistence enabled by default)");
        } catch (Exception e) {
            // Settings can only be set once, so catch the exception if already set
            Log.d(TAG, "Firestore settings already configured or error: " + e.getMessage());
        }
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    // ==================== USER OPERATIONS ====================

    /**
     * Save or update user in Firestore
     */
    public void saveUser(FirebaseUser user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(user.getEmail())
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Get user from Firestore
     */
    public void getUser(String email, OnSuccessListener<FirebaseUser> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS)
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseUser user = documentSnapshot.toObject(FirebaseUser.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // ==================== TRIP OPERATIONS ====================

    /**
     * Save trip to Firestore
     */
    public void saveTrip(FirebaseTrip trip, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        if (trip.getId() == null || trip.getId().isEmpty()) {
            // Create new trip
            db.collection(COLLECTION_TRIPS)
                    .add(trip)
                    .addOnSuccessListener(documentReference -> {
                        String tripId = documentReference.getId();
                        Log.d(TAG, "Trip saved with ID: " + tripId);
                        onSuccess.onSuccess(tripId);
                    })
                    .addOnFailureListener(onFailure);
        } else {
            // Update existing trip
            db.collection(COLLECTION_TRIPS)
                    .document(trip.getId())
                    .set(trip)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Trip updated: " + trip.getId());
                        onSuccess.onSuccess(trip.getId());
                    })
                    .addOnFailureListener(onFailure);
        }
    }

    /**
     * Get trip from Firestore
     */
    public void getTrip(String tripId, OnSuccessListener<FirebaseTrip> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TRIPS)
                .document(tripId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        FirebaseTrip trip = documentSnapshot.toObject(FirebaseTrip.class);
                        onSuccess.onSuccess(trip);
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Get all trips for a specific user
     */
    public void getUserTrips(String userId, OnSuccessListener<List<FirebaseTrip>> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TRIPS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FirebaseTrip> trips = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FirebaseTrip trip = document.toObject(FirebaseTrip.class);
                        trips.add(trip);
                    }
                    Log.d(TAG, "Fetched " + trips.size() + " trips for user: " + userId);
                    onSuccess.onSuccess(trips);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete trip from Firestore
     */
    public void deleteTrip(String tripId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TRIPS)
                .document(tripId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Trip deleted: " + tripId);
                    onSuccess.onSuccess(aVoid);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete all trips for a user
     */
    public void deleteAllUserTrips(String userId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_TRIPS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Log.d(TAG, "All trips deleted for user: " + userId);
                    onSuccess.onSuccess(null);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Get Firestore instance
     */
    public FirebaseFirestore getFirestore() {
        return db;
    }
}
