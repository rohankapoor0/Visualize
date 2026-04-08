package com.insightify.controller;

import com.insightify.dto.ApiResponse;
import com.insightify.dto.ChartResponse;
import com.insightify.dto.DatasetResponse;
import com.insightify.dto.InsightResponse;
import com.insightify.service.ChartService;
import com.insightify.service.DatasetService;
import com.insightify.service.InsightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for dataset operations:
 * - Upload datasets
 * - List/retrieve datasets
 * - Generate charts and insights per dataset
 */
@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private static final Logger log = LoggerFactory.getLogger(DatasetController.class);

    private final DatasetService datasetService;
    private final ChartService chartService;
    private final InsightService insightService;

    public DatasetController(DatasetService datasetService,
                             ChartService chartService,
                             InsightService insightService) {
        this.datasetService = datasetService;
        this.chartService = chartService;
        this.insightService = insightService;
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
     * Auto-generate chart configurations for a dataset.
     */
    @GetMapping("/{id}/charts")
    public ResponseEntity<ApiResponse<List<ChartResponse>>> getCharts(@PathVariable Long id) {
        List<ChartResponse> charts = chartService.generateCharts(id);
        return ResponseEntity.ok(
                ApiResponse.success("Charts generated successfully", charts));
    }

    /**
     * GET /api/datasets/{id}/insights
     * Generate AI-powered insights for a dataset.
     */
    @GetMapping("/{id}/insights")
    public ResponseEntity<ApiResponse<InsightResponse>> getInsights(@PathVariable Long id) {
        InsightResponse insight = insightService.generateInsights(id);
        return ResponseEntity.ok(
                ApiResponse.success("Insights generated successfully", insight));
    }
}
