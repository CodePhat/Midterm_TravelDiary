package com.example.mytraveldiary;

import android.app.Dialog;
import android.content.Context;
import android.widget.*;
import java.util.*;

public class AddExpenseDialog extends Dialog {

    public AddExpenseDialog(Context ctx, String tripId, Runnable onSave) {
        super(ctx);
        setContentView(R.layout.dialog_add_expense);

        AppData data = AppData.getInstance();

        EditText desc = findViewById(R.id.expenseDesc);
        EditText amount = findViewById(R.id.expenseAmount);
        Spinner category = findViewById(R.id.expenseCategory);
        category.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, ExpenseCategory.values()));

        Button save = findViewById(R.id.btnSave);
        Button cancel = findViewById(R.id.btnCancel);

        save.setOnClickListener(v -> {
            String d = desc.getText().toString().trim();
            String amountStr = amount.getText().toString().trim();
            if (d.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(ctx, "Please fill All Fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double a;
            try {
                a = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(ctx, "Invalid Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            ExpenseCategory cat = (ExpenseCategory) category.getSelectedItem();
            data.addExpense(tripId, new Expense(UUID.randomUUID().toString(), d, a, cat, new Date()));

            onSave.run();
            dismiss();
        });

        cancel.setOnClickListener(v -> dismiss());
    }
}