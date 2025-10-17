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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TripsFragment extends Fragment {

    private ActivityResultLauncher<String> galleryLauncher;
    private String selectedImageUri = null;

    private RecyclerView recyclerView;
    private TextView emptyState;
    private TripAdapter adapter;
    private AppData appData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri.toString();
                        Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trips, container, false);

        recyclerView = root.findViewById(R.id.tripsRecycler);
        emptyState = root.findViewById(R.id.emptyStateText);
        FloatingActionButton fab = root.findViewById(R.id.fabAddTrip);

        appData = AppData.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        List<AppData.Trip> tripList = appData.getTrips();
        adapter = new TripAdapter(tripList, requireActivity());
        recyclerView.setAdapter(adapter);

        refreshUI();

        fab.setOnClickListener(v -> {
            AddTripDialog dialog = new AddTripDialog(getActivity(), () -> {
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                refreshUI();
            });

            dialog.setOnChooseImageClicked(() -> galleryLauncher.launch("image/*"));
            dialog.show();
        });

        return root;
    }

    private void refreshUI() {
        List<AppData.Trip> trips = appData.getTrips();
        boolean hasTrips = !trips.isEmpty();

        emptyState.setVisibility(hasTrips ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasTrips ? View.VISIBLE : View.GONE);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}