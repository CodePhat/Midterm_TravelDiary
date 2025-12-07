package com.example.mytraveldiary.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.models.Destination;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Locale;

public class DestinationDetailFragment extends Fragment {

    private static final String ARG_DESTINATION = "destination";

    private Destination destination;
    private ImageView destinationImage;
    private TextView destinationName;
    private TextView location;
    private TextView price;
    private TextView rating;
    private TextView description;
    private ImageButton backButton;
    private ImageButton favoriteButton;
    private MaterialButton bookNowButton;
    private TabLayout tabLayout;

    public static DestinationDetailFragment newInstance(Destination destination) {
        DestinationDetailFragment fragment = new DestinationDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DESTINATION, (java.io.Serializable) destination);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            createDestinationFromBundle();
        }
    }

    private void createDestinationFromBundle() {
        Bundle args = getArguments();
        if (args != null) {
            destination = new Destination();
            destination.setName(args.getString("name", "Kelingking Beach"));
            destination.setLocation(args.getString("location", "Bali, Indonesia"));
            destination.setPrice(args.getDouble("price", 47.0));
            destination.setRating(args.getDouble("rating", 4.5));
            destination.setReviewCount(args.getInt("reviewCount", 850));
            destination.setDescription("Kelingking Beach is one of the most famous spot of Nusa Penida. Major travel sites use its photo to advertise Bali and even sometimes Indonesia.");
        } else {
            destination = new Destination("1", "Kelingking Beach", "Bali, Indonesia", "",
                    47.0, 4.5, 850);
            destination.setDescription("Kelingking Beach is one of the most famous spot of Nusa Penida.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_destination_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupListeners();
        displayDestinationInfo();
    }

    private void initializeViews(View view) {
        destinationImage = view.findViewById(R.id.destinationImage);
        destinationName = view.findViewById(R.id.destinationName);
        location = view.findViewById(R.id.location);
        price = view.findViewById(R.id.price);
        rating = view.findViewById(R.id.rating);
        description = view.findViewById(R.id.description);
        backButton = view.findViewById(R.id.backButton);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        bookNowButton = view.findViewById(R.id.bookNowButton);
        tabLayout = view.findViewById(R.id.tabLayout);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        favoriteButton.setOnClickListener(v -> {
            destination.setFavorite(!destination.isFavorite());
            updateFavoriteButton();
            String message = destination.isFavorite() ?
                    "Added to favorites" : "Removed from favorites";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        bookNowButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Booking " + destination.getName() + "...",
                    Toast.LENGTH_SHORT).show();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                handleTabSelection(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void displayDestinationInfo() {
        if (destination != null) {
            destinationName.setText(destination.getName());
            location.setText(destination.getLocation());
            price.setText(String.format(Locale.getDefault(), "$%.0f", destination.getPrice()));
            rating.setText(String.format(Locale.getDefault(), "%.1f", destination.getRating()));

            if (destination.getDescription() != null && !destination.getDescription().isEmpty()) {
                description.setText(destination.getDescription());
            }

            updateFavoriteButton();
        }
    }

    private void updateFavoriteButton() {
        if (destination.isFavorite()) {
            favoriteButton.setImageResource(R.drawable.ic_heart);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_heart);
        }
    }

    private void handleTabSelection(int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                openMapView();
                break;
            case 2:
                break;
        }
    }

    private void openMapView() {
        DiscoverMapFragment mapFragment = DiscoverMapFragment.newInstance(destination);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mapFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear all view references to prevent memory leaks
        destination = null;
        destinationImage = null;
        destinationName = null;
        location = null;
        price = null;
        rating = null;
        description = null;
        backButton = null;
        favoriteButton = null;
        bookNowButton = null;
        tabLayout = null;
    }
}
