package com.example.mytraveldiary;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class TravelMapFragment extends Fragment {

    private MapView mapView;
    private AppData appData;

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

        setupMap();
        addTripMarkers();

        return root;
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set default view to world view
        mapView.getController().setZoom(2.0);
        mapView.getController().setCenter(new GeoPoint(20.0, 0.0));
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
                marker.setSnippet(trip.getDateRange());

                // Custom marker icon (flag)
                Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_marker);
                if (icon != null) {
                    marker.setIcon(icon);
                }

                // Click listener to show trip details
                marker.setOnMarkerClickListener((clickedMarker, map) -> {
                    clickedMarker.showInfoWindow();
                    Toast.makeText(requireContext(),
                        "Visited: " + trip.getDestination(),
                        Toast.LENGTH_SHORT).show();
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
    }
}
