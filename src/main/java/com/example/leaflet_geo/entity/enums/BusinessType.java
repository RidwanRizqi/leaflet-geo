package com.example.leaflet_geo.entity.enums;

import lombok.Getter;

@Getter
public enum BusinessType {
    WARUNG_KECIL(0.85, 0, 20, "Warung Kecil"),
    RUMAH_MAKAN(1.00, 20, 50, "Rumah Makan"),
    RESTAURANT(1.10, 50, 150, "Restaurant"),
    CAFE_MODERN(1.20, 30, 100, "Café Modern"),
    FRANCHISE(1.15, 40, 200, "Franchise");
    
    private final double coefficient;
    private final int minCapacity;
    private final int maxCapacity;
    private final String displayName;
    
    BusinessType(double coefficient, int minCapacity, int maxCapacity, String displayName) {
        this.coefficient = coefficient;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.displayName = displayName;
    }
    
    public static BusinessType classifyByCapacity(int capacity) {
        for (BusinessType type : values()) {
            if (capacity >= type.minCapacity && capacity < type.maxCapacity) {
                return type;
            }
        }
        return WARUNG_KECIL; // Default fallback
    }
}
