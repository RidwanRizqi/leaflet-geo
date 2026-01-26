package com.example.leaflet_geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KecamatanWithCount {
    private String kdPropinsi;
    private String kdDati2;
    private String kdKecamatan;
    private String nmKecamatan;
    private Integer jumlahBidang;
}
