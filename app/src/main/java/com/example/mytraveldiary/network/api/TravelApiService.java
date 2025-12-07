package com.example.mytraveldiary.network.api;

import com.example.mytraveldiary.network.models.WeatherResponse;
import com.example.mytraveldiary.network.models.PlaceResponse;
import com.example.mytraveldiary.network.models.CurrencyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit API Service interface
 * Defines endpoints for various travel-related APIs
 * Demonstrates: Retrofit, RESTful API integration
 */
public interface TravelApiService {
    /**
     * Get weather information for a location
     * Example API: OpenWeatherMap
     */
    @GET("weather")
    Call<WeatherResponse> getWeather(
        @Query("q") String city,
        @Query("appid") String apiKey,
        @Query("units") String units
    );

    /**
     * Get places/points of interest
     * Example API: Google Places API
     */
    @GET("place/nearbysearch/json")
    Call<PlaceResponse> getNearbyPlaces(
        @Query("location") String location,
        @Query("radius") int radius,
        @Query("type") String type,
        @Query("key") String apiKey
    );

    /**
     * Get currency exchange rates
     * Example API: Exchange Rates API
     */
    @GET("latest")
    Call<CurrencyResponse> getExchangeRates(
        @Query("base") String baseCurrency,
        @Query("symbols") String symbols
    );
}
