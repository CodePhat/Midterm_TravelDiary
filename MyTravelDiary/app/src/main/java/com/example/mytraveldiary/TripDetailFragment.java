package com.example.mytraveldiary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

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

        tripTitle.setText(trip.getDestination());
        tripDates.setText(trip.getDateRange());

        if (trip.getImage() != null && !trip.getImage().isEmpty()) {
            Glide.with(this).load(trip.getImage()).into(tripImage);
        }

        contentLayout = root.findViewById(R.id.contentLayout);

        tabItinerary = root.findViewById(R.id.tabItinerary);
        tabExpenses = root.findViewById(R.id.tabExpenses);
        tabDiary = root.findViewById(R.id.tabDiary);
        tabPhotos = root.findViewById(R.id.tabPhotos);


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

        refreshExpenses();
        return expensesView;
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
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            if (e.getValue() > 0)
                entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

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

        EditText inputDays = new EditText(getContext());
        inputDays.setHint("Trip Duration (days)");
        inputDays.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputDays.setText(String.valueOf(defaultDays));

        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Create Itinerary Plan")
            .setMessage("Destination: " + trip.getDestination() + "\nHow many days will this trip last?")
            .setView(inputDays)
            .setPositiveButton("Create", (d, w) -> {
                String daysStr = inputDays.getText().toString().trim();
                if (!daysStr.isEmpty()) {
                    int days = Integer.parseInt(daysStr);
                    if (days > 0 && days <= 365) {
                        trip.generateItineraryTemplate(days);
                        appData.triggerSave();
                        refreshItineraryList(list);
                        Toast.makeText(getContext(), "Created " + days + " day itinerary!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Please enter between 1-365 days", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showAddActivityDialog(int dayNumber, LinearLayout dayActivitiesList) {
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(40, 20, 40, 20);

        // Time Picker Button
        Button btnSelectTime = new Button(getContext());
        btnSelectTime.setText("Select Time");
        btnSelectTime.setAllCaps(false);
        btnSelectTime.setBackgroundColor(0xFFE0E0E0);
        btnSelectTime.setTextColor(0xFF000000);
        dialogLayout.addView(btnSelectTime);

        // Activity description input
        EditText inputActivity = new EditText(getContext());
        inputActivity.setHint("Activity description");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 0);
        inputActivity.setLayoutParams(params);
        dialogLayout.addView(inputActivity);

        // Store selected time
        final String[] selectedTime = {null};

        // Time picker click listener
        btnSelectTime.setOnClickListener(v -> {
            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    // Format time as "HH:MM AM/PM"
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int hour12 = hourOfDay % 12;
                    if (hour12 == 0) hour12 = 12;

                    String timeStr = String.format("%d:%02d %s", hour12, minute, amPm);
                    selectedTime[0] = timeStr;
                    btnSelectTime.setText(timeStr);
                    btnSelectTime.setTextColor(0xFF16A085); // Green color to show it's selected
                },
                9, // Default hour (9 AM)
                0, // Default minute
                false // Use 12-hour format
            );
            timePickerDialog.show();
        });

        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Add Activity to Day " + dayNumber)
            .setView(dialogLayout)
            .setPositiveButton("Add", (d, w) -> {
                String time = selectedTime[0] != null ? selectedTime[0] : "All day";
                String activity = inputActivity.getText().toString().trim();

                if (!activity.isEmpty()) {
                    trip.addActivityToDay(dayNumber, time, activity);
                    appData.triggerSave();
                    refreshDayActivities(dayNumber, dayActivitiesList);
                } else {
                    Toast.makeText(getContext(), "Please enter an activity description", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
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
            dayNumber.setText(String.format("Day %02d", day.getDayNumber()));

            LinearLayout activitiesList = dayCard.findViewById(R.id.activitiesList);
            TextView emptyText = dayCard.findViewById(R.id.emptyDayText);
            android.widget.Button btnAddActivity = dayCard.findViewById(R.id.btnAddActivity);

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

        btnAddDiary.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setHint("Write your travel story...");

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Add Diary Entry")
                    .setView(input)
                    .setPositiveButton("Save", (d, w) -> {
                        String text = input.getText().toString().trim();
                        if (!text.isEmpty()) {
                            trip.addDiaryEntry(text);
                            appData.triggerSave();
                            refreshDiaryList(diaryList);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        return view;
    }

    private void refreshDiaryList(LinearLayout diaryList) {
        diaryList.removeAllViews();
        List<String> diaryEntries = trip.getDiaryEntries();

        for (String entry : diaryEntries) {
            TextView tv = new TextView(getContext());
            tv.setText("• " + entry);
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
        photoContainer.removeAllViews();
        List<String> photos = trip.getPhotos();

        for (String uriStr : photos) {
            ImageView img = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500
            );
            params.setMargins(0, 10, 0, 10);
            img.setLayoutParams(params);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(Uri.parse(uriStr)).into(img);
            photoContainer.addView(img);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
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

}