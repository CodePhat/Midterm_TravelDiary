package com.example.mytraveldiary.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String email;

    private String name;
    private String password;
    private String profileImageUri;
    private boolean isCurrentUser;

    public UserEntity() {}

    public UserEntity(@NonNull String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.isCurrentUser = false;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
}
