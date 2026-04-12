package com.insightify.dto;

import java.util.List;
import java.util.Map;

/**
 * Dashboard analytics response.
 * Synchronized with the React Frontend (DashboardPage.jsx).
 * Contains locally computed KPIs, Sections (charts), and Insights.
 */
public class AiDashboardResponse {
    
    // Matched to Frontend 'data.kpis'
    // Expected structure: { "total": { "value": 0, "description": "" }, ... }
    private Map<String, Object> kpis;

    // Matched to Frontend 'data.sections'
    private List<DashboardSection> sections;
    
    // Matched to Frontend 'data.aiInsights'
    private AiInsightsResponse aiInsights;

    public AiDashboardResponse() {}

    public AiDashboardResponse(Map<String, Object> kpis, 
                                List<DashboardSection> sections, 
                                AiInsightsResponse aiInsights) {
        this.kpis = kpis;
        this.sections = sections;
        this.aiInsights = aiInsights;
    }

    public Map<String, Object> getKpis() { return kpis; }
    public void setKpis(Map<String, Object> kpis) { this.kpis = kpis; }

    public List<DashboardSection> getSections() { return sections; }
    public void setSections(List<DashboardSection> sections) { this.sections = sections; }

    public AiInsightsResponse getAiInsights() { return aiInsights; }
    public void setAiInsights(AiInsightsResponse aiInsights) { this.aiInsights = aiInsights; }
}
