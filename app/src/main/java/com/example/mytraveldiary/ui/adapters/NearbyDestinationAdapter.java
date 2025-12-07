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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NearbyDestinationAdapter extends RecyclerView.Adapter<NearbyDestinationAdapter.ViewHolder> {

    private List<Destination> destinations;
    private OnDestinationClickListener listener;

    public interface OnDestinationClickListener {
        void onDestinationClick(Destination destination);
    }

    public NearbyDestinationAdapter(OnDestinationClickListener listener) {
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
                .inflate(R.layout.item_destination_nearby, parent, false);
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
        TextView distance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationImage = itemView.findViewById(R.id.destinationImage);
            destinationName = itemView.findViewById(R.id.destinationName);
            destinationLocation = itemView.findViewById(R.id.destinationLocation);
            distance = itemView.findViewById(R.id.distance);
        }

        void bind(Destination destination) {
            destinationName.setText(destination.getName());
            destinationLocation.setText(destination.getLocation());
            distance.setText(String.format(Locale.getDefault(), "%.1fkm", destination.getDistance()));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDestinationClick(destination);
                }
            });
        }
    }
}
