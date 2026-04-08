package com.insightify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a chart configuration generated for a dataset.
 * Stores chart type and the full config as JSON.
 */
@Entity
@Table(name = "charts")
public class Chart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", nullable = false)
    private Long datasetId;

    /** Chart type: bar, line, pie, scatter */
    @Column(nullable = false)
    private String type;

    /** Title of the chart */
    private String title;

    /** Full chart configuration as JSON (labels, datasets, options) */
    @Column(name = "config_json", columnDefinition = "CLOB")
    private String configJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
