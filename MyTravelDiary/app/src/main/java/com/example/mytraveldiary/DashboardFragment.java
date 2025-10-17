package com.example.mytraveldiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private PieChart pieChart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView welcomeText = root.findViewById(R.id.welcomeText);
        pieChart = root.findViewById(R.id.pieChart);

        // Get data from singleton
        AppData data = AppData.getInstance();
        String name = data.getProfile().getName();
        if (name == null || name.trim().isEmpty()) name = "Traveler";
        welcomeText.setText("Welcome back, " + name + "!");

        setupChart(data.getExpenseData());

        return root;
    }

    private void setupChart(Map<String, Float> expenseData) {
        if (expenseData == null || expenseData.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("No expense data available");
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : expenseData.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenses Summary");

        // Safer way to load color resources
        int[] colorIds = {
                R.color.teal_700, R.color.orange, R.color.blue,
                R.color.pink, R.color.purple, R.color.yellow
        };
        ArrayList<Integer> colors = new ArrayList<>();
        for (int id : colorIds) {
            colors.add(ContextCompat.getColor(requireContext(), id));
        }
        dataSet.setColors(colors);

        // Make chart readable
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize chart appearance
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(16f);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);

        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.invalidate();
    }
}