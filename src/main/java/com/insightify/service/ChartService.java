package com.insightify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightify.dto.ChartResponse;
import com.insightify.model.Chart;
import com.insightify.model.Dataset;
import com.insightify.repository.ChartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates chart configurations based on dataset column types.
 *
 * Auto-detection logic:
 *   - numeric + categorical → bar chart
 *   - date/time column present → line chart
 *   - few categories with numeric values → pie chart
 *   - two numeric columns → scatter chart
 */
@Service
public class ChartService {

    private static final Logger log = LoggerFactory.getLogger(ChartService.class);

    private final DatasetService datasetService;
    private final ChartRepository chartRepository;
    private final ObjectMapper objectMapper;

    public ChartService(DatasetService datasetService,
                        ChartRepository chartRepository,
                        ObjectMapper objectMapper) {
        this.datasetService = datasetService;
        this.chartRepository = chartRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate or retrieve chart configs for a dataset.
     */
    @Transactional
    public List<ChartResponse> generateCharts(Long datasetId) {
        // Check if charts already exist
        List<Chart> existing = chartRepository.findByDatasetId(datasetId);
        if (!existing.isEmpty()) {
            return existing.stream().map(this::toResponse).toList();
        }

        Dataset dataset = datasetService.getDatasetEntity(datasetId);
        List<Map<String, String>> rows = datasetService.getDatasetRows(datasetId);
        List<Map<String, String>> columns = datasetService.getColumnMetadata(datasetId);

        List<Chart> charts = new ArrayList<>();

        // Separate columns by type
        List<String> numericCols = columns.stream()
                .filter(c -> "numeric".equals(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        List<String> categoricalCols = columns.stream()
                .filter(c -> "categorical".equals(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        List<String> dateCols = columns.stream()
                .filter(c -> "date".equals(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        // 1. Bar chart: categorical + numeric
        if (!categoricalCols.isEmpty() && !numericCols.isEmpty()) {
            String category = categoricalCols.get(0);
            String value = numericCols.get(0);
            charts.add(createBarChart(datasetId, rows, category, value));
        }

        // 2. Line chart: date/time + numeric
        if (!dateCols.isEmpty() && !numericCols.isEmpty()) {
            String dateCol = dateCols.get(0);
            String valueCol = numericCols.get(0);
            charts.add(createLineChart(datasetId, rows, dateCol, valueCol));
        }

        // 3. Pie chart: categorical with few unique values + numeric
        if (!categoricalCols.isEmpty() && !numericCols.isEmpty()) {
            String category = categoricalCols.get(0);
            String value = numericCols.get(0);
            long uniqueCount = rows.stream()
                    .map(r -> r.get(category))
                    .distinct().count();
            if (uniqueCount <= 10) {
                charts.add(createPieChart(datasetId, rows, category, value));
            }
        }

        // 4. Scatter chart: two numeric columns
        if (numericCols.size() >= 2) {
            charts.add(createScatterChart(datasetId, rows, numericCols.get(0), numericCols.get(1)));
        }

        // Fallback: if no charts were generated, create a basic bar chart with first two columns
        if (charts.isEmpty() && columns.size() >= 2) {
            charts.add(createBarChart(datasetId, rows,
                    columns.get(0).get("name"), columns.get(1).get("name")));
        }

        List<Chart> saved = chartRepository.saveAll(charts);
        log.info("Generated {} charts for dataset {}", saved.size(), datasetId);
        return saved.stream().map(this::toResponse).toList();
    }

    // ----- Chart Builders -----

    private Chart createBarChart(Long datasetId, List<Map<String, String>> rows,
                                 String categoryCol, String valueCol) {
        // Aggregate values by category
        Map<String, Double> aggregated = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String key = row.getOrDefault(categoryCol, "Unknown");
            double val = parseDouble(row.get(valueCol));
            aggregated.merge(key, val, Double::sum);
        }

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("labels", new ArrayList<>(aggregated.keySet()));
        config.put("datasets", List.of(Map.of(
                "label", valueCol + " by " + categoryCol,
                "data", new ArrayList<>(aggregated.values()),
                "backgroundColor", generateColors(aggregated.size())
        )));
        config.put("options", Map.of(
                "responsive", true,
                "plugins", Map.of("legend", Map.of("position", "top"))
        ));

        Chart chart = new Chart();
        chart.setDatasetId(datasetId);
        chart.setType("bar");
        chart.setTitle(valueCol + " by " + categoryCol);
        chart.setConfigJson(toJson(config));
        return chart;
    }

    private Chart createLineChart(Long datasetId, List<Map<String, String>> rows,
                                  String dateCol, String valueCol) {
        List<String> labels = rows.stream()
                .map(r -> r.getOrDefault(dateCol, ""))
                .toList();
        List<Double> values = rows.stream()
                .map(r -> parseDouble(r.get(valueCol)))
                .toList();

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("labels", labels);
        config.put("datasets", List.of(Map.of(
                "label", valueCol + " over time",
                "data", values,
                "borderColor", "#4F46E5",
                "backgroundColor", "rgba(79, 70, 229, 0.1)",
                "fill", true,
                "tension", 0.3
        )));
        config.put("options", Map.of(
                "responsive", true,
                "scales", Map.of(
                        "x", Map.of("title", Map.of("display", true, "text", dateCol)),
                        "y", Map.of("title", Map.of("display", true, "text", valueCol))
                )
        ));

        Chart chart = new Chart();
        chart.setDatasetId(datasetId);
        chart.setType("line");
        chart.setTitle(valueCol + " over time");
        chart.setConfigJson(toJson(config));
        return chart;
    }

    private Chart createPieChart(Long datasetId, List<Map<String, String>> rows,
                                 String categoryCol, String valueCol) {
        Map<String, Double> aggregated = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String key = row.getOrDefault(categoryCol, "Unknown");
            double val = parseDouble(row.get(valueCol));
            aggregated.merge(key, val, Double::sum);
        }

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("labels", new ArrayList<>(aggregated.keySet()));
        config.put("datasets", List.of(Map.of(
                "data", new ArrayList<>(aggregated.values()),
                "backgroundColor", generateColors(aggregated.size()),
                "borderWidth", 2
        )));
        config.put("options", Map.of(
                "responsive", true,
                "plugins", Map.of(
                        "legend", Map.of("position", "right"),
                        "title", Map.of("display", true, "text", valueCol + " distribution by " + categoryCol)
                )
        ));

        Chart chart = new Chart();
        chart.setDatasetId(datasetId);
        chart.setType("pie");
        chart.setTitle(valueCol + " distribution by " + categoryCol);
        chart.setConfigJson(toJson(config));
        return chart;
    }

    private Chart createScatterChart(Long datasetId, List<Map<String, String>> rows,
                                     String xCol, String yCol) {
        List<Map<String, Double>> points = rows.stream()
                .map(r -> Map.of(
                        "x", parseDouble(r.get(xCol)),
                        "y", parseDouble(r.get(yCol))
                ))
                .toList();

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("datasets", List.of(Map.of(
                "label", xCol + " vs " + yCol,
                "data", points,
                "backgroundColor", "#6366F1",
                "pointRadius", 5
        )));
        config.put("options", Map.of(
                "responsive", true,
                "scales", Map.of(
                        "x", Map.of("title", Map.of("display", true, "text", xCol)),
                        "y", Map.of("title", Map.of("display", true, "text", yCol))
                )
        ));

        Chart chart = new Chart();
        chart.setDatasetId(datasetId);
        chart.setType("scatter");
        chart.setTitle(xCol + " vs " + yCol);
        chart.setConfigJson(toJson(config));
        return chart;
    }

    // ----- Helpers -----

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private List<String> generateColors(int count) {
        String[] palette = {
                "#6366F1", "#EC4899", "#14B8A6", "#F59E0B", "#EF4444",
                "#8B5CF6", "#06B6D4", "#84CC16", "#F97316", "#10B981",
                "#3B82F6", "#E11D48", "#22D3EE", "#A855F7", "#FACC15"
        };
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            colors.add(palette[i % palette.length]);
        }
        return colors;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chart config", e);
        }
    }

    private ChartResponse toResponse(Chart chart) {
        Object config = null;
        try {
            config = objectMapper.readValue(chart.getConfigJson(), Object.class);
        } catch (Exception ignored) {}

        return new ChartResponse(
                chart.getId(), chart.getDatasetId(), chart.getType(),
                chart.getTitle(), config, chart.getCreatedAt()
        );
    }
}
