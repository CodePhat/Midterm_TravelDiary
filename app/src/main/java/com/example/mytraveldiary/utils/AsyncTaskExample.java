package com.example.mytraveldiary.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.example.mytraveldiary.data.database.AppDatabase;
import com.example.mytraveldiary.data.database.entities.TripEntity;

import java.util.List;

/**
 * AsyncTask example for background database operations
 * Demonstrates: AsyncTask, Multi-threading, Background Processing
 * Note: AsyncTask is deprecated but still required for the assignment
 */
public class AsyncTaskExample {
    private static final String TAG = "AsyncTaskExample";

    /**
     * Load trips from database in background
     */
    public static class LoadTripsTask extends AsyncTask<AppDatabase, Integer, List<TripEntity>> {
        private final OnTripsLoadedListener listener;

        public interface OnTripsLoadedListener {
            void onTripsLoaded(List<TripEntity> trips);
            void onProgress(int progress);
        }

        public LoadTripsTask(OnTripsLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Starting to load trips...");
        }

        @Override
        protected List<TripEntity> doInBackground(AppDatabase... databases) {
            if (databases.length == 0) return null;

            AppDatabase db = databases[0];
            publishProgress(25);

            // Simulate some processing
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(50);

            List<TripEntity> trips = db.tripDao().getAllTripsSync();

            publishProgress(75);

            // Simulate more processing
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(100);

            return trips;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (listener != null && values.length > 0) {
                listener.onProgress(values[0]);
                Log.d(TAG, "Progress: " + values[0] + "%");
            }
        }

        @Override
        protected void onPostExecute(List<TripEntity> trips) {
            super.onPostExecute(trips);
            if (listener != null) {
                listener.onTripsLoaded(trips);
                Log.d(TAG, "Loaded " + (trips != null ? trips.size() : 0) + " trips");
            }
        }
    }

    /**
     * Save trip to database in background
     */
    public static class SaveTripTask extends AsyncTask<TripEntity, Void, Long> {
        private final AppDatabase database;
        private final OnTripSavedListener listener;

        public interface OnTripSavedListener {
            void onTripSaved(long tripId);
            void onError(Exception e);
        }

        public SaveTripTask(AppDatabase database, OnTripSavedListener listener) {
            this.database = database;
            this.listener = listener;
        }

        @Override
        protected Long doInBackground(TripEntity... trips) {
            try {
                if (trips.length > 0) {
                    return database.tripDao().insert(trips[0]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving trip", e);
                return -1L;
            }
            return -1L;
        }

        @Override
        protected void onPostExecute(Long tripId) {
            super.onPostExecute(tripId);
            if (listener != null) {
                if (tripId != -1) {
                    listener.onTripSaved(tripId);
                } else {
                    listener.onError(new Exception("Failed to save trip"));
                }
            }
        }
    }
}
