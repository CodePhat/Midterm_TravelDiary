package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Google Maps integration fragment
 * Demonstrates: Google Maps, Location Services, Custom Map Markers
 */
public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private AppData appData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_google_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appData = AppData.getInstance();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
            .findFragmentById(R.id.google_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Customize map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add trip markers
        addTripMarkers();

        // Move camera to first trip or default location
        if (appData.getTrips().size() > 0) {
            AppData.Trip firstTrip = appData.getTrips().get(0);
            LatLng location = new LatLng(firstTrip.getLatitude(), firstTrip.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        } else {
            // Default to world view
            LatLng defaultLocation = new LatLng(0, 0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 2));
        }
    }

    private void addTripMarkers() {
        if (googleMap == null) return;

        for (AppData.Trip trip : appData.getTrips()) {
            if (trip.getLatitude() != 0 && trip.getLongitude() != 0) {
                LatLng location = new LatLng(trip.getLatitude(), trip.getLongitude());
                googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(trip.getDestination())
                    .snippet(trip.getStartDate() + " - " + trip.getEndDate()));
            }
        }
    }
}
