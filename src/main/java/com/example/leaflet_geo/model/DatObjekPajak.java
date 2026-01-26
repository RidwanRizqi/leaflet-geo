package com.example.leaflet_geo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatObjekPajak {

    private String kdPropinsi;
    private String kdDati2;
    private String kdKecamatan;
    private String kdKelurahan;
    private String kdBlok;
    private String noUrut;
    private String kdJnsOp;
    private String subjekPajakId;
    private String noFormulirSpop;
    private String noPersil;
    private String jalanOp;
    private String blokKavNoOp;
    private String rwOp;
    private String rtOp;
    private Integer kdStatusCabang;
    private String kdStatusWp;
    private BigDecimal totalLuasBumi;
    private BigDecimal totalLuasBng;
    private BigDecimal njopBumi;
    private BigDecimal njopBng;
    private Integer statusPetaOp;
    private String jnsTransaksiOp;
    private LocalDate tglPendataanOp;
    private String nipPendata;
    private LocalDate tglPemeriksaanOp;
    private String nipPemeriksaOp;
    private LocalDate tglPerekamanOp;
    private String nipPerekamOp;
    private String noSertifikat;
    private String keteranganOp;
    private String keteranganSpop;
    private String latitude;
    private String longitude;

    // Reference data
    private String nmKecamatan;
    private String nmKelurahan;
    private String nmPropinsi;
    private String nmDati2;
}
