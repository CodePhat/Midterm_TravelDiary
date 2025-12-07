package com.example.mytraveldiary.network.interceptors;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Custom OkHttp Interceptor for logging network requests
 * Demonstrates: OkHttp Interceptors, Network Monitoring
 */
public class LoggingInterceptor implements Interceptor {
    private static final String TAG = "NetworkRequest";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        long startTime = System.nanoTime();
        Log.d(TAG, String.format("Sending request: %s", request.url()));

        Response response = chain.proceed(request);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        Log.d(TAG, String.format("Received response for %s in %dms - Code: %d",
            response.request().url(), duration, response.code()));

        return response;
    }
}
