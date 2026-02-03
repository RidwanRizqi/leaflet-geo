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
        
        // ----------------------------------------------------
        // Menu Based Calculation (Metode Nilai Tengah)
        // ----------------------------------------------------
        
        // Default values for Menu Method
        BigDecimal monthlyRevenueMenuBased = null;
        BigDecimal monthlyPbjtMenuBased = null;
        BigDecimal annualPbjtMenuBased = null;
        BigDecimal avgFoodPrice = BigDecimal.ZERO;
        BigDecimal avgBevPrice = BigDecimal.ZERO;
        
        if (request.getMenuItems() != null && !request.getMenuItems().isEmpty()) {
            // 1. Calculate Average Prices
            List<BigDecimal> foodPrices = request.getMenuItems().stream()
                .filter(i -> "FOOD".equalsIgnoreCase(i.getCategory()))
                .map(com.example.leaflet_geo.dto.MenuItemDTO::getPrice)
                .collect(Collectors.toList());
                
            List<BigDecimal> bevPrices = request.getMenuItems().stream()
                .filter(i -> "BEVERAGE".equalsIgnoreCase(i.getCategory()))
                .map(com.example.leaflet_geo.dto.MenuItemDTO::getPrice)
                .collect(Collectors.toList());

            // Use median or average? User said "Nilai Tengah", usually average in simplifictions, or median.
            // Let's use Average for now as per "14 ribu x 70 x 25" example (it implies a single representative value)
            avgFoodPrice = calculateAverage(foodPrices);
            avgBevPrice = calculateAverage(bevPrices);
            
            BigDecimal spendPerPerson = avgFoodPrice.add(avgBevPrice);
            
            // 2. Determine Traffic (Visitors per Day)
            // We need a daily visitor count.
            // From Observation Data (Sample Method), we have dailyRevenue broken down.
            // We need 'Average Daily Visitors'.
            // Let's extract VPH (Visitors Per Hour) from existing logic or re-calculate average visitors per day.
            
            // Helper to get average daily visitors from observations
            BigDecimal averageDailyVisitors = calculateAverageDailyVisitors(request);
            
            // 3. Opening Days
            int openingDays = request.getOpeningDaysPerMonth() != null ? request.getOpeningDaysPerMonth() : 30;
            
            // 4. Calculate Revenue
            // Omzet = (Avg Food + Avg Bev) * Avg Daily Visitors * Opening Days
            monthlyRevenueMenuBased = spendPerPerson
                .multiply(averageDailyVisitors)
                .multiply(BigDecimal.valueOf(openingDays))
                .setScale(2, RoundingMode.HALF_UP);
                
            // 5. Calculate Tax
             monthlyPbjtMenuBased = monthlyRevenueMenuBased.multiply(taxRate)
                .setScale(2, RoundingMode.HALF_UP);
                
             annualPbjtMenuBased = monthlyPbjtMenuBased.multiply(BigDecimal.valueOf(12))
                .multiply(BigDecimal.ONE.add(inflationRate))
                .setScale(2, RoundingMode.HALF_UP);
        }

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
            // Menu Based Results
            .monthlyRevenueMenuBased(monthlyRevenueMenuBased)
            .monthlyPbjtMenuBased(monthlyPbjtMenuBased)
            .annualPbjtMenuBased(annualPbjtMenuBased)
            .averageFoodPrice(avgFoodPrice)
            .averageBeveragePrice(avgBevPrice)
            
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
    
    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageDailyVisitors(AssessmentRequestDTO request) {
        // Calculate average VPH across all observations
        if (request.getObservations() != null && !request.getObservations().isEmpty()) {
            // Use actual observation data
            BigDecimal totalVph = BigDecimal.ZERO;
            for (com.example.leaflet_geo.dto.ObservationDTO obs : request.getObservations()) {
                 BigDecimal vph = BigDecimal.valueOf(obs.getVisitors())
                    .divide(obs.getDurationHours(), 2, RoundingMode.HALF_UP);
                 totalVph = totalVph.add(vph);
            }
            
            BigDecimal averageVph = totalVph.divide(BigDecimal.valueOf(request.getObservations().size()), 2, RoundingMode.HALF_UP);
            
            Duration operatingDuration = Duration.between(
                request.getOperatingHoursStart(),
                request.getOperatingHoursEnd()
            );
            double totalOperatingHours = operatingDuration.toMinutes() / 60.0;
            
            return averageVph.multiply(BigDecimal.valueOf(totalOperatingHours)).setScale(0, RoundingMode.HALF_UP);
        }
        
        // FALLBACK: No observations, estimate from Seating Capacity
        // Assumption: Average turn-over rate is 2-3x per day for restaurants
        // Daily Visitors ≈ Seating Capacity * Turnover Rate
        // For conservative estimate: Turnover Rate = 2.0
        double turnoverRate = 2.0;
        int seatingCapacity = request.getSeatingCapacity() != null ? request.getSeatingCapacity() : 30;
        
        // Also consider operating hours - longer hours = more turnover
        Duration operatingDuration = Duration.between(
            request.getOperatingHoursStart(),
            request.getOperatingHoursEnd()
        );
        double totalOperatingHours = operatingDuration.toMinutes() / 60.0;
        
        // Adjust turnover based on operating hours (8 hours = baseline)
        double hoursFactor = Math.max(0.5, totalOperatingHours / 8.0);
        double estimatedDailyVisitors = seatingCapacity * turnoverRate * hoursFactor;
        
        log.info("Menu Method - No observations. Estimating daily visitors from capacity: {} * {} * {} = {}",
            seatingCapacity, turnoverRate, hoursFactor, estimatedDailyVisitors);
        
        return BigDecimal.valueOf(estimatedDailyVisitors).setScale(0, RoundingMode.HALF_UP);
    }
    
    private Map<String, BigDecimal> calculateDailyRevenues(AssessmentRequestDTO request) {
        // Handle case where observations is null or empty (Menu-only method)
        if (request.getObservations() == null || request.getObservations().isEmpty()) {
            log.info("No observations provided - returning zero daily revenues (Menu-only mode)");
            Map<String, BigDecimal> emptyRevenues = new HashMap<>();
            emptyRevenues.put("weekday", BigDecimal.ZERO);
            emptyRevenues.put("weekend", BigDecimal.ZERO);
            return emptyRevenues;
        }
        
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
        
        // Add Weekend OffPeak Support
        BigDecimal vphWeekendOffPeak = calculateVisitorsPerHour(
            observationsByType.getOrDefault("WEEKEND_OFFPEAK", Collections.emptyList())
        );
        BigDecimal avgTxWeekendOffPeak = calculateAverageTransaction(
            observationsByType.getOrDefault("WEEKEND_OFFPEAK", Collections.emptyList())
        );

        if (vphWeekdayOffPeak.compareTo(BigDecimal.ZERO) == 0) {
            vphWeekdayOffPeak = vphWeekdayPeak.multiply(new BigDecimal("0.60"));
            avgTxWeekdayOffPeak = avgTxWeekdayPeak.multiply(new BigDecimal("0.80"));
        }
        
        // Handle Missing Weekend OffPeak (Default logic)
        if (vphWeekendOffPeak.compareTo(BigDecimal.ZERO) == 0) {
             // Jika tidak ada data weekend off-peak, gunakan data weekday off-peak (asumsi sama sepinya secara default jika tidak diobservasi)
             // Atau fallback ke 60% dari weekend peak
             if (vphWeekdayOffPeak.compareTo(BigDecimal.ZERO) > 0) {
                 vphWeekendOffPeak = vphWeekdayOffPeak;
                 avgTxWeekendOffPeak = avgTxWeekdayOffPeak;
             } else {
                 vphWeekendOffPeak = vphWeekendPeak.multiply(new BigDecimal("0.60"));
                 avgTxWeekendOffPeak = avgTxWeekendPeak.multiply(new BigDecimal("0.80"));
             }
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
            .add(vphWeekendOffPeak.multiply(BigDecimal.valueOf(offPeakHours))
            .multiply(avgTxWeekendOffPeak))
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
                if (obs.getSampleTransactions() == null || obs.getSampleTransactions().isEmpty()) {
                    return BigDecimal.ZERO;
                }
                // Extract amounts from SampleTransactionDTO list
                List<BigDecimal> amounts = obs.getSampleTransactions().stream()
                    .map(ObservationDTO.SampleTransactionDTO::getAmount)
                    .toList();
                BigDecimal sum = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                return sum.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
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
        
        // Factor 1: Proximity to traffic generators
        if (Boolean.TRUE.equals(request.getNearSchool())) {
            score = score.add(new BigDecimal("0.10"));
        }
        if (Boolean.TRUE.equals(request.getNearOffice())) {
            score = score.add(new BigDecimal("0.15"));
        }
        if (Boolean.TRUE.equals(request.getNearMarket())) {
            score = score.add(new BigDecimal("0.12"));
        }
        
        // Factor 2: Road accessibility
        if (request.getRoadType() != null) {
            switch (request.getRoadType().toUpperCase()) {
                case "ARTERI":
                case "JALAN_ARTERI":
                    score = score.add(new BigDecimal("0.20"));
                    break;
                case "KOLEKTOR":
                case "JALAN_KOLEKTOR":
                    score = score.add(new BigDecimal("0.15"));
                    break;
                case "LOKAL":
                case "JALAN_LOKAL":
                    score = score.add(new BigDecimal("0.05"));
                    break;
                default:
                    // Gang or others = 0.00
                    break;
            }
        }
        
        // Cap maximum adjustment
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
