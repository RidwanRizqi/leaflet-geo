package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetRealisasiDTO {
    private String jenisPajak;
    private Integer urutan;
    private BigDecimal target;
    private BigDecimal realisasi;
    private BigDecimal selisih;
    private Double persentasePencapaian;
    
    // Metrik Kalkulasi Probabilitas
    private Double runRateAktual;
    private Double runRateDibutuhkan;
    private Double defisitSurplus;
    private Double rasio;
    private Double probabilitas;
    private String kategori;
    private String warna;
    private Boolean isWarning;

    private List<RekeningDetailDTO> details; // Breakdown per rekening
}
