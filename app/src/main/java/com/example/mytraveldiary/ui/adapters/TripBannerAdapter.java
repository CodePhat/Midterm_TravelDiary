package com.example.mytraveldiary.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;

import java.util.ArrayList;
import java.util.List;

public class TripBannerAdapter extends RecyclerView.Adapter<TripBannerAdapter.BannerViewHolder> {

    private List<AppData.Trip> trips;

    public TripBannerAdapter() {
        this.trips = new ArrayList<>();
    }

    public void setTrips(List<AppData.Trip> trips) {
        this.trips = trips != null ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (trips.isEmpty()) return;

        AppData.Trip trip = trips.get(position);

        // Set title and subtitle
        holder.title.setText(trip.getDestination());
        holder.subtitle.setText(trip.getDateRange());

        // Load image with Glide
        String imageUrl = trip.getImage();
        android.util.Log.d("TripBannerAdapter", "Loading image for " + trip.getDestination() + ": " + imageUrl);

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
                                    android.util.Log.e("TripBannerAdapter", "Failed to load image: " + imageUrl, e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model,
                                                              com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                              com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    android.util.Log.d("TripBannerAdapter", "Successfully loaded image: " + imageUrl);
                                    return false;
                                }
                            })
                            .into(holder.image);
                }
            } catch (Exception e) {
                android.util.Log.e("TripBannerAdapter", "Exception loading image: " + imageUrl, e);
                holder.image.setImageResource(R.drawable.sample_trip);
            }
        } else {
            android.util.Log.d("TripBannerAdapter", "No image URL for trip: " + trip.getDestination());
            holder.image.setImageResource(R.drawable.sample_trip);
        }
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.bannerImage);
            title = itemView.findViewById(R.id.bannerTitle);
            subtitle = itemView.findViewById(R.id.bannerSubtitle);
        }
    }
}
