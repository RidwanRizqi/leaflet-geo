package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResponseDTO {
    
    private Long id;
    private String businessId;
    private String businessName;
    private LocalDate assessmentDate;
    
    // Profile summary
    private BigDecimal buildingArea;
    private Integer seatingCapacity;
    private String businessType;
    private String address;
    
    // Calculation results
    private BigDecimal dailyRevenueWeekday;
    private BigDecimal dailyRevenueWeekend;
    private BigDecimal monthlyRevenueRaw;
    private BigDecimal monthlyRevenueAdjusted;
    private BigDecimal monthlyPbjt;
    private BigDecimal annualPbjt;
    
    // Adjustment factors
    private AdjustmentDetails adjustments;
    
    // Confidence scoring
    private ConfidenceDetails confidence;
    
    // Location
    private LocationDetails location;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdjustmentDetails {
        private String businessType;
        private BigDecimal businessTypeCoefficient;
        private BigDecimal locationScore;
        private BigDecimal operationalRate;
        private BigDecimal taxRate;
        private BigDecimal inflationRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfidenceDetails {
        private Integer score;
        private String level;
        private Map<String, Integer> breakdown;
        private String recommendation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDetails {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String address;
        private String kelurahan;
        private String kecamatan;
        private String kabupaten;
    }
}
