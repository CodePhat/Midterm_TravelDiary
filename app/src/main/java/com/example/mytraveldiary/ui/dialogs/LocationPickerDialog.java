package com.example.mytraveldiary.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.utils.helpers.GeocodingHelper;

public class LocationPickerDialog extends Dialog {

    public interface LocationPickerListener {
        void onLocationPicked(double latitude, double longitude, String locationName);
    }

    private MapView mapView;
    private Marker selectedMarker;
    private TextView locationText;
    private final LocationPickerListener listener;
    private GeoPoint selectedPoint;
    private String selectedLocationName = "";

    public LocationPickerDialog(@NonNull Context context, LocationPickerListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure osmdroid
        Configuration.getInstance().load(
            getContext(),
            PreferenceManager.getDefaultSharedPreferences(getContext())
        );

        setContentView(R.layout.dialog_location_picker);

        // Make dialog fullscreen
        if (getWindow() != null) {
            getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        mapView = findViewById(R.id.mapViewPicker);
        locationText = findViewById(R.id.selectedLocationText);
        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        Button btnCancel = findViewById(R.id.btnCancelLocation);

        setupMap();

        btnConfirm.setOnClickListener(v -> {
            if (selectedPoint != null && listener != null) {
                listener.onLocationPicked(
                    selectedPoint.getLatitude(),
                    selectedPoint.getLongitude(),
                    selectedLocationName
                );
                dismiss();
            } else {
                Toast.makeText(getContext(), "Please select a location on the map", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set default view to world
        mapView.getController().setZoom(3.0);
        mapView.getController().setCenter(new GeoPoint(20.0, 0.0));

        // Add tap listener
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint point) {
                onMapTapped(point);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint point) {
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(0, mapEventsOverlay);
    }

    private void onMapTapped(GeoPoint point) {
        selectedPoint = point;

        // Remove old marker if exists
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }

        // Add new marker
        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(point);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setTitle("Selected Location");
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();

        // Show coordinates while reverse geocoding
        locationText.setText(String.format("Lat: %.4f, Lon: %.4f\nSearching location...",
            point.getLatitude(), point.getLongitude()));

        // Reverse geocode to get location name
        GeocodingHelper.reverseGeocode(
            point.getLatitude(),
            point.getLongitude(),
            new GeocodingHelper.ReverseGeocodingCallback() {
                @Override
                public void onLocationFound(String displayName) {
                    selectedLocationName = displayName;
                    locationText.setText(displayName);
                }

                @Override
                public void onError(String error) {
                    selectedLocationName = String.format("Location (%.4f, %.4f)",
                        point.getLatitude(), point.getLongitude());
                    locationText.setText(selectedLocationName);
                }
            }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onResume();
        }
    }
}
