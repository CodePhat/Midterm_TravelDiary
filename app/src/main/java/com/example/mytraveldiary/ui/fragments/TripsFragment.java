package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.widget.ImageButton;


import java.util.List;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.ui.adapters.TripAdapter;
import com.example.mytraveldiary.ui.adapters.TripBannerAdapter;
import com.example.mytraveldiary.ui.dialogs.AddTripDialog;
import com.example.mytraveldiary.utils.LocaleHelper;
import com.example.mytraveldiary.utils.helpers.MachineTranslationHelper;
import com.google.mlkit.nl.translate.TranslateLanguage;

public class TripsFragment extends Fragment {

    private ActivityResultLauncher<String> galleryLauncher;
    private String selectedImageUri = null;
    private AddTripDialog currentDialog = null;
    private AppData.Trip editingTrip = null; // Track trip being edited for photo change

    private RecyclerView recyclerView;
    private TextView emptyState;
    private TripAdapter adapter;
    private AppData appData;
    private ImageButton btnToggleView;
    private boolean isGridView = true; // Default to grid view
    private SwipeRefreshLayout swipeRefresh;

    // Banner auto-slide
    private ViewPager2 bannerViewPager;
    private TripBannerAdapter bannerAdapter;
    private LinearLayout bannerIndicator;
    private Handler autoSlideHandler;
    private Runnable autoSlideRunnable;
    private static final long AUTO_SLIDE_DELAY = 4000; // 4 seconds

    // ANR FIX: Track if heavy initialization is done to avoid reloading on every tab switch
    private boolean isInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // Take persistent URI permission so we can access the image later
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            selectedImageUri = uri.toString();

                            // Check if we're editing an existing trip's photo
                            if (editingTrip != null) {
                                editingTrip.setImage(selectedImageUri);
                                appData.triggerSave();
                                adapter.notifyDataSetChanged();
                                updateBanner();
                                Toast.makeText(requireContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                                editingTrip = null; // Clear editing state
                            } else {
                                // Setting photo for new trip in dialog
                                Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                                if (currentDialog != null) {
                                    currentDialog.setSelectedImageUri(selectedImageUri);
                                }
                            }
                        } catch (Exception e) {
                            // If taking persistent permission fails, still use the URI
                            selectedImageUri = uri.toString();

                            if (editingTrip != null) {
                                editingTrip.setImage(selectedImageUri);
                                appData.triggerSave();
                                adapter.notifyDataSetChanged();
                                updateBanner();
                                Toast.makeText(requireContext(), "Photo updated!", Toast.LENGTH_SHORT).show();
                                editingTrip = null;
                            } else {
                                Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                                if (currentDialog != null) {
                                    currentDialog.setSelectedImageUri(selectedImageUri);
                                }
                            }
                            android.util.Log.w("TripsFragment", "Could not take persistent URI permission", e);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trips, container, false);

        // Initialize views
        recyclerView = root.findViewById(R.id.tripsRecycler);
        emptyState = root.findViewById(R.id.emptyStateText);
        btnToggleView = root.findViewById(R.id.btnToggleView);
        bannerViewPager = root.findViewById(R.id.bannerViewPager);
        bannerIndicator = root.findViewById(R.id.bannerIndicator);
        swipeRefresh = root.findViewById(R.id.swipeRefresh);
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fab = root.findViewById(R.id.fab_add_trip);

        appData = AppData.getInstance();

        // Setup swipe to refresh
        swipeRefresh.setColorSchemeResources(
            R.color.primary_blue,
            R.color.accent_orange,
            R.color.accent_green
        );
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // Setup banner
        setupBanner();

        // Setup trips recycler view
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),2));
        List<AppData.Trip> tripList = appData.getTrips();
        adapter = new TripAdapter(tripList, requireActivity());

        // Set up photo edit listener
        adapter.setOnPhotoEditListener((trip, position) -> {
            editingTrip = trip;
            galleryLauncher.launch("image/*");
        });

        recyclerView.setAdapter(adapter);

        refreshUI();

        // Set initial icon based on default view mode
        updateToggleIcon();

        // Toggle view mode button
        btnToggleView.setOnClickListener(v -> {
            isGridView = !isGridView;
            switchViewMode();
            updateToggleIcon();
        });

        fab.setOnClickListener(v -> {
            selectedImageUri = null;
            editingTrip = null; // Clear editing state
            currentDialog = new AddTripDialog(getActivity(), () -> {
                // Recreate adapter with updated trip list
                List<AppData.Trip> updatedTrips = appData.getTrips();
                adapter = new TripAdapter(updatedTrips, requireActivity());
                adapter.setOnPhotoEditListener((trip, position) -> {
                    editingTrip = trip;
                    galleryLauncher.launch("image/*");
                });
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(0);
                refreshUI();
                updateBanner(); // Update banner when new trip is added
                currentDialog = null;
            });

            currentDialog.setOnChooseImageClicked(() -> galleryLauncher.launch("image/*"));
            currentDialog.show();
        });

        return root;
    }

    private void setupBanner() {
        // Initialize banner adapter
        bannerAdapter = new TripBannerAdapter();
        bannerViewPager.setAdapter(bannerAdapter);

        // Get trips with images for banner
        updateBanner();

        // Setup auto-slide
        autoSlideHandler = new Handler(Looper.getMainLooper());
        autoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerViewPager != null && bannerAdapter.getItemCount() > 0) {
                    int currentItem = bannerViewPager.getCurrentItem();
                    int nextItem = (currentItem + 1) % bannerAdapter.getItemCount();
                    bannerViewPager.setCurrentItem(nextItem, true);
                    autoSlideHandler.postDelayed(this, AUTO_SLIDE_DELAY);
                }
            }
        };
    }

    private void updateBanner() {
        List<AppData.Trip> allTrips = appData.getTrips();
        // Filter trips that have images
        List<AppData.Trip> tripsWithImages = new java.util.ArrayList<>();
        for (AppData.Trip trip : allTrips) {
            if (trip.getImage() != null && !trip.getImage().isEmpty()) {
                tripsWithImages.add(trip);
            }
        }

        if (!tripsWithImages.isEmpty()) {
            bannerAdapter.setTrips(tripsWithImages);
            bannerViewPager.setVisibility(View.VISIBLE);
            startAutoSlide();
        } else {
            // Show gradient background if no trips with images
            bannerViewPager.setVisibility(View.VISIBLE);
            stopAutoSlide();
        }
    }

    private void startAutoSlide() {
        if (autoSlideHandler != null && autoSlideRunnable != null) {
            stopAutoSlide(); // Stop any existing auto-slide
            autoSlideHandler.postDelayed(autoSlideRunnable, AUTO_SLIDE_DELAY);
        }
    }

    private void stopAutoSlide() {
        if (autoSlideHandler != null && autoSlideRunnable != null) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
        }
    }

    private void refreshUI() {
        List<AppData.Trip> trips = appData.getTrips();
        boolean hasTrips = !trips.isEmpty();

        emptyState.setVisibility(hasTrips ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasTrips ? View.VISIBLE : View.GONE);

        //Recreate adapter with fresh trip list
        if (hasTrips) {
            adapter = new TripAdapter(trips, requireActivity());
            recyclerView.setAdapter(adapter);
        }

        // Use machine translation for dynamic text
        String targetLang = LocaleHelper.getLanguage(requireContext()).equals("vi")
                ? TranslateLanguage.VIETNAMESE
                : TranslateLanguage.ENGLISH;

        MachineTranslationHelper.translateAndSetText(emptyState, "No trips yet. Add one to get started!", targetLang);
    }

    private void refreshData() {
        // Reload data from AppData
        List<AppData.Trip> trips = appData.getTrips();

        // Update adapter with fresh data
        adapter = new TripAdapter(trips, requireActivity());
        recyclerView.setAdapter(adapter);

        // Update UI state
        refreshUI();

        // Update banner
        updateBanner();

        // Stop the refresh animation
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        // Show a toast to confirm refresh
        if (getContext() != null) {
            Toast.makeText(getContext(), "Trips refreshed", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchViewMode() {
        if (isGridView) {
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            Toast.makeText(requireContext(), "Grid view", Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            Toast.makeText(requireContext(), "List view", Toast.LENGTH_SHORT).show();
        }
        //Recreate adapter after layout change
        List<AppData.Trip> trips = appData.getTrips();
        adapter = new TripAdapter(trips, requireActivity());
        recyclerView.setAdapter(adapter);
    }

    private void updateToggleIcon() {
        if (btnToggleView != null) {
            // If currently in grid view, show list icon (to switch to list)
            // If currently in list view, show grid icon (to switch to grid)
            if (isGridView) {
                btnToggleView.setImageResource(R.drawable.ic_list_view);
            } else {
                btnToggleView.setImageResource(R.drawable.ic_grid_view);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // ANR FIX: Only do heavy initialization once
        // When switching tabs, MainActivity uses show/hide which calls onResume repeatedly
        // This prevents reloading all resources every time user switches tabs
        if (!isInitialized) {
            isInitialized = true;

            // Delay Glide resume to let UI settle after language change
            if (getContext() != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getContext() != null && isAdded()) {
                        try {
                            com.bumptech.glide.Glide.with(this).resumeRequests();
                        } catch (Exception e) {
                            android.util.Log.w("TripsFragment", "Failed to resume Glide requests", e);
                        }
                    }
                }, 100); // Small delay to let UI render first
            }
        }

        // Always resume banner auto-slide when fragment is visible
        startAutoSlide();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause Glide requests when fragment is not visible
        if (getContext() != null) {
            try {
                com.bumptech.glide.Glide.with(this).pauseRequests();
            } catch (Exception e) {
                android.util.Log.w("TripsFragment", "Failed to pause Glide requests", e);
            }
        }
        // Pause banner auto-slide
        stopAutoSlide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // ANR FIX: Reset initialization flag
        isInitialized = false;

        // Stop auto-slide and clear handler
        stopAutoSlide();
        autoSlideHandler = null;
        autoSlideRunnable = null;

        // Clear RecyclerView adapter to prevent memory leaks
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }

        // Clear banner
        if (bannerViewPager != null) {
            bannerViewPager.setAdapter(null);
        }

        // Clear all view references to prevent memory leaks
        recyclerView = null;
        emptyState = null;
        adapter = null;
        appData = null;
        btnToggleView = null;
        currentDialog = null;
        selectedImageUri = null;
        bannerViewPager = null;
        bannerAdapter = null;
        bannerIndicator = null;
        swipeRefresh = null;
    }

    // QoL Feature: Public method to show add trip dialog (called from Quick Action FAB)
    public void showAddTripDialog() {
        if (getActivity() != null && isAdded()) {
            selectedImageUri = null;
            editingTrip = null; // Clear editing state
            currentDialog = new AddTripDialog(getActivity(), () -> {
                // Recreate adapter with updated trip list
                List<AppData.Trip> updatedTrips = appData.getTrips();
                adapter = new TripAdapter(updatedTrips, requireActivity());
                adapter.setOnPhotoEditListener((trip, position) -> {
                    editingTrip = trip;
                    galleryLauncher.launch("image/*");
                });
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(0);
                refreshUI();
                updateBanner(); // Update banner when new trip is added
                currentDialog = null;
            });

            currentDialog.setOnChooseImageClicked(() -> galleryLauncher.launch("image/*"));
            currentDialog.show();
        }
    }
}
