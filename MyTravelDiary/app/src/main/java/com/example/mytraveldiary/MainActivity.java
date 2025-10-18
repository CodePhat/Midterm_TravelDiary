package com.example.mytraveldiary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class MainActivity extends AppCompatActivity {

    private LinearLayout navTrips, navDashboard, navProfile;
    private ImageView iconTrips, iconDashboard, iconProfile;
    private TextView textTrips, textDashboard, textProfile;

    // THÊM 2 BIẾN NỀN MỚI
    private View bgTrips, bgDashboard, bgProfile;

    private String currentNavTab = "dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navTrips = findViewById(R.id.nav_trips);
        navDashboard = findViewById(R.id.nav_dashboard);
        navProfile = findViewById(R.id.nav_profile);

        iconTrips = findViewById(R.id.icon_trips);
        iconDashboard = findViewById(R.id.icon_dashboard);
        iconProfile = findViewById(R.id.icon_profile);

        textTrips = findViewById(R.id.text_trips);
        textDashboard = findViewById(R.id.text_dashboard);
        textProfile = findViewById(R.id.text_profile);
        bgTrips = findViewById(R.id.bg_trips);
        bgDashboard = findViewById(R.id.bg_dashboard);
        bgProfile = findViewById(R.id.bg_profile);

        navTrips.setOnClickListener(v -> setNavigationTab("trips"));
        navDashboard.setOnClickListener(v -> setNavigationTab("dashboard"));
        navProfile.setOnClickListener(v -> setNavigationTab("profile"));

        refreshNavigationTab();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new DashboardFragment())
                    .commit();
        }
    }

    private void setNavigationTab(String tab) {
        // Kiểm tra xem có đang chọn lại tab cũ không
        if (tab.equals(currentNavTab)) {
            return; // Không làm gì nếu nhấn lại tab cũ
        }

        currentNavTab = tab;
        refreshNavigationTab(); // Hàm này xử lý đổi màu sắc (đã xong)

        // --- BẮT ĐẦU PHẦN THÊM MỚI (LOGIC CHUYỂN FRAGMENT) ---
        Fragment selectedFragment = null;

        switch (tab) {
            case "trips":
                selectedFragment = new TripsFragment();
                break;
            case "dashboard":
                selectedFragment = new DashboardFragment();
                break;
            case "profile":
                selectedFragment = new ProfileFragment();
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, selectedFragment) // R.id.container là FrameLayout của bạn
                    .commit();
        }
        // --- KẾT THÚC PHẦN THÊM MỚI ---
    }

    private void refreshNavigationTab() {
        resetNavStyle(iconTrips, textTrips);
        resetNavStyle(iconDashboard, textDashboard);
        resetNavStyle(iconProfile, textProfile);

        bgTrips.setVisibility(View.INVISIBLE);
        bgDashboard.setVisibility(View.INVISIBLE);
        bgProfile.setVisibility(View.INVISIBLE);

        switch (currentNavTab) {
            case "trips":
                setNavSelectedStyle(iconTrips, textTrips);
                bgTrips.setVisibility(View.VISIBLE);
                break;
            case "dashboard":
                setNavSelectedStyle(iconDashboard, textDashboard);
                bgDashboard.setVisibility(View.VISIBLE);
                break;
            case "profile":
                setNavSelectedStyle(iconProfile, textProfile);
                bgProfile.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void resetNavStyle(ImageView icon, TextView text) {
        if (this == null) return;
        icon.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_text_unselected));
        text.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_text_unselected));
    }
    private void setNavSelectedStyle(ImageView icon, TextView text) {
        if (this == null) return;
        icon.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_text_selected));
        text.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_text_selected));
    }
}