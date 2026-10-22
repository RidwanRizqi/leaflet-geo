package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PbjtPotensiDTO {
    private Long id;
    private String kodePotensi;
    private String namaUsaha;
    private String tipeUsaha;
    private LocalDate tanggalPendataan;
    private Integer kapasitasKursi;
    private BigDecimal luasBangunan;
    private LocalTime jamBuka;
    private LocalTime jamTutup;
    private String[] paymentMethods;
    private String alamat;
    private String kelurahan;
    private String kecamatan;
    private String kabupaten;
    private String kdKec;
    private String kdKel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String roadType;
    private Boolean nearSchool;
    private Boolean nearOffice;
    private Boolean nearMarket;
    private String crowdLevel;
    private Integer openingDaysPerMonth;
    private List<Map<String, Object>> menuItems;
    private String customerOrderEstimates;
    private String bestSellingMenus;
    private String crowdPeakTimes;
    private BigDecimal omzetBulanan;
    private BigDecimal pbjtBulanan;
    private BigDecimal pbjtTahunan;
    private String surveyorId;
    private String updatedBy;
    private String[] photoUrls;
    private String supportingDocUrl;
    private String notes;
    private Integer observationCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
