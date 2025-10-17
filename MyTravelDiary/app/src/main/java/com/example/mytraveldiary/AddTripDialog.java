package com.example.mytraveldiary;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class AddTripDialog extends Dialog {

    private final Runnable onTripAddedCallback;

    public AddTripDialog(Context context, Runnable onTripAddedCallback) {
        super(context);
        this.onTripAddedCallback = onTripAddedCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_trip);

        EditText destinationInput = findViewById(R.id.inputDestination);
        EditText startDateInput = findViewById(R.id.inputStartDate);
        EditText endDateInput = findViewById(R.id.inputEndDate);
        EditText imageInput = findViewById(R.id.inputImageUrl);

        Button addButton = findViewById(R.id.btnAddTrip);
        Button cancelButton = findViewById(R.id.btnCancelAddTrip);

        cancelButton.setOnClickListener(v -> dismiss());

        addButton.setOnClickListener(v -> {
            String destination = destinationInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();
            String imageUrl = imageInput.getText().toString().trim();

            if (destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create new Trip
            AppData.Trip newTrip = new AppData.Trip(
                    UUID.randomUUID().toString(),
                    destination,
                    startDate,
                    endDate,
                    imageUrl.isEmpty() ? null : imageUrl
            );

            // Add to global data
            AppData.getInstance().addTrip(newTrip);

            // Notify callback
            if (onTripAddedCallback != null) {
                onTripAddedCallback.run();
            }

            Toast.makeText(getContext(), "Trip added!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}