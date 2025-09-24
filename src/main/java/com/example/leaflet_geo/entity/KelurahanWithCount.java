package com.example.leaflet_geo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KelurahanWithCount {
    private String kdPropinsi;
    private String kdDati2;
    private String kdKecamatan;
    private String kdKelurahan;
    private String nmKelurahan;
    private Integer jumlahBidang;
}
