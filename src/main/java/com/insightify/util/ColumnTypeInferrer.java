package com.insightify.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Utility class that infers column data types from string values.
 * Supports: numeric, categorical, date
 */
public class ColumnTypeInferrer {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MMM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    );

    /**
     * Infer the type of a column based on sample values.
     *
     * @param values sample values from the column
     * @return "numeric", "date", or "categorical"
     */
    public static String inferType(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "categorical";
        }

        // Filter out nulls and blanks
        List<String> nonEmpty = values.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .toList();

        if (nonEmpty.isEmpty()) {
            return "categorical";
        }

        // Check if majority are numeric
        long numericCount = nonEmpty.stream().filter(ColumnTypeInferrer::isNumeric).count();
        if (numericCount >= nonEmpty.size() * 0.8) {
            return "numeric";
        }

        // Check if majority are dates
        long dateCount = nonEmpty.stream().filter(ColumnTypeInferrer::isDate).count();
        if (dateCount >= nonEmpty.size() * 0.8) {
            return "date";
        }

        return "categorical";
    }

    /**
     * Infer types for all columns in a dataset.
     *
     * @param headers  column names
     * @param rows     list of rows, each row is a map of column -> value
     * @return list of column metadata maps with "name" and "type"
     */
    public static List<Map<String, String>> inferAllColumnTypes(
            List<String> headers, List<Map<String, String>> rows) {

        List<Map<String, String>> metadata = new ArrayList<>();

        for (String header : headers) {
            List<String> values = rows.stream()
                    .map(row -> row.getOrDefault(header, ""))
                    .toList();

            Map<String, String> colMeta = new LinkedHashMap<>();
            colMeta.put("name", header);
            colMeta.put("type", inferType(values));
            metadata.add(colMeta);
        }

        return metadata;
    }

    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value.replace(",", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDate(String value) {
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                LocalDate.parse(value.trim(), fmt);
                return true;
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return false;
    }
}
