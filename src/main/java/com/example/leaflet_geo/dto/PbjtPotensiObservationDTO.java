package com.example.leaflet_geo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class PbjtPotensiObservationDTO {
    private Long id;
    private Long potensiId;
    private LocalDateTime tanggalObservasi;
    private String tipeWaktu; // PEAK / NORMAL / OFF_PEAK
    private Integer jumlahPengunjung;
    private BigDecimal durasiJam;
    private BigDecimal pengunjungPerJam;
    private List<Map<String, Object>> menuItems;
    private BigDecimal avgFoodPrice;
    private BigDecimal avgBeveragePrice;
    private BigDecimal spendPerPerson;
    private BigDecimal estimatedDailyVisitors;
    private BigDecimal estimatedMonthlyRevenue;
    private BigDecimal estimatedMonthlyPbjt;
    private BigDecimal estimatedAnnualPbjt;
    private String petugas;
    private String catatan;
    private String[] photoUrls;
    private Integer surveyKe;
    private LocalDateTime createdAt;
}
