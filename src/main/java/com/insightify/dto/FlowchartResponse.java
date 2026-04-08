package com.insightify.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for flowchart structures.
 */
public class FlowchartResponse {

    private Long id;
    private Long datasetId;
    private String query;
    private Object structure; // Deserialized nodes + edges JSON
    private LocalDateTime createdAt;

    public FlowchartResponse() {}

    public FlowchartResponse(Long id, Long datasetId, String query,
                             Object structure, LocalDateTime createdAt) {
        this.id = id;
        this.datasetId = datasetId;
        this.query = query;
        this.structure = structure;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Object getStructure() { return structure; }
    public void setStructure(Object structure) { this.structure = structure; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
