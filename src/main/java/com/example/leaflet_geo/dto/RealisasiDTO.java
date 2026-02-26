package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealisasiDTO {
    private Integer tahun;
    private BigDecimal realisasiAmount;
    private Integer jumlahTransaksi;
}
