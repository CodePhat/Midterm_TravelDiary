package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.models.Destination;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.imageview.ShapeableImageView;

public class DiscoverMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_DESTINATION = "destination";

    private Destination destination;
    private GoogleMap googleMap;
    private ImageButton backButton;
    private TextView bottomSheetName;
    private TextView bottomSheetLocation;
    private ShapeableImageView bottomSheetImage;

    public static DiscoverMapFragment newInstance(Destination destination) {
        DiscoverMapFragment fragment = new DiscoverMapFragment();
        Bundle args = new Bundle();
        args.putString("name", destination.getName());
        args.putString("location", destination.getLocation());
        args.putDouble("latitude", destination.getLatitude());
        args.putDouble("longitude", destination.getLongitude());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            destination = new Destination();
            destination.setName(getArguments().getString("name", "Pianemo Island"));
            destination.setLocation(getArguments().getString("location", "Raja Ampat, Indonesia"));
            destination.setLatitude(getArguments().getDouble("latitude", -0.6));
            destination.setLongitude(getArguments().getDouble("longitude", 130.6));
            destination.setDescription("Pianemo island in West Papua is considered a small archip...");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupMap();
        setupListeners();
    }

    private void initializeViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        bottomSheetName = view.findViewById(R.id.bottomSheetName);
        bottomSheetLocation = view.findViewById(R.id.bottomSheetLocation);
        bottomSheetImage = view.findViewById(R.id.bottomSheetImage);

        if (destination != null) {
            bottomSheetName.setText(destination.getName());
            bottomSheetLocation.setText(destination.getDescription());
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        if (destination != null) {
            LatLng location = new LatLng(destination.getLatitude(), destination.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(destination.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
        } else {
            LatLng defaultLocation = new LatLng(-0.6, 130.6);
            googleMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .title("Pianemo Island"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }
}
