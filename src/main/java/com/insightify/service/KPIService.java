package com.insightify.service;

import com.insightify.dto.ItemPerformance;
import com.insightify.dto.MonthlyBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Restaurant-specific KPI calculator.
 * Computes revenue, profit, growth, and per-item performance metrics
 * from raw dataset rows using restaurant column detection.
 */
@Service
public class KPIService {

    private static final Logger log = LoggerFactory.getLogger(KPIService.class);

    private final AggregationService agg;

    public KPIService(AggregationService aggregationService) {
        this.agg = aggregationService;
    }

    /**
     * Build the restaurant KPI map for the dashboard header.
     */
    public Map<String, Object> buildRestaurantKpis(List<Map<String, String>> rows,
                                                     List<Map<String, String>> columns) {
        String itemCol = agg.findItemNameColumn(columns, rows);
        String priceCol = agg.findPriceColumn(columns, rows);
        String qtyCol = agg.findQuantityColumn(columns, rows);
        String costCol = agg.findCostColumn(columns, rows);
        String dateCol = agg.findDateColumn(columns, rows);

        Map<String, Object> kpis = new LinkedHashMap<>();

        // 1. Identify latest date/year/month in the dataset
        java.time.LocalDate latestDate = rows.stream()
                .map(r -> agg.parseToLocalDate(r.get(dateCol)))
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(java.time.LocalDate.now());

        int latestYear = latestDate.getYear();
        int latestMonthValue = latestDate.getMonthValue();
        String currentMonthLabel = latestYear + "/" + String.format("%02d", latestMonthValue);

        // 2. Filter dataset for scopes
        List<Map<String, String>> currentYearRows = rows.stream()
                .filter(r -> {
                    java.time.LocalDate d = agg.parseToLocalDate(r.get(dateCol));
                    return d != null && d.getYear() == latestYear;
                })
                .toList();

        List<Map<String, String>> currentMonthRows = currentYearRows.stream()
                .filter(r -> {
                    java.time.LocalDate d = agg.parseToLocalDate(r.get(dateCol));
                    return d != null && d.getMonthValue() == latestMonthValue;
                })
                .toList();

        // 3. Total Revenue (Current Year only)
        double totalRevenueRef = currentYearRows.stream()
                .mapToDouble(r -> {
                    double price = agg.parseNumeric(r.get(priceCol));
                    double qty = qtyCol != null ? agg.parseNumeric(r.get(qtyCol)) : 1.0;
                    return price * qty;
                })
                .sum();

        kpis.put("totalRevenue", Map.of(
                "value", round2(totalRevenueRef),
                "description", "Total revenue for the current year (" + latestYear + ")"
        ));

        // 4. Total Profit (Current Month only)
        // Per User: sum of all items sold in the current month
        double totalProfitRef = currentMonthRows.stream()
                .mapToDouble(r -> {
                    double price = agg.parseNumeric(r.get(priceCol));
                    double qty = qtyCol != null ? agg.parseNumeric(r.get(qtyCol)) : 1.0;
                    return price * qty;
                })
                .sum();

        kpis.put("totalProfit", Map.of(
                "value", round2(totalProfitRef),
                "description", "Total sales for the current month (" + currentMonthLabel + ")"
        ));

        // 5. Total Items Sold (Current Year)
        int totalItemsSold = (int) currentYearRows.stream()
                .mapToDouble(r -> qtyCol != null ? agg.parseNumeric(r.get(qtyCol)) : 1.0)
                .sum();
        kpis.put("totalItemsSold", Map.of(
                "value", totalItemsSold,
                "description", "Total quantity sold in " + latestYear
        ));

        // 6. Average Order Value (Current Year)
        double avgOrderValue = totalItemsSold > 0 ? totalRevenueRef / totalItemsSold : 0.0;
        kpis.put("avgOrderValue", Map.of(
                "value", round2(avgOrderValue),
                "description", "Average revenue per item in " + latestYear
        ));

        // 7. Monthly Growth (latest vs previous month)
        Map<String, Object> monthOverMonth = computeMonthOverMonth(rows, columns);
        double revenueGrowth = (double) monthOverMonth.getOrDefault("revenueGrowthPct", 0.0);
        kpis.put("monthlyGrowth", Map.of(
                "value", round2(revenueGrowth),
                "description", revenueGrowth >= 0
                        ? "Revenue increased compared to previous month"
                        : "Revenue declined compared to previous month"
        ));

        // 8. Top Item (Current Year)
        List<ItemPerformance> yearItems = agg.aggregateItemPerformance(currentYearRows, columns);
        String topItemName = yearItems.isEmpty() ? "N/A" : yearItems.get(0).getItemName();
        kpis.put("topItem", Map.of(
                "value", topItemName,
                "description", "Best-selling item in " + latestYear
        ));

        log.info("Restaurant KPIs built for year={}: rev={}, profit={}, items={}",
                latestYear, round2(totalRevenueRef), round2(totalProfitRef), totalItemsSold);
        return kpis;
    }

    /**
     * Compute month-over-month comparison.
     * Returns: { revenueGrowthPct, profitGrowthPct, direction, summary }
     */
    public Map<String, Object> computeMonthOverMonth(List<Map<String, String>> rows,
                                                       List<Map<String, String>> columns) {
        List<MonthlyBreakdown> monthly = agg.aggregateByMonthRestaurant(rows, columns);

        Map<String, Object> result = new LinkedHashMap<>();

        if (monthly.size() < 2) {
            result.put("revenueGrowthPct", 0.0);
            result.put("profitGrowthPct", 0.0);
            result.put("direction", "stable");
            result.put("summary", "Not enough monthly data for comparison");
            return result;
        }

        MonthlyBreakdown latest = monthly.get(monthly.size() - 1);
        MonthlyBreakdown previous = monthly.get(monthly.size() - 2);

        double revenueGrowth = previous.getRevenue() != 0
                ? ((latest.getRevenue() - previous.getRevenue()) / Math.abs(previous.getRevenue())) * 100.0
                : (latest.getRevenue() > 0 ? 100.0 : 0.0);

        double profitGrowth = previous.getProfit() != 0
                ? ((latest.getProfit() - previous.getProfit()) / Math.abs(previous.getProfit())) * 100.0
                : (latest.getProfit() > 0 ? 100.0 : 0.0);

        String direction = revenueGrowth > 0 ? "increased" : (revenueGrowth < 0 ? "declined" : "stable");

        String summary;
        if ("increased".equals(direction)) {
            summary = String.format("Business increased by %.1f%% compared to last month (%s vs %s)",
                    Math.abs(revenueGrowth), latest.getMonth(), previous.getMonth());
        } else if ("declined".equals(direction)) {
            summary = String.format("Business declined by %.1f%% compared to last month (%s vs %s)",
                    Math.abs(revenueGrowth), latest.getMonth(), previous.getMonth());
        } else {
            summary = String.format("Business remained stable compared to last month (%s vs %s)",
                    latest.getMonth(), previous.getMonth());
        }

        result.put("revenueGrowthPct", round2(revenueGrowth));
        result.put("profitGrowthPct", round2(profitGrowth));
        result.put("direction", direction);
        result.put("summary", summary);
        result.put("latestMonth", latest.getMonth());
        result.put("previousMonth", previous.getMonth());
        result.put("latestRevenue", round2(latest.getRevenue()));
        result.put("previousRevenue", round2(previous.getRevenue()));

        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE COMPUTATION HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private double computeTotalRevenue(List<Map<String, String>> rows,
                                        String priceCol, String qtyCol) {
        if (priceCol == null) return 0.0;
        double total = 0.0;
        for (Map<String, String> row : rows) {
            double price = agg.parseNumeric(row.get(priceCol));
            double qty = qtyCol != null ? agg.parseNumeric(row.get(qtyCol)) : 1.0;
            total += price * qty;
        }
        return total;
    }

    private double computeTotalProfit(List<Map<String, String>> rows,
                                       String priceCol, String costCol, String qtyCol) {
        if (priceCol == null || costCol == null) return 0.0;
        double total = 0.0;
        for (Map<String, String> row : rows) {
            double price = agg.parseNumeric(row.get(priceCol));
            double cost = agg.parseNumeric(row.get(costCol));
            double qty = qtyCol != null ? agg.parseNumeric(row.get(qtyCol)) : 1.0;
            total += (price - cost) * qty;
        }
        return total;
    }

    private int computeTotalItemsSold(List<Map<String, String>> rows, String qtyCol) {
        if (qtyCol == null) return rows.size();
        return (int) rows.stream()
                .mapToDouble(r -> agg.parseNumeric(r.get(qtyCol)))
                .sum();
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
