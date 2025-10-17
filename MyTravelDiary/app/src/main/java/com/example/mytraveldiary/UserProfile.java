package com.example.mytraveldiary;

public class UserProfile {
    private String name;
    private String email;
    private String favoriteDestinations;

    public UserProfile(String name, String email, String favoriteDestinations) {
        this.name = name;
        this.email = email;
        this.favoriteDestinations = favoriteDestinations;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getFavoriteDestinations() { return favoriteDestinations; }
}