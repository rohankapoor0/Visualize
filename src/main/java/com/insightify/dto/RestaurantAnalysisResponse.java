package com.insightify.dto;

import java.util.List;
import java.util.Map;

/**
 * Top-level response for the Restaurant Analytics Dashboard.
 * Contains all restaurant-specific KPIs, item performance rankings,
 * monthly analysis, trend detection, charts, insights, and menu recommendations.
 *
 * Replaces the generic AiDashboardResponse for restaurant analytics.
 */
public class RestaurantAnalysisResponse {

    // ── Restaurant KPIs ──
    private Map<String, Object> kpis;

    // ── Item Performance Rankings ──
    private List<ItemPerformance> topItems;
    private List<ItemPerformance> leastSellingItems;

    // ── Monthly Analysis ──
    private List<MonthlyBreakdown> monthlyAnalysis;

    // ── Month-over-Month Comparison ──
    private Map<String, Object> monthOverMonth;

    // ── Trend Analysis ──
    private Map<String, Object> trend;

    // ── Most Profitable Month ──
    private Map<String, Object> mostProfitableMonth;

    // ── Profit Optimization ──
    private List<String> profitOptimization;

    // ── Menu Optimization ──
    private List<MenuRecommendation> menuRecommendations;

    // ── Business Insights ──
    private List<String> insights;

    // ── Chart Sections (for frontend rendering) ──
    private List<DashboardSection> sections;

    public RestaurantAnalysisResponse() {}

    // --- Getters & Setters ---

    public Map<String, Object> getKpis() { return kpis; }
    public void setKpis(Map<String, Object> kpis) { this.kpis = kpis; }

    public List<ItemPerformance> getTopItems() { return topItems; }
    public void setTopItems(List<ItemPerformance> topItems) { this.topItems = topItems; }

    public List<ItemPerformance> getLeastSellingItems() { return leastSellingItems; }
    public void setLeastSellingItems(List<ItemPerformance> leastSellingItems) { this.leastSellingItems = leastSellingItems; }

    public List<MonthlyBreakdown> getMonthlyAnalysis() { return monthlyAnalysis; }
    public void setMonthlyAnalysis(List<MonthlyBreakdown> monthlyAnalysis) { this.monthlyAnalysis = monthlyAnalysis; }

    public Map<String, Object> getMonthOverMonth() { return monthOverMonth; }
    public void setMonthOverMonth(Map<String, Object> monthOverMonth) { this.monthOverMonth = monthOverMonth; }

    public Map<String, Object> getTrend() { return trend; }
    public void setTrend(Map<String, Object> trend) { this.trend = trend; }

    public Map<String, Object> getMostProfitableMonth() { return mostProfitableMonth; }
    public void setMostProfitableMonth(Map<String, Object> mostProfitableMonth) { this.mostProfitableMonth = mostProfitableMonth; }

    public List<String> getProfitOptimization() { return profitOptimization; }
    public void setProfitOptimization(List<String> profitOptimization) { this.profitOptimization = profitOptimization; }

    public List<MenuRecommendation> getMenuRecommendations() { return menuRecommendations; }
    public void setMenuRecommendations(List<MenuRecommendation> menuRecommendations) { this.menuRecommendations = menuRecommendations; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }

    public List<DashboardSection> getSections() { return sections; }
    public void setSections(List<DashboardSection> sections) { this.sections = sections; }
}
