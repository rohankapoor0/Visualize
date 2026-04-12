package com.insightify.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Utility class that infers column data types from sample values.
 * Supports: numeric, categorical, date (including month-year formats).
 */
public class ColumnTypeInferrer {

    // Full date formats (with day)
    private static final List<DateTimeFormatter> FULL_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy")
    );

    // Month-year formats (no day — YearMonth parsing)
    private static final List<DateTimeFormatter> MONTH_YEAR_FORMATS = List.of(
            DateTimeFormatter.ofPattern("MMM-yyyy"),
            DateTimeFormatter.ofPattern("MMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("MM/yyyy")
    );

    // Column names that strongly hint at dates
    private static final Set<String> DATE_NAME_HINTS = Set.of(
            "date", "month", "year", "period", "time", "day", "week", "quarter"
    );

    /**
     * Infer the type of a column based on sample values and column name.
     */
    public static String inferType(String columnName, List<String> values) {
        if (values == null || values.isEmpty()) return "categorical";

        List<String> nonEmpty = values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .toList();
        if (nonEmpty.isEmpty()) return "categorical";

        // Check dates FIRST (before numeric) — this prevents "2024" being classified as numeric
        // when it's part of a date column
        boolean nameHintsDate = columnName != null &&
                DATE_NAME_HINTS.stream().anyMatch(h ->
                        columnName.toLowerCase().replaceAll("[_\\-\\s]+", "").contains(h));

        long dateCount = nonEmpty.stream().filter(ColumnTypeInferrer::isDate).count();
        double dateRatio = (double) dateCount / nonEmpty.size();

        // If name hints at date, use a lower threshold
        if (nameHintsDate && dateRatio >= 0.3) return "date";
        if (dateRatio >= 0.5) return "date";

        // Check numeric
        long numericCount = nonEmpty.stream().filter(ColumnTypeInferrer::isNumeric).count();
        if (numericCount >= nonEmpty.size() * 0.5) return "numeric";

        // Categorical: distinct count check
        long distinctCount = nonEmpty.stream().distinct().count();
        if (distinctCount <= 50 || distinctCount < nonEmpty.size() * 0.4) return "categorical";

        return "categorical";
    }

    /**
     * Backward-compatible overload (without column name).
     */
    public static String inferType(List<String> values) {
        return inferType(null, values);
    }

    /**
     * Infer types for all columns in a dataset.
     */
    public static List<Map<String, String>> inferAllColumnTypes(
            List<String> headers, List<Map<String, String>> rows) {

        List<Map<String, String>> metadata = new ArrayList<>();

        int sampleSize = Math.min(1000, rows.size());
        List<Map<String, String>> sampleRows = rows.subList(0, sampleSize);

        for (String header : headers) {
            List<String> values = sampleRows.stream()
                    .map(row -> row.getOrDefault(header, ""))
                    .toList();

            Map<String, String> colMeta = new LinkedHashMap<>();
            colMeta.put("name", header);
            colMeta.put("type", inferType(header, values)); // pass column name for hint
            metadata.add(colMeta);
        }

        return metadata;
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value.replace(",", "").replace("$", "").replace("€", "").trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDate(String value) {
        String trimmed = value.trim();

        // Try full date formats
        for (DateTimeFormatter fmt : FULL_DATE_FORMATS) {
            try {
                LocalDate.parse(trimmed, fmt);
                return true;
            } catch (DateTimeParseException ignored) {}
        }

        // Try month-year formats (YearMonth)
        for (DateTimeFormatter fmt : MONTH_YEAR_FORMATS) {
            try {
                YearMonth.parse(trimmed, fmt);
                return true;
            } catch (DateTimeParseException ignored) {}
        }

        return false;
    }
}
