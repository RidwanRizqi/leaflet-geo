package com.example.leaflet_geo.service;

import com.example.leaflet_geo.dto.PbjtAssessmentWithRealizationDTO;
import com.example.leaflet_geo.model.PbjtAssessment;
import com.example.leaflet_geo.repository.pbjt.PbjtAssessmentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class PbjtRealizationService {

    private final PbjtAssessmentRepository assessmentRepository;
    private final JdbcTemplate pbjtJdbcTemplate;

    public PbjtRealizationService(
            PbjtAssessmentRepository assessmentRepository,
            @Qualifier("pbjtJdbcTemplate") JdbcTemplate pbjtJdbcTemplate) {
        this.assessmentRepository = assessmentRepository;
        this.pbjtJdbcTemplate = pbjtJdbcTemplate;
    }

    public List<PbjtAssessmentWithRealizationDTO> getAllAssessmentsWithRealization() {
        log.info("=== PbjtRealizationService.getAllAssessmentsWithRealization() called ===");

        List<PbjtAssessment> assessments = assessmentRepository.findAll();
        log.info("Found {} assessments from PostgreSQL", assessments.size());

        // Get ALL realisasi data from pbjt_realisasi table (PostgreSQL local)
        Map<Long, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationFromLocal();
        log.info("Got realization data for {} assessments from pbjt_realisasi table", allRealizationData.size());

        List<PbjtAssessmentWithRealizationDTO> results = new ArrayList<>();

        for (PbjtAssessment assessment : assessments) {
            PbjtAssessmentWithRealizationDTO dto = mapToRealizationDTO(assessment);

            Long assessmentId = assessment.getId();
            Map<Integer, BigDecimal> realizationByYear = allRealizationData.getOrDefault(assessmentId,
                    Collections.emptyMap());

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

        results.sort((a, b) -> b.getTotalRealisasi().compareTo(a.getTotalRealisasi()));

        log.info("=== Returning {} results ===", results.size());
        return results;
    }

    public PbjtAssessmentWithRealizationDTO getAssessmentWithRealizationById(Long id) {
        Optional<PbjtAssessment> assessmentOpt = assessmentRepository.findById(id);
        if (assessmentOpt.isEmpty())
            return null;

        PbjtAssessment assessment = assessmentOpt.get();
        PbjtAssessmentWithRealizationDTO dto = mapToRealizationDTO(assessment);

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

    public List<PbjtAssessmentWithRealizationDTO> getAssessmentsByLocationWithRealization(
            String kecamatan, String kelurahan) {
        log.info("=== getAssessmentsByLocationWithRealization({}, {}) ===", kecamatan, kelurahan);

        List<PbjtAssessment> assessments = assessmentRepository.findByKecamatanAndKelurahan(kecamatan, kelurahan);
        log.info("Found {} assessments for {}, {}", assessments.size(), kelurahan, kecamatan);

        Map<Long, Map<Integer, BigDecimal>> allRealizationData = getAllRealizationFromLocal();

        List<PbjtAssessmentWithRealizationDTO> results = new ArrayList<>();

        for (PbjtAssessment assessment : assessments) {
            PbjtAssessmentWithRealizationDTO dto = mapToRealizationDTO(assessment);

            Long assessmentId = assessment.getId();
            Map<Integer, BigDecimal> realizationByYear = allRealizationData.getOrDefault(assessmentId,
                    Collections.emptyMap());

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

    private PbjtAssessmentWithRealizationDTO mapToRealizationDTO(PbjtAssessment assessment) {
        PbjtAssessmentWithRealizationDTO dto = new PbjtAssessmentWithRealizationDTO();
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
        return dto;
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
            log.error("Error querying pbjt_realisasi: {}", e.getMessage());
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
            List<Map<String, Object>> rows = pbjtJdbcTemplate.queryForList(query, assessmentId);

            for (Map<String, Object> row : rows) {
                Integer year = ((Number) row.get("tahun")).intValue();
                BigDecimal amount = (BigDecimal) row.get("realisasi_amount");
                resultMap.put(year, amount);
            }
        } catch (Exception e) {
            log.error("Error querying pbjt_realisasi for assessment {}: {}", assessmentId, e.getMessage());
        }

        return resultMap;
    }
}
