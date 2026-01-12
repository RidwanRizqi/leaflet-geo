package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopKontributorDTO {
    private String npwpd;
    private String namaWp;
    private String jenisPajak;
    private BigDecimal totalPembayaran;
    private Long jumlahTransaksi;
}
