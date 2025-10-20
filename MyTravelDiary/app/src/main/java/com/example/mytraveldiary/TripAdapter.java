package com.example.mytraveldiary;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final List<AppData.Trip> trips;
    private final FragmentActivity activity;

    public TripAdapter(List<AppData.Trip> trips, FragmentActivity activity) {
        this.trips = trips;
        this.activity = activity;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        AppData.Trip trip = trips.get(position);

        holder.tripTitle.setText(trip.getDestination());
        holder.tripDates.setText(trip.getDateRange());

        String imageUrl = trip.getImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl.startsWith("content://") ? Uri.parse(imageUrl) : imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.sample_trip)
                    .into(holder.tripImage);
        } else {
            holder.tripImage.setImageResource(R.drawable.sample_trip);
        }

        // Set favorite icon
        updateFavoriteIcon(holder, trip.isFavorite());

        // Favorite button handler
        holder.btnFavorite.setOnClickListener(v -> {
            trip.toggleFavorite();
            AppData.getInstance().triggerSave();
            updateFavoriteIcon(holder, trip.isFavorite());
            Toast.makeText(holder.itemView.getContext(),
                trip.isFavorite() ? "Added to favorites" : "Removed from favorites",
                Toast.LENGTH_SHORT).show();
        });

        // Show/hide map icon based on location availability
        if (trip.hasLocation()) {
            holder.imgMapIcon.setVisibility(View.VISIBLE);
            holder.imgMapIcon.setOnClickListener(v -> {
                // Navigate to map fragment and center on this trip
                TravelMapFragment mapFragment = new TravelMapFragment();
                Bundle args = new Bundle();
                args.putString("centerTripId", trip.getId());
                mapFragment.setArguments(args);

                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            holder.imgMapIcon.setVisibility(View.GONE);
        }

        // Click handler: open TripDetailFragment
        holder.itemView.setOnClickListener(v -> {
            TripDetailFragment fragment = new TripDetailFragment();
            Bundle args = new Bundle();
            args.putString("tripId", trip.getId());
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Long press handler: show options menu (Delete/View)
        holder.itemView.setOnLongClickListener(v -> {
            String[] options = {"View Trip", "Delete Trip"};

            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle(trip.getDestination())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // View option - open TripDetailFragment
                            TripDetailFragment fragment = new TripDetailFragment();
                            Bundle args = new Bundle();
                            args.putString("tripId", trip.getId());
                            fragment.setArguments(args);

                            activity.getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        } else if (which == 1) {
                            // Delete option - show confirmation
                            showDeleteConfirmation(trip, holder);
                        }
                    })
                    .show();
            return true; // Consume the long click event
        });
    }

    private void showDeleteConfirmation(AppData.Trip trip, TripViewHolder holder) {
        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete \"" + trip.getDestination() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get the trip ID before removing
                    String tripId = trip.getId();

                    // Remove from data source
                    AppData.getInstance().removeTrip(tripId);

                    // Remove from list and notify adapter
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        trips.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, trips.size());
                    }

                    Toast.makeText(holder.itemView.getContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateFavoriteIcon(TripViewHolder holder, boolean isFavorite) {
        holder.btnFavorite.setImageResource(
            isFavorite ? android.R.drawable.star_big_on : android.R.drawable.star_big_off
        );
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView tripImage;
        TextView tripTitle, tripDates;
        android.widget.ImageButton btnFavorite;
        ImageView imgMapIcon;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripImage = itemView.findViewById(R.id.tripImage);
            tripTitle = itemView.findViewById(R.id.tripTitle);
            tripDates = itemView.findViewById(R.id.tripDates);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            imgMapIcon = itemView.findViewById(R.id.imgMapIcon);
        }
    }
}
