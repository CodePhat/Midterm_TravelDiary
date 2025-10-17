package com.example.mytraveldiary;

import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {
    private final List<AppData.Trip> trips;
    private final Fragment parentFragment;

    public TripAdapter(List<AppData.Trip> trips, Fragment parentFragment) {
        this.trips = trips;
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppData.Trip trip = trips.get(position);
        holder.title.setText(trip.getDestination());
        holder.dates.setText(trip.getDateRange());

        // Load image using Glide (with placeholder and error fallback)
        if (trip.getImage() != null && !trip.getImage().isEmpty()) {
            Glide.with(holder.image.getContext())
                    .load(trip.getImage())
                    .placeholder(R.drawable.sample_trip)
                    .error(R.drawable.sample_trip)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.sample_trip);
        }

        // Handle click to open TripDetailFragment
        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("tripId", trip.getId());

            TripDetailFragment detailFragment = new TripDetailFragment();
            detailFragment.setArguments(args);

            parentFragment.requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    )
                    .replace(R.id.container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return trips != null ? trips.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, dates;

        ViewHolder(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.tripImage);
            title = v.findViewById(R.id.tripTitle);
            dates = v.findViewById(R.id.tripDates);
        }
    }
}
