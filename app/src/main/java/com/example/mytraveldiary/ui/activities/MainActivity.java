package com.example.mytraveldiary.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.ui.fragments.DashboardFragment;
import com.example.mytraveldiary.ui.fragments.ProfileFragment;
import com.example.mytraveldiary.ui.fragments.TravelMapFragment;
import com.example.mytraveldiary.ui.fragments.TripsFragment;
import com.example.mytraveldiary.utils.LocaleHelper;
import com.example.mytraveldiary.utils.helpers.AlarmHelper;
import com.example.mytraveldiary.utils.helpers.NotificationHelper;
import com.example.mytraveldiary.utils.helpers.ThemeManager;
import com.example.mytraveldiary.utils.helpers.MachineTranslationHelper;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navTrips, navDashboard, navMap, navProfile;
    private ImageView iconTrips, iconDashboard, iconMap, iconProfile;
    private TextView textTrips, textDashboard, textMap, textProfile;

    // Background views for selected state
    private View bgTrips, bgDashboard, bgMap, bgProfile;

    private String currentNavTab = "dashboard";
    private String previousNavTab = "dashboard";
    private static final String KEY_CURRENT_TAB = "current_tab";

    // Fragment instances to reuse
    private TripsFragment tripsFragment;
    private DashboardFragment dashboardFragment;
    private TravelMapFragment mapFragment;
    private ProfileFragment profileFragment;

    // QoL Feature: Double-tap to exit
    private boolean backPressedOnce = false;
    private static final int BACK_PRESS_DELAY = 2000; // 2 seconds


    @Override
    protected void attachBaseContext(Context newBase) {
        // ANR FIX: Use applyLocale (no I/O) instead of setLocale (with I/O)
        // This prevents blocking the main thread during Activity creation
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme preference BEFORE calling super.onCreate()
        // This prevents visual glitches and blank screens during theme changes
        ThemeManager.applyTheme(this);

        super.onCreate(savedInstanceState);

        // Initialize AppData with persistence - moved to background thread to prevent ANR
        // This is safe because AppData operations are synchronized
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppData.getInstance().initialize(getApplicationContext());
        });

        // Create notification channels (required for Android 8.0+)
        NotificationHelper.createNotificationChannels(this);

        // Schedule daily data sync alarm
        AlarmHelper.scheduleDailySync(this);

        setContentView(R.layout.activity_main);

        // Restore current tab from saved state if activity was recreated
        if (savedInstanceState != null) {
            currentNavTab = savedInstanceState.getString(KEY_CURRENT_TAB, "dashboard");
        }

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

        // QoL Feature 1: Quick Action FAB for adding new trip
        com.google.android.material.floatingactionbutton.FloatingActionButton fabQuickAddTrip =
            findViewById(R.id.fabQuickAddTrip);
        fabQuickAddTrip.setOnClickListener(v -> {
            // Switch to trips tab and trigger add trip
            setNavigationTab("trips");
            // Post delayed to allow fragment to load
            fabQuickAddTrip.postDelayed(() -> {
                if (tripsFragment != null) {
                    tripsFragment.showAddTripDialog();
                }
            }, 100);
        });

        refreshNavigationTab();

        // Setup modern back press handling with double-tap to exit
        setupBackPressedCallback();

        // Initialize all fragments on first creation
        if (savedInstanceState == null) {
            // Initialize and show the initial fragment
            initializeFragments();
        } else {
            // Restore fragment references after configuration change
            restoreFragments();
        }
    }



    private void initializeFragments() {
        // Create all fragments once
        tripsFragment = new TripsFragment();
        dashboardFragment = new DashboardFragment();
        mapFragment = new TravelMapFragment();
        profileFragment = new ProfileFragment();

        // Add all fragments to container
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, tripsFragment, "trips");
        transaction.add(R.id.container, dashboardFragment, "dashboard");
        transaction.add(R.id.container, mapFragment, "map");
        transaction.add(R.id.container, profileFragment, "profile");

        // Hide all fragments initially
        transaction.hide(tripsFragment);
        transaction.hide(mapFragment);
        transaction.hide(profileFragment);

        // Show the initial fragment based on currentNavTab
        switch (currentNavTab) {
            case "trips":
                transaction.show(tripsFragment);
                break;
            case "dashboard":
                transaction.show(dashboardFragment);
                break;
            case "map":
                transaction.show(mapFragment);
                break;
            case "profile":
                transaction.show(profileFragment);
                break;
            default:
                transaction.show(dashboardFragment);
                break;
        }

        transaction.commit();
    }

    private void restoreFragments() {
        // Restore fragment references from FragmentManager
        tripsFragment = (TripsFragment) getSupportFragmentManager().findFragmentByTag("trips");
        dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag("dashboard");
        mapFragment = (TravelMapFragment) getSupportFragmentManager().findFragmentByTag("map");
        profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("profile");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current tab so it can be restored after configuration change
        outState.putString(KEY_CURRENT_TAB, currentNavTab);
    }

    private void setNavigationTab(String tab) {
        // Check if clicking the same tab again
        if (tab.equals(currentNavTab)) {
            return; // Do nothing if same tab is clicked
        }

        // Pop back stack to remove any detail fragments (like TripDetailFragment)
        // This allows navigation to work properly from detail views
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        previousNavTab = currentNavTab;
        currentNavTab = tab;
        refreshNavigationTab(); // Handle color changes

        // Determine slide direction based on tab order
        int previousIndex = getTabIndex(previousNavTab);
        int currentIndex = getTabIndex(currentNavTab);
        boolean slideLeft = currentIndex > previousIndex;

        // Fragment switching using show/hide pattern to preserve state
        // This prevents onDestroyView from being called, avoiding blank screens and memory leaks
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Add slide animations
        if (slideLeft) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,   // popEnter
                R.anim.slide_out_right  // popExit
            );
        } else {
            transaction.setCustomAnimations(
                R.anim.slide_in_left,   // enter
                R.anim.slide_out_right, // exit
                R.anim.slide_in_right,  // popEnter
                R.anim.slide_out_left   // popExit
            );
        }

        // Hide all fragments first
        if (tripsFragment != null && tripsFragment.isAdded()) {
            transaction.hide(tripsFragment);
        }
        if (dashboardFragment != null && dashboardFragment.isAdded()) {
            transaction.hide(dashboardFragment);
        }
        if (mapFragment != null && mapFragment.isAdded()) {
            transaction.hide(mapFragment);
        }
        if (profileFragment != null && profileFragment.isAdded()) {
            transaction.hide(profileFragment);
        }

        // Show the selected fragment
        switch (tab) {
            case "trips":
                if (tripsFragment != null) {
                    transaction.show(tripsFragment);
                }
                break;
            case "dashboard":
                if (dashboardFragment != null) {
                    transaction.show(dashboardFragment);
                }
                break;
            case "map":
                if (mapFragment != null) {
                    transaction.show(mapFragment);
                }
                break;
            case "profile":
                if (profileFragment != null) {
                    transaction.show(profileFragment);
                }
                break;
        }

        transaction.commit();
    }

    private int getTabIndex(String tab) {
        switch (tab) {
            case "dashboard": return 0;
            case "trips": return 1;
            case "map": return 2;
            case "profile": return 3;
            default: return 0;
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
        icon.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_text_unselected));
        text.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_text_unselected));
    }
    private void setNavSelectedStyle(ImageView icon, TextView text) {
        icon.setColorFilter(ContextCompat.getColor(this, R.color.bottom_nav_text_selected));
        text.setTextColor(ContextCompat.getColor(this, R.color.bottom_nav_text_selected));
    }

    private void setupBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If there are fragments in the back stack, pop them
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // QoL Feature 2: Double-tap to exit confirmation
                    if (backPressedOnce) {
                        // Allow default back behavior (close app)
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                        return;
                    }

                    // First press - show toast and set flag
                    backPressedOnce = true;
                    android.widget.Toast.makeText(MainActivity.this,
                        getString(R.string.press_back_again_to_exit),
                        android.widget.Toast.LENGTH_SHORT).show();

                    // Reset flag after delay
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        backPressedOnce = false;
                    }, BACK_PRESS_DELAY);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release translation models to prevent memory leaks
        MachineTranslationHelper.releaseAll();

        // Clear fragment references to prevent memory leaks
        tripsFragment = null;
        dashboardFragment = null;
        mapFragment = null;
        profileFragment = null;

        // Clear view references
        navTrips = null;
        navDashboard = null;
        navMap = null;
        navProfile = null;
        iconTrips = null;
        iconDashboard = null;
        iconMap = null;
        iconProfile = null;
        textTrips = null;
        textDashboard = null;
        textMap = null;
        textProfile = null;
        bgTrips = null;
        bgDashboard = null;
        bgMap = null;
        bgProfile = null;
    }
}