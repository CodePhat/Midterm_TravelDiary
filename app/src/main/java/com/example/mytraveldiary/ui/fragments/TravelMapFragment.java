package com.example.mytraveldiary.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.utils.helpers.GeocodingHelper;

public class TravelMapFragment extends Fragment {

    private MapView mapView;
    private AppData appData;
    private SeekBar zoomSeekBar;
    private AutoCompleteTextView searchLocationInput;
    private ImageButton clearSearchBtn;
    private ArrayAdapter<GeocodingHelper.LocationSuggestion> searchAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isUpdatingZoom = false;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Configure osmdroid
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        );

        View root = inflater.inflate(R.layout.fragment_travel_map, container, false);

        appData = AppData.getInstance();
        mapView = root.findViewById(R.id.mapView);
        zoomSeekBar = root.findViewById(R.id.zoomSeekBar);
        searchLocationInput = root.findViewById(R.id.searchLocationInput);
        clearSearchBtn = root.findViewById(R.id.clearSearchBtn);

        setupMap();
        setupZoomControl();
        setupSearchBar();
        addTripMarkers();

        return root;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(false); // Disable default zoom controls
        mapView.setMultiTouchControls(true);

        // Set default view to world view
        mapView.getController().setZoom(2.0);
        mapView.getController().setCenter(new GeoPoint(20.0, 0.0));
    }

    private void setupZoomControl() {
        // Set initial zoom level to match map
        zoomSeekBar.setProgress((int) mapView.getZoomLevelDouble());

        // Update map zoom when slider changes
        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    isUpdatingZoom = true;
                    mapView.getController().setZoom((double) progress);
                    isUpdatingZoom = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update slider when map zoom changes (e.g., pinch to zoom)
        mapView.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                if (!isUpdatingZoom) {
                    int currentZoom = (int) mapView.getZoomLevelDouble();
                    if (zoomSeekBar.getProgress() != currentZoom) {
                        zoomSeekBar.setProgress(currentZoom);
                    }
                }
                return false;
            }
        });
    }

    private void setupSearchBar() {
        // Initialize adapter for autocomplete with LocationSuggestion objects
        searchAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        searchLocationInput.setAdapter(searchAdapter);
        searchLocationInput.setThreshold(2);

        // Search location as user types with debouncing
        searchLocationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Always cancel previous pending search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                String query = s != null ? s.toString().trim() : "";

                if (query.length() >= 2) {
                    clearSearchBtn.setVisibility(View.VISIBLE);
                    // Schedule new search quickly (faster suggestions)
                    searchRunnable = () -> performLocationSearch(query);
                    // Use 250ms debounce so it feels responsive
                    searchHandler.postDelayed(searchRunnable, 250);
                } else {
                    clearSearchBtn.setVisibility(View.GONE);
                    searchAdapter.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle location selection from suggestions - navigate immediately
        searchLocationInput.setOnItemClickListener((parent, view, position, id) -> {
            GeocodingHelper.LocationSuggestion selected = searchAdapter.getItem(position);
            if (selected != null) {
                navigateToLocation(selected.latitude, selected.longitude, selected.displayName);
            }
        });

        // Handle IME action (Search/Enter button)
        searchLocationInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchLocationInput.getText().toString().trim();
            if (!query.isEmpty() && query.length() >= 2) {
                // Search for what user typed
                searchAndNavigateToLocation(query);
                return true;
            }
            return false;
        });

        // Clear search button
        clearSearchBtn.setOnClickListener(v -> {
            searchLocationInput.setText("");
            searchAdapter.clear();
            searchAdapter.notifyDataSetChanged();
            clearSearchBtn.setVisibility(View.GONE);
        });

        // Hint is already set in layout XML using @string/map_search_hint
        // No need to set text programmatically - it will use the hint resource
    }

    private void performLocationSearch(String query) {
        GeocodingHelper.getLocationSuggestions(query, new GeocodingHelper.AutocompleteCallback() {
            @Override
            public void onSuggestionsFound(List<GeocodingHelper.LocationSuggestion> suggestions) {
                searchAdapter.clear();
                searchAdapter.addAll(suggestions);
                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Silently fail - autocomplete is optional
            }
        });
    }

    private void navigateToLocation(double latitude, double longitude, String displayName) {
        GeoPoint point = new GeoPoint(latitude, longitude);
        mapView.getController().setCenter(point);
        mapView.getController().setZoom(12.0);

        // Add temporary marker for searched location
        Marker searchMarker = new Marker(mapView);
        searchMarker.setPosition(point);
        searchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        searchMarker.setTitle("Search Result");
        searchMarker.setSnippet(displayName);

        Drawable icon = ContextCompat.getDrawable(requireContext(),
            android.R.drawable.ic_menu_mylocation);
        if (icon != null) {
            searchMarker.setIcon(icon);
        }

        mapView.getOverlays().add(searchMarker);
        searchMarker.showInfoWindow();
        mapView.invalidate();

        Toast.makeText(requireContext(),
            "Location: " + displayName,
            Toast.LENGTH_SHORT).show();
    }

    private void searchAndNavigateToLocation(String locationName) {
        GeocodingHelper.geocodeLocation(locationName, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onLocationFound(double latitude, double longitude, String displayName) {
                navigateToLocation(latitude, longitude, displayName);
            }

            @Override
            public void onLocationNotFound() {
                Toast.makeText(requireContext(),
                    "Location not found",
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(),
                    "Error finding location: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTripMarkers() {
        List<AppData.Trip> trips = appData.getTrips();

        if (trips.isEmpty()) {
            Toast.makeText(requireContext(), "No trips with locations yet", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasLocation = false;
        GeoPoint firstLocation = null;
        GeoPoint centerLocation = null;

        // Get trip ID to center on if provided
        String centerTripId = null;
        if (getArguments() != null) {
            centerTripId = getArguments().getString("centerTripId");
        }

        for (AppData.Trip trip : trips) {
            if (trip.hasLocation()) {
                hasLocation = true;
                GeoPoint point = new GeoPoint(trip.getLatitude(), trip.getLongitude());

                if (firstLocation == null) {
                    firstLocation = point;
                }

                // Check if this is the trip to center on
                if (centerTripId != null && trip.getId().equals(centerTripId)) {
                    centerLocation = point;
                }

                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(trip.getDestination());

                // Create detailed snippet with trip information
                StringBuilder snippetBuilder = new StringBuilder();
                snippetBuilder.append("📅 ").append(trip.getDateRange()).append("\n");

                // Add expense info if available
                if (trip.getExpenses() != null && !trip.getExpenses().isEmpty()) {
                    Map<String, Double> expenseSummary = trip.getExpenseSummary();
                    double totalExpense = 0;
                    for (Double amount : expenseSummary.values()) {
                        totalExpense += amount;
                    }
                    snippetBuilder.append("💰 Total Expenses: $").append(String.format("%.2f", totalExpense)).append("\n");
                }

                // Add activities count
                if (trip.getItineraryDays() != null) {
                    int totalActivities = 0;
                    for (AppData.ItineraryDay day : trip.getItineraryDays()) {
                        totalActivities += day.getActivities().size();
                    }
                    if (totalActivities > 0) {
                        snippetBuilder.append("📋 Activities: ").append(totalActivities).append("\n");
                    }
                }

                // Add photos count
                if (trip.getPhotos() != null && !trip.getPhotos().isEmpty()) {
                    int photosCount = trip.getPhotos().size();
                    snippetBuilder.append("📷 Photos: ").append(photosCount);
                }

                marker.setSnippet(snippetBuilder.toString());

                // Custom marker icon (flag)
                Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_marker);
                if (icon != null) {
                    marker.setIcon(icon);
                }

                // Enhanced click listener to show detailed trip info
                marker.setOnMarkerClickListener((clickedMarker, map) -> {
                    // Close all other info windows
                    InfoWindow.closeAllInfoWindowsOn(map);

                    // Show this marker's info window
                    clickedMarker.showInfoWindow();

                    // Center map on clicked marker with slight offset
                    map.getController().animateTo(clickedMarker.getPosition());

                    // Show toast with location details
                    String locationInfo = "📍 " + trip.getDestination() + "\n" +
                                         "📅 " + trip.getDateRange();
                    Toast.makeText(requireContext(), locationInfo, Toast.LENGTH_SHORT).show();

                    return true;
                });

                mapView.getOverlays().add(marker);

                // Open info window for centered trip
                if (centerLocation != null && point.equals(centerLocation)) {
                    marker.showInfoWindow();
                }
            }
        }

        if (hasLocation) {
            // Zoom to centered location or first location
            GeoPoint targetLocation = centerLocation != null ? centerLocation : firstLocation;
            if (targetLocation != null) {
                mapView.getController().setZoom(8.0);
                mapView.getController().setCenter(targetLocation);
            }
        } else {
            Toast.makeText(requireContext(), "Add locations to your trips to see them on the map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
