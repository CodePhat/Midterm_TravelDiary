package com.example.mytraveldiary.data.firebase.sync;

import android.content.Context;
import android.util.Log;

import com.example.mytraveldiary.data.database.AppDatabase;
import com.example.mytraveldiary.data.database.entities.TripEntity;
import com.example.mytraveldiary.data.database.entities.UserEntity;
import com.example.mytraveldiary.data.firebase.models.FirebaseTrip;
import com.example.mytraveldiary.data.firebase.models.FirebaseUser;
import com.example.mytraveldiary.data.firebase.repository.FirebaseRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Synchronization manager for syncing data between Room (local) and Firestore (cloud)
 * Handles bidirectional sync operations
 */
public class FirebaseSyncManager {
    private static final String TAG = "FirebaseSyncManager";

    private final Context context;
    private final AppDatabase localDb;
    private final FirebaseRepository firebaseRepo;
    private final ExecutorService executorService;
    private static FirebaseSyncManager instance;

    private FirebaseSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.localDb = AppDatabase.getInstance(context);
        this.firebaseRepo = FirebaseRepository.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized FirebaseSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseSyncManager(context);
        }
        return instance;
    }

    // ==================== USER SYNC ====================

    /**
     * Sync user to cloud
     */
    public void syncUserToCloud(String userEmail, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                UserEntity localUser = localDb.userDao().getUserByEmail(userEmail);
                if (localUser != null) {
                    FirebaseUser firebaseUser = convertToFirebaseUser(localUser);
                    firebaseRepo.saveUser(firebaseUser,
                            aVoid -> {
                                Log.d(TAG, "User synced to cloud: " + userEmail);
                                if (callback != null) callback.onSuccess();
                            },
                            e -> {
                                Log.e(TAG, "Failed to sync user to cloud", e);
                                if (callback != null) callback.onFailure(e);
                            });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing user to cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Sync user from cloud to local
     */
    public void syncUserFromCloud(String userEmail, SyncCallback callback) {
        firebaseRepo.getUser(userEmail,
                firebaseUser -> {
                    if (firebaseUser != null) {
                        executorService.execute(() -> {
                            try {
                                UserEntity localUser = convertToUserEntity(firebaseUser);
                                localDb.userDao().insert(localUser);
                                Log.d(TAG, "User synced from cloud: " + userEmail);
                                if (callback != null) callback.onSuccess();
                            } catch (Exception e) {
                                Log.e(TAG, "Error saving user to local database", e);
                                if (callback != null) callback.onFailure(e);
                            }
                        });
                    } else {
                        Log.d(TAG, "User not found in cloud: " + userEmail);
                        if (callback != null) callback.onSuccess();
                    }
                },
                e -> {
                    Log.e(TAG, "Failed to fetch user from cloud", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // ==================== TRIP SYNC ====================

    /**
     * Sync all trips to cloud for a user
     */
    public void syncTripsToCloud(String userEmail, SyncCallback callback) {
        executorService.execute(() -> {
            try {
                List<TripEntity> localTrips = localDb.tripDao().getAllTripsSync();
                int totalTrips = localTrips.size();
                int[] syncedCount = {0};

                if (totalTrips == 0) {
                    Log.d(TAG, "No trips to sync");
                    if (callback != null) callback.onSuccess();
                    return;
                }

                for (TripEntity trip : localTrips) {
                    FirebaseTrip firebaseTrip = convertToFirebaseTrip(trip, userEmail);
                    firebaseRepo.saveTrip(firebaseTrip,
                            tripId -> {
                                syncedCount[0]++;
                                Log.d(TAG, "Trip synced: " + syncedCount[0] + "/" + totalTrips);
                                if (syncedCount[0] == totalTrips) {
                                    if (callback != null) callback.onSuccess();
                                }
                            },
                            e -> {
                                Log.e(TAG, "Failed to sync trip", e);
                                syncedCount[0]++;
                                if (syncedCount[0] == totalTrips) {
                                    if (callback != null) callback.onFailure(e);
                                }
                            });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error syncing trips to cloud", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Sync all trips from cloud to local
     */
    public void syncTripsFromCloud(String userEmail, SyncCallback callback) {
        firebaseRepo.getUserTrips(userEmail,
                firebaseTrips -> {
                    executorService.execute(() -> {
                        try {
                            // Clear local trips before syncing from cloud
                            localDb.tripDao().deleteAllTrips();

                            // Insert trips from cloud
                            for (FirebaseTrip firebaseTrip : firebaseTrips) {
                                TripEntity localTrip = convertToTripEntity(firebaseTrip);
                                localDb.tripDao().insert(localTrip);
                            }

                            Log.d(TAG, "Synced " + firebaseTrips.size() + " trips from cloud");
                            if (callback != null) callback.onSuccess();
                        } catch (Exception e) {
                            Log.e(TAG, "Error saving trips to local database", e);
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                },
                e -> {
                    Log.e(TAG, "Failed to fetch trips from cloud", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Full bidirectional sync - syncs both user and trips
     */
    public void performFullSync(String userEmail, SyncCallback callback) {
        Log.d(TAG, "Starting full sync for user: " + userEmail);

        // First sync user
        syncUserToCloud(userEmail, new SyncCallback() {
            @Override
            public void onSuccess() {
                // Then sync trips
                syncTripsToCloud(userEmail, new SyncCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Full sync completed successfully");
                        if (callback != null) callback.onSuccess();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Failed to sync trips", e);
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to sync user", e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    // ==================== CONVERSION METHODS ====================

    private FirebaseUser convertToFirebaseUser(UserEntity userEntity) {
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.setId(userEntity.getEmail());
        firebaseUser.setEmail(userEntity.getEmail());
        firebaseUser.setName(userEntity.getName());
        firebaseUser.setProfileImageUri(userEntity.getProfileImageUri());
        return firebaseUser;
    }

    private UserEntity convertToUserEntity(FirebaseUser firebaseUser) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(firebaseUser.getEmail());
        userEntity.setName(firebaseUser.getName());
        userEntity.setProfileImageUri(firebaseUser.getProfileImageUri());
        return userEntity;
    }

    private FirebaseTrip convertToFirebaseTrip(TripEntity tripEntity, String userId) {
        FirebaseTrip firebaseTrip = new FirebaseTrip();
        firebaseTrip.setUserId(userId);
        firebaseTrip.setTitle(tripEntity.getTitle());
        firebaseTrip.setImageUri(tripEntity.getImageUri());

        // Convert dates to Timestamp
        if (tripEntity.getStartDate() != null) {
            firebaseTrip.setStartDate(new Timestamp(tripEntity.getStartDate()));
        }
        if (tripEntity.getEndDate() != null) {
            firebaseTrip.setEndDate(new Timestamp(tripEntity.getEndDate()));
        }

        firebaseTrip.setLatitude(tripEntity.getLatitude());
        firebaseTrip.setLongitude(tripEntity.getLongitude());
        firebaseTrip.setLocation(tripEntity.getLocation());
        firebaseTrip.setFavorite(tripEntity.isFavorite());

        // Convert complex types to Maps (simplified)
        firebaseTrip.setExpenses(new ArrayList<>());
        firebaseTrip.setItinerary(new ArrayList<>());
        firebaseTrip.setDiary(new ArrayList<>());
        firebaseTrip.setPhotos(tripEntity.getPhotos() != null ? tripEntity.getPhotos() : new ArrayList<>());

        return firebaseTrip;
    }

    private TripEntity convertToTripEntity(FirebaseTrip firebaseTrip) {
        TripEntity tripEntity = new TripEntity();
        tripEntity.setTitle(firebaseTrip.getTitle());
        tripEntity.setImageUri(firebaseTrip.getImageUri());

        // Convert Timestamp to Date
        if (firebaseTrip.getStartDate() != null) {
            tripEntity.setStartDate(firebaseTrip.getStartDate().toDate());
        }
        if (firebaseTrip.getEndDate() != null) {
            tripEntity.setEndDate(firebaseTrip.getEndDate().toDate());
        }

        tripEntity.setLatitude(firebaseTrip.getLatitude());
        tripEntity.setLongitude(firebaseTrip.getLongitude());
        tripEntity.setLocation(firebaseTrip.getLocation());
        tripEntity.setFavorite(firebaseTrip.isFavorite());
        tripEntity.setPhotos(firebaseTrip.getPhotos() != null ? firebaseTrip.getPhotos() : new ArrayList<>());

        return tripEntity;
    }

    // ==================== CALLBACK INTERFACE ====================

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
