package com.insightify.dto;

/**
 * Request DTO for flowchart generation.
 */
public class FlowchartRequest {

    private Long datasetId;
    private String query;

    public FlowchartRequest() {}

    public FlowchartRequest(Long datasetId, String query) {
        this.datasetId = datasetId;
        this.query = query;
    }

    public Long getDatasetId() { return datasetId; }
    public void setDatasetId(Long datasetId) { this.datasetId = datasetId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
