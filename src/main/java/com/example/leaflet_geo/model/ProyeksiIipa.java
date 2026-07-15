package com.example.leaflet_geo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_proyeksi_pajak_iipa", schema = "sig")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyeksiIipa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jenis_pajak_id", nullable = false)
    private Integer jenisPajakId;

    @Column(name = "nama_pajak", nullable = false, length = 100)
    private String namaPajak;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "bulan", nullable = false)
    private Integer bulan;

    @Column(name = "tanggal_proyeksi", nullable = false)
    private LocalDate tanggalProyeksi;

    @Column(name = "nilai_proyeksi", nullable = false, precision = 18, scale = 2)
    private BigDecimal nilaiProyeksi;

    @Column(name = "model_pemenang", nullable = false, length = 50)
    private String modelPemenang;

    @Column(name = "strategi_guncangan", nullable = false, length = 50)
    private String strategiGuncangan;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
