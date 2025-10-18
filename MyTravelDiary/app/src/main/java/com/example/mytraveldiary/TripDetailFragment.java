package com.example.mytraveldiary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable; // <-- THÊM IMPORT NÀY
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // <-- THÊM IMPORT NÀY
import androidx.core.graphics.drawable.DrawableCompat; // <-- THÊM IMPORT NÀY
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
    private String activeTab = "expenses"; // Mặc định là expenses
    private PieChart pieChart;
    private TextView totalText;
    private Button btnAddExpense;
    private LinearLayout expensesContainer;
    private Button tabItinerary, tabExpenses, tabDiary, tabPhotos; // <-- THÊM CÁC BIẾN NÀY
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
        activeTab = tab;
        refreshTab();
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
        contentLayout.removeAllViews(); // Dòng này giữ nguyên
        switch (activeTab) {
            case "expenses":
                showExpenses();
                break;
            case "itinerary":
                showItinerary();
                break;
            case "diary":
                showDiary();
                break;
            case "photos":
                showPhotos();
                break;
        }
    }
    private void showExpenses() {
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

    private void showItinerary() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_itinerary_section, contentLayout, false);
        contentLayout.addView(view);

        LinearLayout list = view.findViewById(R.id.itineraryList);
        Button btnAdd = view.findViewById(R.id.btnAddDay);

        refreshItineraryList(list); // ✅ initial load

        btnAdd.setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            input.setHint("Enter plan for the day");

            new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Add Itinerary Day")
                    .setView(input)
                    .setPositiveButton("Add", (d, w) -> {
                        String plan = input.getText().toString().trim();
                        if (!plan.isEmpty()) {
                            trip.addItinerary(plan);
                            refreshItineraryList(list); // ✅ instant refresh
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void refreshItineraryList(LinearLayout list) {
        list.removeAllViews();
        List<String> itinerary = trip.getItinerary();

        for (int i = 0; i < itinerary.size(); i++) {
            TextView tv = new TextView(getContext());
            tv.setText("Day " + (i + 1) + ": " + itinerary.get(i));
            tv.setPadding(20, 15, 20, 15);
            list.addView(tv);
        }
    }

    private void showDiary() {
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
                            refreshDiaryList(diaryList); // ✅ instant refresh
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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
                            if (photoContainer != null) refreshPhotoList(); // ✅ instant update
                        }
                    }
                }
        );
    }

    private void showPhotos() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_photos_section, contentLayout, false);
        contentLayout.addView(view);

        photoContainer = view.findViewById(R.id.photoList);
        Button btnAddPhoto = view.findViewById(R.id.btnAddPhoto);

        refreshPhotoList();

        btnAddPhoto.setOnClickListener(v -> openGallery());
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

        // Lấy icon (vị trí [1] là 'top')
        Drawable[] icons = button.getCompoundDrawables();
        if (icons[1] != null) {
            Drawable icon = DrawableCompat.wrap(icons[1]); // Bọc icon
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.tab_text_color_selector)); // Set màu
        }
    }

    private void setTabSelected(Button button) {
        if (getContext() == null || button == null) return;

        button.setBackgroundResource(R.drawable.tab_selected_background); // Nền xanh đậm
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white)); // Chữ trắng

        Drawable[] icons = button.getCompoundDrawables();
        if (icons[1] != null) {
            Drawable icon = DrawableCompat.wrap(icons[1]); // Bọc icon
            DrawableCompat.setTintList(icon, ContextCompat.getColorStateList(getContext(), R.color.white)); // Set màu
        }
    }

}