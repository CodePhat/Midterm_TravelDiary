package com.example.mytraveldiary.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;

import java.util.*;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.data.models.Expense;
import com.example.mytraveldiary.data.models.ExpenseCategory;
import com.example.mytraveldiary.ui.dialogs.AddExpenseDialog;

import java.util.Calendar;
import java.util.Random;

public class TripDetailFragment extends Fragment {

    private AppData.Trip trip;
    private AppData appData;
    private LinearLayout contentLayout;
    private String activeTab = "expenses"; // Default tab is expenses
    private String previousTab = "expenses";
    private PieChart pieChart;
    private TextView totalText;
    private Button btnAddExpense;
    private LinearLayout expensesContainer;
    private Button tabItinerary, tabExpenses, tabDiary, tabPhotos;
    private LinearLayout photoContainer;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;
    private com.google.android.material.textfield.TextInputEditText currentDiaryInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize speech recognizer launcher
        speechRecognizerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty() && currentDiaryInput != null) {
                        String currentText = currentDiaryInput.getText() != null ? currentDiaryInput.getText().toString() : "";
                        String spokenText = matches.get(0);

                        // Append spoken text to existing text
                        if (!currentText.isEmpty() && !currentText.endsWith(" ")) {
                            currentText += " ";
                        }
                        currentDiaryInput.setText(currentText + spokenText);
                        currentDiaryInput.setSelection(currentDiaryInput.getText().length());
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_trip_detail, container, false);

        appData = AppData.getInstance();
        String tripId = getArguments() != null ? getArguments().getString("tripId") : null;
        if (tripId == null) {
            Toast.makeText(getContext(), "Trip ID missing", Toast.LENGTH_SHORT).show();
            return root;
        }

        trip = appData.getTrip(tripId);
        if (trip == null) {
            Toast.makeText(getContext(), "Trip not found", Toast.LENGTH_SHORT).show();
            return root;
        }

        ImageView tripImage = root.findViewById(R.id.tripImage);
        TextView tripTitle = root.findViewById(R.id.tripTitle);
        TextView tripDates = root.findViewById(R.id.tripDates);

        // Safe null checks before using trip data
        if (tripTitle != null) {
            tripTitle.setText(trip.getDestination() != null ? trip.getDestination() : "Unknown");
        }
        if (tripDates != null) {
            tripDates.setText(trip.getDateRange() != null ? trip.getDateRange() : "");
        }

        if (tripImage != null && trip.getImage() != null && !trip.getImage().isEmpty()) {
            try {
                if (getContext() != null && isAdded()) {
                    Object imageSource = trip.getImage().startsWith("content://") ?
                        Uri.parse(trip.getImage()) : trip.getImage();

                    Glide.with(requireContext().getApplicationContext())
                        .load(imageSource)
                        .centerCrop()
                        .placeholder(R.drawable.sample_trip)
                        .error(R.drawable.sample_trip)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                        .into(tripImage);
                }
            } catch (Exception e) {
                tripImage.setImageResource(R.drawable.sample_trip);
            }
        }

        contentLayout = root.findViewById(R.id.contentLayout);

        tabItinerary = root.findViewById(R.id.tabItinerary);
        tabExpenses = root.findViewById(R.id.tabExpenses);
        tabDiary = root.findViewById(R.id.tabDiary);
        tabPhotos = root.findViewById(R.id.tabPhotos);

        // Setup toolbar with back button
        androidx.appcompat.widget.Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // Navigate back to previous fragment
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        setupTabs();
        setupImagePicker();
        refreshTab();

        return root;
    }

    private void setupTabs() {
        tabItinerary.setOnClickListener(v -> setActiveTab("itinerary"));
        tabExpenses.setOnClickListener(v -> setActiveTab("expenses"));
        tabDiary.setOnClickListener(v -> setActiveTab("diary"));
        tabPhotos.setOnClickListener(v -> setActiveTab("photos"));
    }

    private void setActiveTab(String tab) {
        if (activeTab.equals(tab)) return; // Don't switch if same tab
        previousTab = activeTab;
        activeTab = tab;
        refreshTab();
    }

    private int getTabIndex(String tab) {
        switch (tab) {
            case "itinerary": return 0;
            case "expenses": return 1;
            case "diary": return 2;
            case "photos": return 3;
            default: return 1;
        }
    }

    private void refreshTab() {
        resetTabStyles(tabItinerary);
        resetTabStyles(tabExpenses);
        resetTabStyles(tabDiary);
        resetTabStyles(tabPhotos);

        switch (activeTab) {
            case "itinerary":
                setTabSelected(tabItinerary);
                break;
            case "expenses":
                setTabSelected(tabExpenses);
                break;
            case "diary":
                setTabSelected(tabDiary);
                break;
            case "photos":
                setTabSelected(tabPhotos);
                break;
        }

        // Determine slide direction
        boolean slideLeft = getTabIndex(activeTab) > getTabIndex(previousTab);

        // Apply exit animation to current view
        if (contentLayout.getChildCount() > 0) {
            View currentView = contentLayout.getChildAt(0);
            currentView.animate()
                .translationX(slideLeft ? -contentLayout.getWidth() : contentLayout.getWidth())
                .alpha(0f)
                .setDuration(250)
                .withEndAction(() -> {
                    contentLayout.removeAllViews();
                    showNewTabContent(slideLeft);
                })
                .start();
        } else {
            showNewTabContent(slideLeft);
        }
    }

    private void showNewTabContent(boolean slideLeft) {
        View newView = null;
        switch (activeTab) {
            case "expenses":
                newView = showExpenses();
                break;
            case "itinerary":
                newView = showItinerary();
                break;
            case "diary":
                newView = showDiary();
                break;
            case "photos":
                newView = showPhotos();
                break;
        }

        if (newView != null) {
            // Set initial position for slide in animation
            newView.setTranslationX(slideLeft ? contentLayout.getWidth() : -contentLayout.getWidth());
            newView.setAlpha(0f);

            // Animate in
            newView.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(250)
                .start();
        }
    }
    private View showExpenses() {
        View expensesView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_expenses_section, contentLayout, false);
        contentLayout.addView(expensesView);

        totalText = expensesView.findViewById(R.id.totalExpenses);
        pieChart = expensesView.findViewById(R.id.expenseChart);
        expensesContainer = expensesView.findViewById(R.id.expenseList);
        btnAddExpense = expensesView.findViewById(R.id.btnAddExpense);

        btnAddExpense.setOnClickListener(v ->
                new AddExpenseDialog(getContext(), trip.getId(), this::refreshExpenses).show()
        );

        // Add button for mock data (for testing)
        Button btnMockData = new Button(getContext());
        btnMockData.setText("📊 Generate Mock Data");
        btnMockData.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.accent_green));
        btnMockData.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        btnMockData.setOnClickListener(v -> {
            generateMockExpenses();
            Toast.makeText(getContext(), "Mock expense data generated!", Toast.LENGTH_SHORT).show();
        });

        // Insert mock button before expenses container
        LinearLayout parentLayout = (LinearLayout) expensesView;
        ViewGroup expenseParent = (ViewGroup) expensesContainer.getParent();
        int index = parentLayout.indexOfChild(expenseParent);
        if (index > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16);
            btnMockData.setLayoutParams(params);
            parentLayout.addView(btnMockData, index);
        }

        refreshExpenses();
        return expensesView;
    }

    private void generateMockExpenses() {
        if (trip == null) return;

        // Clear existing expenses
        trip.getExpenses().clear();

        // Generate mock expenses over 7 days with varying amounts
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7); // Start from 7 days ago

        String[] categories = {"Food", "Transport", "Accommodation", "Entertainment", "Shopping"};
        String[] descriptions = {
            "Breakfast at cafe", "Lunch at restaurant", "Dinner with friends",
            "Taxi to hotel", "Bus ticket", "Train ride",
            "Hotel night stay", "Airbnb booking",
            "Museum ticket", "Concert tickets", "Movie night",
            "Souvenir shopping", "Local market", "Gift shop"
        };

        Random random = new Random();

        // Generate 15-20 expenses with realistic patterns
        int numExpenses = 15 + random.nextInt(6);
        for (int i = 0; i < numExpenses; i++) {
            // Random date within 7 days
            Calendar expenseDate = (Calendar) cal.clone();
            expenseDate.add(Calendar.DAY_OF_MONTH, random.nextInt(8));

            // Create varying expense amounts (higher on certain days to show trends)
            double baseAmount = 20 + random.nextDouble() * 150;

            // Add spikes on certain days
            int dayOfWeek = expenseDate.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                baseAmount *= 1.5; // Weekend spending spike
            }

            String category = categories[random.nextInt(categories.length)];
            String description = descriptions[random.nextInt(descriptions.length)];

            ExpenseCategory expenseCategory;
            try {
                expenseCategory = ExpenseCategory.valueOf(category.replace(" ", ""));
            } catch (IllegalArgumentException e) {
                expenseCategory = ExpenseCategory.Other;
            }

            Expense expense = new Expense(
                trip.getId(),
                description,
                baseAmount,
                expenseCategory,
                expenseDate.getTime()
            );

            trip.getExpenses().add(expense);
        }

        // Save and refresh
        appData.triggerSave();
        refreshExpenses();
    }

    private void refreshExpenses() {
        List<Expense> expenses = trip.getExpenses();
        expensesContainer.removeAllViews();

        if (expenses == null || expenses.isEmpty()) {
            TextView msg = new TextView(getContext());
            msg.setText("No expenses added yet!");
            msg.setTextSize(16);
            msg.setPadding(30, 50, 30, 50);
            expensesContainer.addView(msg);
            totalText.setText("Total: $0.00");
            pieChart.clear();
            return;
        }

        double total = 0;
        for (Expense exp : expenses) {
            total += exp.getAmount();

            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_expense, expensesContainer, false);

            ((TextView) item.findViewById(R.id.expenseDesc)).setText(exp.getDescription());
            ((TextView) item.findViewById(R.id.expenseCategory)).setText(exp.getCategory().name());
            ((TextView) item.findViewById(R.id.expenseAmount))
                    .setText(String.format("$%.2f", exp.getAmount()));

            expensesContainer.addView(item);
        }

        totalText.setText(String.format("Total: $%.2f", total));
        updatePieChart(trip.getExpenseSummary());
    }

    private void updatePieChart(Map<String, Double> data) {
        if (pieChart == null || getContext() == null || !isAdded()) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            if (e.getValue() > 0)
                entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        }

        if (entries.isEmpty()) {
            pieChart.clear();
            pieChart.setData(null);
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Beautiful modern colors
        int[] colors = {
            ContextCompat.getColor(getContext(), R.color.primary_blue),
            ContextCompat.getColor(getContext(), R.color.accent_orange),
            android.graphics.Color.parseColor("#4CAF50"),
            ContextCompat.getColor(getContext(), R.color.bottom_nav_dark_green),
            android.graphics.Color.parseColor("#FF9800"),
            android.graphics.Color.parseColor("#9C27B0")
        };
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(14f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setCenterText("Expenses\nBreakdown");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.text_primary_light));
        pieChart.setDrawEntryLabels(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(13f);
        legend.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary_light));
        legend.setWordWrapEnabled(true);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10f);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);

        pieChart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.invalidate();
    }


    private View showItinerary() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_itinerary_section, contentLayout, false);
        contentLayout.addView(view);

        LinearLayout list = view.findViewById(R.id.itineraryList);
        Button btnCreateItinerary = view.findViewById(R.id.btnCreateItinerary);

        refreshItineraryList(list);

        btnCreateItinerary.setOnClickListener(v -> showCreateItineraryDialog(list));
        return view;
    }

    private void showCreateItineraryDialog(LinearLayout list) {
        // Calculate trip duration
        String start = trip.getStartDate();
        String end = trip.getEndDate();

        // Simple day calculation (you could make this more sophisticated)
        int defaultDays = 7; // Default to 7 days if calculation fails

        // Create custom dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_itinerary, null);

        com.google.android.material.textfield.TextInputEditText inputDays = dialogView.findViewById(R.id.inputDays);
        TextView destinationText = dialogView.findViewById(R.id.destinationText);

        destinationText.setText(trip.getDestination());
        inputDays.setText(String.valueOf(defaultDays));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();

        // Set dialog background to transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String daysStr = inputDays.getText().toString().trim();
            if (!daysStr.isEmpty()) {
                int days = Integer.parseInt(daysStr);
                if (days > 0 && days <= 365) {
                    trip.generateItineraryTemplate(days);
                    appData.triggerSave();
                    refreshItineraryList(list);
                    Toast.makeText(getContext(), "Created " + days + " day itinerary!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Please enter between 1-365 days", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void showAddActivityDialog(int dayNumber, LinearLayout dayActivitiesList) {
        // Create custom dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_activity, null);

        com.google.android.material.textfield.TextInputEditText inputActivity = dialogView.findViewById(R.id.inputActivity);
        TextView dayNumberText = dialogView.findViewById(R.id.dayNumberText);
        Button btnSelectTime = dialogView.findViewById(R.id.btnSelectTime);

        // Alarm components
        com.google.android.material.switchmaterial.SwitchMaterial switchSetAlarm =
            dialogView.findViewById(R.id.switchSetActivityAlarm);
        LinearLayout alarmSettingsLayout = dialogView.findViewById(R.id.activityAlarmSettingsLayout);
        com.google.android.material.textfield.TextInputEditText inputAlarmMessage =
            dialogView.findViewById(R.id.inputActivityAlarmMessage);

        dayNumberText.setText(getString(R.string.label_day, dayNumber));

        // Store selected time and alarm settings
        final String[] selectedTime = {null};
        final int[] selectedHour = {-1};
        final int[] selectedMinute = {-1};
        final boolean[] alarmEnabled = {false};

        // Alarm switch toggle
        if (switchSetAlarm != null && alarmSettingsLayout != null) {
            switchSetAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarmEnabled[0] = isChecked;
                alarmSettingsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }

        // Time picker click listener
        btnSelectTime.setOnClickListener(v -> {
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // Store 24-hour format for alarm
                    selectedHour[0] = hourOfDay;
                    selectedMinute[0] = minute;

                    // Format time as "HH:MM AM/PM" for display
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int hour12 = hourOfDay % 12;
                    if (hour12 == 0) hour12 = 12;

                    String timeStr = String.format(java.util.Locale.US, "%d:%02d %s", hour12, minute, amPm);
                    selectedTime[0] = timeStr;
                    btnSelectTime.setText(timeStr);
                    btnSelectTime.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_blue));
                },
                9, // Default hour (9 AM)
                0, // Default minute
                false // Use 12-hour format
            );
            timePickerDialog.show();
        });

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();

        // Set dialog background to transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancelActivity).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnAddActivity).setOnClickListener(v -> {
            String time = selectedTime[0] != null ? selectedTime[0] : "All day";
            String activity = inputActivity.getText().toString().trim();

            if (!activity.isEmpty()) {
                trip.addActivityToDay(dayNumber, time, activity);
                appData.triggerSave();
                refreshDayActivities(dayNumber, dayActivitiesList);

                // Schedule alarm if enabled
                if (alarmEnabled[0] && selectedHour[0] >= 0) {
                    String alarmMessage = inputAlarmMessage != null &&
                        inputAlarmMessage.getText() != null &&
                        !inputAlarmMessage.getText().toString().trim().isEmpty() ?
                        inputAlarmMessage.getText().toString().trim() :
                        "Activity: " + activity;

                    scheduleActivityAlarm(dayNumber, selectedHour[0], selectedMinute[0], alarmMessage);
                }

                dialog.dismiss();
                Toast.makeText(getContext(), "Activity added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please enter an activity description", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void refreshItineraryList(LinearLayout list) {
        list.removeAllViews();
        List<AppData.ItineraryDay> days = trip.getItineraryDays();

        if (days.isEmpty()) {
            TextView emptyMsg = new TextView(getContext());
            emptyMsg.setText("Click 'Create Itinerary' to plan your trip day by day");
            emptyMsg.setPadding(20, 30, 20, 30);
            emptyMsg.setTextColor(0xFF888888);
            emptyMsg.setGravity(android.view.Gravity.CENTER);
            list.addView(emptyMsg);
            return;
        }

        for (AppData.ItineraryDay day : days) {
            View dayCard = LayoutInflater.from(getContext())
                .inflate(R.layout.item_itinerary_day, list, false);

            TextView dayNumber = dayCard.findViewById(R.id.dayNumber);
            dayNumber.setText(getString(R.string.label_day, day.getDayNumber()));

            LinearLayout activitiesList = dayCard.findViewById(R.id.activitiesList);
            TextView emptyText = dayCard.findViewById(R.id.emptyDayText);
            android.widget.Button btnAddActivity = dayCard.findViewById(R.id.btnAddActivity);
            ImageButton btnDeleteDay = dayCard.findViewById(R.id.btnDeleteDay);

            // Refresh activities for this day
            refreshDayActivities(day.getDayNumber(), activitiesList);

            // Show/hide empty state
            if (day.hasActivities()) {
                emptyText.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.VISIBLE);
            }

            // Add activity button
            btnAddActivity.setOnClickListener(v -> showAddActivityDialog(day.getDayNumber(), activitiesList));

            // Delete day button with undo
            btnDeleteDay.setOnClickListener(v -> {
                int dayNum = day.getDayNumber();

                // Store the day data before removing
                AppData.ItineraryDay deletedDay = day;

                // Remove the day
                trip.getItineraryDays().remove(day);
                appData.triggerSave();
                refreshItineraryList(list);

                // Show Snackbar with undo option
                com.google.android.material.snackbar.Snackbar snackbar =
                    com.google.android.material.snackbar.Snackbar.make(
                        getView(),
                        "Day " + dayNum + " deleted",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    );

                snackbar.setAction("UNDO", undoView -> {
                    // Re-add the day
                    trip.getItineraryDays().add(deletedDay);
                    // Sort by day number
                    trip.getItineraryDays().sort((d1, d2) -> Integer.compare(d1.getDayNumber(), d2.getDayNumber()));
                    appData.triggerSave();
                    refreshItineraryList(list);
                    Toast.makeText(getContext(), "Day " + dayNum + " restored", Toast.LENGTH_SHORT).show();
                });

                snackbar.setActionTextColor(ContextCompat.getColor(getContext(), R.color.accent_orange));
                snackbar.show();
            });

            list.addView(dayCard);
        }
    }

    private void refreshDayActivities(int dayNumber, LinearLayout activitiesList) {
        activitiesList.removeAllViews();

        AppData.ItineraryDay day = trip.getDay(dayNumber);
        if (day == null) return;

        List<AppData.DayActivity> activities = day.getActivities();
        for (AppData.DayActivity activity : activities) {
            View activityView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_day_activity, activitiesList, false);

            TextView timeView = activityView.findViewById(R.id.activityTime);
            TextView descView = activityView.findViewById(R.id.activityDescription);

            timeView.setText(activity.getTime());
            descView.setText(activity.getActivity());

            activitiesList.addView(activityView);
        }

        // Update parent visibility
        View parent = (View) activitiesList.getParent();
        if (parent != null) {
            TextView emptyText = parent.findViewById(R.id.emptyDayText);
            if (emptyText != null) {
                emptyText.setVisibility(activities.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }

    private View showDiary() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_diary_section, contentLayout, false);
        contentLayout.addView(view);

        LinearLayout diaryList = view.findViewById(R.id.diaryList);
        Button btnAddDiary = view.findViewById(R.id.btnAddDiary);

        refreshDiaryList(diaryList);

        btnAddDiary.setOnClickListener(v -> showAddDiaryDialog(diaryList));
        return view;
    }

    private void showAddDiaryDialog(LinearLayout diaryList) {
        // Create custom dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_diary, null);

        com.google.android.material.textfield.TextInputEditText inputDiary = dialogView.findViewById(R.id.inputDiaryEntry);
        TextView dateTimeText = dialogView.findViewById(R.id.currentDateTime);

        // Store reference to current input for speech recognition
        currentDiaryInput = inputDiary;

        // Set current date and time
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE, MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault());
        dateTimeText.setText(dateFormat.format(new java.util.Date()));

        // Setup microphone button
        com.google.android.material.textfield.TextInputLayout inputLayout =
            (com.google.android.material.textfield.TextInputLayout) inputDiary.getParent().getParent();
        if (inputLayout != null) {
            inputLayout.setEndIconOnClickListener(v -> startSpeechToText());
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getContext())
            .setView(dialogView)
            .create();

        // Set dialog background to transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancelDiary).setOnClickListener(v -> {
            currentDiaryInput = null;
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnSaveDiary).setOnClickListener(v -> {
            String text = inputDiary.getText().toString().trim();
            if (!text.isEmpty()) {
                trip.addDiaryEntry(text);
                appData.triggerSave();
                refreshDiaryList(diaryList);
                currentDiaryInput = null;
                dialog.dismiss();
                Toast.makeText(getContext(), "Diary entry added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Please write something", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your travel story...");

        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshDiaryList(LinearLayout diaryList) {
        if (diaryList == null || getContext() == null || !isAdded()) return;

        // Clear existing views to prevent memory leak
        for (int i = 0; i < diaryList.getChildCount(); i++) {
            View child = diaryList.getChildAt(i);
            if (child != null) {
                child.setBackground(null);
            }
        }
        diaryList.removeAllViews();

        List<String> diaryEntries = trip.getDiaryEntries();
        if (diaryEntries == null || diaryEntries.isEmpty()) {
            TextView emptyMsg = new TextView(getContext());
            emptyMsg.setText("No diary entries yet. Tap 'Add Diary Entry' to start!");
            emptyMsg.setTextSize(16);
            emptyMsg.setPadding(30, 50, 30, 50);
            emptyMsg.setGravity(android.view.Gravity.CENTER);
            emptyMsg.setTextColor(0xFF888888);
            diaryList.addView(emptyMsg);
            return;
        }

        for (String entry : diaryEntries) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + entry);
            tv.setTextSize(15);
            tv.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary_light));
            tv.setPadding(20, 15, 20, 15);
            diaryList.addView(tv);
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            trip.addPhoto(uri.toString());
                            appData.triggerSave();
                            if (photoContainer != null) refreshPhotoList();
                        }
                    }
                }
        );
    }

    private View showPhotos() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_photos_section, contentLayout, false);
        contentLayout.addView(view);

        photoContainer = view.findViewById(R.id.photoList);
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);

        refreshPhotoList();

        btnAddPhoto.setOnClickListener(v -> openGallery());
        return view;
    }

    private void refreshPhotoList() {
        if (photoContainer == null || getContext() == null) return;

        photoContainer.removeAllViews();
        List<String> photos = trip.getPhotos();

        if (photos == null || photos.isEmpty()) {
            TextView emptyText = new TextView(getContext());
            emptyText.setText("No photos added yet");
            emptyText.setTextSize(16);
            emptyText.setPadding(30, 50, 30, 50);
            emptyText.setGravity(android.view.Gravity.CENTER);
            photoContainer.addView(emptyText);
            return;
        }

        for (String uriStr : photos) {
            if (getContext() == null || !isAdded()) break;

            ImageView img = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500
            );
            params.setMargins(0, 10, 0, 10);
            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            try {
                Object imageSource = uriStr.startsWith("content://") ?
                    Uri.parse(uriStr) : uriStr;

                Glide.with(requireContext().getApplicationContext())
                    .load(imageSource)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.AUTOMATIC)
                    .into(img);
            } catch (Exception e) {
                // Handle Glide exceptions
                img.setImageResource(R.drawable.ic_image_placeholder);
            }
            photoContainer.addView(img);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause Glide requests when fragment is not visible
        try {
            if (getContext() != null) {
                Glide.with(this).pauseRequests();
            }
        } catch (Exception e) {
            // Ignore if already paused
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume Glide requests when fragment is visible again
        try {
            if (getContext() != null) {
                Glide.with(this).resumeRequests();
            }
        } catch (Exception e) {
            // Ignore if context is null
        }
    }

    private void resetTabStyles(Button button) {
        if (getContext() == null || button == null) return;

        button.setBackgroundResource(R.drawable.tab_background_selector);
        button.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.tab_text_color_selector));

        // Get drawable icon (position [1] is 'top')
        Drawable[] icons = button.getCompoundDrawables();
        if (icons[1] != null) {
            Drawable icon = DrawableCompat.wrap(icons[1]);
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.tab_text_color_selector));
        }
    }

    private void setTabSelected(Button button) {
        if (getContext() == null || button == null) return;

        button.setBackgroundResource(R.drawable.tab_selected_background);
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

        Drawable[] icons = button.getCompoundDrawables();
        if (icons[1] != null) {
            Drawable icon = DrawableCompat.wrap(icons[1]);
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.white));
        }
    }

    private void scheduleActivityAlarm(int dayNumber, int hour, int minute, String message) {
        try {
            if (trip == null || trip.getStartDate() == null) return;

            // Parse trip start date
            String[] dateParts = trip.getStartDate().split("-");
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.set(Calendar.YEAR, Integer.parseInt(dateParts[0]));
            alarmCalendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            alarmCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[2]));

            // Add days to get to the activity day
            alarmCalendar.add(Calendar.DAY_OF_MONTH, dayNumber - 1);

            // Set the time
            alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
            alarmCalendar.set(Calendar.MINUTE, minute);
            alarmCalendar.set(Calendar.SECOND, 0);

            long alarmTimeMillis = alarmCalendar.getTimeInMillis();

            // Only schedule if in the future
            if (alarmTimeMillis > System.currentTimeMillis()) {
                String alarmId = trip.getId() + "_day" + dayNumber + "_" + hour + minute;
                com.example.mytraveldiary.utils.helpers.AlarmHelper.scheduleCustomTripReminder(
                    requireContext(), alarmId, message, alarmTimeMillis);
                Toast.makeText(getContext(), "Reminder set for activity", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Activity time is in the past", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to set reminder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Clear Glide to prevent memory leaks
        try {
            if (getContext() != null) {
                Glide.with(getContext()).pauseRequests();
                Glide.get(getContext()).clearMemory();
            }
        } catch (Exception e) {
            // Ignore if context is null
        }

        // Cancel any ongoing animations
        if (contentLayout != null) {
            for (int i = 0; i < contentLayout.getChildCount(); i++) {
                View child = contentLayout.getChildAt(i);
                if (child != null) {
                    child.animate().cancel();
                    child.clearAnimation();
                }
            }
            contentLayout.removeAllViews();
            contentLayout = null;
        }

        // Clear chart to prevent memory leaks
        if (pieChart != null) {
            pieChart.clear();
            pieChart.setData(null);
            pieChart.invalidate();
            pieChart = null;
        }

        // Clear photo container
        if (photoContainer != null) {
            photoContainer.removeAllViews();
            photoContainer = null;
        }

        // Clear expenses container
        if (expensesContainer != null) {
            expensesContainer.removeAllViews();
            expensesContainer = null;
        }

        // Null out all references to prevent memory leaks
        totalText = null;
        btnAddExpense = null;
        tabItinerary = null;
        tabExpenses = null;
        tabDiary = null;
        tabPhotos = null;
        trip = null;
        appData = null;
        imagePickerLauncher = null;
    }

}