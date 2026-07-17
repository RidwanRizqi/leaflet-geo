package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyeksiSummaryDTO {
    private Integer tahun;
    private BigDecimal totalTargetProyeksi;
    private Integer totalJenisPajak;
    private String statusSistem;
    private List<ProyeksiItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProyeksiItemDTO {
        private Long id;
        private Integer jenisPajakId;
        private String namaPajak;
        private Integer tahun;
        private Integer bulan;
        private String tanggalProyeksi;
        private BigDecimal nilaiProyeksi;
        private String modelPemenang;
        private String strategiGuncangan;
    }
}
