package com.example.mytraveldiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TripsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyState;
    private TripAdapter adapter;
    private AppData appData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trips, container, false);

        recyclerView = root.findViewById(R.id.tripsRecycler);
        emptyState = root.findViewById(R.id.emptyStateText);
        FloatingActionButton fab = root.findViewById(R.id.fabAddTrip);

        appData = AppData.getInstance();

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new TripAdapter(appData.getTrips(), this);
        recyclerView.setAdapter(adapter);

        refreshUI();

        fab.setOnClickListener(v -> {
            AddTripDialog dialog = new AddTripDialog(requireContext(), () -> {
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                refreshUI();
            });
            dialog.show();
        });

        return root;
    }

    private void refreshUI() {
        List<AppData.Trip> trips = appData.getTrips();
        boolean hasTrips = !trips.isEmpty();

        emptyState.setVisibility(hasTrips ? View.GONE : View.VISIBLE);
        recyclerView.setVisibility(hasTrips ? View.VISIBLE : View.GONE);
    }
}