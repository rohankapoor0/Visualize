package com.insightify.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for chart configurations.
 */
public class ChartResponse {

    private Long id;
    private Long datasetId;
    private String type;
    private String title;
    private Object config; // Deserialized chart config JSON
    private LocalDateTime createdAt;

    public ChartResponse() {}

    public ChartResponse(Long id, Long datasetId, String type, String title,
                         Object config, LocalDateTime createdAt) {
        this.id = id;
        this.datasetId = datasetId;
        this.type = type;
        this.title = title;
        this.config = config;
        this.createdAt = createdAt;
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

    public Object getConfig() { return config; }
    public void setConfig(Object config) { this.config = config; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
