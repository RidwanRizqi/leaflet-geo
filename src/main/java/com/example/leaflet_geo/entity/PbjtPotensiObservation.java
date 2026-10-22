package com.example.leaflet_geo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pbjt_potensi_observation")
@Getter
@Setter
public class PbjtPotensiObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "potensi_id", nullable = false)
    @JsonIgnore
    private PbjtPotensi potensi;

    @Column(name = "tanggal_observasi", nullable = false)
    private LocalDateTime tanggalObservasi;

    @Column(name = "tipe_waktu", length = 50)
    private String tipeWaktu; // e.g. "PEAK", "NORMAL", "OFF_PEAK"

    @Column(name = "jumlah_pengunjung", nullable = false)
    private Integer jumlahPengunjung;

    @Column(name = "durasi_jam", precision = 5, scale = 2, nullable = false)
    private BigDecimal durasiJam;

    @Column(name = "pengunjung_per_jam", precision = 10, scale = 2)
    private BigDecimal pengunjungPerJam;

    @Column(name = "menu_items", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> menuItems;

    @Column(name = "avg_food_price", precision = 15, scale = 2)
    private BigDecimal avgFoodPrice;

    @Column(name = "avg_beverage_price", precision = 15, scale = 2)
    private BigDecimal avgBeveragePrice;

    @Column(name = "spend_per_person", precision = 15, scale = 2)
    private BigDecimal spendPerPerson;

    @Column(name = "estimated_daily_visitors", precision = 10, scale = 2)
    private BigDecimal estimatedDailyVisitors;

    @Column(name = "estimated_monthly_revenue", precision = 15, scale = 2)
    private BigDecimal estimatedMonthlyRevenue;

    @Column(name = "estimated_monthly_pbjt", precision = 15, scale = 2)
    private BigDecimal estimatedMonthlyPbjt;

    @Column(name = "estimated_annual_pbjt", precision = 15, scale = 2)
    private BigDecimal estimatedAnnualPbjt;

    @Column(name = "petugas", length = 100)
    private String petugas;

    @Column(name = "catatan", columnDefinition = "text")
    private String catatan;

    @Column(name = "photo_urls", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] photoUrls;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PbjtPotensiObservation() {
    }

    public PbjtPotensiObservation(Long id, PbjtPotensi potensi, LocalDateTime tanggalObservasi,
                                  String tipeWaktu, Integer jumlahPengunjung, BigDecimal durasiJam,
                                  BigDecimal pengunjungPerJam, List<Map<String, Object>> menuItems,
                                  BigDecimal avgFoodPrice, BigDecimal avgBeveragePrice, BigDecimal spendPerPerson,
                                  BigDecimal estimatedDailyVisitors, BigDecimal estimatedMonthlyRevenue,
                                  BigDecimal estimatedMonthlyPbjt, BigDecimal estimatedAnnualPbjt,
                                  String petugas, String catatan, LocalDateTime createdAt) {
        this.id = id;
        this.potensi = potensi;
        this.tanggalObservasi = tanggalObservasi;
        this.tipeWaktu = tipeWaktu;
        this.jumlahPengunjung = jumlahPengunjung;
        this.durasiJam = durasiJam;
        this.pengunjungPerJam = pengunjungPerJam;
        this.menuItems = menuItems;
        this.avgFoodPrice = avgFoodPrice;
        this.avgBeveragePrice = avgBeveragePrice;
        this.spendPerPerson = spendPerPerson;
        this.estimatedDailyVisitors = estimatedDailyVisitors;
        this.estimatedMonthlyRevenue = estimatedMonthlyRevenue;
        this.estimatedMonthlyPbjt = estimatedMonthlyPbjt;
        this.estimatedAnnualPbjt = estimatedAnnualPbjt;
        this.petugas = petugas;
        this.catatan = catatan;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.tanggalObservasi == null) {
            this.tanggalObservasi = LocalDateTime.now();
        }
    }
}
