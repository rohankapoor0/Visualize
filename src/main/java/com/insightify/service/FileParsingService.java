package com.insightify.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Handles parsing of uploaded files (CSV, Excel, plain text) into a
 * uniform List<Map<String, String>> structure where each map is a row.
 */
@Service
public class FileParsingService {

    private static final Logger log = LoggerFactory.getLogger(FileParsingService.class);

    /**
     * Parse an uploaded file into rows of column->value maps.
     *
     * @param file the uploaded multipart file
     * @return ParseResult containing headers and rows
     */
    public ParseResult parse(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File must have a name");
        }

        String lower = filename.toLowerCase();

        try {
            if (lower.endsWith(".csv")) {
                return parseCsv(file.getInputStream());
            } else if (lower.endsWith(".xlsx")) {
                return parseExcel(file.getInputStream(), true);
            } else if (lower.endsWith(".xls")) {
                return parseExcel(file.getInputStream(), false);
            } else if (lower.endsWith(".txt") || lower.endsWith(".tsv")) {
                return parseTsv(file.getInputStream());
            } else {
                throw new IllegalArgumentException(
                        "Unsupported file type: " + filename +
                        ". Supported: .csv, .xlsx, .xls, .txt, .tsv");
            }
        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
            throw new RuntimeException("Failed to parse file: " + e.getMessage(), e);
        }
    }

    // ----- CSV Parsing -----

    private ParseResult parseCsv(InputStream inputStream) throws IOException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            List<String[]> allLines = reader.readAll();
            if (allLines.isEmpty()) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headerArray = allLines.get(0);
            List<String> headers = Arrays.stream(headerArray)
                    .map(String::trim)
                    .toList();

            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < allLines.size(); i++) {
                String[] line = allLines.get(i);
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String value = (j < line.length) ? line[j].trim() : "";
                    row.put(headers.get(j), value);
                }
                rows.add(row);
            }

            log.info("Parsed CSV: {} columns, {} rows", headers.size(), rows.size());
            return new ParseResult(headers, rows);

        } catch (CsvException e) {
            throw new IOException("CSV parsing error: " + e.getMessage(), e);
        }
    }

    // ----- TSV / Text Parsing -----

    private ParseResult parseTsv(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = br.lines().filter(l -> !l.isBlank()).toList();
            if (lines.isEmpty()) {
                throw new IllegalArgumentException("Text file is empty");
            }

            // Auto-detect delimiter: tab, pipe, or comma
            String delimiter = detectDelimiter(lines.get(0));

            String[] headerArray = lines.get(0).split(delimiter);
            List<String> headers = Arrays.stream(headerArray)
                    .map(String::trim)
                    .toList();

            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(delimiter);
                Map<String, String> row = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String value = (j < parts.length) ? parts[j].trim() : "";
                    row.put(headers.get(j), value);
                }
                rows.add(row);
            }

            log.info("Parsed text file: {} columns, {} rows", headers.size(), rows.size());
            return new ParseResult(headers, rows);
        }
    }

    private String detectDelimiter(String firstLine) {
        if (firstLine.contains("\t")) return "\t";
        if (firstLine.contains("|")) return "\\|";
        return ",";
    }

    // ----- Excel Parsing -----

    private ParseResult parseExcel(InputStream inputStream, boolean isXlsx) throws IOException {
        Workbook workbook = isXlsx
                ? new XSSFWorkbook(inputStream)
                : new HSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet.getPhysicalNumberOfRows() == 0) {
            workbook.close();
            throw new IllegalArgumentException("Excel file is empty");
        }

        // Read headers from first row
        Row headerRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        for (int j = 0; j < headerRow.getLastCellNum(); j++) {
            Cell cell = headerRow.getCell(j);
            headers.add(cell != null ? getCellValueAsString(cell) : "Column_" + j);
        }

        // Read data rows
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int j = 0; j < headers.size(); j++) {
                Cell cell = row.getCell(j);
                rowMap.put(headers.get(j), cell != null ? getCellValueAsString(cell) : "");
            }
            rows.add(rowMap);
        }

        workbook.close();
        log.info("Parsed Excel: {} columns, {} rows", headers.size(), rows.size());
        return new ParseResult(headers, rows);
    }

    private String getCellValueAsString(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double val = cell.getNumericCellValue();
                // Return integers without decimal point
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    // ----- Result Container -----

    /**
     * Container for parsed file data: headers + rows.
     */
    public static class ParseResult {
        private final List<String> headers;
        private final List<Map<String, String>> rows;

        public ParseResult(List<String> headers, List<Map<String, String>> rows) {
            this.headers = headers;
            this.rows = rows;
        }

        public List<String> getHeaders() { return headers; }
        public List<Map<String, String>> getRows() { return rows; }
    }
}
