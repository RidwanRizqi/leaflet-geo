package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.dto.AssessmentResponseDTO;
import com.example.leaflet_geo.dto.CalculationResultDTO;
import com.example.leaflet_geo.dto.ObservationDTO;
import com.example.leaflet_geo.entity.ObservationHistory;
import com.example.leaflet_geo.entity.PbjtAssessment;
import com.example.leaflet_geo.repository.pbjt.ObservationHistoryRepository;
import com.example.leaflet_geo.repository.pbjt.PbjtAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PbjtAssessmentService {
    
    private final PbjtAssessmentRepository assessmentRepository;
    private final ObservationHistoryRepository observationRepository;
    private final PbjtCalculationService calculationService;
    
    public PbjtAssessment createAssessment(AssessmentRequestDTO request) {
        log.info("Creating new assessment for business: {}", request.getBusinessId());
        
        // Check if business ID already exists
        if (assessmentRepository.existsByBusinessId(request.getBusinessId())) {
            throw new RuntimeException("Business ID already exists: " + request.getBusinessId());
        }
        
        // Calculate PBJT
        CalculationResultDTO calculation = calculationService.calculate(request);
        
        // Build entity
        PbjtAssessment assessment = PbjtAssessment.builder()
            .businessId(request.getBusinessId())
            .businessName(request.getBusinessName())
            .assessmentDate(request.getAssessmentDate())
            .buildingArea(request.getBuildingArea())
            .seatingCapacity(request.getSeatingCapacity())
            .operatingHoursStart(request.getOperatingHoursStart())
            .operatingHoursEnd(request.getOperatingHoursEnd())
            .businessType(request.getBusinessType())
            .paymentMethods(request.getPaymentMethods() != null ? 
                request.getPaymentMethods().toArray(new String[0]) : null)
            .dailyRevenueWeekday(calculation.getDailyRevenueWeekday())
            .dailyRevenueWeekend(calculation.getDailyRevenueWeekend())
            .monthlyRevenueRaw(calculation.getMonthlyRevenueRaw())
            .monthlyRevenueAdjusted(calculation.getMonthlyRevenueAdjusted())
            .businessTypeCoefficient(calculation.getBusinessTypeCoefficient())
            .locationScore(calculation.getLocationScore())
            .operationalRate(calculation.getOperationalRate())
            .monthlyPbjt(calculation.getMonthlyPbjt())
            .annualPbjt(calculation.getAnnualPbjt())
            .taxRate(calculation.getTaxRate())
            .inflationRate(calculation.getInflationRate())
            .confidenceScore(calculation.getConfidenceScore())
            .confidenceLevel(determineConfidenceLevel(calculation.getConfidenceScore()))
            .surveyorId(request.getSurveyorId())
            .verifiedBy(request.getVerifiedBy())
            .taxpayerSigned(request.getTaxpayerSigned())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .address(request.getAddress())
            .kelurahan(request.getKelurahan())
            .kecamatan(request.getKecamatan())
            .kabupaten(request.getKabupaten())
            .photoUrls(request.getPhotoUrls() != null ? 
                request.getPhotoUrls().toArray(new String[0]) : null)
            .supportingDocUrl(request.getSupportingDocUrl())
            .build();
        
        // Save assessment
        PbjtAssessment savedAssessment = assessmentRepository.save(assessment);
        
        // Save observations
        List<ObservationHistory> observations = request.getObservations().stream()
            .map(obs -> convertToObservationHistory(obs, savedAssessment))
            .collect(Collectors.toList());
        observationRepository.saveAll(observations);
        
        log.info("Assessment created successfully with ID: {}", savedAssessment.getId());
        return savedAssessment;
    }
    
    public Optional<PbjtAssessment> getAssessmentById(Long id) {
        return assessmentRepository.findById(id);
    }
    
    public Optional<PbjtAssessment> getAssessmentByBusinessId(String businessId) {
        return assessmentRepository.findByBusinessId(businessId);
    }
    
    public Page<PbjtAssessment> getAllAssessments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return assessmentRepository.findAll(pageable);
    }
    
    public List<PbjtAssessment> getAssessmentsByKabupaten(String kabupaten) {
        return assessmentRepository.findByKabupaten(kabupaten);
    }
    
    public List<PbjtAssessment> getAssessmentsByKecamatan(String kecamatan) {
        return assessmentRepository.findByKecamatan(kecamatan);
    }
    
    public PbjtAssessment updateAssessment(Long id, AssessmentRequestDTO request) {
        PbjtAssessment existing = assessmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Assessment not found with ID: " + id));
        
        // Recalculate
        CalculationResultDTO calculation = calculationService.calculate(request);
        
        // Update fields
        existing.setBusinessName(request.getBusinessName());
        existing.setAssessmentDate(request.getAssessmentDate());
        existing.setBuildingArea(request.getBuildingArea());
        existing.setSeatingCapacity(request.getSeatingCapacity());
        existing.setOperatingHoursStart(request.getOperatingHoursStart());
        existing.setOperatingHoursEnd(request.getOperatingHoursEnd());
        existing.setBusinessType(request.getBusinessType());
        existing.setDailyRevenueWeekday(calculation.getDailyRevenueWeekday());
        existing.setDailyRevenueWeekend(calculation.getDailyRevenueWeekend());
        existing.setMonthlyRevenueRaw(calculation.getMonthlyRevenueRaw());
        existing.setMonthlyRevenueAdjusted(calculation.getMonthlyRevenueAdjusted());
        existing.setBusinessTypeCoefficient(calculation.getBusinessTypeCoefficient());
        existing.setLocationScore(calculation.getLocationScore());
        existing.setMonthlyPbjt(calculation.getMonthlyPbjt());
        existing.setAnnualPbjt(calculation.getAnnualPbjt());
        existing.setConfidenceScore(calculation.getConfidenceScore());
        existing.setConfidenceLevel(determineConfidenceLevel(calculation.getConfidenceScore()));
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setAddress(request.getAddress());
        existing.setKelurahan(request.getKelurahan());
        existing.setKecamatan(request.getKecamatan());
        existing.setKabupaten(request.getKabupaten());
        
        // Update observations
        observationRepository.deleteAll(existing.getObservationHistories());
        List<ObservationHistory> newObservations = request.getObservations().stream()
            .map(obs -> convertToObservationHistory(obs, existing))
            .collect(Collectors.toList());
        observationRepository.saveAll(newObservations);
        
        return assessmentRepository.save(existing);
    }
    
    public void deleteAssessment(Long id) {
        assessmentRepository.deleteById(id);
    }
    
    public AssessmentResponseDTO convertToResponseDTO(PbjtAssessment assessment) {
        return AssessmentResponseDTO.builder()
            .id(assessment.getId())
            .businessId(assessment.getBusinessId())
            .businessName(assessment.getBusinessName())
            .assessmentDate(assessment.getAssessmentDate())
            .buildingArea(assessment.getBuildingArea())
            .seatingCapacity(assessment.getSeatingCapacity())
            .businessType(assessment.getBusinessType())
            .address(assessment.getAddress())
            .dailyRevenueWeekday(assessment.getDailyRevenueWeekday())
            .dailyRevenueWeekend(assessment.getDailyRevenueWeekend())
            .monthlyRevenueRaw(assessment.getMonthlyRevenueRaw())
            .monthlyRevenueAdjusted(assessment.getMonthlyRevenueAdjusted())
            .monthlyPbjt(assessment.getMonthlyPbjt())
            .annualPbjt(assessment.getAnnualPbjt())
            .adjustments(AssessmentResponseDTO.AdjustmentDetails.builder()
                .businessType(assessment.getBusinessType())
                .businessTypeCoefficient(assessment.getBusinessTypeCoefficient())
                .locationScore(assessment.getLocationScore())
                .operationalRate(assessment.getOperationalRate())
                .taxRate(assessment.getTaxRate())
                .inflationRate(assessment.getInflationRate())
                .build())
            .confidence(AssessmentResponseDTO.ConfidenceDetails.builder()
                .score(assessment.getConfidenceScore())
                .level(assessment.getConfidenceLevel())
                .build())
            .location(AssessmentResponseDTO.LocationDetails.builder()
                .latitude(assessment.getLatitude())
                .longitude(assessment.getLongitude())
                .address(assessment.getAddress())
                .kelurahan(assessment.getKelurahan())
                .kecamatan(assessment.getKecamatan())
                .kabupaten(assessment.getKabupaten())
                .build())
            .createdAt(assessment.getCreatedAt())
            .updatedAt(assessment.getUpdatedAt())
            .build();
    }
    
    private ObservationHistory convertToObservationHistory(ObservationDTO dto, PbjtAssessment assessment) {
        return ObservationHistory.builder()
            .assessment(assessment)
            .observationDate(dto.getObservationDate())
            .dayType(dto.getDayType())
            .visitors(dto.getVisitors())
            .durationHours(dto.getDurationHours())
            .avgTransaction(dto.getSampleTransactions().stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(java.math.BigDecimal.valueOf(dto.getSampleTransactions().size()), 
                    2, java.math.RoundingMode.HALF_UP))
            .sampleTransactions(dto.getSampleTransactions())
            .visitorsPerHour(java.math.BigDecimal.valueOf(dto.getVisitors())
                .divide(dto.getDurationHours(), 2, java.math.RoundingMode.HALF_UP))
            .notes(dto.getNotes())
            .build();
    }
    
    private String determineConfidenceLevel(Integer score) {
        if (score >= 80) return "HIGH";
        if (score >= 60) return "MEDIUM";
        return "LOW";
    }
    
    // Map statistics methods
    public List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO> getStatsByKecamatan() {
        log.info("Getting statistics by kecamatan");
        List<String> kecamatanList = assessmentRepository.findDistinctKecamatan();
        
        return kecamatanList.stream().map(kecamatan -> {
            List<PbjtAssessment> assessments = assessmentRepository.findByKecamatan(kecamatan);
            return buildLocationStats(kecamatan, null, assessments);
        }).collect(Collectors.toList());
    }
    
    public List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO> getStatsByKelurahan(String kecamatan) {
        log.info("Getting statistics by kelurahan for kecamatan: {}", kecamatan);
        List<String> kelurahanList = assessmentRepository.findDistinctKelurahanByKecamatan(kecamatan);
        
        return kelurahanList.stream().map(kelurahan -> {
            List<PbjtAssessment> assessments = assessmentRepository.findByKecamatanAndKelurahan(kecamatan, kelurahan);
            return buildLocationStats(kecamatan, kelurahan, assessments);
        }).collect(Collectors.toList());
    }
    
    public List<PbjtAssessment> getAssessmentsByLocation(String kecamatan, String kelurahan) {
        log.info("Getting assessments for location: {}, {}", kecamatan, kelurahan);
        return assessmentRepository.findByKecamatanAndKelurahan(kecamatan, kelurahan);
    }
    
    private com.example.leaflet_geo.dto.PbjtLocationStatsDTO buildLocationStats(String kecamatan, String kelurahan, List<PbjtAssessment> assessments) {
        long jumlahUsaha = assessments.size();
        
        java.math.BigDecimal totalPbjt = assessments.stream()
            .map(PbjtAssessment::getAnnualPbjt)
            .filter(pbjt -> pbjt != null)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        double avgConfidence = assessments.stream()
            .filter(a -> a.getConfidenceScore() != null)
            .mapToInt(PbjtAssessment::getConfidenceScore)
            .average()
            .orElse(0.0);
        
        java.util.Map<String, Long> businessTypes = assessments.stream()
            .filter(a -> a.getBusinessType() != null)
            .collect(Collectors.groupingBy(PbjtAssessment::getBusinessType, Collectors.counting()));
        
        return com.example.leaflet_geo.dto.PbjtLocationStatsDTO.builder()
            .kecamatan(kecamatan)
            .kelurahan(kelurahan)
            .jumlahUsaha(jumlahUsaha)
            .totalAnnualPbjt(totalPbjt)
            .avgConfidenceScore(java.math.BigDecimal.valueOf(avgConfidence))
            .businessTypes(businessTypes)
            .build();
    }
}
