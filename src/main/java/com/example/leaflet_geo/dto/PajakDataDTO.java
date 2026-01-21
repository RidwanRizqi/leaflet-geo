package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO untuk data pajak bulanan per kategori
 * Digunakan oleh Dashboard Pajak untuk menampilkan trend bulanan per jenis
 * pajak
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PajakDataDTO {
    private String kategori;
    private Integer tahun;
    private String bulan;
    private BigDecimal value;
}
