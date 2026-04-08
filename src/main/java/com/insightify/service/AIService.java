package com.insightify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Mock AI service that simulates LLM-generated insights and explanations.
 * 
 * This is a placeholder implementation designed to be easily replaceable
 * with a real LLM integration (e.g., OpenAI, Google Gemini, Ollama).
 * 
 * To replace: implement a new class with the same method signatures,
 * or add an interface and swap the implementation via Spring profiles.
 */
@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    /**
     * Generate a natural-language summary of the dataset.
     */
    public String generateSummary(List<Map<String, String>> rows,
                                   List<Map<String, String>> columns,
                                   String datasetName) {
        log.debug("Generating mock summary for dataset: {}", datasetName);

        int rowCount = rows.size();
        int colCount = columns.size();

        List<String> numericCols = columns.stream()
                .filter(c -> "numeric".equals(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        List<String> categoricalCols = columns.stream()
                .filter(c -> "categorical".equals(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "This dataset '%s' contains %d records across %d columns. ",
                datasetName, rowCount, colCount));

        if (!numericCols.isEmpty()) {
            sb.append(String.format(
                    "Numeric columns include: %s. ", String.join(", ", numericCols)));
        }
        if (!categoricalCols.isEmpty()) {
            sb.append(String.format(
                    "Categorical columns include: %s. ", String.join(", ", categoricalCols)));
        }

        sb.append("The data appears well-structured and suitable for analysis.");
        return sb.toString();
    }

    /**
     * Generate detailed insights including trends and anomalies.
     */
    public Map<String, Object> generateDetailedInsights(
            List<Map<String, String>> rows,
            List<Map<String, String>> columns,
            String datasetName) {
        log.debug("Generating mock detailed insights for: {}", datasetName);

        Map<String, Object> details = new LinkedHashMap<>();

        // --- Trends ---
        List<String> trends = new ArrayList<>();
        for (Map<String, String> col : columns) {
            if ("numeric".equals(col.get("type"))) {
                String colName = col.get("name");
                DoubleSummaryStatistics stats = rows.stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .summaryStatistics();

                trends.add(String.format(
                        "%s ranges from %.2f to %.2f (avg: %.2f)",
                        colName, stats.getMin(), stats.getMax(), stats.getAverage()));

                // Simple trend: compare first half vs second half
                int mid = rows.size() / 2;
                double firstHalf = rows.subList(0, mid).stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .average().orElse(0);
                double secondHalf = rows.subList(mid, rows.size()).stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .average().orElse(0);

                if (secondHalf > firstHalf * 1.1) {
                    trends.add(String.format("%s shows an upward trend (+%.1f%%)",
                            colName, ((secondHalf - firstHalf) / firstHalf) * 100));
                } else if (secondHalf < firstHalf * 0.9) {
                    trends.add(String.format("%s shows a downward trend (%.1f%%)",
                            colName, ((secondHalf - firstHalf) / firstHalf) * 100));
                } else {
                    trends.add(String.format("%s remains relatively stable", colName));
                }
            }
        }
        details.put("trends", trends);

        // --- Anomalies ---
        List<String> anomalies = new ArrayList<>();
        for (Map<String, String> col : columns) {
            if ("numeric".equals(col.get("type"))) {
                String colName = col.get("name");
                DoubleSummaryStatistics stats = rows.stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .summaryStatistics();

                double mean = stats.getAverage();
                double stdDev = Math.sqrt(rows.stream()
                        .mapToDouble(r -> {
                            double v = parseDouble(r.getOrDefault(colName, "0"));
                            return (v - mean) * (v - mean);
                        })
                        .average().orElse(0));

                // Flag values > 2 standard deviations from mean
                long outlierCount = rows.stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .filter(v -> Math.abs(v - mean) > 2 * stdDev)
                        .count();

                if (outlierCount > 0) {
                    anomalies.add(String.format(
                            "%s has %d potential outlier(s) (values beyond 2σ from mean)",
                            colName, outlierCount));
                }
            }
        }
        if (anomalies.isEmpty()) {
            anomalies.add("No significant anomalies detected in the dataset.");
        }
        details.put("anomalies", anomalies);

        // --- Key Statistics ---
        Map<String, Object> statistics = new LinkedHashMap<>();
        for (Map<String, String> col : columns) {
            if ("numeric".equals(col.get("type"))) {
                String colName = col.get("name");
                DoubleSummaryStatistics stats = rows.stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .summaryStatistics();
                statistics.put(colName, Map.of(
                        "min", stats.getMin(),
                        "max", stats.getMax(),
                        "average", Math.round(stats.getAverage() * 100.0) / 100.0,
                        "sum", stats.getSum(),
                        "count", stats.getCount()
                ));
            }
        }
        details.put("statistics", statistics);

        // --- Simple Explanation ---
        details.put("explanation", generateSimpleExplanation(rows, columns, datasetName));

        return details;
    }

    /**
     * Generate a simple, non-technical explanation of the data.
     */
    public String generateSimpleExplanation(
            List<Map<String, String>> rows,
            List<Map<String, String>> columns,
            String datasetName) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "In simple terms, this dataset tells us about '%s'. ", datasetName));
        sb.append(String.format(
                "We have %d data points to work with. ", rows.size()));

        for (Map<String, String> col : columns) {
            if ("numeric".equals(col.get("type"))) {
                String colName = col.get("name");
                DoubleSummaryStatistics stats = rows.stream()
                        .mapToDouble(r -> parseDouble(r.getOrDefault(colName, "0")))
                        .summaryStatistics();
                sb.append(String.format(
                        "The '%s' values go from %.0f to %.0f, averaging around %.0f. ",
                        colName, stats.getMin(), stats.getMax(), stats.getAverage()));
            }
        }

        sb.append("Overall, the data looks consistent and ready for deeper analysis.");
        return sb.toString();
    }

    /**
     * Generate a flowchart structure from a dataset or query.
     */
    public Map<String, Object> generateFlowchart(
            List<Map<String, String>> rows,
            List<Map<String, String>> columns,
            String query) {
        log.debug("Generating mock flowchart for query: {}", query);

        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        // Start node
        nodes.add(createNode("start", "Start: Dataset Loaded",
                "input", 0, 0));

        // Parse step
        nodes.add(createNode("parse", "Parse & Validate Data",
                "process", 0, 100));
        edges.add(createEdge("start", "parse", "raw data"));

        // Column analysis
        nodes.add(createNode("analyze_cols", "Analyze " + columns.size() + " Columns",
                "process", 0, 200));
        edges.add(createEdge("parse", "analyze_cols", "structured data"));

        // Branch by column types
        long numericCount = columns.stream()
                .filter(c -> "numeric".equals(c.get("type"))).count();
        long categoricalCount = columns.stream()
                .filter(c -> "categorical".equals(c.get("type"))).count();

        if (numericCount > 0) {
            nodes.add(createNode("numeric", numericCount + " Numeric Columns",
                    "data", -150, 300));
            edges.add(createEdge("analyze_cols", "numeric", ""));
        }

        if (categoricalCount > 0) {
            nodes.add(createNode("categorical", categoricalCount + " Categorical Columns",
                    "data", 150, 300));
            edges.add(createEdge("analyze_cols", "categorical", ""));
        }

        // Chart generation
        nodes.add(createNode("charts", "Generate Chart Configurations",
                "process", 0, 400));
        if (numericCount > 0) edges.add(createEdge("numeric", "charts", ""));
        if (categoricalCount > 0) edges.add(createEdge("categorical", "charts", ""));

        // Insight generation
        nodes.add(createNode("insights", "Generate Insights & Trends",
                "process", 0, 500));
        edges.add(createEdge("charts", "insights", "chart data"));

        // Output
        nodes.add(createNode("output", "Return Analysis Results",
                "output", 0, 600));
        edges.add(createEdge("insights", "output", "complete analysis"));

        Map<String, Object> structure = new LinkedHashMap<>();
        structure.put("nodes", nodes);
        structure.put("edges", edges);
        structure.put("description", query != null
                ? "Flowchart generated for query: " + query
                : "Data processing flowchart for dataset analysis");

        return structure;
    }

    // ----- Private helpers -----

    private Map<String, Object> createNode(String id, String label,
                                           String type, int x, int y) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", id);
        node.put("label", label);
        node.put("type", type);
        node.put("position", Map.of("x", x, "y", y));
        return node;
    }

    private Map<String, Object> createEdge(String source, String target, String label) {
        Map<String, Object> edge = new LinkedHashMap<>();
        edge.put("id", source + "->" + target);
        edge.put("source", source);
        edge.put("target", target);
        if (label != null && !label.isEmpty()) {
            edge.put("label", label);
        }
        return edge;
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
