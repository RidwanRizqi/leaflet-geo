package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.dto.CalculationResultDTO;
import com.example.leaflet_geo.dto.ObservationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PbjtCalculationService {
    
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.10");
    private static final BigDecimal DEFAULT_INFLATION_RATE = new BigDecimal("0.03");
    private static final BigDecimal DEFAULT_OPERATIONAL_RATE = new BigDecimal("0.90");
    private static final int TYPICAL_WEEKDAYS_PER_MONTH = 22;
    private static final int TYPICAL_WEEKENDS_PER_MONTH = 8;
    private static final int TYPICAL_HOLIDAYS_PER_MONTH = 1;
    
    public CalculationResultDTO calculate(AssessmentRequestDTO request) {
        log.info("Starting PBJT calculation for business: {}", request.getBusinessId());
        
        Map<String, BigDecimal> dailyRevenues = calculateDailyRevenues(request);
        
        BigDecimal operationalRate = request.getOperationalRate() != null ? 
            request.getOperationalRate() : DEFAULT_OPERATIONAL_RATE;
        BigDecimal monthlyRevenueRaw = calculateMonthlyRevenue(dailyRevenues, operationalRate);
        
        BigDecimal typeCoefficient = getBusinessTypeCoefficient(request.getBusinessType(), request.getSeatingCapacity());
        BigDecimal monthlyRevenueAfterType = monthlyRevenueRaw.multiply(typeCoefficient);
        
        BigDecimal locationScore = calculateLocationScore(request);
        BigDecimal monthlyRevenueAdjusted = monthlyRevenueAfterType.multiply(locationScore);
        
        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : DEFAULT_TAX_RATE;
        BigDecimal inflationRate = request.getInflationRate() != null ? 
            request.getInflationRate() : DEFAULT_INFLATION_RATE;
        
        BigDecimal monthlyPbjt = monthlyRevenueAdjusted.multiply(taxRate)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal annualPbjt = monthlyPbjt.multiply(BigDecimal.valueOf(12))
            .multiply(BigDecimal.ONE.add(inflationRate))
            .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Integer> confidenceBreakdown = calculateConfidenceScore(request);
        Integer totalConfidence = confidenceBreakdown.values().stream()
            .mapToInt(Integer::intValue).sum();
        
        String recommendation = generateRecommendation(totalConfidence, confidenceBreakdown);
        
        log.info("Calculation completed. Monthly PBJT: {}, Annual PBJT: {}, Confidence: {}", 
            monthlyPbjt, annualPbjt, totalConfidence);
        
        return CalculationResultDTO.builder()
            .dailyRevenueWeekday(dailyRevenues.get("weekday"))
            .dailyRevenueWeekend(dailyRevenues.get("weekend"))
            .monthlyRevenueRaw(monthlyRevenueRaw)
            .monthlyRevenueAdjusted(monthlyRevenueAdjusted)
            .monthlyPbjt(monthlyPbjt)
            .annualPbjt(annualPbjt)
            .businessTypeCoefficient(typeCoefficient)
            .locationScore(locationScore)
            .operationalRate(operationalRate)
            .taxRate(taxRate)
            .inflationRate(inflationRate)
            .confidenceScore(totalConfidence)
            .confidenceBreakdown(confidenceBreakdown)
            .recommendation(recommendation)
            .build();
    }
    
    private Map<String, BigDecimal> calculateDailyRevenues(AssessmentRequestDTO request) {
        Duration operatingDuration = Duration.between(
            request.getOperatingHoursStart(),
            request.getOperatingHoursEnd()
        );
        double totalOperatingHours = operatingDuration.toMinutes() / 60.0;
        double peakHours = 4.0;
        double offPeakHours = totalOperatingHours - peakHours;
        
        Map<String, List<ObservationDTO>> observationsByType = request.getObservations()
            .stream()
            .collect(Collectors.groupingBy(ObservationDTO::getDayType));
        
        BigDecimal vphWeekdayPeak = calculateVisitorsPerHour(
            observationsByType.getOrDefault("WEEKDAY_PEAK", Collections.emptyList())
        );
        BigDecimal vphWeekdayOffPeak = calculateVisitorsPerHour(
            observationsByType.getOrDefault("WEEKDAY_OFFPEAK", Collections.emptyList())
        );
        BigDecimal vphWeekendPeak = calculateVisitorsPerHour(
            observationsByType.getOrDefault("WEEKEND_PEAK", Collections.emptyList())
        );
        
        BigDecimal avgTxWeekdayPeak = calculateAverageTransaction(
            observationsByType.getOrDefault("WEEKDAY_PEAK", Collections.emptyList())
        );
        BigDecimal avgTxWeekdayOffPeak = calculateAverageTransaction(
            observationsByType.getOrDefault("WEEKDAY_OFFPEAK", Collections.emptyList())
        );
        BigDecimal avgTxWeekendPeak = calculateAverageTransaction(
            observationsByType.getOrDefault("WEEKEND_PEAK", Collections.emptyList())
        );
        
        if (vphWeekdayOffPeak.compareTo(BigDecimal.ZERO) == 0) {
            vphWeekdayOffPeak = vphWeekdayPeak.multiply(new BigDecimal("0.60"));
            avgTxWeekdayOffPeak = avgTxWeekdayPeak.multiply(new BigDecimal("0.80"));
        }
        
        BigDecimal dailyRevenueWeekday = vphWeekdayPeak
            .multiply(BigDecimal.valueOf(peakHours))
            .multiply(avgTxWeekdayPeak)
            .add(vphWeekdayOffPeak.multiply(BigDecimal.valueOf(offPeakHours))
            .multiply(avgTxWeekdayOffPeak))
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal dailyRevenueWeekend = vphWeekendPeak
            .multiply(BigDecimal.valueOf(peakHours))
            .multiply(avgTxWeekendPeak)
            .add(vphWeekdayOffPeak.multiply(BigDecimal.valueOf(offPeakHours))
            .multiply(avgTxWeekendPeak))
            .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("weekday", dailyRevenueWeekday);
        result.put("weekend", dailyRevenueWeekend);
        return result;
    }
    
    private BigDecimal calculateVisitorsPerHour(List<ObservationDTO> observations) {
        if (observations.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalVph = observations.stream()
            .map(obs -> BigDecimal.valueOf(obs.getVisitors())
                .divide(obs.getDurationHours(), 2, RoundingMode.HALF_UP))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalVph.divide(BigDecimal.valueOf(observations.size()), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateAverageTransaction(List<ObservationDTO> observations) {
        if (observations.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal totalAvg = observations.stream()
            .map(obs -> {
                List<BigDecimal> samples = obs.getSampleTransactions();
                BigDecimal sum = samples.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                return sum.divide(BigDecimal.valueOf(samples.size()), 2, RoundingMode.HALF_UP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAvg.divide(BigDecimal.valueOf(observations.size()), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateMonthlyRevenue(Map<String, BigDecimal> dailyRevenues, BigDecimal operationalRate) {
        int effectiveWeekdays = TYPICAL_WEEKDAYS_PER_MONTH - TYPICAL_HOLIDAYS_PER_MONTH;
        int effectiveWeekends = TYPICAL_WEEKENDS_PER_MONTH;
        
        BigDecimal weekdayRevenue = dailyRevenues.get("weekday")
            .multiply(BigDecimal.valueOf(effectiveWeekdays))
            .multiply(operationalRate);
        
        BigDecimal weekendRevenue = dailyRevenues.get("weekend")
            .multiply(BigDecimal.valueOf(effectiveWeekends))
            .multiply(operationalRate);
        
        return weekdayRevenue.add(weekendRevenue).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal getBusinessTypeCoefficient(String businessType, Integer seatingCapacity) {
        if (businessType != null) {
            switch (businessType.toUpperCase()) {
                case "WARUNG_KECIL": return new BigDecimal("0.85");
                case "RUMAH_MAKAN": return new BigDecimal("1.00");
                case "RESTAURANT": return new BigDecimal("1.10");
                case "CAFE_MODERN": return new BigDecimal("1.20");
                case "FRANCHISE": return new BigDecimal("1.15");
            }
        }
        
        // Classify by capacity
        if (seatingCapacity < 20) return new BigDecimal("0.85");
        if (seatingCapacity < 50) return new BigDecimal("1.00");
        if (seatingCapacity < 150) return new BigDecimal("1.10");
        return new BigDecimal("1.20");
    }
    
    private BigDecimal calculateLocationScore(AssessmentRequestDTO request) {
        BigDecimal score = BigDecimal.ONE;
        String address = request.getAddress().toLowerCase();
        
        if (address.contains("jalan raya") || address.contains("jl. raya")) {
            score = score.add(new BigDecimal("0.15"));
        }
        if (address.contains("pasar") || address.contains("market")) {
            score = score.add(new BigDecimal("0.12"));
        }
        
        String kecamatan = request.getKecamatan();
        if (kecamatan != null && (kecamatan.toLowerCase().contains("kota") || 
                                  kecamatan.toLowerCase().contains("pusat"))) {
            score = score.add(new BigDecimal("0.10"));
        }
        
        if (score.compareTo(new BigDecimal("1.50")) > 0) {
            score = new BigDecimal("1.50");
        }
        
        return score.setScale(2, RoundingMode.HALF_UP);
    }
    
    private Map<String, Integer> calculateConfidenceScore(AssessmentRequestDTO request) {
        Map<String, Integer> breakdown = new HashMap<>();
        
        int dataCompleteness = 0;
        if (request.getObservations().size() >= 3) dataCompleteness += 20;
        else if (request.getObservations().size() >= 2) dataCompleteness += 10;
        if (request.getSeatingCapacity() != null) dataCompleteness += 5;
        if (request.getOperatingHoursStart() != null) dataCompleteness += 5;
        if (request.getBuildingArea() != null) dataCompleteness += 5;
        if (request.getPhotoUrls() != null && request.getPhotoUrls().size() >= 3) dataCompleteness += 5;
        breakdown.put("data_completeness", dataCompleteness);
        
        int validationSources = 0;
        if (request.getValidationData() != null) {
            if (request.getValidationData().containsKey("electricityBill")) validationSources += 10;
            if (request.getValidationData().containsKey("qrisData")) validationSources += 15;
            if (request.getValidationData().containsKey("supplierInvoice")) validationSources += 5;
        }
        breakdown.put("validation_sources", validationSources);
        
        int surveyQuality = 15;
        if (request.getVerifiedBy() != null && !request.getVerifiedBy().isEmpty()) surveyQuality += 10;
        if (Boolean.TRUE.equals(request.getTaxpayerSigned())) surveyQuality += 5;
        breakdown.put("survey_quality", surveyQuality);
        
        return breakdown;
    }
    
    private String generateRecommendation(Integer totalScore, Map<String, Integer> breakdown) {
        List<String> recommendations = new ArrayList<>();
        
        if (totalScore >= 80) {
            recommendations.add("High confidence assessment - reliable for tax collection");
        } else if (totalScore >= 60) {
            recommendations.add("Medium confidence - acceptable with periodic review");
        } else {
            recommendations.add("Low confidence - requires improvement");
        }
        
        if (breakdown.get("data_completeness") < 30) {
            recommendations.add("Add more observation sessions to improve accuracy");
        }
        if (breakdown.get("validation_sources") < 15) {
            recommendations.add("Include electricity bill or QRIS data for validation");
        }
        if (breakdown.get("survey_quality") < 20) {
            recommendations.add("Obtain verification from supervisor and taxpayer signature");
        }
        
        return String.join(". ", recommendations);
    }
}
