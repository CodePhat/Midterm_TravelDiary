package com.example.mytraveldiary;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
    private String favoriteDestinations;
    private String profileImageUri;

    public UserProfile(String name, String email, String favoriteDestinations) {
        this.name = name;
        this.email = email;
        this.favoriteDestinations = favoriteDestinations;
        this.profileImageUri = null;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getFavoriteDestinations() { return favoriteDestinations; }
    public String getProfileImageUri() { return profileImageUri; }
    public void setProfileImageUri(String uri) { this.profileImageUri = uri; }
}