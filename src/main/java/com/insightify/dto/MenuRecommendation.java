package com.insightify.dto;

/**
 * Menu optimization recommendation for restaurant owners.
 * Flags items that should be reviewed, repriced, or removed.
 */
public class MenuRecommendation {

    private String itemName;
    private String reason;         // e.g. "Low sales volume", "Negative profit margin"
    private String action;         // e.g. "Consider removing from menu", "Revise pricing"
    private String severity;       // "warning" or "critical"

    public MenuRecommendation() {}

    public MenuRecommendation(String itemName, String reason, String action, String severity) {
        this.itemName = itemName;
        this.reason = reason;
        this.action = action;
        this.severity = severity;
    }

    // --- Getters & Setters ---

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}
