package com.example.mytraveldiary.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.models.Destination;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private List<Destination> destinations;
    private OnDestinationClickListener listener;

    public interface OnDestinationClickListener {
        void onDestinationClick(Destination destination);
        void onFavoriteClick(Destination destination);
    }

    public DestinationAdapter(OnDestinationClickListener listener) {
        this.destinations = new ArrayList<>();
        this.listener = listener;
    }

    public void setDestinations(List<Destination> destinations) {
        this.destinations = destinations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destination_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.bind(destination);
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView destinationImage;
        TextView destinationName;
        TextView destinationLocation;
        FloatingActionButton favoriteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationImage = itemView.findViewById(R.id.destinationImage);
            destinationName = itemView.findViewById(R.id.destinationName);
            destinationLocation = itemView.findViewById(R.id.destinationLocation);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }

        void bind(Destination destination) {
            destinationName.setText(destination.getName());
            destinationLocation.setText(destination.getLocation());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDestinationClick(destination);
                }
            });

            favoriteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(destination);
                }
            });
        }
    }
}
