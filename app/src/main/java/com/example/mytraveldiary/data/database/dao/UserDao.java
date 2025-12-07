package com.example.mytraveldiary.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mytraveldiary.data.database.entities.UserEntity;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> getAllUsers();

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsersSync();

    @Query("SELECT * FROM users WHERE email = :email")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email")
    LiveData<UserEntity> getUserByEmailLive(String email);

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    UserEntity getCurrentUser();

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    LiveData<UserEntity> getCurrentUserLive();

    @Query("UPDATE users SET isCurrentUser = 0")
    void clearCurrentUser();

    @Query("UPDATE users SET isCurrentUser = 1 WHERE email = :email")
    void setCurrentUser(String email);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT COUNT(*) FROM users WHERE email = :email AND password = :password")
    int validateLogin(String email, String password);
}
