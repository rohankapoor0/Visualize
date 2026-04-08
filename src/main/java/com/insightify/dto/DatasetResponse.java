package com.insightify.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for dataset information.
 * Excludes raw data to keep responses lightweight.
 */
public class DatasetResponse {

    private Long id;
    private String name;
    private String originalFilename;
    private int rowCount;
    private int columnCount;
    private Object columnMetadata; // Deserialized JSON
    private LocalDateTime createdAt;

    // --- Constructors ---

    public DatasetResponse() {}

    public DatasetResponse(Long id, String name, String originalFilename,
                           int rowCount, int columnCount, Object columnMetadata,
                           LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.originalFilename = originalFilename;
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.columnMetadata = columnMetadata;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public int getColumnCount() { return columnCount; }
    public void setColumnCount(int columnCount) { this.columnCount = columnCount; }

    public Object getColumnMetadata() { return columnMetadata; }
    public void setColumnMetadata(Object columnMetadata) { this.columnMetadata = columnMetadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
