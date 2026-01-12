package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PbjtAssessmentWithRealizationDTO {
    private Long id;
    private String businessId;
    private String businessName;
    private String businessType;
    private String ownerName;
    private String taxObjectNumber; // NOP
    private String address;
    private String kelurahan;
    private String kecamatan;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // Realisasi per tahun dari SIMATDA
    private BigDecimal realisasi2021;
    private BigDecimal realisasi2022;
    private BigDecimal realisasi2023;
    private BigDecimal realisasi2024;
    private BigDecimal realisasi2025;
    private BigDecimal totalRealisasi;
    
    // Assessment data
    private BigDecimal annualPbjt;
    private BigDecimal monthlyPbjt;
    private String confidenceLevel;
    private Integer confidenceScore;
}
