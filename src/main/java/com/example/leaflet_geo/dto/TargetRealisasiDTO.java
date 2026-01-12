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
    private List<RekeningDetailDTO> details; // Breakdown per rekening
}
