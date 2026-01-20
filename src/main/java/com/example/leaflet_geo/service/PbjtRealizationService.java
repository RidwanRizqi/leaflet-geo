package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.PbjtAssessmentWithRealizationDTO;
import com.example.leaflet_geo.entity.PbjtAssessment;
import com.example.leaflet_geo.repository.pbjt.PbjtAssessmentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PbjtRealizationService {
    
    private final PbjtAssessmentRepository assessmentRepository;
    private final JdbcTemplate pbjtJdbcTemplate; // JdbcTemplate untuk database pbjt_assessment_db
    
    // Constructor - gunakan pbjtJdbcTemplate untuk query tabel pbjt_realisasi
    public PbjtRealizationService(
            PbjtAssessmentRepository assessmentRepository,
            @Qualifier("pbjtJdbcTemplate") JdbcTemplate pbjtJdbcTemplate) {
        this.assessmentRepository = assessmentRepository;
        this.pbjtJdbcTemplate = pbjtJdbcTemplate;
    }
    
    public List<PbjtAssessmentWithRealizationDTO> getAllAssessmentsWithRealization() {
        System.out.println("=== PbjtRealizationService.getAllAssessmentsWithRealization() called ===");
        
        // Get all assessments
        List<PbjtAssessment> assessments = assessmentRepository.findAll();
        System.out.println("Found " + assessments.size() + " assessments from PostgreSQL");
        
        // Get ALL realisasi data from pbjt_realisasi table (PostgreSQL local)
        Map<Long, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationFromLocal();
        System.out.println("Got realization data for " + allRealizationData.size() + " assessments from pbjt_realisasi table");
        
        List<PbjtAssessmentWithRealizationDTO> results = new ArrayList<>();
        
        for (PbjtAssessment assessment : assessments) {
            PbjtAssessmentWithRealizationDTO dto = new PbjtAssessmentWithRealizationDTO();
            
            // Basic info
            dto.setId(assessment.getId());
            dto.setBusinessName(assessment.getBusinessName());
            dto.setBusinessType(assessment.getBusinessType());
            dto.setOwnerName(assessment.getOwnerName());
            dto.setTaxObjectNumber(assessment.getTaxObjectNumber());
            dto.setAddress(assessment.getAddress());
            dto.setKelurahan(assessment.getKelurahan());
            dto.setKecamatan(assessment.getKecamatan());
            dto.setLatitude(assessment.getLatitude());
            dto.setLongitude(assessment.getLongitude());
            dto.setAnnualPbjt(assessment.getAnnualPbjt());
            dto.setMonthlyPbjt(assessment.getMonthlyPbjt());
            dto.setConfidenceLevel(assessment.getConfidenceLevel());
            
            // Get realisasi from local pbjt_realisasi table
            Long assessmentId = assessment.getId();
            Map<Integer, BigDecimal> realizationByYear = allRealizationData.getOrDefault(assessmentId, Collections.emptyMap());
            
            dto.setRealisasi2021(realizationByYear.getOrDefault(2021, BigDecimal.ZERO));
            dto.setRealisasi2022(realizationByYear.getOrDefault(2022, BigDecimal.ZERO));
            dto.setRealisasi2023(realizationByYear.getOrDefault(2023, BigDecimal.ZERO));
            dto.setRealisasi2024(realizationByYear.getOrDefault(2024, BigDecimal.ZERO));
            dto.setRealisasi2025(realizationByYear.getOrDefault(2025, BigDecimal.ZERO));
            
            // Calculate total
            BigDecimal total = dto.getRealisasi2021()
                .add(dto.getRealisasi2022())
                .add(dto.getRealisasi2023())
                .add(dto.getRealisasi2024())
                .add(dto.getRealisasi2025());
            dto.setTotalRealisasi(total);
            
            results.add(dto);
        }
        
        // Sort by total realisasi descending
        results.sort((a, b) -> b.getTotalRealisasi().compareTo(a.getTotalRealisasi()));
        
        System.out.println("=== Returning " + results.size() + " results ===");
        return results;
    }
    
    /**
     * Get ALL realization data from pbjt_realisasi table (PostgreSQL local)
     * Returns Map: assessment_id -> (year -> amount)
     */
    private Map<Long, Map<Integer, BigDecimal>> getAllRealizationFromLocal() {
        String query = """
            SELECT 
                assessment_id,
                tahun,
                realisasi_amount
            FROM pbjt_realisasi
            ORDER BY assessment_id, tahun
            """;
        
        Map<Long, Map<Integer, BigDecimal>> resultMap = new HashMap<>();
        
        try {
            List<Map<String, Object>> rows = pbjtJdbcTemplate.queryForList(query);
            
            for (Map<String, Object> row : rows) {
                Long assessmentId = ((Number) row.get("assessment_id")).longValue();
                Integer year = ((Number) row.get("tahun")).intValue();
                BigDecimal amount = (BigDecimal) row.get("realisasi_amount");
                
                resultMap.computeIfAbsent(assessmentId, k -> new HashMap<>()).put(year, amount);
            }
        } catch (Exception e) {
            System.err.println("Error querying pbjt_realisasi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultMap;
    }
    
    /**
     * Get realization for single assessment from pbjt_realisasi table
     */
    private Map<Integer, BigDecimal> getRealizationByAssessmentId(Long assessmentId) {
        String query = """
            SELECT 
                tahun,
                realisasi_amount
            FROM pbjt_realisasi
            WHERE assessment_id = ?
            ORDER BY tahun
            """;
        
        Map<Integer, BigDecimal> resultMap = new HashMap<>();
        
        try {
            System.out.println("DEBUG: Querying realisasi for assessment_id = " + assessmentId);
            List<Map<String, Object>> rows = pbjtJdbcTemplate.queryForList(query, assessmentId);
            System.out.println("DEBUG: Found " + rows.size() + " rows");
            
            for (Map<String, Object> row : rows) {
                Integer year = ((Number) row.get("tahun")).intValue();
                BigDecimal amount = (BigDecimal) row.get("realisasi_amount");
                System.out.println("DEBUG: Year=" + year + ", Amount=" + amount);
                resultMap.put(year, amount);
            }
        } catch (Exception e) {
            System.err.println("Error querying pbjt_realisasi for assessment " + assessmentId + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultMap;
    }
    
    public PbjtAssessmentWithRealizationDTO getAssessmentWithRealizationById(Long id) {
        Optional<PbjtAssessment> assessmentOpt = assessmentRepository.findById(id);
        
        if (assessmentOpt.isEmpty()) {
            return null;
        }
        
        PbjtAssessment assessment = assessmentOpt.get();
        PbjtAssessmentWithRealizationDTO dto = new PbjtAssessmentWithRealizationDTO();
        
        // Basic info
        dto.setId(assessment.getId());
        dto.setBusinessName(assessment.getBusinessName());
        dto.setBusinessType(assessment.getBusinessType());
        dto.setOwnerName(assessment.getOwnerName());
        dto.setTaxObjectNumber(assessment.getTaxObjectNumber());
        dto.setAddress(assessment.getAddress());
        dto.setKelurahan(assessment.getKelurahan());
        dto.setKecamatan(assessment.getKecamatan());
        dto.setLatitude(assessment.getLatitude());
        dto.setLongitude(assessment.getLongitude());
        dto.setAnnualPbjt(assessment.getAnnualPbjt());
        dto.setMonthlyPbjt(assessment.getMonthlyPbjt());
        dto.setConfidenceLevel(assessment.getConfidenceLevel());
        
        // Get realization from pbjt_realisasi table
        Map<Integer, BigDecimal> realizationByYear = getRealizationByAssessmentId(id);
        
        dto.setRealisasi2021(realizationByYear.getOrDefault(2021, BigDecimal.ZERO));
        dto.setRealisasi2022(realizationByYear.getOrDefault(2022, BigDecimal.ZERO));
        dto.setRealisasi2023(realizationByYear.getOrDefault(2023, BigDecimal.ZERO));
        dto.setRealisasi2024(realizationByYear.getOrDefault(2024, BigDecimal.ZERO));
        dto.setRealisasi2025(realizationByYear.getOrDefault(2025, BigDecimal.ZERO));
        
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : realizationByYear.values()) {
            total = total.add(value);
        }
        dto.setTotalRealisasi(total);
        
        return dto;
    }
    
    /**
     * Get assessments by location (kecamatan + kelurahan) with realization data
     */
    public List<PbjtAssessmentWithRealizationDTO> getAssessmentsByLocationWithRealization(String kecamatan, String kelurahan) {
        System.out.println("=== getAssessmentsByLocationWithRealization(" + kecamatan + ", " + kelurahan + ") ===");
        
        // Get assessments filtered by location
        List<PbjtAssessment> assessments = assessmentRepository.findByKecamatanIgnoreCaseAndKelurahanIgnoreCase(kecamatan, kelurahan);
        System.out.println("Found " + assessments.size() + " assessments for " + kelurahan + ", " + kecamatan);
        
        // Get realisasi data from local table
        Map<Long, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationFromLocal();
        
        List<PbjtAssessmentWithRealizationDTO> results = new ArrayList<>();
        
        for (PbjtAssessment assessment : assessments) {
            PbjtAssessmentWithRealizationDTO dto = new PbjtAssessmentWithRealizationDTO();
            
            // Basic info
            dto.setId(assessment.getId());
            dto.setBusinessId(assessment.getBusinessId());
            dto.setBusinessName(assessment.getBusinessName());
            dto.setBusinessType(assessment.getBusinessType());
            dto.setOwnerName(assessment.getOwnerName());
            dto.setTaxObjectNumber(assessment.getTaxObjectNumber());
            dto.setAddress(assessment.getAddress());
            dto.setKelurahan(assessment.getKelurahan());
            dto.setKecamatan(assessment.getKecamatan());
            dto.setLatitude(assessment.getLatitude());
            dto.setLongitude(assessment.getLongitude());
            dto.setAnnualPbjt(assessment.getAnnualPbjt());
            dto.setMonthlyPbjt(assessment.getMonthlyPbjt());
            dto.setConfidenceLevel(assessment.getConfidenceLevel());
            dto.setConfidenceScore(assessment.getConfidenceScore());
            
            // Get realisasi from local table
            Long assessmentId = assessment.getId();
            Map<Integer, BigDecimal> realizationByYear = allRealizationData.getOrDefault(assessmentId, Collections.emptyMap());
            
            dto.setRealisasi2021(realizationByYear.getOrDefault(2021, BigDecimal.ZERO));
            dto.setRealisasi2022(realizationByYear.getOrDefault(2022, BigDecimal.ZERO));
            dto.setRealisasi2023(realizationByYear.getOrDefault(2023, BigDecimal.ZERO));
            dto.setRealisasi2024(realizationByYear.getOrDefault(2024, BigDecimal.ZERO));
            dto.setRealisasi2025(realizationByYear.getOrDefault(2025, BigDecimal.ZERO));
            
            BigDecimal total = dto.getRealisasi2021()
                .add(dto.getRealisasi2022())
                .add(dto.getRealisasi2023())
                .add(dto.getRealisasi2024())
                .add(dto.getRealisasi2025());
            dto.setTotalRealisasi(total);
            
            results.add(dto);
        }
        
        return results;
    }
}
