package com.example.leaflet_geo.entity.enums;

import lombok.Getter;

@Getter
public enum DayType {
    WEEKDAY_PEAK("Weekday Peak", 4.0),
    WEEKDAY_OFFPEAK("Weekday Off-Peak", 8.0),
    WEEKEND_PEAK("Weekend Peak", 4.0),
    HOLIDAY("Holiday", 4.0);
    
    private final String displayName;
    private final double typicalPeakHours;
    
    DayType(String displayName, double typicalPeakHours) {
        this.displayName = displayName;
        this.typicalPeakHours = typicalPeakHours;
    }
}
