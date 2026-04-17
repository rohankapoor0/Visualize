package com.insightify.service;

import com.insightify.dto.ItemPerformance;
import com.insightify.dto.MenuRecommendation;
import com.insightify.dto.MonthlyBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Restaurant-specific insight and recommendation generator.
 * Produces actionable business insights for restaurant owners:
 *   - Item performance insights (top/bottom sellers)
 *   - Monthly comparison insights
 *   - Profit analysis insights
 *   - Menu optimization recommendations (items to remove/review)
 *   - Strategic suggestions
 *
 * Every insight is backed by real computed data — no generic statements.
 */
@Service
public class InsightService {

    private static final Logger log = LoggerFactory.getLogger(InsightService.class);

    private final AggregationService agg;

    public InsightService(AggregationService aggregationService) {
        this.agg = aggregationService;
    }

    /**
     * Generate restaurant-specific business insights.
     * Returns a list of human-readable, actionable insight strings.
     */
    public List<String> generateRestaurantInsights(List<Map<String, String>> rows,
                                                     List<Map<String, String>> columns,
                                                     List<ItemPerformance> items,
                                                     List<MonthlyBreakdown> monthly,
                                                     Map<String, Object> monthOverMonth) {
        List<String> insights = new ArrayList<>();

        if (items.isEmpty()) {
            insights.add("Unable to generate restaurant insights — no item data detected.");
            return insights;
        }

        // ── 1. Top Item Insight ──────────────────────────────────
        ItemPerformance topItem = items.get(0);
        insights.add(String.format(
                "%s is the top-selling item contributing %.1f%% of total revenue (₹%,.2f total revenue).",
                topItem.getItemName(), topItem.getRevenueContributionPct(), topItem.getRevenue()
        ));

        // ── 2. Bottom Item Insight ──────────────────────────────────
        if (items.size() > 1) {
            ItemPerformance bottomItem = items.get(items.size() - 1);
            insights.add(String.format(
                    "%s shows the lowest sales (%d units sold, ₹%,.2f revenue) and may need reconsideration.",
                    bottomItem.getItemName(), bottomItem.getQuantitySold(), bottomItem.getRevenue()
            ));
        }

        // ── 3. Top 3 Combined ──────────────────────────────────
        if (items.size() >= 3) {
            double top3Pct = items.stream().limit(3)
                    .mapToDouble(ItemPerformance::getRevenueContributionPct).sum();
            String top3Names = items.stream().limit(3)
                    .map(ItemPerformance::getItemName)
                    .collect(Collectors.joining(", "));
            insights.add(String.format(
                    "Top 3 items (%s) account for %.1f%% of total revenue.",
                    top3Names, top3Pct
            ));
        }

        // ── 4. Monthly Comparison ──────────────────────────────────
        if (monthOverMonth != null) {
            String summary = (String) monthOverMonth.get("summary");
            if (summary != null && !summary.contains("Not enough")) {
                insights.add(summary);
            }

            double revenueGrowth = (double) monthOverMonth.getOrDefault("revenueGrowthPct", 0.0);
            if (revenueGrowth > 0) {
                insights.add(String.format(
                        "Revenue increased by %.1f%% compared to last month — positive momentum.",
                        Math.abs(revenueGrowth)
                ));
            } else if (revenueGrowth < 0) {
                insights.add(String.format(
                        "Revenue declined by %.1f%% compared to last month — requires attention.",
                        Math.abs(revenueGrowth)
                ));
            }
        }

        // ── 5. Profit Analysis ──────────────────────────────────
        boolean hasCost = items.stream().anyMatch(i -> i.getProfit() != 0);
        if (hasCost) {
            ItemPerformance highestProfit = items.stream()
                    .max(Comparator.comparingDouble(ItemPerformance::getProfit))
                    .orElse(topItem);
            insights.add(String.format(
                    "Highest profit item is %s with ₹%,.2f total profit (%.1f%% margin).",
                    highestProfit.getItemName(), highestProfit.getProfit(), highestProfit.getProfitMarginPct()
            ));

            // Negative profit warning
            List<ItemPerformance> negativeProfit = items.stream()
                    .filter(i -> i.getProfit() < 0)
                    .toList();
            if (!negativeProfit.isEmpty()) {
                String names = negativeProfit.stream()
                        .map(ItemPerformance::getItemName)
                        .collect(Collectors.joining(", "));
                insights.add(String.format(
                        "⚠️ WARNING: %d item(s) have negative profit margins: %s — immediate pricing review recommended.",
                        negativeProfit.size(), names
                ));
            }
        }

        // ── 6. Strategic Recommendations ──────────────────────────────────
        insights.add("💡 Focus on promoting high-performing items to maximize revenue.");
        if (hasCost) {
            insights.add("💡 Consider revising pricing or recipe for low-profit items to improve margins.");
        }

        log.info("Generated {} restaurant insights for {} items", insights.size(), items.size());
        return insights;
    }

    /**
     * Generate menu optimization recommendations.
     * Flags items that should be removed or reviewed based on:
     *   - Very low sales volume (bottom 10%)
     *   - Low revenue contribution (< 2% of total)
     *   - Low or negative profit margin
     */
    public List<MenuRecommendation> generateMenuRecommendations(List<ItemPerformance> items) {
        if (items.isEmpty()) return List.of();

        List<MenuRecommendation> recommendations = new ArrayList<>();

        // Calculate thresholds
        double avgQty = items.stream().mapToInt(ItemPerformance::getQuantitySold).average().orElse(0);
        double lowQtyThreshold = avgQty * 0.25; // bottom quarter
        double lowRevenueThreshold = 2.0; // 2% contribution

        for (ItemPerformance item : items) {
            List<String> reasons = new ArrayList<>();

            // Check low sales
            if (item.getQuantitySold() <= lowQtyThreshold) {
                reasons.add("Very low sales volume (" + item.getQuantitySold() + " units)");
            }

            // Check low revenue contribution
            if (item.getRevenueContributionPct() < lowRevenueThreshold) {
                reasons.add(String.format("Low revenue contribution (%.1f%%)", item.getRevenueContributionPct()));
            }

            // Check negative profit
            if (item.getProfit() < 0) {
                reasons.add(String.format("Negative profit (₹%,.2f loss)", Math.abs(item.getProfit())));
            }
            // Check very low margin
            else if (item.getProfitMarginPct() > 0 && item.getProfitMarginPct() < 10) {
                reasons.add(String.format("Very low profit margin (%.1f%%)", item.getProfitMarginPct()));
            }

            if (!reasons.isEmpty()) {
                String severity = item.getProfit() < 0 ? "critical" : "warning";
                String action;
                if (item.getProfit() < 0) {
                    action = "Immediate pricing or recipe revision needed — currently losing money";
                } else if (reasons.size() >= 2) {
                    action = "Consider removing from menu or significant recipe/pricing overhaul";
                } else if (reasons.stream().anyMatch(r -> r.contains("low sales"))) {
                    action = "Consider removing or running promotion to boost sales";
                } else {
                    action = "Review pricing strategy to improve profitability";
                }

                recommendations.add(new MenuRecommendation(
                        item.getItemName(),
                        String.join("; ", reasons),
                        action,
                        severity
                ));
            }
        }

        log.info("Generated {} menu recommendations for {} items", recommendations.size(), items.size());
        return recommendations;
    }

    public List<String> generateProfitOptimizationSuggestions(List<ItemPerformance> leastSelling) {
        List<String> suggestions = new ArrayList<>();
        
        // Only focus on low performing items (bottom sellers)
        for (ItemPerformance item : leastSelling) {
            double averagePrice = item.getQuantitySold() > 0 ? item.getRevenue() / item.getQuantitySold() : 0.0;
            
            if (averagePrice > 0 && averagePrice <= 50) {
                suggestions.add(String.format("Keep %s as an affordable add-on. At a low price point of ₹%.0f, removing it won't optimize costs. Consider bundling it instead.", 
                        item.getItemName(), averagePrice));
            } else if (item.getQuantitySold() < 100) {
                suggestions.add(String.format("Consider retiring %s due to critically low sales volume (%d units) for a premium item.", 
                        item.getItemName(), item.getQuantitySold()));
            } else if (item.getProfit() < 0) {
                suggestions.add(String.format("Increase the price or optimize the recipe cost for %s. Despite selling %d units, it costs more than it earns.", 
                        item.getItemName(), item.getQuantitySold()));
            } else {
                suggestions.add(String.format("Consider tweaking the price point of %s to improve margin. It sells decently (%d units) but underperforms in total revenue.", 
                        item.getItemName(), item.getQuantitySold()));
            }
        }

        // Focus on items losing money
        if (leastSelling.stream().anyMatch(i -> i.getProfit() < 0)) {
            suggestions.add("Analyze cost structure for items with negative profit margins to eliminate active losses.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Current menu performance is stable; continue monitoring seasonal trends for optimization opportunities.");
        }

        return suggestions;
    }

    /**
     * Plain-text summary for the legacy /insights endpoint.
     */
    public String generatePlainTextInsights(List<Map<String, String>> rows,
                                             List<Map<String, String>> columns) {
        List<ItemPerformance> items = agg.aggregateItemPerformance(rows, columns);
        List<MonthlyBreakdown> monthly = agg.aggregateByMonthRestaurant(rows, columns);
        List<String> insights = generateRestaurantInsights(rows, columns, items, monthly, null);
        List<MenuRecommendation> recs = generateMenuRecommendations(items);

        StringBuilder sb = new StringBuilder();
        sb.append("═══ Restaurant Analytics Report ═══\n\n");

        sb.append("📊 Key Insights:\n");
        for (int i = 0; i < insights.size(); i++) {
            sb.append(String.format("  %d. %s\n", i + 1, insights.get(i)));
        }

        if (!recs.isEmpty()) {
            sb.append("\n🔍 Menu Optimization Recommendations:\n");
            for (MenuRecommendation rec : recs) {
                sb.append(String.format("  • %s — %s → %s\n",
                        rec.getItemName(), rec.getReason(), rec.getAction()));
            }
        }

        return sb.toString();
    }
}
