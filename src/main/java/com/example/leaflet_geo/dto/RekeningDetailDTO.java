package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekeningDetailDTO {
    private String namaRekening;
    private Integer idRekening;
    private String kodeRekening;
    private String jenisPajak;
    private BigDecimal realisasi;
}
