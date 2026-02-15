package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.dto.AssessmentResponseDTO;
import com.example.leaflet_geo.dto.CalculationResultDTO;
import com.example.leaflet_geo.dto.ObservationDTO;
import com.example.leaflet_geo.entity.ObservationHistory;
import com.example.leaflet_geo.entity.PbjtAssessment;
import com.example.leaflet_geo.repository.pbjt.ObservationHistoryRepository;
import com.example.leaflet_geo.repository.pbjt.PbjtAssessmentRepository;
import com.example.leaflet_geo.dto.RealisasiDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
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
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@Slf4j
@Transactional
public class PbjtAssessmentService {
    
    private final PbjtAssessmentRepository assessmentRepository;
    private final ObservationHistoryRepository observationRepository;
    private final PbjtCalculationService calculationService;
    private final JdbcTemplate mysqlJdbcTemplate;

    public PbjtAssessmentService(
            PbjtAssessmentRepository assessmentRepository,
            ObservationHistoryRepository observationRepository,
            PbjtCalculationService calculationService,
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.assessmentRepository = assessmentRepository;
        this.observationRepository = observationRepository;
        this.calculationService = calculationService;
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }
    
    public CalculationResultDTO calculateAssessment(AssessmentRequestDTO request) {
        return calculationService.calculate(request);
    }

    public PbjtAssessment createAssessment(AssessmentRequestDTO request) {
        log.info("Creating new assessment for business: {}", request.getBusinessId());
        
        // Auto-generate Business ID if not provided
        if (request.getBusinessId() == null || request.getBusinessId().trim().isEmpty()) {
            long count = assessmentRepository.count();
            String generatedId = "NOP-AUTO-" + String.format("%05d", count + 1);
            
            // Simple collision check (retry a few times if needed, or just increment)
            while (assessmentRepository.existsByBusinessId(generatedId)) {
                count++;
                generatedId = "NOP-AUTO-" + String.format("%05d", count + 1);
            }
            log.info("Auto-generated Business ID: {}", generatedId);
            request.setBusinessId(generatedId);
        }

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
            
            // Map Menu-based results
            .monthlyRevenueMenuBased(calculation.getMonthlyRevenueMenuBased())
            .monthlyPbjtMenuBased(calculation.getMonthlyPbjtMenuBased())
            .annualPbjtMenuBased(calculation.getAnnualPbjtMenuBased())
            .openingDaysPerMonth(request.getOpeningDaysPerMonth())
            // Map new Menu Items (Convert List<DTO> to List<Map<String, Object>>)
            .menuItems(request.getMenuItems() != null ? 
                request.getMenuItems().stream()
                .map(item -> {
                     Map<String, Object> map = new java.util.HashMap<>();
                     map.put("name", item.getName());
                     map.put("price", item.getPrice());
                     map.put("category", item.getCategory());
                     return map;
                }) .collect(Collectors.toList()) : null)

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
            .roadType(request.getRoadType())
            .nearSchool(request.getNearSchool())
            .nearOffice(request.getNearOffice())
            .nearMarket(request.getNearMarket())
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
        // Menu Based Results
        existing.setMonthlyRevenueMenuBased(calculation.getMonthlyRevenueMenuBased());
        existing.setMonthlyPbjtMenuBased(calculation.getMonthlyPbjtMenuBased());
        existing.setAnnualPbjtMenuBased(calculation.getAnnualPbjtMenuBased());
        existing.setOpeningDaysPerMonth(request.getOpeningDaysPerMonth());
        // Update Menu Items
        if (request.getMenuItems() != null) {
            existing.setMenuItems(request.getMenuItems().stream()
                .map(item -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", item.getName());
                    map.put("price", item.getPrice());
                    map.put("category", item.getCategory());
                    return map;
                }).collect(Collectors.toList()));
        }
        existing.setConfidenceScore(calculation.getConfidenceScore());
        existing.setConfidenceLevel(determineConfidenceLevel(calculation.getConfidenceScore()));
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());
        existing.setAddress(request.getAddress());
        existing.setRoadType(request.getRoadType());
        existing.setNearSchool(request.getNearSchool());
        existing.setNearOffice(request.getNearOffice());
        existing.setNearMarket(request.getNearMarket());
        existing.setKelurahan(request.getKelurahan());
        existing.setKecamatan(request.getKecamatan());
        existing.setKabupaten(request.getKabupaten());
        
        // Update photo URLs if provided
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            existing.setPhotoUrls(request.getPhotoUrls().toArray(new String[0]));
        }
        
        // Update surveyor ID
        if (request.getSurveyorId() != null) {
            existing.setSurveyorId(request.getSurveyorId());
        }
        
        // Save assessment first
        PbjtAssessment savedAssessment = assessmentRepository.save(existing);
        
        // Update observations - delete old ones and create new ones
        if (savedAssessment.getObservationHistories() != null && !savedAssessment.getObservationHistories().isEmpty()) {
            observationRepository.deleteAll(savedAssessment.getObservationHistories());
            observationRepository.flush(); // Force delete to complete before inserting new ones
        }
        
        List<ObservationHistory> newObservations = request.getObservations().stream()
            .map(obs -> convertToObservationHistory(obs, savedAssessment))
            .collect(Collectors.toList());
        observationRepository.saveAll(newObservations);
        
        return savedAssessment;
    }
    
    public void deleteAssessment(Long id) {
        assessmentRepository.deleteById(id);
    }
    

    
    private ObservationHistory convertToObservationHistory(ObservationDTO dto, PbjtAssessment assessment) {
        // Calculate average from sample transactions
        java.math.BigDecimal avgTransaction = java.math.BigDecimal.ZERO;
        List<ObservationHistory.SampleTransaction> sampleTransactions = new ArrayList<>();
        
        if (dto.getSampleTransactions() != null && !dto.getSampleTransactions().isEmpty()) {
             avgTransaction = dto.getSampleTransactions().stream()
                .map(ObservationDTO.SampleTransactionDTO::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(java.math.BigDecimal.valueOf(dto.getSampleTransactions().size()), 
                    2, java.math.RoundingMode.HALF_UP);
             
             // Convert DTOs to entity SampleTransaction objects
             sampleTransactions = dto.getSampleTransactions().stream()
                .map(txDto -> ObservationHistory.SampleTransaction.builder()
                    .amount(txDto.getAmount())
                    .notes(txDto.getNotes())
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return ObservationHistory.builder()
            .assessment(assessment)
            .observationDate(dto.getObservationDate())
            .dayType(dto.getDayType())
            .visitors(dto.getVisitors())
            .durationHours(dto.getDurationHours())
            .avgTransaction(avgTransaction)
            .sampleTransactions(sampleTransactions)
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
        
        // Get kdKec from first assessment that has it
        String kdKec = assessments.stream()
            .filter(a -> a.getKdKec() != null && !a.getKdKec().isEmpty())
            .findFirst()
            .map(PbjtAssessment::getKdKec)
            .orElse(null);
        
        // Get kdKel from first assessment that has it
        String kdKel = assessments.stream()
            .filter(a -> a.getKdKel() != null && !a.getKdKel().isEmpty())
            .findFirst()
            .map(PbjtAssessment::getKdKel)
            .orElse(null);
        
        return com.example.leaflet_geo.dto.PbjtLocationStatsDTO.builder()
            .kecamatan(kecamatan)
            .kdKec(kdKec)
            .kdKel(kdKel)
            .kelurahan(kelurahan)
            .jumlahUsaha(jumlahUsaha)
            .totalAnnualPbjt(totalPbjt)
            .avgConfidenceScore(java.math.BigDecimal.valueOf(avgConfidence))
            .businessTypes(businessTypes)
            .build();
    }

    private List<RealisasiDTO> getRealisasiHistory(String taxObjectId) {
        if (taxObjectId == null || taxObjectId.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        String sql = """
            SELECT 
                YEAR(t_tglpembayaran) as tahun,
                SUM(COALESCE(t_jmlhpembayaran, 0)) as realisasi_amount,
                COUNT(t_idtransaksi) as jumlah_transaksi
            FROM t_transaksi
            WHERE t_idwpobjek = ?
              AND t_tglpembayaran IS NOT NULL
              AND YEAR(t_tglpembayaran) BETWEEN 2021 AND 2025
            GROUP BY YEAR(t_tglpembayaran)
            ORDER BY YEAR(t_tglpembayaran)
        """;

        try {
            return mysqlJdbcTemplate.query(sql, (rs, rowNum) -> RealisasiDTO.builder()
                .tahun(rs.getInt("tahun"))
                .realisasiAmount(rs.getBigDecimal("realisasi_amount"))
                .jumlahTransaksi(rs.getInt("jumlah_transaksi"))
                .build(), taxObjectId);
        } catch (Exception e) {
            log.error("Error fetching realisasi for tax object: {}", taxObjectId, e);
            // Return empty list instead of failing
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * Batch fetch realization history for multiple tax object IDs.
     * Use this for lists to avoid N+1 Application <-> Database roundtrips.
     */
    public Map<String, List<RealisasiDTO>> getRealisasiHistoryMap(List<String> taxObjectIds) {
        if (taxObjectIds == null || taxObjectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Filter out nulls/empties
        List<String> validIds = taxObjectIds.stream()
            .filter(id -> id != null && !id.isEmpty())
            .distinct()
            .collect(Collectors.toList());
            
        if (validIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Build WHERE IN clause dynamically
        String inSql = String.join(",", Collections.nCopies(validIds.size(), "?"));
        
        String sql = String.format("""
            SELECT 
                t_idwpobjek,
                YEAR(t_tglpembayaran) as tahun,
                SUM(COALESCE(t_jmlhpembayaran, 0)) as realisasi_amount,
                COUNT(t_idtransaksi) as jumlah_transaksi
            FROM t_transaksi
            WHERE t_idwpobjek IN (%s)
              AND t_tglpembayaran IS NOT NULL
              AND YEAR(t_tglpembayaran) BETWEEN 2021 AND 2025
            GROUP BY t_idwpobjek, YEAR(t_tglpembayaran)
            ORDER BY t_idwpobjek, YEAR(t_tglpembayaran)
        """, inSql);
        
        try {
            // Flatten the result into a list of objects holding (id, dto)
            // Since JdbcTemplate doesn't support grouping directly easily
            List<Map<String, Object>> rows = mysqlJdbcTemplate.query(sql, (rs, rowNum) -> {
                RealisasiDTO dto = RealisasiDTO.builder()
                    .tahun(rs.getInt("tahun"))
                    .realisasiAmount(rs.getBigDecimal("realisasi_amount"))
                    .jumlahTransaksi(rs.getInt("jumlah_transaksi"))
                    .build();
                return Map.of("id", rs.getString("t_idwpobjek"), "dto", dto);
            }, validIds.toArray());
            
            // Group by ID in memory
            Map<String, List<RealisasiDTO>> result = new java.util.HashMap<>();
            for (Map<String, Object> row : rows) {
                String id = (String) row.get("id");
                RealisasiDTO dto = (RealisasiDTO) row.get("dto");
                result.computeIfAbsent(id, k -> new ArrayList<>()).add(dto);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error fetching batch realisasi", e);
            return Collections.emptyMap();
        }
    }
    
    public AssessmentResponseDTO convertToResponseDTO(PbjtAssessment assessment) {
        return convertToResponseDTO(assessment, getRealisasiHistory(assessment.getTaxObjectId()));
    }

    public AssessmentResponseDTO convertToResponseDTO(PbjtAssessment assessment, List<RealisasiDTO> providedHistory) {
         return AssessmentResponseDTO.builder()
            .id(assessment.getId())
            .businessId(assessment.getBusinessId())
            .taxObjectId(assessment.getTaxObjectId())
            .businessName(assessment.getBusinessName())
            .assessmentDate(assessment.getAssessmentDate())
            .buildingArea(assessment.getBuildingArea())
            .seatingCapacity(assessment.getSeatingCapacity())
            .businessType(assessment.getBusinessType())
            .operatingHoursStart(assessment.getOperatingHoursStart() != null ? assessment.getOperatingHoursStart().toString() : null)
            .operatingHoursEnd(assessment.getOperatingHoursEnd() != null ? assessment.getOperatingHoursEnd().toString() : null)
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
            .realisasiHistory(providedHistory)
            .observations(observationRepository.findByAssessmentIdOrderByDateDesc(assessment.getId()).stream()
                .map(obs -> AssessmentResponseDTO.ObservationDetails.builder()
                    .id(obs.getId())
                    .observationDate(obs.getObservationDate())
                    .dayType(obs.getDayType())
                    .visitors(obs.getVisitors())
                    .durationHours(obs.getDurationHours())
                    .avgTransaction(obs.getAvgTransaction())
                    .visitorsPerHour(obs.getVisitorsPerHour())
                    .sampleTransactions(obs.getSampleTransactions().stream()
                        .map(tx -> AssessmentResponseDTO.SampleTransactionDetails.builder()
                            .amount(tx.getAmount())
                            .notes(tx.getNotes())
                            .build())
                        .collect(java.util.stream.Collectors.toList()))
                    .notes(obs.getNotes())
                    .build())
                .collect(java.util.stream.Collectors.toList()))
            .photoUrls(assessment.getPhotoUrls())
            .supportingDocUrl(assessment.getSupportingDocUrl())
            .surveyorId(assessment.getSurveyorId())
            .createdAt(assessment.getCreatedAt())
            .updatedAt(assessment.getUpdatedAt())
            .build();
    }
}

