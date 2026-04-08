package com.insightify.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightify.dto.DatasetResponse;
import com.insightify.exception.ResourceNotFoundException;
import com.insightify.model.Dataset;
import com.insightify.repository.DatasetRepository;
import com.insightify.util.ColumnTypeInferrer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Service for uploading, storing, and retrieving datasets.
 */
@Service
public class DatasetService {

    private static final Logger log = LoggerFactory.getLogger(DatasetService.class);

    private final DatasetRepository datasetRepository;
    private final FileParsingService fileParsingService;
    private final ObjectMapper objectMapper;

    public DatasetService(DatasetRepository datasetRepository,
                          FileParsingService fileParsingService,
                          ObjectMapper objectMapper) {
        this.datasetRepository = datasetRepository;
        this.fileParsingService = fileParsingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Upload and process a file into a persisted Dataset.
     */
    public DatasetResponse uploadDataset(MultipartFile file, String name) {
        // 1. Parse the file
        FileParsingService.ParseResult result = fileParsingService.parse(file);
        List<String> headers = result.getHeaders();
        List<Map<String, String>> rows = result.getRows();

        // 2. Infer column types
        List<Map<String, String>> columnMetadata =
                ColumnTypeInferrer.inferAllColumnTypes(headers, rows);

        // 3. Build the Dataset entity
        Dataset dataset = new Dataset();
        dataset.setName(name != null && !name.isBlank() ? name : file.getOriginalFilename());
        dataset.setOriginalFilename(file.getOriginalFilename());
        dataset.setRowCount(rows.size());
        dataset.setColumnCount(headers.size());

        try {
            dataset.setRawData(objectMapper.writeValueAsString(rows));
            dataset.setColumnMetadata(objectMapper.writeValueAsString(columnMetadata));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize dataset: " + e.getMessage(), e);
        }

        // 4. Persist
        Dataset saved = datasetRepository.save(dataset);
        log.info("Dataset uploaded: id={}, name={}, rows={}, cols={}",
                saved.getId(), saved.getName(), saved.getRowCount(), saved.getColumnCount());

        return toResponse(saved);
    }

    /**
     * Retrieve a dataset by ID.
     */
    public Dataset getDatasetEntity(Long id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset", id));
    }

    public DatasetResponse getDataset(Long id) {
        return toResponse(getDatasetEntity(id));
    }

    /**
     * List all datasets (without raw data).
     */
    public List<DatasetResponse> listDatasets() {
        return datasetRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get parsed rows from a dataset.
     */
    public List<Map<String, String>> getDatasetRows(Long id) {
        Dataset dataset = getDatasetEntity(id);
        try {
            return objectMapper.readValue(dataset.getRawData(),
                    new TypeReference<List<Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize dataset rows", e);
        }
    }

    /**
     * Get column metadata from a dataset.
     */
    public List<Map<String, String>> getColumnMetadata(Long id) {
        Dataset dataset = getDatasetEntity(id);
        try {
            return objectMapper.readValue(dataset.getColumnMetadata(),
                    new TypeReference<List<Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize column metadata", e);
        }
    }

    // --- Private helpers ---

    private DatasetResponse toResponse(Dataset d) {
        Object colMeta = null;
        try {
            colMeta = objectMapper.readValue(d.getColumnMetadata(), Object.class);
        } catch (Exception ignored) {}

        return new DatasetResponse(
                d.getId(), d.getName(), d.getOriginalFilename(),
                d.getRowCount(), d.getColumnCount(), colMeta, d.getCreatedAt()
        );
    }
}
