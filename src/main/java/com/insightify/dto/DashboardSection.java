package com.insightify.dto;

/**
 * Represents a logical section in the dashboard (e.g., Distribution, Trend).
 */
public class DashboardSection {
    
    private String title;
    private Object chart; // Generic holding Chart config metadata
    private String summary;
    private String businessImpact;

    public DashboardSection() {}

    public DashboardSection(String title, Object chart, String summary, String businessImpact) {
        this.title = title;
        this.chart = chart;
        this.summary = summary;
        this.businessImpact = businessImpact;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Object getChart() { return chart; }
    public void setChart(Object chart) { this.chart = chart; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getBusinessImpact() { return businessImpact; }
    public void setBusinessImpact(String businessImpact) { this.businessImpact = businessImpact; }
}
