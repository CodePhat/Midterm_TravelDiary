package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.data.models.UserProfile;
import com.example.mytraveldiary.ui.adapters.TripAdapter;
import com.example.mytraveldiary.utils.helpers.MachineTranslationHelper;
import com.example.mytraveldiary.utils.LocaleHelper;
import com.google.mlkit.nl.translate.TranslateLanguage;

public class DashboardFragment extends Fragment {

    private PieChart pieChart;
    private TextView totalTripsText, totalSpentText, avgPerTripText;
    private RecyclerView recentTripsRecycler;
    private LinearLayout noRecentTripsContainer;
    private TextView noRecentTripsText, noRecentTripsSubText;
    private TextView seeAllTrips;
    private SwipeRefreshLayout swipeRefresh;
    private com.google.android.material.imageview.ShapeableImageView profileAvatarView;
    private android.content.BroadcastReceiver profileUpdateReceiver;

    // MEMORY LEAK FIX: Track animated views to cancel animations in onDestroyView
    private View floatingIcon1;
    private View floatingIcon2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView welcomeText = root.findViewById(R.id.welcomeText);
        pieChart = root.findViewById(R.id.pieChart);
        totalTripsText = root.findViewById(R.id.totalTripsText);
        totalSpentText = root.findViewById(R.id.totalSpentText);
        avgPerTripText = root.findViewById(R.id.avgPerTripText);

        // Setup floating animations
        // MEMORY LEAK FIX: Store references to cancel animations in onDestroyView
        floatingIcon1 = root.findViewById(R.id.floatingIcon1);
        floatingIcon2 = root.findViewById(R.id.floatingIcon2);
        if (floatingIcon1 != null) {
            startFloatingAnimation(floatingIcon1, 3000, 20f);
        }
        if (floatingIcon2 != null) {
            startFloatingAnimation(floatingIcon2, 4000, 15f);
        }

        // Update profile avatar
        profileAvatarView = root.findViewById(R.id.profileAvatar);
        if (profileAvatarView != null) {
            updateProfileAvatar(profileAvatarView);
        }

        // Register receiver to listen for profile picture updates
        registerProfileUpdateReceiver();

        recentTripsRecycler = root.findViewById(R.id.recentTripsRecycler);
        noRecentTripsContainer = root.findViewById(R.id.noRecentTripsContainer);
        noRecentTripsText = root.findViewById(R.id.noRecentTripsText);
        noRecentTripsSubText = root.findViewById(R.id.noRecentTripsSubText);
        seeAllTrips = root.findViewById(R.id.seeAllTrips);
        swipeRefresh = root.findViewById(R.id.swipeRefresh);

        // Get data from singleton
        AppData data = AppData.getInstance();
        String name = "Traveler";
        UserProfile profile = data.getProfile();
        if (profile != null && profile.getName() != null && !profile.getName().trim().isEmpty()) {
            name = profile.getName();
        }
        // TRANSLATION FIX: Use string resource for proper translation
        welcomeText.setText(getString(R.string.dashboard_welcome_full, name));

        // Setup statistics
        setupStatistics(data);

        // Setup recent trips
        setupRecentTrips(data);

        // Setup chart
        setupChart(data.getExpenseData());

        // Setup "See All" button to navigate to Trips tab
        seeAllTrips.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Navigate to Trips tab by simulating nav button click
                android.widget.LinearLayout navTrips = getActivity().findViewById(R.id.nav_trips);
                if (navTrips != null) {
                    navTrips.performClick();
                }
            }
        });

        // Setup swipe to refresh
        swipeRefresh.setColorSchemeResources(
            R.color.primary_blue,
            R.color.accent_orange,
            R.color.accent_green
        );
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // TRANSLATION FIX: Don't translate number fields (totalTripsText, totalSpentText, avgPerTripText)
        // Those show numbers like "5" or "$1500", not text labels
        // Only translate the actual text labels for empty states
        String targetLang = LocaleHelper.getLanguage(requireContext()).equals("vi")
                ? TranslateLanguage.VIETNAMESE
                : TranslateLanguage.ENGLISH;

        // Only translate empty state messages and action buttons
        MachineTranslationHelper.translateAndSetText(noRecentTripsText, "No recent trips to show.", targetLang);
        MachineTranslationHelper.translateAndSetText(noRecentTripsSubText, "Start planning your next adventure!", targetLang);
        MachineTranslationHelper.translateAndSetText(seeAllTrips, "See All", targetLang);

        return root;
    }

    private void refreshData() {
        // Reload data from AppData
        AppData data = AppData.getInstance();

        // Update statistics
        setupStatistics(data);

        // Update chart
        setupChart(data.getExpenseData());

        // Update recent trips
        setupRecentTrips(data);

        // Stop the refresh animation
        swipeRefresh.setRefreshing(false);

        // Show a toast to confirm refresh
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "Dashboard refreshed", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void setupStatistics(AppData data) {
        // You'll need to add these methods to your AppData class
        int totalTrips = data.getTotalTrips(); // Add this method
        float totalSpent = calculateTotal(data.getExpenseData());
        float avgPerTrip = totalTrips > 0 ? totalSpent / totalTrips : 0;

        totalTripsText.setText(String.valueOf(totalTrips));
        totalSpentText.setText(String.format("$%.0f", totalSpent));
        avgPerTripText.setText(String.format("$%.0f", avgPerTrip));
    }

    private float calculateTotal(Map<String, Float> expenseData) {
        if (expenseData == null || expenseData.isEmpty()) return 0;
        float total = 0;
        for (Float value : expenseData.values()) {
            total += value;
        }
        return total;
    }

    private void setupChart(Map<String, Float> expenseData) {
        if (expenseData == null || expenseData.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data available");
            pieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : expenseData.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Enhanced color palette
        int[] colorIds = {
                R.color.teal_700, R.color.orange, R.color.blue,
                R.color.pink, R.color.purple, R.color.yellow
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for (int id : colorIds) {
            colors.add(ContextCompat.getColor(requireContext(), id));
        }
        dataSet.setColors(colors);

        // Enhanced text styling
        dataSet.setValueTextSize(13f);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Calculate total for center text
        float total = calculateTotal(expenseData);
        pieChart.setCenterText(String.format(java.util.Locale.US, "$%.0f\nTotal", total));
        pieChart.setCenterTextSize(20f);
        pieChart.setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light));

        // Enhanced chart appearance
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(50f);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setTransparentCircleColor(android.graphics.Color.WHITE);
        pieChart.setTransparentCircleAlpha(50);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f);

        // Rotation and interaction
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setRotationAngle(0);
        pieChart.setDrawCenterText(true);

        // Legend configuration - improved styling
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(13f);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary_light));
        legend.setFormSize(12f);
        legend.setFormLineWidth(1f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setXEntrySpace(15f);
        legend.setYEntrySpace(5f);
        legend.setWordWrapEnabled(true);

        // Animate the chart
        pieChart.animateY(1200, Easing.EaseInOutQuad);

        pieChart.invalidate();
    }

    private void startFloatingAnimation(View view, long duration, float distance) {
        view.animate()
            .translationY(-distance)
            .setDuration(duration / 2)
            .withEndAction(() -> {
                view.animate()
                    .translationY(0)
                    .setDuration(duration / 2)
                    .withEndAction(() -> startFloatingAnimation(view, duration, distance))
                    .start();
            })
            .start();

        // Add rotation animation
        view.animate()
            .rotation(360f)
            .setDuration(duration * 2)
            .withEndAction(() -> {
                view.setRotation(0f);
            })
            .start();
    }

    private void updateProfileAvatar(com.google.android.material.imageview.ShapeableImageView profileAvatar) {
        if (getContext() == null || !isAdded() || profileAvatar == null) return;

        UserProfile profile = AppData.getInstance().getProfile();
        if (profile != null && profile.getProfileImageUri() != null && !profile.getProfileImageUri().isEmpty()) {
            try {
                // FIX: Clear Glide cache and force reload to update profile picture immediately
                Glide.with(requireContext().getApplicationContext())
                    .load(android.net.Uri.parse(profile.getProfileImageUri()))
                    .circleCrop()
                    .placeholder(R.drawable.img)
                    .error(R.drawable.img)
                    .skipMemoryCache(true) // Skip memory cache to force reload
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Skip disk cache
                    .into(profileAvatar);
            } catch (Exception e) {
                profileAvatar.setImageResource(R.drawable.img);
            }
        } else {
            profileAvatar.setImageResource(R.drawable.img);
        }
    }

    private void setupRecentTrips(AppData data) {
        try {
            List<AppData.Trip> allTrips = data.getTrips();

            if (allTrips == null || allTrips.isEmpty()) {
                recentTripsRecycler.setVisibility(View.GONE);
                noRecentTripsContainer.setVisibility(View.VISIBLE);
                return;
            }

            recentTripsRecycler.setVisibility(View.VISIBLE);
            noRecentTripsContainer.setVisibility(View.GONE);

            // Get up to 3 most recent trips
            List<AppData.Trip> recentTrips = allTrips.subList(0, Math.min(3, allTrips.size()));

            recentTripsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            TripAdapter adapter = new TripAdapter(recentTrips, requireActivity());
            recentTripsRecycler.setAdapter(adapter);
        } catch (Exception e) {
            android.util.Log.e("DashboardFragment", "Error setting up recent trips", e);
            recentTripsRecycler.setVisibility(View.GONE);
            noRecentTripsContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume Glide requests when fragment becomes visible
        if (getContext() != null) {
            try {
                Glide.with(this).resumeRequests();
            } catch (Exception e) {
                android.util.Log.w("DashboardFragment", "Failed to resume Glide requests", e);
            }
        }

        // FIX: Refresh profile avatar when fragment resumes (in case it was updated in ProfileFragment)
        if (profileAvatarView != null) {
            updateProfileAvatar(profileAvatarView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause Glide requests when fragment is not visible to save resources
        if (getContext() != null) {
            try {
                Glide.with(this).pauseRequests();
            } catch (Exception e) {
                android.util.Log.w("DashboardFragment", "Failed to pause Glide requests", e);
            }
        }
    }

    private void registerProfileUpdateReceiver() {
        if (getContext() == null) return;

        profileUpdateReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, android.content.Intent intent) {
                // FIX: Refresh profile avatar when profile is updated
                android.util.Log.d("DashboardFragment", "Profile update broadcast received, refreshing avatar");
                if (profileAvatarView != null) {
                    updateProfileAvatar(profileAvatarView);
                } else {
                    android.util.Log.w("DashboardFragment", "profileAvatarView is null, cannot update");
                }
            }
        };

        android.content.IntentFilter filter = new android.content.IntentFilter("com.example.mytraveldiary.PROFILE_UPDATED");

        // Use compatibility registration for older Android versions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(profileUpdateReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
        } else {
            // For older versions, use ContextCompat which handles the flag automatically
            androidx.core.content.ContextCompat.registerReceiver(
                getContext(),
                profileUpdateReceiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // MEMORY LEAK FIX: Cancel all animations to prevent leaking Fragment
        if (floatingIcon1 != null) {
            floatingIcon1.animate().cancel();
            floatingIcon1.clearAnimation();
            floatingIcon1 = null;
        }
        if (floatingIcon2 != null) {
            floatingIcon2.animate().cancel();
            floatingIcon2.clearAnimation();
            floatingIcon2 = null;
        }

        // Unregister broadcast receiver
        if (profileUpdateReceiver != null && getContext() != null) {
            try {
                getContext().unregisterReceiver(profileUpdateReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver was already unregistered
            }
            profileUpdateReceiver = null;
        }

        // Clear RecyclerView adapter to prevent memory leaks
        if (recentTripsRecycler != null) {
            recentTripsRecycler.setAdapter(null);
        }

        // Clear all view references to prevent memory leaks
        recentTripsRecycler = null;
        noRecentTripsContainer = null;
        noRecentTripsText = null;
        noRecentTripsSubText = null;
        pieChart = null;
        totalTripsText = null;
        totalSpentText = null;
        avgPerTripText = null;
        seeAllTrips = null;
        profileAvatarView = null;
    }
}

