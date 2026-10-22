package com.example.leaflet_geo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pbjt_master_menus")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PbjtMasterMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_menu", nullable = false, unique = true, length = 255)
    private String namaMenu;

    @Column(name = "kategori", nullable = false, length = 50)
    private String kategori; // FOOD / BEVERAGE

    @Column(name = "default_harga", precision = 15, scale = 2)
    private BigDecimal defaultHarga;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (kategori == null) {
            kategori = "FOOD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
