package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResultDTO {
    
    private BigDecimal dailyRevenueWeekday;
    private BigDecimal dailyRevenueWeekend;
    private BigDecimal monthlyRevenueRaw;
    private BigDecimal monthlyRevenueAdjusted;
    private BigDecimal monthlyPbjt;
    private BigDecimal annualPbjt;
    
    private BigDecimal businessTypeCoefficient;
    private BigDecimal locationScore;
    private BigDecimal operationalRate;
    private BigDecimal taxRate;
    private BigDecimal inflationRate;
    
    private Integer confidenceScore;
    private Map<String, Integer> confidenceBreakdown;
    private String recommendation;
}
