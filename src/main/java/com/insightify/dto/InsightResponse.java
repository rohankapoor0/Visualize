package com.insightify.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for AI-generated insights.
 */
public class InsightResponse {

    private Long id;
    private Long datasetId;
    private String summary;
    private Object details; // Deserialized insight details JSON
    private LocalDateTime createdAt;

    public InsightResponse() {}

    public InsightResponse(Long id, Long datasetId, String summary,
                           Object details, LocalDateTime createdAt) {
        this.id = id;
        this.datasetId = datasetId;
        this.summary = summary;
        this.details = details;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
