package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterMenuDTO {
    private Long id;
    private String namaMenu;
    private String kategori;
    private BigDecimal defaultHarga;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
