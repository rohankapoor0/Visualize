package com.insightify.dto;

/**
 * Monthly aggregation for restaurant performance tracking.
 * Groups revenue, profit, and sales volume by calendar month.
 */
public class MonthlyBreakdown {

    private String month;          // e.g. "2024-01", "Jan 2024"
    private double revenue;
    private double profit;
    private int itemsSold;

    public MonthlyBreakdown() {}

    public MonthlyBreakdown(String month, double revenue, double profit, int itemsSold) {
        this.month = month;
        this.revenue = revenue;
        this.profit = profit;
        this.itemsSold = itemsSold;
    }

    // --- Getters & Setters ---

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }

    public int getItemsSold() { return itemsSold; }
    public void setItemsSold(int itemsSold) { this.itemsSold = itemsSold; }
}
