package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}
