package com.insightify.service;

import com.insightify.dto.ItemPerformance;
import com.insightify.dto.MonthlyBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Restaurant-specific chart data generator.
 * Produces:
 *   1. Bar chart → Item performance (top items by revenue)
 *   2. Pie chart → Revenue contribution per item
 *   3. Line chart → Monthly revenue/profit trend
 * All charts are JSON-ready for Recharts frontend rendering.
 */
@Service
public class ChartService {

    private static final Logger log = LoggerFactory.getLogger(ChartService.class);

    private final AggregationService agg;

    public ChartService(AggregationService aggregationService) {
        this.agg = aggregationService;
    }

    /**
     * Generate all restaurant-specific charts.
     */
    public List<Map<String, Object>> generateCharts(List<Map<String, String>> rows,
                                                     List<Map<String, String>> columns) {
        List<Map<String, Object>> charts = new ArrayList<>();

        List<ItemPerformance> items = agg.aggregateItemPerformance(rows, columns);
        List<MonthlyBreakdown> monthly = agg.aggregateByMonthRestaurant(rows, columns);

        if (items.isEmpty()) {
            log.warn("No item performance data — cannot generate restaurant charts");
            return charts;
        }

        // 1. BAR CHART — Item Performance (Top 10 by Revenue)
        charts.add(generateItemPerformanceBar(items));

        // 2. PIE CHART — Revenue Contribution
        charts.add(generateRevenueContributionPie(items));

        log.info("Generated {} restaurant charts for dataset ({} rows, {} items, {} months)",
                charts.size(), rows.size(), items.size(), monthly.size());
        return charts;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BAR CHART — Item Performance
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Horizontal bar chart: Top 10 items by total revenue.
     */
    private Map<String, Object> generateItemPerformanceBar(List<ItemPerformance> items) {
        List<ItemPerformance> top10 = items.stream().limit(10).toList();

        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "bar");
        chart.put("title", "Top Menu Items by Revenue");
        chart.put("labels", top10.stream().map(ItemPerformance::getItemName).toList());
        chart.put("data", top10.stream().map(ItemPerformance::getRevenue).toList());

        return chart;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PIE CHART — Revenue Contribution
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Pie chart: Revenue contribution as percentage per item.
     * Top 8 items + "Other" for the rest.
     */
    private Map<String, Object> generateRevenueContributionPie(List<ItemPerformance> items) {
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        double otherPct = 0;
        int count = 0;

        for (ItemPerformance item : items) {
            if (count < 8) {
                labels.add(item.getItemName());
                data.add(round2(item.getRevenueContributionPct()));
            } else {
                otherPct += item.getRevenueContributionPct();
            }
            count++;
        }

        if (otherPct > 0) {
            labels.add("Other");
            data.add(round2(otherPct));
        }

        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "pie");
        chart.put("title", "Revenue Contribution by Item");
        chart.put("labels", labels);
        chart.put("data", data);

        return chart;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  LINE CHART — Monthly Trend
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Line chart: Monthly trend (profit by default, fallback to revenue).
     * X = month, Y = value.
     */
    private Map<String, Object> generateMonthlyTrendLine(List<MonthlyBreakdown> monthly) {
        boolean hasProfitData = monthly.stream().anyMatch(m -> m.getProfit() != 0);

        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("type", "line");
        chart.put("title", hasProfitData ? "Monthly Profit Trend" : "Monthly Revenue Trend");
        chart.put("labels", monthly.stream().map(MonthlyBreakdown::getMonth).toList());
        
        if (hasProfitData) {
            chart.put("data", monthly.stream().map(MonthlyBreakdown::getProfit).toList());
        } else {
            chart.put("data", monthly.stream().map(MonthlyBreakdown::getRevenue).toList());
        }

        return chart;
    }



    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
