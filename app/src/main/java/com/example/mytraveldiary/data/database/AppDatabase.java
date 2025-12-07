package com.example.mytraveldiary.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mytraveldiary.data.database.converters.DateConverter;
import com.example.mytraveldiary.data.database.converters.ListConverter;
import com.example.mytraveldiary.data.database.dao.TripDao;
import com.example.mytraveldiary.data.database.dao.UserDao;
import com.example.mytraveldiary.data.database.entities.TripEntity;
import com.example.mytraveldiary.data.database.entities.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {TripEntity.class, UserEntity.class},
    version = 1,
    exportSchema = true
)
@TypeConverters({DateConverter.class, ListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "travel_diary_db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService for background database operations
    public static final ExecutorService databaseExecutor =
        Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Abstract methods to get DAOs
    public abstract TripDao tripDao();
    public abstract UserDao userDao();

    // Singleton pattern
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration() // For development
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // For testing purposes
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
