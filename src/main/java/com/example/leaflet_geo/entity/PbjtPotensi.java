package com.example.leaflet_geo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pbjt_potensi")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PbjtPotensi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_potensi", nullable = false, unique = true, length = 50)
    private String kodePotensi;

    @Column(name = "nama_usaha", nullable = false)
    private String namaUsaha;

    @Column(name = "tipe_usaha", length = 50)
    private String tipeUsaha;

    @Column(name = "tanggal_pendataan", nullable = false)
    private LocalDate tanggalPendataan;

    @Column(name = "kapasitas_kursi")
    private Integer kapasitasKursi;

    @Column(name = "luas_bangunan", precision = 10, scale = 2)
    private BigDecimal luasBangunan;

    @Column(name = "jam_buka")
    private LocalTime jamBuka;

    @Column(name = "jam_tutup")
    private LocalTime jamTutup;

    @Column(name = "payment_methods", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] paymentMethods;

    @Column(name = "alamat", columnDefinition = "text")
    private String alamat;

    @Column(name = "kelurahan")
    private String kelurahan;

    @Column(name = "kecamatan")
    private String kecamatan;

    @Column(name = "kabupaten")
    private String kabupaten;

    @Column(name = "kd_kec", length = 10)
    private String kdKec;

    @Column(name = "kd_kel", length = 10)
    private String kdKel;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "road_type", length = 50)
    private String roadType;

    @Column(name = "near_school")
    private Boolean nearSchool;

    @Column(name = "near_office")
    private Boolean nearOffice;

    @Column(name = "near_market")
    private Boolean nearMarket;

    @Column(name = "crowd_level", length = 20)
    private String crowdLevel;

    @Column(name = "opening_days_per_month")
    private Integer openingDaysPerMonth;

    @Column(name = "menu_items", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> menuItems;

    @Column(name = "customer_order_estimates", length = 1000)
    private String customerOrderEstimates;

    @Column(name = "best_selling_menus", length = 1000)
    private String bestSellingMenus;

    @Column(name = "crowd_peak_times", length = 1000)
    private String crowdPeakTimes;

    @Column(name = "omzet_bulanan", precision = 15, scale = 2)
    private BigDecimal omzetBulanan;

    @Column(name = "pbjt_bulanan", precision = 15, scale = 2)
    private BigDecimal pbjtBulanan;

    @Column(name = "pbjt_tahunan", precision = 15, scale = 2)
    private BigDecimal pbjtTahunan;

    @Column(name = "surveyor_id", length = 50)
    private String surveyorId;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "photo_urls", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] photoUrls;

    @Column(name = "supporting_doc_url")
    private String supportingDocUrl;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "potensi", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tanggalObservasi DESC")
    private List<PbjtPotensiObservation> observations;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (kabupaten == null) {
            kabupaten = "Lumajang";
        }
        if (openingDaysPerMonth == null) {
            openingDaysPerMonth = 30;
        }
        if (crowdLevel == null) {
            crowdLevel = "NORMAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
