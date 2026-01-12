package com.example.leaflet_geo.entity.enums;

import lombok.Getter;

@Getter
public enum ConfidenceLevel {
    LOW(0, 59, "Low", "Requires additional validation"),
    MEDIUM(60, 79, "Medium", "Acceptable with periodic review"),
    HIGH(80, 100, "High", "Reliable for tax assessment");
    
    private final int minScore;
    private final int maxScore;
    private final String displayName;
    private final String description;
    
    ConfidenceLevel(int minScore, int maxScore, String displayName, String description) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.displayName = displayName;
        this.description = description;
    }
    
    public static ConfidenceLevel fromScore(int score) {
        for (ConfidenceLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        return LOW;
    }
}
