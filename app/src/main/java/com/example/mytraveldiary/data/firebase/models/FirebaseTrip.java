package com.example.mytraveldiary.data.firebase.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Firebase model for Trip data
 * Optimized for Firestore storage and synchronization
 */
public class FirebaseTrip {
    @DocumentId
    private String id;

    private String userId; // Owner of this trip
    private String title;
    private String imageUri;
    private Timestamp startDate;
    private Timestamp endDate;
    private double latitude;
    private double longitude;
    private String location;
    private boolean isFavorite;

    // Complex types
    private List<Map<String, Object>> expenses;
    private List<Map<String, Object>> itinerary;
    private List<Map<String, Object>> diary;
    private List<String> photos;

    // Metadata
    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    private boolean synced;

    public FirebaseTrip() {
        this.expenses = new ArrayList<>();
        this.itinerary = new ArrayList<>();
        this.diary = new ArrayList<>();
        this.photos = new ArrayList<>();
        this.synced = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<Map<String, Object>> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Map<String, Object>> expenses) {
        this.expenses = expenses;
    }

    public List<Map<String, Object>> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<Map<String, Object>> itinerary) {
        this.itinerary = itinerary;
    }

    public List<Map<String, Object>> getDiary() {
        return diary;
    }

    public void setDiary(List<Map<String, Object>> diary) {
        this.diary = diary;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }
}
