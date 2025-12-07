package com.example.mytraveldiary.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Weather API response model
 * Demonstrates: JSON parsing, GSON serialization
 */
public class WeatherResponse {
    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("name")
    private String cityName;

    public Main getMain() {
        return main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public String getCityName() {
        return cityName;
    }

    public static class Main {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("feels_like")
        private double feelsLike;

        @SerializedName("humidity")
        private int humidity;

        public double getTemperature() {
            return temperature;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }
}
