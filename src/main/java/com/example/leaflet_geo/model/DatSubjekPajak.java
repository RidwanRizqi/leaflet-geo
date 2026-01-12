package com.example.leaflet_geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatSubjekPajak {

    private String subjekPajakId;
    private String nmWp;
    private String jalanWp;
    private String blokKavNoWp;
    private String rwWp;
    private String rtWp;
    private String kelurahanWp;
    private String kotaWp;
    private String kdPosWp;
    private String telpWp;
    private String npwp;
    private String statusPekerjaanWp;
    private String npwpd;
    private String email;

    // Reference data
    private String nmKecamatan;
    private String nmKelurahan;
    private String nmPropinsi;
    private String nmDati2;
}
