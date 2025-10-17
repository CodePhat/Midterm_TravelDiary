package com.example.mytraveldiary;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.UUID;

public class AddTripDialog extends Dialog {

    private final Runnable onTripAdded;
    private Runnable onChooseImageClicked;

    private EditText inputDestination, inputStartDate, inputEndDate;
    private ImageView imagePreview;

    // store chosen image URI
    private String selectedImageUri = null;

    public AddTripDialog(@NonNull Context context, Runnable onTripAdded) {
        super(context);
        this.onTripAdded = onTripAdded;
    }

    // Allow fragment to set callback for opening gallery
    public void setOnChooseImageClicked(Runnable onChooseImageClicked) {
        this.onChooseImageClicked = onChooseImageClicked;
    }

    // Let fragment update selected image URI (after picking)
    public void setSelectedImageUri(String uri) {
        this.selectedImageUri = uri;

        // ✅ update preview immediately if dialog still visible
        if (imagePreview != null && uri != null) {
            Glide.with(getContext())
                    .load(Uri.parse(uri))
                    .centerCrop()
                    .placeholder(R.drawable.sample_trip)
                    .into(imagePreview);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_trip);

        inputDestination = findViewById(R.id.inputDestination);
        inputStartDate = findViewById(R.id.inputStartDate);
        inputEndDate = findViewById(R.id.inputEndDate);
        imagePreview = findViewById(R.id.imagePreview);

        Button chooseImageBtn = findViewById(R.id.btnChooseImage);
        Button cancelBtn = findViewById(R.id.btnCancelAddTrip);
        Button addTripBtn = findViewById(R.id.btnAddTrip);

        chooseImageBtn.setOnClickListener(v -> {
            if (onChooseImageClicked != null) {
                onChooseImageClicked.run(); // fragment opens gallery
            }
        });

        cancelBtn.setOnClickListener(v -> dismiss());

        addTripBtn.setOnClickListener(v -> {
            String destination = inputDestination.getText().toString().trim();
            String startDate = inputStartDate.getText().toString().trim();
            String endDate = inputEndDate.getText().toString().trim();

            if (destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Create trip with selected image URI
            AppData.Trip newTrip = new AppData.Trip(
                    UUID.randomUUID().toString(),
                    destination,
                    startDate,
                    endDate,
                    selectedImageUri // may be null
            );

            AppData.getInstance().addTrip(newTrip);
            if (onTripAdded != null) onTripAdded.run();
            dismiss();
        });
    }
}