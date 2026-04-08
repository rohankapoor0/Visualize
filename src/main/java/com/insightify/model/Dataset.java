package com.insightify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an uploaded dataset with parsed content stored as JSON.
 */
@Entity
@Table(name = "datasets")
public class Dataset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Original filename uploaded by the user */
    @Column(name = "original_filename")
    private String originalFilename;

    /** Parsed data stored as a JSON string (array of row objects) */
    @Column(name = "raw_data", columnDefinition = "CLOB")
    private String rawData;

    /** Column metadata stored as JSON (name, inferred type) */
    @Column(name = "column_metadata", columnDefinition = "CLOB")
    private String columnMetadata;

    @Column(name = "row_count")
    private int rowCount;

    @Column(name = "column_count")
    private int columnCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public String getColumnMetadata() { return columnMetadata; }
    public void setColumnMetadata(String columnMetadata) { this.columnMetadata = columnMetadata; }

    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }

    public int getColumnCount() { return columnCount; }
    public void setColumnCount(int columnCount) { this.columnCount = columnCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
