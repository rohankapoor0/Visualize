package com.insightify.dto;

import java.util.List;

/**
 * Structured DTO for dataset insights and trend analysis.
 */
public class AiInsightsResponse {
    private String summary;
    private List<String> insights;
    private String trendAnalysis;
    private String topSegment;

    public AiInsightsResponse() {}

    public AiInsightsResponse(String summary, List<String> insights, String trendAnalysis, String topSegment) {
        this.summary = summary;
        this.insights = insights;
        this.trendAnalysis = trendAnalysis;
        this.topSegment = topSegment;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getInsights() { return insights; }
    public void setInsights(List<String> insights) { this.insights = insights; }

    public String getTrendAnalysis() { return trendAnalysis; }
    public void setTrendAnalysis(String trendAnalysis) { this.trendAnalysis = trendAnalysis; }

    public String getTopSegment() { return topSegment; }
    public void setTopSegment(String topSegment) { this.topSegment = topSegment; }
}
