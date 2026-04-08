package com.insightify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a flowchart generated from a dataset or query.
 * Stores nodes and edges as a JSON structure.
 */
@Entity
@Table(name = "flowcharts")
public class Flowchart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id")
    private Long datasetId;

    /** Optional query that triggered this flowchart */
    @Column(columnDefinition = "CLOB")
    private String query;

    /** JSON structure with nodes and edges */
    @Column(name = "structure_json", columnDefinition = "CLOB")
    private String structureJson;

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

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getStructureJson() { return structureJson; }
    public void setStructureJson(String structureJson) { this.structureJson = structureJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
