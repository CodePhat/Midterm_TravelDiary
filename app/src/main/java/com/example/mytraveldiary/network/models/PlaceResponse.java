package com.example.mytraveldiary.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Places API response model
 */
public class PlaceResponse {
    @SerializedName("results")
    private List<Place> results;

    @SerializedName("status")
    private String status;

    public List<Place> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

    public static class Place {
        @SerializedName("name")
        private String name;

        @SerializedName("vicinity")
        private String address;

        @SerializedName("rating")
        private double rating;

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public double getRating() {
            return rating;
        }
    }
}
