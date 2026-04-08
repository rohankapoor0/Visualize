package com.insightify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightify.dto.InsightResponse;
import com.insightify.model.Dataset;
import com.insightify.model.Insight;
import com.insightify.repository.InsightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for generating and managing AI-powered insights.
 */
@Service
public class InsightService {

    private static final Logger log = LoggerFactory.getLogger(InsightService.class);

    private final DatasetService datasetService;
    private final AIService aiService;
    private final InsightRepository insightRepository;
    private final ObjectMapper objectMapper;

    public InsightService(DatasetService datasetService,
                          AIService aiService,
                          InsightRepository insightRepository,
                          ObjectMapper objectMapper) {
        this.datasetService = datasetService;
        this.aiService = aiService;
        this.insightRepository = insightRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate or retrieve insights for a dataset.
     */
    @Transactional
    public InsightResponse generateInsights(Long datasetId) {
        // Return existing if available
        Optional<Insight> existing = insightRepository.findFirstByDatasetId(datasetId);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Dataset dataset = datasetService.getDatasetEntity(datasetId);
        List<Map<String, String>> rows = datasetService.getDatasetRows(datasetId);
        List<Map<String, String>> columns = datasetService.getColumnMetadata(datasetId);

        // Generate using AI service
        String summary = aiService.generateSummary(rows, columns, dataset.getName());
        Map<String, Object> details = aiService.generateDetailedInsights(
                rows, columns, dataset.getName());

        Insight insight = new Insight();
        insight.setDatasetId(datasetId);
        insight.setSummary(summary);
        try {
            insight.setDetails(objectMapper.writeValueAsString(details));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize insights", e);
        }

        Insight saved = insightRepository.save(insight);
        log.info("Generated insights for dataset {}", datasetId);
        return toResponse(saved);
    }

    private InsightResponse toResponse(Insight insight) {
        Object details = null;
        try {
            details = objectMapper.readValue(insight.getDetails(), Object.class);
        } catch (Exception ignored) {}

        return new InsightResponse(
                insight.getId(), insight.getDatasetId(),
                insight.getSummary(), details, insight.getCreatedAt()
        );
    }
}
