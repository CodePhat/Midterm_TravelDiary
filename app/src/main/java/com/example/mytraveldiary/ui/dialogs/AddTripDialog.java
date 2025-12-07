package com.example.mytraveldiary.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.broadcasts.TripBroadcastReceiver;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.utils.helpers.AlarmHelper;
import com.example.mytraveldiary.utils.helpers.GeocodingHelper;
import com.example.mytraveldiary.utils.helpers.NotificationHelper;

public class AddTripDialog extends Dialog {

    private final Runnable onTripAdded;
    private Runnable onChooseImageClicked;

    private AutoCompleteTextView inputDestination;
    private android.widget.EditText inputStartDate, inputEndDate;
    private ImageView imagePreview;
    private TextInputLayout destinationInputLayout;
    private ArrayAdapter<GeocodingHelper.LocationSuggestion> autocompleteAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Store chosen image URI and location
    private String selectedImageUri = null;
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private boolean hasManualLocation = false;
    private LocationPickerDialog locationPickerDialog;

    // Alarm/reminder variables
    private boolean alarmEnabled = false;
    private String alarmTime = null;
    private String alarmMessage = null;

    // Smart Language Detection
    private com.example.mytraveldiary.utils.helpers.SmartLanguageDetector languageDetector;
    private boolean hasShownLanguageSuggestion = false;

    public AddTripDialog(@NonNull Context context, Runnable onTripAdded) {
        super(context);
        this.onTripAdded = onTripAdded;
        this.languageDetector = new com.example.mytraveldiary.utils.helpers.SmartLanguageDetector(context);
    }

    // Allow fragment to set callback for opening gallery
    public void setOnChooseImageClicked(Runnable onChooseImageClicked) {
        this.onChooseImageClicked = onChooseImageClicked;
    }


    // Let fragment update selected image URI (after picking)
    public void setSelectedImageUri(String uri) {
        this.selectedImageUri = uri;

        // Update preview immediately if dialog still visible
        if (imagePreview != null && uri != null && !uri.isEmpty()) {
            try {
                if (getContext() != null) {
                    Glide.with(getContext().getApplicationContext())
                            .load(Uri.parse(uri))
                            .centerCrop()
                            .placeholder(R.drawable.sample_trip)
                            .error(R.drawable.sample_trip)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                            .into(imagePreview);
                }
            } catch (Exception e) {
                imagePreview.setImageResource(R.drawable.sample_trip);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_trip);

        // Make dialog wider for better visibility
        if (getWindow() != null) {
            getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        inputDestination = findViewById(R.id.inputDestination);
        inputStartDate = findViewById(R.id.inputStartDate);
        inputEndDate = findViewById(R.id.inputEndDate);
        imagePreview = findViewById(R.id.imagePreview);
        destinationInputLayout = findViewById(R.id.destinationInputLayout);

        Button chooseImageBtn = findViewById(R.id.btnChooseImage);
        Button cancelBtn = findViewById(R.id.btnCancelAddTrip);
        Button addTripBtn = findViewById(R.id.btnAddTrip);
        android.widget.ImageButton closeBtn = findViewById(R.id.btnCloseDialog);

        // Alarm components
        com.google.android.material.switchmaterial.SwitchMaterial switchSetAlarm = findViewById(R.id.switchSetAlarm);
        android.widget.LinearLayout alarmSettingsLayout = findViewById(R.id.alarmSettingsLayout);
        Button btnSetAlarmTime = findViewById(R.id.btnSetAlarmTime);
        com.google.android.material.textfield.TextInputEditText inputAlarmMessage = findViewById(R.id.inputAlarmMessage);

        // Close button dismisses dialog
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dismiss());
        }

        // Alarm switch toggle
        if (switchSetAlarm != null && alarmSettingsLayout != null) {
            switchSetAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarmEnabled = isChecked;
                alarmSettingsLayout.setVisibility(isChecked ? android.view.View.VISIBLE : android.view.View.GONE);
            });
        }

        // Set alarm time button
        if (btnSetAlarmTime != null) {
            btnSetAlarmTime.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minuteOfDay) -> {
                        alarmTime = String.format(java.util.Locale.US, "%02d:%02d", hourOfDay, minuteOfDay);
                        btnSetAlarmTime.setText("Reminder at: " + alarmTime);
                        btnSetAlarmTime.setTextColor(getContext().getColor(R.color.primary_blue));
                    },
                    hour,
                    minute,
                    true
                );
                timePicker.show();
            });
        }

        // Setup autocomplete for destination
        setupAutocomplete();

        // Setup smart language detection on destination input
        setupSmartLanguageDetection();

        // Setup map picker button (globe icon)
        destinationInputLayout.setEndIconOnClickListener(v -> openLocationPicker());

        // Setup date pickers
        setupDatePickers();

        chooseImageBtn.setOnClickListener(v -> {
            if (onChooseImageClicked != null) {
                onChooseImageClicked.run();
            }
        });

        cancelBtn.setOnClickListener(v -> dismiss());

        addTripBtn.setOnClickListener(v -> {
            String destination = inputDestination.getText().toString().trim();
            String startDate = inputStartDate.getText().toString().trim();
            String endDate = inputEndDate.getText().toString().trim();

            if (destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button while processing
            addTripBtn.setEnabled(false);
            addTripBtn.setText("Adding trip...");

            // Create trip with selected image URI (may be null)
            AppData.Trip newTrip = new AppData.Trip(
                    UUID.randomUUID().toString(),
                    destination,
                    startDate,
                    endDate,
                    selectedImageUri
            );

            // Get alarm message if provided
            if (alarmEnabled && inputAlarmMessage != null) {
                alarmMessage = inputAlarmMessage.getText().toString().trim();
                if (alarmMessage.isEmpty()) {
                    alarmMessage = "Trip to " + destination + " starting soon!";
                }
            }

            // Use manual location if picked from map, otherwise geocode
            if (hasManualLocation) {
                newTrip.setLocation(selectedLatitude, selectedLongitude);
                AppData.getInstance().addTrip(newTrip);

                // Schedule custom alarm if enabled
                if (alarmEnabled && alarmTime != null) {
                    scheduleCustomAlarm(newTrip.getId(), destination, startDate, alarmTime, alarmMessage);
                } else {
                    // Schedule default alarm for trip reminder
                    AlarmHelper.scheduleTripReminder(getContext(), newTrip.getId(), destination, startDate);
                }

                // Send broadcast that trip was added
                Intent broadcastIntent = new Intent(TripBroadcastReceiver.ACTION_TRIP_ADDED);
                broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_DESTINATION, destination);
                broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_ID, newTrip.getId());
                getContext().sendBroadcast(broadcastIntent);

                Toast.makeText(getContext(), "Trip added with map location!", Toast.LENGTH_SHORT).show();
                if (onTripAdded != null) onTripAdded.run();
                dismiss();
            } else {
                // Automatically geocode the destination
                GeocodingHelper.geocodeLocation(destination, new GeocodingHelper.GeocodingCallback() {
                    @Override
                    public void onLocationFound(double latitude, double longitude, String displayName) {
                        newTrip.setLocation(latitude, longitude);
                        Toast.makeText(getContext(), "Trip added with location: " + displayName, Toast.LENGTH_LONG).show();

                        AppData.getInstance().addTrip(newTrip);

                        // Schedule custom alarm if enabled, otherwise default
                        if (alarmEnabled && alarmTime != null) {
                            scheduleCustomAlarm(newTrip.getId(), destination, startDate, alarmTime, alarmMessage);
                        } else {
                            AlarmHelper.scheduleTripReminder(getContext(), newTrip.getId(), destination, startDate);
                        }

                        // Send broadcast that trip was added
                        Intent broadcastIntent = new Intent(TripBroadcastReceiver.ACTION_TRIP_ADDED);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_DESTINATION, destination);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_ID, newTrip.getId());
                        getContext().sendBroadcast(broadcastIntent);

                        if (onTripAdded != null) onTripAdded.run();
                        dismiss();
                    }

                    @Override
                    public void onLocationNotFound() {
                        Toast.makeText(getContext(), "Location not found, trip added without map pin", Toast.LENGTH_SHORT).show();

                        AppData.getInstance().addTrip(newTrip);

                        // Schedule custom alarm if enabled, otherwise default
                        if (alarmEnabled && alarmTime != null) {
                            scheduleCustomAlarm(newTrip.getId(), destination, startDate, alarmTime, alarmMessage);
                        } else {
                            AlarmHelper.scheduleTripReminder(getContext(), newTrip.getId(), destination, startDate);
                        }

                        // Send broadcast
                        Intent broadcastIntent = new Intent(TripBroadcastReceiver.ACTION_TRIP_ADDED);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_DESTINATION, destination);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_ID, newTrip.getId());
                        getContext().sendBroadcast(broadcastIntent);

                        if (onTripAdded != null) onTripAdded.run();
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Could not find location, trip added without map pin", Toast.LENGTH_SHORT).show();

                        AppData.getInstance().addTrip(newTrip);

                        // Schedule custom alarm if enabled, otherwise default
                        if (alarmEnabled && alarmTime != null) {
                            scheduleCustomAlarm(newTrip.getId(), destination, startDate, alarmTime, alarmMessage);
                        } else {
                            AlarmHelper.scheduleTripReminder(getContext(), newTrip.getId(), destination, startDate);
                        }

                        // Send broadcast
                        Intent broadcastIntent = new Intent(TripBroadcastReceiver.ACTION_TRIP_ADDED);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_DESTINATION, destination);
                        broadcastIntent.putExtra(TripBroadcastReceiver.EXTRA_TRIP_ID, newTrip.getId());
                        getContext().sendBroadcast(broadcastIntent);

                        if (onTripAdded != null) onTripAdded.run();
                        dismiss();
                    }
                });
            }
        });
    }

    private void setupAutocomplete() {
        autocompleteAdapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_dropdown_item_1line,
            new ArrayList<>()
        );
        inputDestination.setAdapter(autocompleteAdapter);

        // Listen for text changes and fetch suggestions
        inputDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search with delay (debounce)
                searchRunnable = () -> fetchLocationSuggestions(s.toString());
                searchHandler.postDelayed(searchRunnable, 500); // 500ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // When user selects a suggestion, store its coordinates
        inputDestination.setOnItemClickListener((parent, view, position, id) -> {
            GeocodingHelper.LocationSuggestion selected = autocompleteAdapter.getItem(position);
            if (selected != null) {
                selectedLatitude = selected.latitude;
                selectedLongitude = selected.longitude;
                hasManualLocation = true;
                Toast.makeText(getContext(), "Location selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLocationSuggestions(String query) {
        GeocodingHelper.getLocationSuggestions(query, new GeocodingHelper.AutocompleteCallback() {
            @Override
            public void onSuggestionsFound(List<GeocodingHelper.LocationSuggestion> suggestions) {
                autocompleteAdapter.clear();
                autocompleteAdapter.addAll(suggestions);
                autocompleteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Silently fail - autocomplete is optional
            }
        });
    }

    private void openLocationPicker() {
        locationPickerDialog = new LocationPickerDialog(getContext(),
            (latitude, longitude, locationName) -> {
                selectedLatitude = latitude;
                selectedLongitude = longitude;
                hasManualLocation = true;
                inputDestination.setText(locationName);
                Toast.makeText(getContext(), "Location picked from map", Toast.LENGTH_SHORT).show();
            });
        locationPickerDialog.show();
    }

    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();

        // Start date picker
        inputStartDate.setOnClickListener(v -> {
            DatePickerDialog startDatePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    inputStartDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            startDatePicker.show();
        });

        // End date picker
        inputEndDate.setOnClickListener(v -> {
            DatePickerDialog endDatePicker = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    inputEndDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            endDatePicker.show();
        });
    }

    private void scheduleCustomAlarm(String tripId, String destination, String startDate, String time, String message) {
        try {
            // Parse the start date and time
            String[] dateParts = startDate.split("-");
            String[] timeParts = time.split(":");

            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            alarmCalendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            alarmCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));
            alarmCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            alarmCalendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            alarmCalendar.set(Calendar.SECOND, 0);

            long alarmTimeMillis = alarmCalendar.getTimeInMillis();

            // Use AlarmHelper to schedule the notification
            AlarmHelper.scheduleCustomTripReminder(getContext(), tripId, message != null ? message : destination, alarmTimeMillis);

            Toast.makeText(getContext(), "Reminder set for " + time + " on " + startDate, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to set reminder", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup smart language detection for destination input
     */
    private void setupSmartLanguageDetection() {
        // Check if user has disabled language suggestions
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean dontAskAgain = prefs.getBoolean("language_suggestion_disabled", false);

        if (dontAskAgain) {
            return; // User doesn't want language suggestions
        }

        String currentLanguage = com.example.mytraveldiary.utils.LocaleHelper.getLanguage(getContext());

        // Add text watcher to destination input
        inputDestination.addTextChangedListener(
            com.example.mytraveldiary.utils.helpers.SmartLanguageDetector.createSmartTextWatcher(
                inputDestination,
                getContext(),
                currentLanguage,
                (detectedLang, suggestedLang, reason) -> {
                    // Only show suggestion once per dialog
                    if (!hasShownLanguageSuggestion) {
                        hasShownLanguageSuggestion = true;
                        showLanguageSuggestionDialog(suggestedLang);
                    }
                }
            )
        );
    }

    /**
     * Show subtle floating snackbar suggesting language change (non-intrusive)
     */
    private void showLanguageSuggestionDialog(String suggestedLanguage) {
        String languageName = suggestedLanguage.equals("vi")
            ? getContext().getString(R.string.language_vietnamese)
            : getContext().getString(R.string.language_english);

        // Create custom snackbar view
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(getContext());
        android.view.View snackbarView = inflater.inflate(R.layout.layout_language_suggestion_snackbar, null);

        // Set message
        android.widget.TextView messageText = snackbarView.findViewById(R.id.snackbarMessage);
        String message = "Typing in " + languageName + "?";
        messageText.setText(message);

        // Set button text
        com.google.android.material.button.MaterialButton switchBtn = snackbarView.findViewById(R.id.btnSwitch);
        switchBtn.setText("Switch");

        // Create a container to show the snackbar at the bottom
        android.widget.FrameLayout container = new android.widget.FrameLayout(getContext());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = android.view.Gravity.BOTTOM;
        container.setLayoutParams(params);
        container.addView(snackbarView);

        // Add to dialog's root view
        android.view.ViewGroup dialogRoot = (android.view.ViewGroup) getWindow().getDecorView();
        dialogRoot.addView(container);

        // Animate in
        snackbarView.setTranslationY(200);
        snackbarView.setAlpha(0);
        snackbarView.animate()
            .translationY(0)
            .alpha(1)
            .setDuration(300)
            .start();

        // Auto-dismiss after 8 seconds
        Handler dismissHandler = new Handler(Looper.getMainLooper());
        Runnable dismissRunnable = () -> {
            if (snackbarView.getParent() != null) {
                snackbarView.animate()
                    .translationY(200)
                    .alpha(0)
                    .setDuration(300)
                    .withEndAction(() -> dialogRoot.removeView(container))
                    .start();
            }
        };
        dismissHandler.postDelayed(dismissRunnable, 8000);

        // Switch button - change language
        switchBtn.setOnClickListener(v -> {
            dismissHandler.removeCallbacks(dismissRunnable);

            // Animate out the snackbar
            snackbarView.animate()
                .translationY(200)
                .alpha(0)
                .setDuration(200)
                .withEndAction(() -> {
                    dialogRoot.removeView(container);
                })
                .start();

            // Save the language preference
            com.example.mytraveldiary.utils.LocaleHelper.persistLanguage(getContext(), suggestedLanguage);

            // Show a toast to indicate language is changing
            Toast.makeText(getContext(), "Language changed. Please log back in to apply.", Toast.LENGTH_LONG).show();

            // Dismiss this dialog first
            dismiss();

            // Simple approach: just recreate activity
            // User will need to log back in for full language change
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getContext() instanceof android.app.Activity) {
                    android.app.Activity activity = (android.app.Activity) getContext();
                    activity.recreate();
                }
            }, 300);
        });

        // Close button - dismiss
        android.widget.ImageButton closeBtn = snackbarView.findViewById(R.id.btnClose);
        closeBtn.setOnClickListener(v -> {
            dismissHandler.removeCallbacks(dismissRunnable);
            snackbarView.animate()
                .translationY(200)
                .alpha(0)
                .setDuration(300)
                .withEndAction(() -> dialogRoot.removeView(container))
                .start();
        });

        // Long press on close - disable permanently
        closeBtn.setOnLongClickListener(v -> {
            dismissHandler.removeCallbacks(dismissRunnable);
            dialogRoot.removeView(container);

            // Save preference
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("language_suggestion_disabled", true).apply();
            Toast.makeText(getContext(), "Language suggestions disabled", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Clean up language detector
        if (languageDetector != null) {
            languageDetector.close();
        }
    }
}
