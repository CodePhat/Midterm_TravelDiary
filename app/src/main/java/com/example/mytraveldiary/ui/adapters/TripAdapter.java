package com.example.mytraveldiary.ui.adapters;

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

import java.lang.ref.WeakReference;
import java.util.List;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.ui.fragments.TravelMapFragment;
import com.example.mytraveldiary.ui.fragments.TripDetailFragment;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final List<AppData.Trip> trips;
    private final WeakReference<FragmentActivity> activityRef;
    private OnPhotoEditListener photoEditListener;

    public interface OnPhotoEditListener {
        void onEditPhoto(AppData.Trip trip, int position);
    }

    public TripAdapter(List<AppData.Trip> trips, FragmentActivity activity) {
        this.trips = trips;
        this.activityRef = new WeakReference<>(activity);
    }

    public void setOnPhotoEditListener(OnPhotoEditListener listener) {
        this.photoEditListener = listener;
    }

    private FragmentActivity getActivity() {
        return activityRef.get();
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
        android.util.Log.d("TripAdapter", "Loading image for " + trip.getDestination() + ": " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Parse URI if it's a content:// URI
                Object imageSource = imageUrl.startsWith("content://") ? Uri.parse(imageUrl) : imageUrl;

                android.content.Context context = holder.itemView.getContext();
                if (context != null) {
                    Glide.with(context.getApplicationContext())
                            .load(imageSource)
                            .centerCrop()
                            .placeholder(R.drawable.sample_trip)
                            .error(R.drawable.sample_trip)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model,
                                                           com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                           boolean isFirstResource) {
                                    android.util.Log.e("TripAdapter", "Failed to load image: " + imageUrl, e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                              com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                              com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    android.util.Log.d("TripAdapter", "Successfully loaded image: " + imageUrl);
                                    return false;
                                }
                            })
                            .into(holder.tripImage);
                }
            } catch (Exception e) {
                // If image loading fails (e.g., permission denied), use placeholder
                android.util.Log.e("TripAdapter", "Exception loading image: " + imageUrl, e);
                holder.tripImage.setImageResource(R.drawable.sample_trip);
            }
        } else {
            android.util.Log.d("TripAdapter", "No image URL for trip: " + trip.getDestination());
            holder.tripImage.setImageResource(R.drawable.sample_trip);
        }

        // Set favorite icon
        updateFavoriteIcon(holder, trip.isFavorite());

        // Edit photo button handler
        holder.btnEditPhoto.setOnClickListener(v -> {
            if (photoEditListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    photoEditListener.onEditPhoto(trip, adapterPosition);
                }
            }
        });

        // Favorite button handler
        holder.btnFavorite.setOnClickListener(v -> {
            trip.toggleFavorite();
            AppData.getInstance().triggerSave();
            updateFavoriteIcon(holder, trip.isFavorite());
            Toast.makeText(holder.itemView.getContext(),
                trip.isFavorite() ? "Added to favorites" : "Removed from favorites",
                Toast.LENGTH_SHORT).show();
        });

        // Show/hide map button based on location availability
        if (trip.hasLocation()) {
            holder.btnMapLocation.setVisibility(View.VISIBLE);
            holder.btnMapLocation.setOnClickListener(v -> {
                FragmentActivity activity = getActivity();
                if (activity == null) return;

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
            holder.btnMapLocation.setVisibility(View.GONE);
        }

        // Click handler: open TripDetailFragment
        holder.itemView.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity == null) return;

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
            // FIX: Use Activity context for proper locale handling
            FragmentActivity activity = getActivity();
            if (activity == null) return false;

            String[] options = {"View Trip", "Delete Trip"};


            new AlertDialog.Builder(activity)
                    .setTitle(trip.getDestination())
                    .setItems(options, (dialog, which) -> {
                        FragmentActivity activityForDialog = getActivity();
                        if (activityForDialog == null) return;

                        if (which == 0) {
                            // View option - open TripDetailFragment
                            TripDetailFragment fragment = new TripDetailFragment();
                            Bundle args = new Bundle();
                            args.putString("tripId", trip.getId());
                            fragment.setArguments(args);

                            activityForDialog.getSupportFragmentManager()
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
        // FIX: Use Activity context for proper locale handling
        FragmentActivity activity = getActivity();
        if (activity == null) return;

        new AlertDialog.Builder(activity)
                .setTitle("Delete Trip")
                .setMessage("Are you sure you want to delete \"" + trip.getDestination() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get the trip ID before removing
                    String tripId = trip.getId();

                    // Remove from data source
                    AppData.getInstance().removeTrip(tripId);

                    // Remove from list and notify adapter
                    int currentPosition = holder.getBindingAdapterPosition();
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
        if (isFavorite) {
            holder.btnFavorite.setImageResource(android.R.drawable.star_big_on);
            holder.btnFavorite.setColorFilter(0xFFFFD700); // Gold/yellow color
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.star_big_off);
            holder.btnFavorite.setColorFilter(0xFFFFFFFF); // White color
        }
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView tripImage;
        TextView tripTitle, tripDates;
        android.widget.ImageButton btnFavorite;
        android.widget.ImageButton btnMapLocation;
        android.widget.ImageButton btnEditPhoto;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripImage = itemView.findViewById(R.id.tripImage);
            tripTitle = itemView.findViewById(R.id.tripTitle);
            tripDates = itemView.findViewById(R.id.tripDates);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnMapLocation = itemView.findViewById(R.id.btnMapLocation);
            btnEditPhoto = itemView.findViewById(R.id.btnEditPhoto);
        }
    }
}
