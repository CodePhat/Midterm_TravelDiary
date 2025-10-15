package com.example.mytraveldiary;

import java.util.Date;

public class Expense {
    private final String id;
    private final String description;
    private final double amount;
    private final ExpenseCategory category;
    private final Date date;

    public Expense(String id, String description, double amount, ExpenseCategory category, Date date) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public ExpenseCategory getCategory() { return category; }
    public Date getDate() { return date; }
}
