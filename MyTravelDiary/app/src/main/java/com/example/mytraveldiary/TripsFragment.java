package com.example.mytraveldiary;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TripsFragment extends Fragment {

    private ActivityResultLauncher<String> galleryLauncher;
    private String selectedImageUri = null;
    private AddTripDialog currentDialog = null;

    private RecyclerView recyclerView;
    private TextView emptyState;
    private TripAdapter adapter;
    private AppData appData;
    private ImageButton btnToggleView;
    private boolean isGridView = true; // Default to grid view

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri.toString();
                        Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                        if (currentDialog != null) {
                            currentDialog.setSelectedImageUri(selectedImageUri);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trips, container, false);

        recyclerView = root.findViewById(R.id.tripsRecycler);
        emptyState = root.findViewById(R.id.emptyStateText);
        btnToggleView = root.findViewById(R.id.btnToggleView);
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fab = root.findViewById(R.id.fab_add_trip);

        appData = AppData.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),2));
        List<AppData.Trip> tripList = appData.getTrips();
        adapter = new TripAdapter(tripList, requireActivity());
        recyclerView.setAdapter(adapter);

        refreshUI();

        // Toggle view mode button
        btnToggleView.setOnClickListener(v -> {
            isGridView = !isGridView;
            switchViewMode();
        });

        fab.setOnClickListener(v -> {
            selectedImageUri = null;
            currentDialog = new AddTripDialog(getActivity(), () -> {
                // Recreate adapter with updated trip list
                List<AppData.Trip> updatedTrips = appData.getTrips();
                adapter = new TripAdapter(updatedTrips, requireActivity());
                recyclerView.setAdapter(adapter);
                recyclerView.scrollToPosition(0);
                refreshUI();
                currentDialog = null;
            });

            currentDialog.setOnChooseImageClicked(() -> galleryLauncher.launch("image/*"));
            currentDialog.show();
        });

        return root;
    }

    private void refreshUI() {
        List<AppData.Trip> trips = appData.getTrips();
        boolean hasTrips = !trips.isEmpty();

        emptyState.setVisibility(hasTrips ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasTrips ? View.VISIBLE : View.GONE);

        // Recreate adapter with fresh trip list
        if (hasTrips) {
            adapter = new TripAdapter(trips, requireActivity());
            recyclerView.setAdapter(adapter);
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
        // Recreate adapter after layout change
        List<AppData.Trip> trips = appData.getTrips();
        adapter = new TripAdapter(trips, requireActivity());
        recyclerView.setAdapter(adapter);
    }
}
