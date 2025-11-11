package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendBulananDTO {
    private Integer bulan;
    private String namaBulan;
    private BigDecimal realisasiBulan;
    private BigDecimal realisasiKumulatif;
}
