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
    private final JdbcTemplate mysqlJdbcTemplate;
    
    // Constructor dengan @Qualifier untuk mysqlJdbcTemplate
    public PbjtRealizationService(
            PbjtAssessmentRepository assessmentRepository,
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate) {
        this.assessmentRepository = assessmentRepository;
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
    }
    
    public List<PbjtAssessmentWithRealizationDTO> getAllAssessmentsWithRealization() {
        System.out.println("=== PbjtRealizationService.getAllAssessmentsWithRealization() called ===");
        
        // Get all assessments
        List<PbjtAssessment> assessments = assessmentRepository.findAll();
        System.out.println("Found " + assessments.size() + " assessments from PostgreSQL");
        
        // Collect all NOPs
        List<String> allNops = assessments.stream()
            .map(PbjtAssessment::getTaxObjectNumber)
            .filter(nop -> nop != null && !nop.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        
        System.out.println("Found " + allNops.size() + " unique NOPs to query");
        
        // Batch query all realization data in ONE query
        Map<String, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationDataBatch(allNops);
        System.out.println("Got realization data for " + allRealizationData.size() + " NOPs from SIMATDA");
        
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
            
            // Get realization from batch result
            String nop = assessment.getTaxObjectNumber();
            Map<Integer, BigDecimal> realizationByYear = (nop != null && !nop.isEmpty()) 
                ? allRealizationData.getOrDefault(nop, Collections.emptyMap())
                : Collections.emptyMap();
            
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
     * Batch query - satu query untuk NOP tertentu saja menggunakan IN clause
     */
    private Map<String, Map<Integer, BigDecimal>> getAllRealizationDataBatch(List<String> nops) {
        if (nops.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Build IN clause dengan placeholder
        String placeholders = nops.stream().map(n -> "?").collect(Collectors.joining(","));
        
        String query = """
            SELECT 
                b.t_nop as nop,
                YEAR(a.t_tglpembayaran) as tahun,
                SUM(COALESCE(a.t_jmlhpembayaran, 0)) as total_bayar
            FROM t_transaksi a
            LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
            LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
            WHERE b.t_nop IN (%s)
              AND c.s_jenisobjek = 2
              AND a.t_tglpembayaran IS NOT NULL
              AND YEAR(a.t_tglpembayaran) BETWEEN 2021 AND 2025
            GROUP BY b.t_nop, YEAR(a.t_tglpembayaran)
            """.formatted(placeholders);
        
        Map<String, Map<Integer, BigDecimal>> resultMap = new HashMap<>();
        
        try {
            System.out.println("Executing batch SIMATDA query for " + nops.size() + " NOPs...");
            List<Map<String, Object>> rows = mysqlJdbcTemplate.queryForList(query, nops.toArray());
            System.out.println("Got " + rows.size() + " rows from SIMATDA");
            
            for (Map<String, Object> row : rows) {
                String nop = (String) row.get("nop");
                Integer year = ((Number) row.get("tahun")).intValue();
                BigDecimal amount = new BigDecimal(row.get("total_bayar").toString());
                
                resultMap.computeIfAbsent(nop, k -> new HashMap<>()).put(year, amount);
            }
        } catch (Exception e) {
            System.err.println("Error batch querying SIMATDA: " + e.getMessage());
            e.printStackTrace();
        }
        
        return resultMap;
    }
    
    private Map<Integer, BigDecimal> getRealizationByYear(String nop) {
        String query = """
            SELECT 
                YEAR(a.t_tglpembayaran) as tahun,
                SUM(COALESCE(a.t_jmlhpembayaran, 0)) as total_bayar
            FROM t_transaksi a
            LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
            LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
            WHERE b.t_nop = ?
              AND c.s_jenisobjek = 2
              AND a.t_tglpembayaran IS NOT NULL
              AND YEAR(a.t_tglpembayaran) BETWEEN 2021 AND 2025
            GROUP BY YEAR(a.t_tglpembayaran)
            """;
        
        Map<Integer, BigDecimal> resultMap = new HashMap<>();
        
        try {
            List<Map<String, Object>> rows = mysqlJdbcTemplate.queryForList(query, nop);
            
            for (Map<String, Object> row : rows) {
                Integer year = ((Number) row.get("tahun")).intValue();
                BigDecimal amount = new BigDecimal(row.get("total_bayar").toString());
                resultMap.put(year, amount);
            }
        } catch (Exception e) {
            System.err.println("Error querying SIMATDA for NOP " + nop + ": " + e.getMessage());
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
        
        // Get realization from SIMATDA
        if (assessment.getTaxObjectNumber() != null && !assessment.getTaxObjectNumber().isEmpty()) {
            Map<Integer, BigDecimal> realizationByYear = getRealizationByYear(assessment.getTaxObjectNumber());
            
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
        }
        
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
        
        // Collect all NOPs
        List<String> allNops = assessments.stream()
            .map(PbjtAssessment::getTaxObjectNumber)
            .filter(nop -> nop != null && !nop.isEmpty())
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        // Batch query all realization data
        Map<String, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationDataBatch(allNops);
        
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
            
            // Get realization from batch result
            String nop = assessment.getTaxObjectNumber();
            Map<Integer, BigDecimal> realizationByYear = (nop != null && !nop.isEmpty()) 
                ? allRealizationData.getOrDefault(nop, java.util.Collections.emptyMap())
                : java.util.Collections.emptyMap();
            
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
        
        return results;
    }
}
