package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private BigDecimal totalTarget;
    private BigDecimal totalRealisasi;
    private Long totalWp;
    private Long totalObjek;
    private Long totalTransaksi;
    private Long jenisPajakAktif;
    private Double persentasePencapaian;
    private BigDecimal selisih;
}
