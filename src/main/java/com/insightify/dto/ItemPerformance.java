package com.insightify.dto;

/**
 * Per-item performance metrics for restaurant analysis.
 * Tracks sales volume, revenue, profit, and contribution percentage.
 */
public class ItemPerformance {

    private String itemName;
    private String category;
    private int quantitySold;
    private double revenue;
    private double profit;
    private double revenueContributionPct;
    private double profitMarginPct;

    public ItemPerformance() {}

    public ItemPerformance(String itemName, String category, int quantitySold,
                           double revenue, double profit,
                           double revenueContributionPct, double profitMarginPct) {
        this.itemName = itemName;
        this.category = category;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
        this.profit = profit;
        this.revenueContributionPct = revenueContributionPct;
        this.profitMarginPct = profitMarginPct;
    }

    // --- Getters & Setters ---

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantitySold() { return quantitySold; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public double getProfit() { return profit; }
    public void setProfit(double profit) { this.profit = profit; }

    public double getRevenueContributionPct() { return revenueContributionPct; }
    public void setRevenueContributionPct(double revenueContributionPct) { this.revenueContributionPct = revenueContributionPct; }

    public double getProfitMarginPct() { return profitMarginPct; }
    public void setProfitMarginPct(double profitMarginPct) { this.profitMarginPct = profitMarginPct; }
}
