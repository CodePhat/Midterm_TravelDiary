package com.example.mytraveldiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private PieChart pieChart;
    private TextView totalTripsText, totalSpentText, avgPerTripText;
    private RecyclerView recentTripsRecycler;
    private TextView noRecentTripsText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView welcomeText = root.findViewById(R.id.welcomeText);
        pieChart = root.findViewById(R.id.pieChart);
        totalTripsText = root.findViewById(R.id.totalTripsText);
        totalSpentText = root.findViewById(R.id.totalSpentText);
        avgPerTripText = root.findViewById(R.id.avgPerTripText);
        recentTripsRecycler = root.findViewById(R.id.recentTripsRecycler);
        noRecentTripsText = root.findViewById(R.id.noRecentTripsText);

        // Get data from singleton
        AppData data = AppData.getInstance();
        String name = "Traveler";
        UserProfile profile = data.getProfile();
        if (profile != null && profile.getName() != null && !profile.getName().trim().isEmpty()) {
            name = profile.getName();
        }
        welcomeText.setText("Welcome back, " + name + "!");

        // Setup statistics
        setupStatistics(data);

        // Setup recent trips
        setupRecentTrips(data);

        // Setup chart
        setupChart(data.getExpenseData());

        return root;
    }

    private void setupStatistics(AppData data) {
        // You'll need to add these methods to your AppData class
        int totalTrips = data.getTotalTrips(); // Add this method
        float totalSpent = calculateTotal(data.getExpenseData());
        float avgPerTrip = totalTrips > 0 ? totalSpent / totalTrips : 0;

        totalTripsText.setText(String.valueOf(totalTrips));
        totalSpentText.setText(String.format("$%.0f", totalSpent));
        avgPerTripText.setText(String.format("$%.0f", avgPerTrip));
    }

    private float calculateTotal(Map<String, Float> expenseData) {
        if (expenseData == null || expenseData.isEmpty()) return 0;
        float total = 0;
        for (Float value : expenseData.values()) {
            total += value;
        }
        return total;
    }

    private void setupChart(Map<String, Float> expenseData) {
        if (expenseData == null || expenseData.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data available");
            pieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : expenseData.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Enhanced color palette
        int[] colorIds = {
                R.color.teal_700, R.color.orange, R.color.blue,
                R.color.pink, R.color.purple, R.color.yellow
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for (int id : colorIds) {
            colors.add(ContextCompat.getColor(requireContext(), id));
        }
        dataSet.setColors(colors);

        // Enhanced text styling
        dataSet.setValueTextSize(13f);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Calculate total for center text
        float total = calculateTotal(expenseData);
        pieChart.setCenterText(String.format("$%.0f\nTotal", total));
        pieChart.setCenterTextSize(18f);
        pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        // Enhanced chart appearance
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);

        // Rotation and interaction
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Legend configuration
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setFormSize(10f);
        legend.setXEntrySpace(12f);
        legend.setYEntrySpace(8f);

        // Animate the chart
        pieChart.animateY(1200, Easing.EaseInOutQuad);

        pieChart.invalidate();
    }

    private void setupRecentTrips(AppData data) {
        List<AppData.Trip> allTrips = data.getTrips();

        if (allTrips.isEmpty()) {
            recentTripsRecycler.setVisibility(View.GONE);
            noRecentTripsText.setVisibility(View.VISIBLE);
            return;
        }

        recentTripsRecycler.setVisibility(View.VISIBLE);
        noRecentTripsText.setVisibility(View.GONE);

        // Get up to 3 most recent trips
        List<AppData.Trip> recentTrips = allTrips.subList(0, Math.min(3, allTrips.size()));

        recentTripsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        TripAdapter adapter = new TripAdapter(recentTrips, requireActivity());
        recentTripsRecycler.setAdapter(adapter);
    }
}