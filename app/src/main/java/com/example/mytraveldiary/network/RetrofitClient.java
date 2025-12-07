package com.example.mytraveldiary.network;

import com.example.mytraveldiary.network.api.TravelApiService;
import com.example.mytraveldiary.network.interceptors.LoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client singleton
 * Demonstrates: Retrofit, OkHttp, Networking, HTTP Interceptors
 */
public class RetrofitClient {
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/";
    private static final String CURRENCY_BASE_URL = "https://api.exchangerate-api.com/v4/";

    private static Retrofit weatherRetrofit;
    private static Retrofit placesRetrofit;
    private static Retrofit currencyRetrofit;

    /**
     * Get OkHttpClient with logging interceptor
     */
    private static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(new LoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * Get Weather API service
     */
    public static TravelApiService getWeatherApiService() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return weatherRetrofit.create(TravelApiService.class);
    }

    /**
     * Get Places API service
     */
    public static TravelApiService getPlacesApiService() {
        if (placesRetrofit == null) {
            placesRetrofit = new Retrofit.Builder()
                .baseUrl(PLACES_BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return placesRetrofit.create(TravelApiService.class);
    }

    /**
     * Get Currency API service
     */
    public static TravelApiService getCurrencyApiService() {
        if (currencyRetrofit == null) {
            currencyRetrofit = new Retrofit.Builder()
                .baseUrl(CURRENCY_BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return currencyRetrofit.create(TravelApiService.class);
    }
}
