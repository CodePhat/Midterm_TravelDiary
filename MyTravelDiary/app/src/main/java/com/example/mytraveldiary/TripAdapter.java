package com.example.mytraveldiary;

import android.net.Uri;
import android.os.Bundle; // ✅ added
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        // ✅ Click handler: open TripDetailFragment
        holder.itemView.setOnClickListener(v -> {
            TripDetailFragment fragment = new TripDetailFragment();
            Bundle args = new Bundle();
            args.putString("tripId", trip.getId());
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment) // ✅ fix container ID
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView tripImage;
        TextView tripTitle, tripDates;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tripImage = itemView.findViewById(R.id.tripImage);
            tripTitle = itemView.findViewById(R.id.tripTitle);
            tripDates = itemView.findViewById(R.id.tripDates);
        }
    }
}