package com.insightify.controller;

import com.insightify.dto.*;
import com.insightify.service.ChartService;
import com.insightify.service.DatasetService;
import com.insightify.service.FlowchartService;
import com.insightify.service.InsightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for analysis operations:
 * - Flowchart generation
 * - Full analysis (combined charts + insights + flowchart)
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final DatasetService datasetService;
    private final ChartService chartService;
    private final InsightService insightService;
    private final FlowchartService flowchartService;

    public AnalysisController(DatasetService datasetService,
                              ChartService chartService,
                              InsightService insightService,
                              FlowchartService flowchartService) {
        this.datasetService = datasetService;
        this.chartService = chartService;
        this.insightService = insightService;
        this.flowchartService = flowchartService;
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
     * GET /api/datasets/{id}/full-analysis
     * Returns combined analysis: dataset info + charts + insights + flowchart.
     */
    @GetMapping("/datasets/{id}/full-analysis")
    public ResponseEntity<ApiResponse<FullAnalysisResponse>> getFullAnalysis(
            @PathVariable Long id) {
        log.info("Full analysis request for dataset {}", id);

        // Gather all analysis components
        DatasetResponse dataset = datasetService.getDataset(id);
        List<ChartResponse> charts = chartService.generateCharts(id);
        InsightResponse insight = insightService.generateInsights(id);
        FlowchartResponse flowchart = flowchartService.getOrGenerateForDataset(id);

        FullAnalysisResponse analysis = new FullAnalysisResponse(
                dataset, charts, insight, flowchart);

        return ResponseEntity.ok(
                ApiResponse.success("Full analysis generated successfully", analysis));
    }
}
