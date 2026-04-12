package com.insightify.service;

import com.insightify.dto.ItemPerformance;
import com.insightify.dto.MonthlyBreakdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Professional-grade aggregation engine.
 * Handles smart column detection, date bucketing, category grouping,
 * trend analysis, KPI calculation — all from raw parsed rows.
 */
@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    // ── Date formats: LocalDate (with day) ──
    private static final List<DateTimeFormatter> FULL_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
    );

    // ── Date formats: YearMonth (without day, e.g. Jan-2024) ──
    private static final List<DateTimeFormatter> MONTH_FORMATS = List.of(
            DateTimeFormatter.ofPattern("MMM-yyyy"),
            DateTimeFormatter.ofPattern("MMM yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("MM/yyyy"),
            DateTimeFormatter.ofPattern("MMMM yyyy")
    );

    // Column names that strongly suggest a date even if format detection is weak
    private static final Set<String> DATE_COLUMN_HINTS = Set.of(
            "date", "month", "year", "period", "time", "day", "week", "quarter"
    );

    // Column names that strongly suggest categories (not numeric metrics)
    private static final Set<String> CATEGORICAL_COLUMN_HINTS = Set.of(
            "category", "product", "region", "type", "name", "brand", "department",
            "segment", "group", "class", "country", "city", "state", "store",
            "channel", "status", "gender", "item", "sku", "model", "tier"
    );

    // Column names that strongly suggest primary numeric metrics
    private static final Set<String> METRIC_COLUMN_HINTS = Set.of(
            "sales", "revenue", "profit", "amount", "total", "price", "cost",
            "income", "quantity", "units", "count", "value", "spend", "budget",
            "earning", "turnover", "gross", "net", "margin", "volume",
            "weekly_sales", "monthly_sales", "daily_sales"
    );

    // ═══════════════════════════════════════════════════════════════════
    //  RESTAURANT-SPECIFIC COLUMN HINTS
    // ═══════════════════════════════════════════════════════════════════

    private static final Set<String> ITEM_NAME_HINTS = Set.of(
            "item", "item_name", "itemname", "menu_item", "menuitem",
            "product", "product_name", "productname", "dish", "food",
            "food_item", "fooditem", "meal", "name", "description"
    );

    private static final Set<String> PRICE_HINTS = Set.of(
            "price", "unit_price", "unitprice", "selling_price", "sellingprice",
            "menu_price", "menuprice", "item_price", "itemprice", "rate",
            "mrp", "amount"
    );

    private static final Set<String> QUANTITY_HINTS = Set.of(
            "quantity", "qty", "quantity_sold", "quantitysold", "units_sold",
            "unitssold", "units", "count", "orders", "sold", "num_sold",
            "numsold", "volume"
    );

    private static final Set<String> COST_HINTS = Set.of(
            "cost", "unit_cost", "unitcost", "cogs", "food_cost", "foodcost",
            "cost_price", "costprice", "expense", "material_cost", "materialcost"
    );

    // ═══════════════════════════════════════════════════════════════════
    //  SMART COLUMN DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Find the BEST numeric column for the primary metric.
     * Priority: name-hinted metric columns > non-ID numeric columns > any numeric column.
     * Excludes ID-like columns (Store, ID, etc.) from being the primary metric.
     */
    public String findMetricColumn(List<Map<String, String>> columns, List<Map<String, String>> rows) {
        List<String> numericCols = columns.stream()
                .filter(c -> "numeric".equalsIgnoreCase(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        if (numericCols.isEmpty()) return null;

        // 1st pass: find columns whose name matches known metric hints
        for (String col : numericCols) {
            String lower = col.toLowerCase().replaceAll("[_\\-\\s]+", "_");
            for (String hint : METRIC_COLUMN_HINTS) {
                if (lower.contains(hint)) {
                    log.debug("Metric column selected by name hint: {} (matched '{}')", col, hint);
                    return col;
                }
            }
        }

        // 2nd pass: exclude ID-like columns (low cardinality relative to rows, or name hints)
        List<String> nonIdCols = numericCols.stream()
                .filter(col -> !isIdLikeColumn(col, rows))
                .toList();

        if (!nonIdCols.isEmpty()) {
            // Pick the one with the highest variance (most interesting data)
            return nonIdCols.stream()
                    .max(Comparator.comparingDouble(col -> computeVariance(rows, col)))
                    .orElse(nonIdCols.get(0));
        }

        // Fallback: return the first numeric column
        return numericCols.get(0);
    }

    /**
     * Check if a numeric column is likely an ID/key rather than a metric.
     */
    private boolean isIdLikeColumn(String colName, List<Map<String, String>> rows) {
        String lower = colName.toLowerCase();
        // Name-based heuristic
        if (lower.endsWith("id") || lower.endsWith("_id") || lower.equals("store")
                || lower.equals("index") || lower.equals("row") || lower.equals("no")
                || lower.equals("number") || lower.equals("serial")) {
            return true;
        }
        // Check if values form a small integer set (likely IDs)
        int sampleSize = Math.min(200, rows.size());
        long distinctCount = rows.subList(0, sampleSize).stream()
                .map(r -> r.getOrDefault(colName, ""))
                .filter(v -> !v.trim().isEmpty())
                .distinct()
                .count();
        // If very few distinct values relative to sample, might be an ID/category
        return distinctCount <= 10 && distinctCount < sampleSize * 0.05;
    }

    private double computeVariance(List<Map<String, String>> rows, String col) {
        int sampleSize = Math.min(200, rows.size());
        double[] values = rows.subList(0, sampleSize).stream()
                .mapToDouble(r -> parseNumeric(r.get(col)))
                .toArray();
        double mean = Arrays.stream(values).average().orElse(0);
        return Arrays.stream(values).map(v -> (v - mean) * (v - mean)).average().orElse(0);
    }

    /**
     * Find the best categorical column.
     * Prefers columns with name hints, then columns with moderate cardinality.
     */
    public String findCategoricalColumn(List<Map<String, String>> columns, List<Map<String, String>> rows) {
        List<String> categoricalCols = columns.stream()
                .filter(c -> "categorical".equalsIgnoreCase(c.get("type")))
                .map(c -> c.get("name"))
                .toList();

        // Also consider numeric columns that are really categories (e.g., Store=1,2,3)
        List<String> allCandidates = new ArrayList<>(categoricalCols);
        columns.stream()
                .filter(c -> "numeric".equalsIgnoreCase(c.get("type")))
                .map(c -> c.get("name"))
                .filter(name -> isIdLikeColumn(name, rows))
                .forEach(allCandidates::add);

        if (allCandidates.isEmpty()) return null;

        // Prefer name-hinted columns
        for (String col : allCandidates) {
            String lower = col.toLowerCase().replaceAll("[_\\-\\s]+", "");
            for (String hint : CATEGORICAL_COLUMN_HINTS) {
                if (lower.contains(hint)) {
                    log.debug("Categorical column selected by name hint: {} (matched '{}')", col, hint);
                    return col;
                }
            }
        }

        // Pick the one with best cardinality for charting (2-50 distinct values)
        return allCandidates.stream()
                .max(Comparator.comparingInt(col -> {
                    int sampleSize = Math.min(500, rows.size());
                    int distinct = (int) rows.subList(0, sampleSize).stream()
                            .map(r -> r.getOrDefault(col, "").trim())
                            .filter(v -> !v.isEmpty())
                            .distinct()
                            .count();
                    // Sweet spot: 2-30 categories. Penalize too many or too few.
                    if (distinct >= 2 && distinct <= 30) return 100 - distinct;
                    if (distinct > 30) return 30 - distinct; // negative, penalized
                    return -100; // only 1 distinct value, useless
                }))
                .orElse(allCandidates.get(0));
    }

    /**
     * Find the date column.
     * Uses both type inference AND column name hints.
     * Also detects month-year formats (e.g., "Jan-2024") that the type inferrer misses.
     */
    public String findDateColumn(List<Map<String, String>> columns, List<Map<String, String>> rows) {
        // First: check columns tagged as "date" by the type inferrer
        for (Map<String, String> col : columns) {
            if ("date".equalsIgnoreCase(col.get("type"))) {
                return col.get("name");
            }
        }

        // Second: check column names for date hints and verify data can be parsed
        for (Map<String, String> col : columns) {
            String name = col.get("name");
            String lower = name.toLowerCase().replaceAll("[_\\-\\s]+", "");
            boolean nameHints = DATE_COLUMN_HINTS.stream().anyMatch(lower::contains);

            if (nameHints) {
                // Try to parse a few sample values
                int sampleSize = Math.min(10, rows.size());
                long parseable = rows.subList(0, sampleSize).stream()
                        .map(r -> r.getOrDefault(name, ""))
                        .filter(v -> !v.trim().isEmpty())
                        .filter(v -> parseToSortableDate(v) != null)
                        .count();
                if (parseable >= sampleSize * 0.5) {
                    log.debug("Date column detected by name hint + parsing: {}", name);
                    return name;
                }
            }
        }

        // Third: brute-force check all non-numeric columns
        for (Map<String, String> col : columns) {
            String type = col.get("type");
            if ("numeric".equalsIgnoreCase(type)) continue;
            String name = col.get("name");
            int sampleSize = Math.min(20, rows.size());
            long parseable = rows.subList(0, sampleSize).stream()
                    .map(r -> r.getOrDefault(name, ""))
                    .filter(v -> !v.trim().isEmpty())
                    .filter(v -> parseToSortableDate(v) != null)
                    .count();
            if (parseable >= sampleSize * 0.6) {
                log.debug("Date column detected by brute-force parsing: {}", name);
                return name;
            }
        }

        return null;
    }

    /**
     * Return ALL numeric column names (for multi-metric support).
     */
    public List<String> findAllNumericColumns(List<Map<String, String>> columns) {
        return columns.stream()
                .filter(c -> "numeric".equalsIgnoreCase(c.get("type")))
                .map(c -> c.get("name"))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PARSING HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Parse string to double safely.
     */
    public double parseNumeric(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").replace("$", "").replace("€", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Parse a date string into a sortable key (yyyy-MM-dd or yyyy-MM).
     * Supports both full dates and month-year formats.
     */
    public String parseToSortableDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String trimmed = value.trim();

        // Try full date formats first (these have day)
        for (DateTimeFormatter fmt : FULL_DATE_FORMATS) {
            try {
                LocalDate date = LocalDate.parse(trimmed, fmt);
                return date.toString(); // yyyy-MM-dd
            } catch (DateTimeParseException ignored) {}
        }

        // Try month-year formats (no day)
        for (DateTimeFormatter fmt : MONTH_FORMATS) {
            try {
                YearMonth ym = YearMonth.parse(trimmed, fmt);
                return ym.toString(); // yyyy-MM
            } catch (DateTimeParseException ignored) {}
        }

        return null;
    }

    /**
     * Parse to LocalDate for sorting (month-year formats get day=1).
     */
    public LocalDate parseToLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String trimmed = value.trim();

        for (DateTimeFormatter fmt : FULL_DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {}
        }

        for (DateTimeFormatter fmt : MONTH_FORMATS) {
            try {
                YearMonth ym = YearMonth.parse(trimmed, fmt);
                return ym.atDay(1);
            } catch (DateTimeParseException ignored) {}
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  AGGREGATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Aggregate by date: sum metric per date, sorted chronologically.
     * For large datasets (>100 dates), auto-buckets to weekly or monthly.
     */
    public LinkedHashMap<String, Double> aggregateByDate(List<Map<String, String>> rows,
                                                          String dateCol, String metricCol) {
        // Group by raw date → sum
        Map<String, Double> rawGrouped = new HashMap<>();
        Map<String, LocalDate> dateMap = new HashMap<>();

        for (Map<String, String> row : rows) {
            String rawDate = row.get(dateCol);
            if (rawDate == null || rawDate.trim().isEmpty()) continue;
            rawDate = rawDate.trim();

            LocalDate parsed = parseToLocalDate(rawDate);
            if (parsed == null) continue;

            String key = parsed.toString(); // normalize to yyyy-MM-dd
            dateMap.put(key, parsed);
            rawGrouped.merge(key, parseNumeric(row.get(metricCol)), Double::sum);
        }

        if (rawGrouped.isEmpty()) return new LinkedHashMap<>();

        // Determine granularity: if too many distinct dates, bucket
        int distinctDates = rawGrouped.size();
        if (distinctDates > 90) {
            // Monthly bucketing
            return bucketMonthly(rawGrouped, dateMap);
        } else if (distinctDates > 30) {
            // Weekly bucketing
            return bucketWeekly(rawGrouped, dateMap);
        }

        // Sort by date
        return rawGrouped.entrySet().stream()
                .sorted(Comparator.comparing(e -> dateMap.get(e.getKey())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    private LinkedHashMap<String, Double> bucketMonthly(Map<String, Double> rawGrouped,
                                                         Map<String, LocalDate> dateMap) {
        Map<String, Double> monthly = new TreeMap<>();
        for (Map.Entry<String, Double> entry : rawGrouped.entrySet()) {
            LocalDate d = dateMap.get(entry.getKey());
            String monthKey = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            monthly.merge(monthKey, entry.getValue(), Double::sum);
        }
        return new LinkedHashMap<>(monthly); // TreeMap is already sorted
    }

    private LinkedHashMap<String, Double> bucketWeekly(Map<String, Double> rawGrouped,
                                                        Map<String, LocalDate> dateMap) {
        Map<String, Double> weekly = new TreeMap<>();
        for (Map.Entry<String, Double> entry : rawGrouped.entrySet()) {
            LocalDate d = dateMap.get(entry.getKey());
            int weekNum = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            String weekKey = d.getYear() + "-W" + String.format("%02d", weekNum);
            weekly.merge(weekKey, entry.getValue(), Double::sum);
        }
        return new LinkedHashMap<>(weekly);
    }

    /**
     * Aggregate by category: sum metric per category, sorted descending.
     */
    public LinkedHashMap<String, Double> aggregateByCategory(List<Map<String, String>> rows,
                                                              String categoryCol, String metricCol) {
        Map<String, Double> grouped = new HashMap<>();
        for (Map<String, String> row : rows) {
            String cat = row.get(categoryCol);
            if (cat == null || cat.trim().isEmpty()) continue;
            grouped.merge(cat.trim(), parseNumeric(row.get(metricCol)), Double::sum);
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TREND ANALYSIS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Compute growth rate from aggregated date series: ((last - first) / |first|) * 100
     */
    public double computeGrowthRate(LinkedHashMap<String, Double> dateSeries) {
        if (dateSeries == null || dateSeries.size() < 2) return 0.0;
        List<Double> values = new ArrayList<>(dateSeries.values());
        double first = values.get(0);
        double last = values.get(values.size() - 1);
        if (first == 0) return last > 0 ? 100.0 : 0.0;
        return Math.round(((last - first) / Math.abs(first)) * 10000.0) / 100.0;
    }

    /**
     * Detect trend using linear regression on aggregated series.
     * Returns "increasing", "decreasing", or "stable".
     */
    public String detectTrend(LinkedHashMap<String, Double> dateSeries) {
        if (dateSeries == null || dateSeries.size() < 2) return "stable";
        List<Double> values = new ArrayList<>(dateSeries.values());
        int n = values.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values.get(i);
            sumXY += i * values.get(i);
            sumX2 += (double) i * i;
        }

        double denom = n * sumX2 - sumX * sumX;
        if (denom == 0) return "stable";

        double slope = (n * sumXY - sumX * sumY) / denom;
        double avgVal = sumY / n;
        if (avgVal == 0) return "stable";

        double normalizedSlope = slope / avgVal;
        if (normalizedSlope > 0.015) return "increasing";
        if (normalizedSlope < -0.015) return "decreasing";
        return "stable";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  TOP SEGMENT (NEVER returns N/A when data exists)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Find the top segment. NEVER returns "N/A" if there's any data.
     * Fallback chain:
     *  1. Category with highest sum of metric
     *  2. Numeric column with highest total (if no categories)
     *  3. Description of the largest contributor
     */
    public String findTopSegment(List<Map<String, String>> rows,
                                  List<Map<String, String>> columns) {
        String metricCol = findMetricColumn(columns, rows);
        String categoryCol = findCategoricalColumn(columns, rows);

        // Primary: use categorical grouping
        if (categoryCol != null && metricCol != null) {
            LinkedHashMap<String, Double> catTotals = aggregateByCategory(rows, categoryCol, metricCol);
            if (!catTotals.isEmpty()) {
                return catTotals.entrySet().iterator().next().getKey();
            }
        }

        // Fallback: if no categorical but numeric exists, identify highest-total column
        if (metricCol != null) {
            List<String> allNumeric = findAllNumericColumns(columns);
            if (allNumeric.size() > 1) {
                String best = allNumeric.stream()
                        .max(Comparator.comparingDouble(col -> computeTotal(rows, col)))
                        .orElse(metricCol);
                return "Highest Total: " + best;
            }
            return "Primary Metric: " + metricCol;
        }

        return "Dataset";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  BASIC COMPUTATIONS
    // ═══════════════════════════════════════════════════════════════════

    public double computeTotal(List<Map<String, String>> rows, String col) {
        return rows.stream().mapToDouble(r -> parseNumeric(r.get(col))).sum();
    }

    public double computeAverage(List<Map<String, String>> rows, String col) {
        return rows.stream().mapToDouble(r -> parseNumeric(r.get(col))).average().orElse(0.0);
    }

    public double computeMax(List<Map<String, String>> rows, String col) {
        return rows.stream().mapToDouble(r -> parseNumeric(r.get(col))).max().orElse(0.0);
    }

    public double computeMin(List<Map<String, String>> rows, String col) {
        return rows.stream().mapToDouble(r -> parseNumeric(r.get(col)))
                .filter(v -> v != 0.0).min().orElse(0.0);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  KPI BUILDER
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build KPI map matching the frontend's expected structure.
     * All descriptions are short and clear.
     */
    public Map<String, Object> buildKpis(List<Map<String, String>> rows,
                                          List<Map<String, String>> columns) {
        String metricCol = findMetricColumn(columns, rows);
        String categoryCol = findCategoricalColumn(columns, rows);
        String dateCol = findDateColumn(columns, rows);

        Map<String, Object> kpis = new LinkedHashMap<>();

        if (metricCol != null) {
            double total = computeTotal(rows, metricCol);
            double average = computeAverage(rows, metricCol);

            kpis.put("total", Map.of(
                    "value", round2(total),
                    "description", "Sum of all " + metricCol + " values across the dataset."
            ));
            kpis.put("average", Map.of(
                    "value", round2(average),
                    "description", "Average " + metricCol + " per record."
            ));
            kpis.put("metricName", metricCol);
        } else {
            kpis.put("total", Map.of("value", 0, "description", "No numeric metric column detected."));
            kpis.put("average", Map.of("value", 0, "description", "No numeric metric column detected."));
            kpis.put("metricName", "N/A");
        }

        // Growth
        if (dateCol != null && metricCol != null) {
            LinkedHashMap<String, Double> dateSeries = aggregateByDate(rows, dateCol, metricCol);
            double growth = computeGrowthRate(dateSeries);
            kpis.put("growth", Map.of(
                    "value", growth,
                    "description", "Change from earliest to latest period."
            ));
        } else {
            kpis.put("growth", Map.of("value", 0.0, "description", "No date column for growth calculation."));
        }

        // Top segment (NEVER N/A)
        String topSegment = findTopSegment(rows, columns);
        kpis.put("topCategory", Map.of(
                "value", topSegment,
                "description", "Category contributing highest total value."
        ));

        log.info("KPIs built: metric={}, dateCol={}, categoryCol={}, topSegment={}",
                metricCol, dateCol, categoryCol, topSegment);
        return kpis;
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RESTAURANT-SPECIFIC COLUMN DETECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Find the column containing menu item names.
     * Looks for "Item Name", "Product", "Dish", "Food", etc.
     */
    public String findItemNameColumn(List<Map<String, String>> columns,
                                      List<Map<String, String>> rows) {
        return findColumnByHints(columns, rows, ITEM_NAME_HINTS, false);
    }

    /**
     * Find the price column.
     * Looks for "Price", "Unit Price", "Selling Price", etc.
     */
    public String findPriceColumn(List<Map<String, String>> columns,
                                   List<Map<String, String>> rows) {
        return findColumnByHints(columns, rows, PRICE_HINTS, true);
    }

    /**
     * Find the quantity sold column.
     * Looks for "Quantity Sold", "Qty", "Units Sold", etc.
     */
    public String findQuantityColumn(List<Map<String, String>> columns,
                                      List<Map<String, String>> rows) {
        return findColumnByHints(columns, rows, QUANTITY_HINTS, true);
    }

    /**
     * Find the cost column (optional — may return null).
     * Looks for "Cost", "COGS", "Food Cost", etc.
     */
    public String findCostColumn(List<Map<String, String>> columns,
                                  List<Map<String, String>> rows) {
        return findColumnByHints(columns, rows, COST_HINTS, true);
    }

    /**
     * Generic column finder by hint set.
     * @param requireNumeric if true, only considers numeric columns
     */
    private String findColumnByHints(List<Map<String, String>> columns,
                                      List<Map<String, String>> rows,
                                      Set<String> hints,
                                      boolean requireNumeric) {
        for (Map<String, String> col : columns) {
            String name = col.get("name");
            String type = col.get("type");
            if (requireNumeric && !"numeric".equalsIgnoreCase(type)) continue;
            if (!requireNumeric && "numeric".equalsIgnoreCase(type)) continue;

            String normalized = name.toLowerCase().replaceAll("[_\\-\\s]+", "_").trim();
            String compact = normalized.replace("_", "");

            for (String hint : hints) {
                if (normalized.equals(hint) || compact.equals(hint.replace("_", ""))
                        || normalized.contains(hint) || compact.contains(hint.replace("_", ""))) {
                    log.debug("Restaurant column detected: {} → hint '{}'", name, hint);
                    return name;
                }
            }
        }

        // Fallback for item name: use first categorical column
        if (!requireNumeric) {
            String catCol = findCategoricalColumn(columns, rows);
            if (catCol != null) {
                log.debug("Fallback item name column: {}", catCol);
                return catCol;
            }
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  RESTAURANT AGGREGATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Aggregate item-level performance: quantity, revenue, profit per item.
     * Returns sorted by revenue descending.
     */
    public List<ItemPerformance> aggregateItemPerformance(List<Map<String, String>> rows,
                                                           List<Map<String, String>> columns) {
        String itemCol = findItemNameColumn(columns, rows);
        String priceCol = findPriceColumn(columns, rows);
        String qtyCol = findQuantityColumn(columns, rows);
        String costCol = findCostColumn(columns, rows);
        String catCol = findCategoricalColumn(columns, rows);

        if (itemCol == null) {
            log.warn("No item name column found for restaurant analysis");
            return List.of();
        }

        // Aggregate by item
        Map<String, int[]> qtyMap = new LinkedHashMap<>();     // item → totalQty
        Map<String, double[]> revenueMap = new LinkedHashMap<>(); // item → totalRevenue
        Map<String, double[]> profitMap = new LinkedHashMap<>();  // item → totalProfit
        Map<String, String> categoryMap = new LinkedHashMap<>();  // item → category

        for (Map<String, String> row : rows) {
            String item = row.get(itemCol);
            if (item == null || item.trim().isEmpty()) continue;
            item = item.trim();

            double price = priceCol != null ? parseNumeric(row.get(priceCol)) : 0;
            double qty = qtyCol != null ? parseNumeric(row.get(qtyCol)) : 1;
            double cost = costCol != null ? parseNumeric(row.get(costCol)) : 0;
            double revenue = price * qty;
            double profit = (price - cost) * qty;

            qtyMap.computeIfAbsent(item, k -> new int[]{0})[0] += (int) qty;
            revenueMap.computeIfAbsent(item, k -> new double[]{0})[0] += revenue;
            profitMap.computeIfAbsent(item, k -> new double[]{0})[0] += profit;

            if (catCol != null && !categoryMap.containsKey(item)) {
                String cat = row.get(catCol);
                if (cat != null && !cat.trim().isEmpty()) {
                    categoryMap.put(item, cat.trim());
                }
            }
        }

        // Compute grand total revenue for contribution %
        double grandTotalRevenue = revenueMap.values().stream()
                .mapToDouble(a -> a[0]).sum();

        // Build ItemPerformance list, sorted by revenue desc
        List<ItemPerformance> items = new ArrayList<>();
        for (String item : revenueMap.keySet()) {
            double rev = revenueMap.get(item)[0];
            double prof = profitMap.getOrDefault(item, new double[]{0})[0];
            int qty = qtyMap.getOrDefault(item, new int[]{0})[0];
            double contributionPct = grandTotalRevenue > 0 ? (rev / grandTotalRevenue) * 100.0 : 0;
            double marginPct = rev > 0 ? (prof / rev) * 100.0 : 0;
            String category = categoryMap.getOrDefault(item, "");

            items.add(new ItemPerformance(
                    item, category, qty, round2(rev), round2(prof),
                    round2(contributionPct), round2(marginPct)
            ));
        }

        items.sort((a, b) -> Double.compare(b.getRevenue(), a.getRevenue()));
        return items;
    }

    /**
     * Aggregate by month for restaurant analysis.
     * Returns: revenue, profit, items sold per month — sorted chronologically.
     */
    public List<MonthlyBreakdown> aggregateByMonthRestaurant(List<Map<String, String>> rows,
                                                              List<Map<String, String>> columns) {
        String dateCol = findDateColumn(columns, rows);
        String priceCol = findPriceColumn(columns, rows);
        String qtyCol = findQuantityColumn(columns, rows);
        String costCol = findCostColumn(columns, rows);

        if (dateCol == null || priceCol == null) {
            log.warn("Cannot compute monthly breakdown: dateCol={}, priceCol={}", dateCol, priceCol);
            return List.of();
        }

        // month → [revenue, profit, itemsSold]
        Map<String, double[]> monthData = new TreeMap<>();

        for (Map<String, String> row : rows) {
            String rawDate = row.get(dateCol);
            if (rawDate == null || rawDate.trim().isEmpty()) continue;

            LocalDate parsed = parseToLocalDate(rawDate.trim());
            if (parsed == null) continue;

            String monthKey = parsed.getYear() + "-" + String.format("%02d", parsed.getMonthValue());

            double price = parseNumeric(row.get(priceCol));
            double qty = qtyCol != null ? parseNumeric(row.get(qtyCol)) : 1;
            double cost = costCol != null ? parseNumeric(row.get(costCol)) : 0;
            double revenue = price * qty;
            double profit = (price - cost) * qty;

            double[] data = monthData.computeIfAbsent(monthKey, k -> new double[3]);
            data[0] += revenue;
            data[1] += profit;
            data[2] += qty;
        }

        List<MonthlyBreakdown> result = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : monthData.entrySet()) {
            double[] d = entry.getValue();
            result.add(new MonthlyBreakdown(
                    entry.getKey(), round2(d[0]), round2(d[1]), (int) d[2]
            ));
        }

        return result;
    }
}
