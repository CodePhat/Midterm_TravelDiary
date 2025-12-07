package com.example.mytraveldiary.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mytraveldiary.data.database.converters.DateConverter;
import com.example.mytraveldiary.data.database.converters.ListConverter;
import com.example.mytraveldiary.data.models.Expense;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "trips")
@TypeConverters({DateConverter.class, ListConverter.class})
public class TripEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String imageUri;
    private Date startDate;
    private Date endDate;
    private double latitude;
    private double longitude;
    private String location;
    private boolean isFavorite;

    // Complex types stored as JSON strings
    private List<Expense> expenses;
    private List<ItineraryDay> itinerary;
    private List<DiaryEntry> diary;
    private List<String> photos;

    public TripEntity() {
        this.expenses = new ArrayList<>();
        this.itinerary = new ArrayList<>();
        this.diary = new ArrayList<>();
        this.photos = new ArrayList<>();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public List<ItineraryDay> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryDay> itinerary) {
        this.itinerary = itinerary;
    }

    public List<DiaryEntry> getDiary() {
        return diary;
    }

    public void setDiary(List<DiaryEntry> diary) {
        this.diary = diary;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    // Inner classes for complex data types
    public static class ItineraryDay {
        private String day;
        private List<DayActivity> activities;

        public ItineraryDay() {
            this.activities = new ArrayList<>();
        }

        public ItineraryDay(String day) {
            this.day = day;
            this.activities = new ArrayList<>();
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public List<DayActivity> getActivities() {
            return activities;
        }

        public void setActivities(List<DayActivity> activities) {
            this.activities = activities;
        }
    }

    public static class DayActivity {
        private String time;
        private String activity;

        public DayActivity() {}

        public DayActivity(String time, String activity) {
            this.time = time;
            this.activity = activity;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }
    }

    public static class DiaryEntry {
        private String date;
        private String content;

        public DiaryEntry() {}

        public DiaryEntry(String date, String content) {
            this.date = date;
            this.content = content;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
