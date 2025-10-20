package com.example.mytraveldiary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;


public class MainActivity extends AppCompatActivity {

    private LinearLayout navTrips, navDashboard, navMap, navProfile;
    private ImageView iconTrips, iconDashboard, iconMap, iconProfile;
    private TextView textTrips, textDashboard, textMap, textProfile;

    // Background views for selected state
    private View bgTrips, bgDashboard, bgMap, bgProfile;

    private String currentNavTab = "dashboard";
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize AppData with persistence
        AppData.getInstance().initialize(this);

        // Apply saved theme preference
        ThemeManager.applyTheme(this);

        setContentView(R.layout.activity_main);

        // Set up toolbar with back button
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        navTrips = findViewById(R.id.nav_trips);
        navDashboard = findViewById(R.id.nav_dashboard);
        navMap = findViewById(R.id.nav_map);
        navProfile = findViewById(R.id.nav_profile);

        iconTrips = findViewById(R.id.icon_trips);
        iconDashboard = findViewById(R.id.icon_dashboard);
        iconMap = findViewById(R.id.icon_map);
        iconProfile = findViewById(R.id.icon_profile);

        textTrips = findViewById(R.id.text_trips);
        textDashboard = findViewById(R.id.text_dashboard);
        textMap = findViewById(R.id.text_map);
        textProfile = findViewById(R.id.text_profile);
        bgTrips = findViewById(R.id.bg_trips);
        bgDashboard = findViewById(R.id.bg_dashboard);
        bgMap = findViewById(R.id.bg_map);
        bgProfile = findViewById(R.id.bg_profile);

        navTrips.setOnClickListener(v -> setNavigationTab("trips"));
        navDashboard.setOnClickListener(v -> setNavigationTab("dashboard"));
        navMap.setOnClickListener(v -> setNavigationTab("map"));
        navProfile.setOnClickListener(v -> setNavigationTab("profile"));

        // Listen for back stack changes to show/hide back button
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateBackButton();
        });

        refreshNavigationTab();
        updateBackButton();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new DashboardFragment())
                    .commit();
        }
    }

    private void setNavigationTab(String tab) {
        // Check if clicking the same tab again
        if (tab.equals(currentNavTab)) {
            return; // Do nothing if same tab is clicked
        }

        currentNavTab = tab;
        refreshNavigationTab(); // Handle color changes

        // Fragment switching logic
        Fragment selectedFragment = null;

        switch (tab) {
            case "trips":
                selectedFragment = new TripsFragment();
                break;
            case "dashboard":
                selectedFragment = new DashboardFragment();
                break;
            case "map":
                selectedFragment = new TravelMapFragment();
                break;
            case "profile":
                selectedFragment = new ProfileFragment();
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    .replace(R.id.container, selectedFragment)
                    .commit();
        }
    }

    private void refreshNavigationTab() {
        resetNavStyle(iconTrips, textTrips);
        resetNavStyle(iconDashboard, textDashboard);
        resetNavStyle(iconMap, textMap);
        resetNavStyle(iconProfile, textProfile);

        bgTrips.setVisibility(View.INVISIBLE);
        bgDashboard.setVisibility(View.INVISIBLE);
        bgMap.setVisibility(View.INVISIBLE);
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
            case "map":
                setNavSelectedStyle(iconMap, textMap);
                bgMap.setVisibility(View.VISIBLE);
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

    private void updateBackButton() {
        // Show back button if there are fragments in the back stack
        boolean showBackButton = getSupportFragmentManager().getBackStackEntryCount() > 0;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
            getSupportActionBar().setDisplayShowHomeEnabled(showBackButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If there are fragments in the back stack, pop them
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}