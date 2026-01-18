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
public class PbjtLocationStatsDTO {
    private String kecamatan;
    private String kdKec;  // Kode kecamatan untuk drill-down
    private String kdKel;  // Kode kelurahan dari SISMIOP
    private String kelurahan;
    private Long jumlahUsaha;
    private BigDecimal totalAnnualPbjt;
    private BigDecimal avgConfidenceScore;
    private Map<String, Long> businessTypes;
}
