package com.example.mytraveldiary;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);

        // Restore previously selected tab on rotation
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selected = new DashboardFragment();
            } else if (itemId == R.id.nav_trips) {
                selected = new TripsFragment();
            } else if (itemId == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
}
