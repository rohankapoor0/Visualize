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
 * Uses LocalAnalysisService for data-driven flowchart generation.
 */
@Service
public class FlowchartService {

    private static final Logger log = LoggerFactory.getLogger(FlowchartService.class);

    private final DatasetService datasetService;
    private final LocalAnalysisService localAnalysisService;
    private final FlowchartRepository flowchartRepository;
    private final ObjectMapper objectMapper;

    public FlowchartService(DatasetService datasetService,
                            LocalAnalysisService localAnalysisService,
                            FlowchartRepository flowchartRepository,
                            ObjectMapper objectMapper) {
        this.datasetService = datasetService;
        this.localAnalysisService = localAnalysisService;
        this.flowchartRepository = flowchartRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FlowchartResponse generateFlowchart(FlowchartRequest request) {
        Long datasetId = request.getDatasetId();
        String query = request.getQuery();

        if (datasetId == null && (query == null || query.isBlank())) {
            throw new IllegalArgumentException("Either datasetId or query must be provided");
        }

        if (datasetId == null) {
            return createQueryFlowchart(query);
        }

        Optional<Flowchart> existing = flowchartRepository.findFirstByDatasetId(datasetId);
        if (existing.isPresent() && (query == null || query.isBlank())) {
            return toResponse(existing.get());
        }

        datasetService.getDatasetEntity(datasetId);
        List<Map<String, String>> rows = datasetService.getDatasetRows(datasetId);
        List<Map<String, String>> columns = datasetService.getColumnMetadata(datasetId);

        // Locally generated flowchart structure
        Map<String, Object> structure = localAnalysisService.generateFlowchartStructure(rows, columns, query);

        Flowchart flowchart = new Flowchart();
        flowchart.setDatasetId(datasetId);
        flowchart.setQuery(query);
        try {
            flowchart.setStructureJson(objectMapper.writeValueAsString(structure));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize flowchart", e);
        }

        Flowchart saved = flowchartRepository.save(flowchart);
        log.info("Saved locally generated flowchart for dataset {}", datasetId);
        return toResponse(saved);
    }

    @Transactional
    public FlowchartResponse getOrGenerateForDataset(Long datasetId) {
        FlowchartRequest request = new FlowchartRequest();
        request.setDatasetId(datasetId);
        return generateFlowchart(request);
    }

    private FlowchartResponse createQueryFlowchart(String query) {
        Map<String, Object> structure = Map.of(
            "nodes", List.of(Map.of("id", "q", "label", "Query: " + query, "type", "process", "position", Map.of("x", 0, "y", 0))),
            "edges", List.of(),
            "description", "Query flow"
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
