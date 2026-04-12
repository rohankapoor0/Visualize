package com.insightify.service;

import com.insightify.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Orchestrator: coordinates restaurant-specific aggregation, KPI computation,
 * chart generation, insight generation, and menu optimization into a single
 * RestaurantAnalysisResponse.
 *
 * This is the heart of the restaurant decision-making engine.
 */
@Service
public class LocalAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LocalAnalysisService.class);

    private final AggregationService agg;
    private final KPIService kpiService;
    private final ChartService chartService;
    private final InsightService insightService;

    public LocalAnalysisService(AggregationService aggregationService,
                                 KPIService kpiService,
                                 ChartService chartService,
                                 InsightService insightService) {
        this.agg = aggregationService;
        this.kpiService = kpiService;
        this.chartService = chartService;
        this.insightService = insightService;
    }

    /**
     * Generate the full restaurant analytics dashboard.
     * Returns a RestaurantAnalysisResponse with all metrics, rankings, charts, and insights.
     */
    public RestaurantAnalysisResponse generateFullAnalysis(List<Map<String, String>> rows,
                                                            List<Map<String, String>> columns) {
        log.info("Building restaurant analytics for {} rows × {} columns", rows.size(), columns.size());
        long t0 = System.currentTimeMillis();

        RestaurantAnalysisResponse response = new RestaurantAnalysisResponse();

        // 1. KPIs
        Map<String, Object> kpis = kpiService.buildRestaurantKpis(rows, columns);
        response.setKpis(kpis);

        // 2. Item Performance Rankings
        List<ItemPerformance> allItems = agg.aggregateItemPerformance(rows, columns);
        
        List<ItemPerformance> sortedByQtyDesc = allItems.stream()
                .sorted(Comparator.comparingDouble(ItemPerformance::getQuantitySold).reversed())
                .toList();

        List<ItemPerformance> topItems = sortedByQtyDesc.stream().limit(2).toList();
        Set<String> topItemNames = topItems.stream().map(ItemPerformance::getItemName).collect(Collectors.toSet());

        List<ItemPerformance> leastSelling = allItems.stream()
                .sorted(Comparator.comparingDouble(ItemPerformance::getQuantitySold))
                .filter(item -> !topItemNames.contains(item.getItemName()))
                .limit(3)
                .toList();

        response.setTopItems(topItems);
        response.setLeastSellingItems(leastSelling);

        // 3. Monthly Analysis
        List<MonthlyBreakdown> monthly = agg.aggregateByMonthRestaurant(rows, columns);
        response.setMonthlyAnalysis(monthly);

        // Most Profitable Month logic
        if (!monthly.isEmpty()) {
            MonthlyBreakdown bestMonth = monthly.stream()
                    .max(Comparator.comparingDouble(MonthlyBreakdown::getRevenue))
                    .orElse(monthly.get(0));
            Map<String, Object> mostProfitable = new LinkedHashMap<>();
            mostProfitable.put("month", bestMonth.getMonth());
            mostProfitable.put("totalRevenue", bestMonth.getRevenue());
            response.setMostProfitableMonth(mostProfitable);
        }

        // 4. Month-over-Month Comparison
        Map<String, Object> monthOverMonth = kpiService.computeMonthOverMonth(rows, columns);
        response.setMonthOverMonth(monthOverMonth);

        // 5. Charts
        List<Map<String, Object>> charts = chartService.generateCharts(rows, columns);
        List<DashboardSection> sections = wrapChartsInSections(charts, allItems, monthly, monthOverMonth);
        response.setSections(sections);

        // 7. Insights
        List<String> insights = insightService.generateRestaurantInsights(
                rows, columns, allItems, monthly, monthOverMonth);
        response.setInsights(insights);

        // 8. Menu Recommendations & Profit Optimization
        List<MenuRecommendation> menuRecs = insightService.generateMenuRecommendations(allItems);
        response.setMenuRecommendations(menuRecs);

        List<String> profitOpt = insightService.generateProfitOptimizationSuggestions(leastSelling, topItems);
        response.setProfitOptimization(profitOpt);

        long elapsed = System.currentTimeMillis() - t0;
        log.info("Restaurant analytics built in {}ms ({} items, {} months, {} charts, {} insights, {} recommendations)",
                elapsed, allItems.size(), monthly.size(), charts.size(), insights.size(), menuRecs.size());

        return response;
    }

    /**
     * Chart data only (legacy /charts endpoint).
     */
    public List<Map<String, Object>> generateCharts(List<Map<String, String>> rows,
                                                     List<Map<String, String>> columns) {
        return chartService.generateCharts(rows, columns);
    }

    /**
     * Text insights only (legacy /insights endpoint).
     */
    public String generateInsightsText(List<Map<String, String>> rows,
                                        List<Map<String, String>> columns) {
        return insightService.generatePlainTextInsights(rows, columns);
    }

    /**
     * Flowchart structure (data pipeline visualization).
     */
    public Map<String, Object> generateFlowchartStructure(List<Map<String, String>> rows,
                                                           List<Map<String, String>> columns,
                                                           String query) {
        String metricCol = agg.findMetricColumn(columns, rows);
        String categoryCol = agg.findCategoricalColumn(columns, rows);
        String dateCol = agg.findDateColumn(columns, rows);

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        int id = 1;

        // Root
        nodes.add(node(id, "Dataset (" + rows.size() + " rows)", "input", 0, 0));
        int root = id++;

        // Column detection
        String colSummary = columns.stream()
                .map(c -> c.get("name") + " (" + c.get("type") + ")")
                .limit(5).collect(Collectors.joining(", "));
        nodes.add(node(id, colSummary, "process", 0, 100));
        edges.add(edge(root, id));
        int colNode = id++;

        if (metricCol != null) {
            nodes.add(node(id, "Metric: " + metricCol, "process", -200, 200));
            edges.add(edge(colNode, id));
            int mNode = id++;
            double total = agg.computeTotal(rows, metricCol);
            nodes.add(node(id, String.format("Total: %,.2f", total), "output", -200, 300));
            edges.add(edge(mNode, id));
            id++;
        }

        if (categoryCol != null) {
            nodes.add(node(id, "Group: " + categoryCol, "process", 0, 200));
            edges.add(edge(colNode, id));
            int cNode = id++;
            String topSeg = agg.findTopSegment(rows, columns);
            nodes.add(node(id, "Top: " + topSeg, "output", 0, 300));
            edges.add(edge(cNode, id));
            id++;
        }

        if (dateCol != null) {
            nodes.add(node(id, "Trend: " + dateCol, "process", 200, 200));
            edges.add(edge(colNode, id));
            int tNode = id++;
            if (metricCol != null) {
                var dateSeries = agg.aggregateByDate(rows, dateCol, metricCol);
                String trend = agg.detectTrend(dateSeries);
                nodes.add(node(id, "Direction: " + trend, "output", 200, 300));
                edges.add(edge(tNode, id));
                id++;
            }
        }

        if (query != null && !query.isBlank()) {
            nodes.add(node(id, "Query: " + query, "decision", 400, 100));
        }

        return Map.of("nodes", nodes, "edges", edges,
                "description", "Analysis pipeline for " + rows.size() + " rows");
    }



    // ═══════════════════════════════════════════════════════════════════
    //  SECTION BUILDER — Restaurant Context
    // ═══════════════════════════════════════════════════════════════════

    private List<DashboardSection> wrapChartsInSections(List<Map<String, Object>> charts,
                                                         List<ItemPerformance> items,
                                                         List<MonthlyBreakdown> monthly,
                                                         Map<String, Object> monthOverMonth) {
        List<DashboardSection> sections = new ArrayList<>();

        for (Map<String, Object> chart : charts) {
            String type = (String) chart.get("type");
            String title = (String) chart.get("title");

            String observation;
            String impact;

            switch (type) {
                case "bar":
                    if (title.contains("Top Menu")) {
                        if (!items.isEmpty()) {
                            ItemPerformance top = items.get(0);
                            observation = String.format("%s leads with ₹%,.2f revenue (%.1f%% of total).",
                                    top.getItemName(), top.getRevenue(), top.getRevenueContributionPct());
                            impact = String.format("Focus marketing efforts on %s and similar high-performers.", top.getItemName());
                        } else {
                            observation = title;
                            impact = "Identify top and bottom performing menu items.";
                        }
                    } else if (title.contains("Profit")) {
                        ItemPerformance highProfit = items.stream()
                                .max(Comparator.comparingDouble(ItemPerformance::getProfit))
                                .orElse(null);
                        if (highProfit != null) {
                            observation = String.format("%s generates the highest profit at ₹%,.2f (%.1f%% margin).",
                                    highProfit.getItemName(), highProfit.getProfit(), highProfit.getProfitMarginPct());
                            impact = "Optimize menu to prioritize high-margin items for maximum profitability.";
                        } else {
                            observation = title;
                            impact = "Analyze profit composition across menu items.";
                        }
                    } else {
                        observation = title;
                        impact = "Analyze menu item performance.";
                    }
                    break;

                case "pie":
                    if (!items.isEmpty()) {
                        double top3Pct = items.stream().limit(3)
                                .mapToDouble(ItemPerformance::getRevenueContributionPct).sum();
                        observation = String.format("Top 3 items account for %.1f%% of total revenue.", top3Pct);
                        impact = top3Pct > 60
                                ? "High concentration risk — consider diversifying the menu."
                                : "Healthy revenue distribution across menu items.";
                    } else {
                        observation = title;
                        impact = "Evaluate revenue concentration vs. diversification.";
                    }
                    break;



                default:
                    observation = title;
                    impact = "Analyze the data patterns shown.";
            }

            sections.add(new DashboardSection(title, chart, observation, impact));
        }

        return sections;
    }

    private Map<String, Object> node(int id, String label, String type, int x, int y) {
        return Map.of("id", String.valueOf(id), "label", label,
                "type", type, "position", Map.of("x", x, "y", y));
    }

    private Map<String, Object> edge(int from, int to) {
        return Map.of("source", String.valueOf(from), "target", String.valueOf(to));
    }
}
