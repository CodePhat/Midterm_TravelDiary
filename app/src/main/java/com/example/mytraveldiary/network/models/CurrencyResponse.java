package com.example.mytraveldiary.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Currency exchange rate API response model
 */
public class CurrencyResponse {
    @SerializedName("base")
    private String base;

    @SerializedName("rates")
    private Map<String, Double> rates;

    @SerializedName("date")
    private String date;

    public String getBase() {
        return base;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public String getDate() {
        return date;
    }
}
