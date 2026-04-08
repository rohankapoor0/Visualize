package com.insightify.dto;

import java.util.List;

/**
 * Response DTO for the full analysis endpoint.
 * Combines charts, insights, explanations, and flowchart.
 */
public class FullAnalysisResponse {

    private DatasetResponse dataset;
    private List<ChartResponse> charts;
    private InsightResponse insight;
    private FlowchartResponse flowchart;

    public FullAnalysisResponse() {}

    public FullAnalysisResponse(DatasetResponse dataset, List<ChartResponse> charts,
                                InsightResponse insight, FlowchartResponse flowchart) {
        this.dataset = dataset;
        this.charts = charts;
        this.insight = insight;
        this.flowchart = flowchart;
    }

    // --- Getters and Setters ---

    public DatasetResponse getDataset() { return dataset; }
    public void setDataset(DatasetResponse dataset) { this.dataset = dataset; }

    public List<ChartResponse> getCharts() { return charts; }
    public void setCharts(List<ChartResponse> charts) { this.charts = charts; }

    public InsightResponse getInsight() { return insight; }
    public void setInsight(InsightResponse insight) { this.insight = insight; }

    public FlowchartResponse getFlowchart() { return flowchart; }
    public void setFlowchart(FlowchartResponse flowchart) { this.flowchart = flowchart; }
}
