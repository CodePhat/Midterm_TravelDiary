package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.models.Destination;
import com.example.mytraveldiary.ui.adapters.DestinationAdapter;
import com.example.mytraveldiary.ui.adapters.NearbyDestinationAdapter;

import java.util.ArrayList;
import java.util.List;

public class DiscoverDestinationsFragment extends Fragment {

    private RecyclerView popularDestinationsRecycler;
    private RecyclerView nearbyDestinationsRecycler;
    private DestinationAdapter popularAdapter;
    private NearbyDestinationAdapter nearbyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover_destinations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerViews();
        loadSampleData();
    }

    private void initializeViews(View view) {
        popularDestinationsRecycler = view.findViewById(R.id.popularDestinationsRecycler);
        nearbyDestinationsRecycler = view.findViewById(R.id.nearbyDestinationsRecycler);
    }

    private void setupRecyclerViews() {
        popularAdapter = new DestinationAdapter(new DestinationAdapter.OnDestinationClickListener() {
            @Override
            public void onDestinationClick(Destination destination) {
                openDestinationDetail(destination);
            }

            @Override
            public void onFavoriteClick(Destination destination) {
                toggleFavorite(destination);
            }
        });

        nearbyAdapter = new NearbyDestinationAdapter(destination -> openDestinationDetail(destination));

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        popularDestinationsRecycler.setLayoutManager(gridLayoutManager);
        popularDestinationsRecycler.setAdapter(popularAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
        nearbyDestinationsRecycler.setLayoutManager(linearLayoutManager);
        nearbyDestinationsRecycler.setAdapter(nearbyAdapter);
    }

    private void loadSampleData() {
        List<Destination> popularDestinations = new ArrayList<>();
        popularDestinations.add(new Destination("1", "Painemo Island", "Raja Ampat, Indonesia",
                "", 45.0, 4.8, 1200));
        popularDestinations.add(new Destination("2", "Kelingking Beach", "Bali, Indonesia",
                "", 47.0, 4.5, 850));
        popularDestinations.add(new Destination("3", "Mount Bromo", "East Java, Indonesia",
                "", 35.0, 4.7, 920));
        popularDestinations.add(new Destination("4", "Gili Islands", "Lombok, Indonesia",
                "", 52.0, 4.6, 780));

        List<Destination> nearbyDestinations = new ArrayList<>();
        Destination nearby1 = new Destination("5", "Botok Beach", "Sragen, Indonesia",
                "", 25.0, 4.3, 150);
        nearby1.setDistance(3.5);
        nearbyDestinations.add(nearby1);

        Destination nearby2 = new Destination("6", "Parangtritis Beach", "Yogyakarta, Indonesia",
                "", 20.0, 4.4, 320);
        nearby2.setDistance(5.2);
        nearbyDestinations.add(nearby2);

        Destination nearby3 = new Destination("7", "Prambanan Temple", "Yogyakarta, Indonesia",
                "", 15.0, 4.7, 580);
        nearby3.setDistance(8.1);
        nearbyDestinations.add(nearby3);

        popularAdapter.setDestinations(popularDestinations);
        nearbyAdapter.setDestinations(nearbyDestinations);
    }

    private void openDestinationDetail(Destination destination) {
        DestinationDetailFragment detailFragment = DestinationDetailFragment.newInstance(destination);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void toggleFavorite(Destination destination) {
        destination.setFavorite(!destination.isFavorite());
        String message = destination.isFavorite() ?
                destination.getName() + " added to favorites" :
                destination.getName() + " removed from favorites";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static DiscoverDestinationsFragment newInstance() {
        return new DiscoverDestinationsFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear all view references to prevent memory leaks
        popularDestinationsRecycler = null;
        nearbyDestinationsRecycler = null;
        popularAdapter = null;
        nearbyAdapter = null;
    }
}
