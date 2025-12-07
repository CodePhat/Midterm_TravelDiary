package com.example.mytraveldiary.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.example.mytraveldiary.R;
import com.example.mytraveldiary.data.repository.AppData;
import com.example.mytraveldiary.data.models.Expense;
import com.example.mytraveldiary.data.models.ExpenseCategory;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class AddExpenseDialog extends Dialog {

    private Calendar selectedDateTime = Calendar.getInstance();
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

    public AddExpenseDialog(Context ctx, String tripId, Runnable onSave) {
        super(ctx);
        setContentView(R.layout.dialog_add_expense);

        // Make dialog wider
        if (getWindow() != null) {
            getWindow().setLayout(
                (int)(ctx.getResources().getDisplayMetrics().widthPixels * 0.95), // 95% of screen width
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        AppData data = AppData.getInstance();

        // Find views
        TextInputEditText desc = findViewById(R.id.expenseDesc);
        TextInputEditText amount = findViewById(R.id.expenseAmount);
        TextInputEditText dateField = findViewById(R.id.expenseDate);
        ChipGroup chipGroupCategory = findViewById(R.id.chipGroupExpenseCategory);
        ImageButton btnClose = findViewById(R.id.btnCloseDialog);
        Button save = findViewById(R.id.btnSave);
        Button cancel = findViewById(R.id.btnCancel);

        // Set default date to now
        dateField.setText(dateTimeFormat.format(selectedDateTime.getTime()));

        // Setup date and time picker
        dateField.setOnClickListener(v -> {
            // First show date picker
            DatePickerDialog datePicker = new DatePickerDialog(
                ctx,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Then show time picker
                    TimePickerDialog timePicker = new TimePickerDialog(
                        ctx,
                        (timeView, hourOfDay, minute) -> {
                            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedDateTime.set(Calendar.MINUTE, minute);
                            dateField.setText(dateTimeFormat.format(selectedDateTime.getTime()));
                        },
                        selectedDateTime.get(Calendar.HOUR_OF_DAY),
                        selectedDateTime.get(Calendar.MINUTE),
                        false // 12-hour format
                    );
                    timePicker.show();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Select first category by default (Food)
        Chip chipFood = findViewById(R.id.chipFood);
        chipFood.setChecked(true);

        // Close button
        btnClose.setOnClickListener(v -> dismiss());

        // Save button
        save.setOnClickListener(v -> {
            String d = desc.getText().toString().trim();
            String amountStr = amount.getText().toString().trim();

            if (d.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(ctx, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if a category is selected
            int selectedChipId = chipGroupCategory.getCheckedChipId();
            if (selectedChipId == -1) {
                Toast.makeText(ctx, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            double a;
            try {
                a = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(ctx, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Map chip ID to category
            ExpenseCategory cat = ExpenseCategory.Other;
            if (selectedChipId == R.id.chipFood) {
                cat = ExpenseCategory.Food;
            } else if (selectedChipId == R.id.chipTransport) {
                cat = ExpenseCategory.Transport;
            } else if (selectedChipId == R.id.chipAccommodation) {
                cat = ExpenseCategory.Accommodation;
            } else if (selectedChipId == R.id.chipEntertainment) {
                cat = ExpenseCategory.Entertainment;
            } else if (selectedChipId == R.id.chipShopping) {
                cat = ExpenseCategory.Shopping;
            } else if (selectedChipId == R.id.chipOther) {
                cat = ExpenseCategory.Other;
            }

            data.addExpense(tripId, new Expense(UUID.randomUUID().toString(), d, a, cat, selectedDateTime.getTime()));

            onSave.run();
            dismiss();
        });

        cancel.setOnClickListener(v -> dismiss());
    }
}