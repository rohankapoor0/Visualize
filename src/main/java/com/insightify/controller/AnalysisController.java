package com.insightify.controller;

import com.insightify.dto.*;
import com.insightify.service.DatasetService;
import com.insightify.service.FlowchartService;
import com.insightify.service.LocalAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for analysis operations:
 * - Flowchart generation
 * - Full dashboard analysis (locally computed charts + insights)
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final DatasetService datasetService;
    private final FlowchartService flowchartService;
    private final LocalAnalysisService localAnalysisService;

    public AnalysisController(DatasetService datasetService,
                              FlowchartService flowchartService,
                              LocalAnalysisService localAnalysisService) {
        this.datasetService = datasetService;
        this.flowchartService = flowchartService;
        this.localAnalysisService = localAnalysisService;
    }

    /**
     * POST /api/flowcharts
     * Generate a flowchart from a dataset and/or query.
     */
    @PostMapping("/flowcharts")
    public ResponseEntity<ApiResponse<FlowchartResponse>> generateFlowchart(
            @RequestBody FlowchartRequest request) {
        log.info("Flowchart request: datasetId={}, query={}",
                request.getDatasetId(), request.getQuery());

        FlowchartResponse flowchart = flowchartService.generateFlowchart(request);
        return ResponseEntity.ok(
                ApiResponse.success("Flowchart generated successfully", flowchart));
    }

    /**
     * GET /api/datasets/{id}/dashboard
     * Returns restaurant-specific analytics dashboard with KPIs, item rankings,
     * monthly analysis, charts, insights, and menu recommendations.
     * All data computed locally — no external API calls.
     */
    @GetMapping("/datasets/{id}/dashboard")
    public ResponseEntity<ApiResponse<RestaurantAnalysisResponse>> getDashboardAnalysis(
            @PathVariable Long id) {
        log.info("Restaurant dashboard analysis request for dataset {}", id);

        // Extract tabular data
        List<java.util.Map<String, String>> rows = datasetService.getDatasetRows(id);
        List<java.util.Map<String, String>> columns = datasetService.getColumnMetadata(id);

        // Generate restaurant analytics dashboard
        RestaurantAnalysisResponse dashboard = localAnalysisService.generateFullAnalysis(rows, columns);

        return ResponseEntity.ok(
                ApiResponse.success("Restaurant analytics dashboard generated", dashboard));
    }
}
