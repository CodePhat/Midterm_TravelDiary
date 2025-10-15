package com.example.mytraveldiary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.bumptech.glide.Glide;
import java.util.*;

public class TripDetailFragment extends Fragment {

    private AppData.Trip trip;
    private AppData appData;
    private LinearLayout contentLayout;
    private String activeTab = "expenses"; // Default tab
    private PieChart pieChart;
    private TextView totalText;
    private Button btnAddExpense;
    private LinearLayout expensesContainer;

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

        // --- UI Bindings ---
        ImageView tripImage = root.findViewById(R.id.tripImage);
        TextView tripTitle = root.findViewById(R.id.tripTitle);
        TextView tripDates = root.findViewById(R.id.tripDates);

        tripTitle.setText(trip.getDestination());
        tripDates.setText(trip.getDateRange());

        if (trip.getImage() != null && !trip.getImage().isEmpty()) {
            Glide.with(this).load(trip.getImage()).into(tripImage);
        }

        contentLayout = root.findViewById(R.id.contentLayout);
        pieChart = root.findViewById(R.id.expenseChart);
        totalText = root.findViewById(R.id.totalExpenses);
        expensesContainer = root.findViewById(R.id.expenseList);
        btnAddExpense = root.findViewById(R.id.btnAddExpense);

        btnAddExpense.setOnClickListener(v ->
                new AddExpenseDialog(getContext(), trip.getId(), this::refreshExpenses).show()
        );

        setupTabs(root);
        refreshTab();

        return root;
    }


    private void setupTabs(View root) {
        Button tabItinerary = root.findViewById(R.id.tabItinerary);
        Button tabExpenses = root.findViewById(R.id.tabExpenses);
        Button tabDiary = root.findViewById(R.id.tabDiary);
        Button tabPhotos = root.findViewById(R.id.tabPhotos);

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
        contentLayout.removeAllViews();
        switch (activeTab) {
            case "expenses":
                showExpenses();
                break;
            case "itinerary":
                showPlaceholder("🗺️ Itinerary feature coming soon!");
                break;
            case "diary":
                showPlaceholder("📖 Diary feature coming soon!");
                break;
            case "photos":
                showPlaceholder("📷 Photo gallery coming soon!");
                break;
        }
    }

    private void showPlaceholder(String msg) {
        TextView text = new TextView(getContext());
        text.setText(msg);
        text.setTextSize(16);
        text.setPadding(30, 50, 30, 50);
        contentLayout.addView(text);
    }

    private void showExpenses() {
        contentLayout.removeAllViews(); // clear previous tab content

        // Inflate the expense layout
        View expensesView = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_expenses_section, contentLayout, false);
        contentLayout.addView(expensesView);

        // Bind views
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

            ((TextView) item.findViewById(R.id.expenseDesc))
                    .setText(exp.getDescription());
            ((TextView) item.findViewById(R.id.expenseCategory))
                    .setText(exp.getCategory().name());
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
        dataSet.setValueTextColor(Color.WHITE);
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
}
