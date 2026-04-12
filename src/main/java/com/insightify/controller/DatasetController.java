package com.insightify.controller;

import com.insightify.dto.ApiResponse;
import com.insightify.dto.DatasetResponse;
import com.insightify.service.LocalAnalysisService;
import com.insightify.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for dataset operations.
 * Analytics endpoints use locally computed results — no external APIs.
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    private final DatasetService datasetService;
    private final LocalAnalysisService localAnalysisService;

    public DatasetController(DatasetService datasetService, LocalAnalysisService localAnalysisService) {
        this.datasetService = datasetService;
        this.localAnalysisService = localAnalysisService;
    }

    /**
     * POST /api/datasets/upload
     * Upload a CSV, Excel, or text file.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DatasetResponse>> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name) {

        log.info("Upload request: file={}, size={} bytes",
                file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        DatasetResponse response = datasetService.uploadDataset(file, name);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dataset uploaded successfully", response));
    }

    /**
     * GET /api/datasets
     * List all uploaded datasets.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DatasetResponse>>> listDatasets() {
        List<DatasetResponse> datasets = datasetService.listDatasets();
        return ResponseEntity.ok(ApiResponse.success(datasets));
    }

    /**
     * GET /api/datasets/{id}
     * Get a specific dataset by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DatasetResponse>> getDataset(@PathVariable Long id) {
        DatasetResponse response = datasetService.getDataset(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/datasets/{id}/charts
     * Generate chart data for a dataset (locally computed).
     */
    @GetMapping("/{id}/charts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCharts(@PathVariable Long id) {
        log.info("Generating charts for dataset {}", id);
        List<java.util.Map<String, String>> rows = datasetService.getDatasetRows(id);
        List<java.util.Map<String, String>> columns = datasetService.getColumnMetadata(id);

        List<Map<String, Object>> charts = localAnalysisService.generateCharts(rows, columns);
        return ResponseEntity.ok(ApiResponse.success(charts));
    }

    /**
     * GET /api/datasets/{id}/insights
     * Generate text insights for a dataset (locally computed).
     */
    @GetMapping("/{id}/insights")
    public ResponseEntity<ApiResponse<String>> getInsights(@PathVariable Long id) {
        log.info("Generating insights for dataset {}", id);
        List<java.util.Map<String, String>> rows = datasetService.getDatasetRows(id);
        List<java.util.Map<String, String>> columns = datasetService.getColumnMetadata(id);

        String insights = localAnalysisService.generateInsightsText(rows, columns);
        return ResponseEntity.ok(ApiResponse.success(insights));
    }
}
