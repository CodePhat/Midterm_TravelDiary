package com.example.mytraveldiary.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mytraveldiary.data.database.entities.TripEntity;

import java.util.List;

@Dao
public interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TripEntity trip);

    @Update
    void update(TripEntity trip);

    @Delete
    void delete(TripEntity trip);

    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    LiveData<List<TripEntity>> getAllTrips();

    @Query("SELECT * FROM trips ORDER BY startDate DESC")
    List<TripEntity> getAllTripsSync();

    @Query("SELECT * FROM trips WHERE id = :tripId")
    LiveData<TripEntity> getTripById(long tripId);

    @Query("SELECT * FROM trips WHERE id = :tripId")
    TripEntity getTripByIdSync(long tripId);

    @Query("SELECT * FROM trips WHERE isFavorite = 1 ORDER BY startDate DESC")
    LiveData<List<TripEntity>> getFavoriteTrips();

    @Query("DELETE FROM trips")
    void deleteAllTrips();

    @Query("SELECT COUNT(*) FROM trips")
    int getTripCount();

    @Query("SELECT COUNT(*) FROM trips")
    LiveData<Integer> getTripCountLive();

    @Query("UPDATE trips SET isFavorite = :isFavorite WHERE id = :tripId")
    void updateFavoriteStatus(long tripId, boolean isFavorite);
}
