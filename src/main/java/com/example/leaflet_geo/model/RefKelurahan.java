package com.example.leaflet_geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefKelurahan {

    private String kdPropinsi;
    private String kdDati2;
    private String kdKecamatan;
    private String kdKelurahan;
    private String kdSektor;
    private String nmKelurahan;
    private Integer noKelurahan;
    private String kdPosKelurahan;
}
