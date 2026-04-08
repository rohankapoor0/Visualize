package com.insightify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightify.dto.FlowchartRequest;
import com.insightify.dto.FlowchartResponse;

import com.insightify.model.Flowchart;
import com.insightify.repository.FlowchartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for generating flowchart structures from datasets or queries.
 */
@Service
public class FlowchartService {

    private static final Logger log = LoggerFactory.getLogger(FlowchartService.class);

    private final DatasetService datasetService;
    private final AIService aiService;
    private final FlowchartRepository flowchartRepository;
    private final ObjectMapper objectMapper;

    public FlowchartService(DatasetService datasetService,
                            AIService aiService,
                            FlowchartRepository flowchartRepository,
                            ObjectMapper objectMapper) {
        this.datasetService = datasetService;
        this.aiService = aiService;
        this.flowchartRepository = flowchartRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate a flowchart from a request (datasetId and/or query).
     */
    @Transactional
    public FlowchartResponse generateFlowchart(FlowchartRequest request) {
        Long datasetId = request.getDatasetId();
        String query = request.getQuery();

        if (datasetId == null && (query == null || query.isBlank())) {
            throw new IllegalArgumentException("Either datasetId or query must be provided");
        }

        // If no datasetId, create a generic flowchart from the query
        if (datasetId == null) {
            return createQueryFlowchart(query);
        }

        // Check for existing
        Optional<Flowchart> existing = flowchartRepository.findFirstByDatasetId(datasetId);
        if (existing.isPresent() && (query == null || query.isBlank())) {
            return toResponse(existing.get());
        }

        datasetService.getDatasetEntity(datasetId); // validate dataset exists
        List<Map<String, String>> rows = datasetService.getDatasetRows(datasetId);
        List<Map<String, String>> columns = datasetService.getColumnMetadata(datasetId);

        // Generate flowchart using AI service
        Map<String, Object> structure = aiService.generateFlowchart(rows, columns, query);

        Flowchart flowchart = new Flowchart();
        flowchart.setDatasetId(datasetId);
        flowchart.setQuery(query);
        try {
            flowchart.setStructureJson(objectMapper.writeValueAsString(structure));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize flowchart", e);
        }

        Flowchart saved = flowchartRepository.save(flowchart);
        log.info("Generated flowchart for dataset {} (query: {})", datasetId, query);
        return toResponse(saved);
    }

    /**
     * Get flowchart for a dataset (for full-analysis endpoint).
     */
    @Transactional
    public FlowchartResponse getOrGenerateForDataset(Long datasetId) {
        FlowchartRequest request = new FlowchartRequest();
        request.setDatasetId(datasetId);
        return generateFlowchart(request);
    }

    /**
     * Create a simple flowchart from a query without a dataset.
     */
    private FlowchartResponse createQueryFlowchart(String query) {
        Map<String, Object> structure = Map.of(
                "nodes", List.of(
                        Map.of("id", "start", "label", "Query Received", "type", "input",
                                "position", Map.of("x", 0, "y", 0)),
                        Map.of("id", "process", "label", "Process: " + query, "type", "process",
                                "position", Map.of("x", 0, "y", 100)),
                        Map.of("id", "result", "label", "Return Results", "type", "output",
                                "position", Map.of("x", 0, "y", 200))
                ),
                "edges", List.of(
                        Map.of("id", "e1", "source", "start", "target", "process"),
                        Map.of("id", "e2", "source", "process", "target", "result")
                ),
                "description", "Flowchart for query: " + query
        );

        Flowchart flowchart = new Flowchart();
        flowchart.setQuery(query);
        try {
            flowchart.setStructureJson(objectMapper.writeValueAsString(structure));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize flowchart", e);
        }

        Flowchart saved = flowchartRepository.save(flowchart);
        return toResponse(saved);
    }

    private FlowchartResponse toResponse(Flowchart flowchart) {
        Object structure = null;
        try {
            structure = objectMapper.readValue(flowchart.getStructureJson(), Object.class);
        } catch (Exception ignored) {}

        return new FlowchartResponse(
                flowchart.getId(), flowchart.getDatasetId(),
                flowchart.getQuery(), structure, flowchart.getCreatedAt()
        );
    }
}
