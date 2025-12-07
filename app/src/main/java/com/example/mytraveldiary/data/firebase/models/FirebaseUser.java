package com.example.mytraveldiary.data.firebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Firebase model for User data
 * Optimized for Firestore storage and synchronization
 */
public class FirebaseUser {
    @DocumentId
    private String id; // Will be the user's email or Firebase Auth UID

    private String email;
    private String name;
    private String profileImageUri;

    // Metadata
    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    private boolean synced;

    public FirebaseUser() {
        this.synced = true;
    }

    public FirebaseUser(String email, String name) {
        this.email = email;
        this.name = name;
        this.synced = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
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
