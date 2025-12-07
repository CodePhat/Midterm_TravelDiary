package com.example.mytraveldiary.data.database.converters;

import androidx.room.TypeConverter;

import com.example.mytraveldiary.data.database.entities.TripEntity;
import com.example.mytraveldiary.data.models.Expense;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListConverter {
    private static final Gson gson = new Gson();

    // String List Converter
    @TypeConverter
    public static List<String> fromStringList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String stringListToString(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    // Expense List Converter
    @TypeConverter
    public static List<Expense> fromExpenseList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Expense>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String expenseListToString(List<Expense> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    // Itinerary List Converter
    @TypeConverter
    public static List<TripEntity.ItineraryDay> fromItineraryList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<TripEntity.ItineraryDay>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String itineraryListToString(List<TripEntity.ItineraryDay> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    // Diary List Converter
    @TypeConverter
    public static List<TripEntity.DiaryEntry> fromDiaryList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<TripEntity.DiaryEntry>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String diaryListToString(List<TripEntity.DiaryEntry> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
}
