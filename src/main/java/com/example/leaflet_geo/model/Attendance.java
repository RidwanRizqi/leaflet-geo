package com.example.leaflet_geo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance", schema = "system")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "tanggal", nullable = false)
    private LocalDate tanggal;

    @Column(name = "jam_masuk")
    private LocalDateTime jamMasuk;

    @Column(name = "jam_pulang")
    private LocalDateTime jamPulang;

    // Lokasi Absen Masuk
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "lokasi_status", length = 20, nullable = false)
    private String lokasiStatus; // 'DI_KANTOR' | 'DI_LUAR_KANTOR'

    @Column(name = "jarak_meter")
    private Double jarakMeter;

    @Column(name = "gps_accuracy")
    private Double gpsAccuracy;

    // Lokasi Absen Pulang
    @Column(name = "pulang_latitude")
    private Double pulangLatitude;

    @Column(name = "pulang_longitude")
    private Double pulangLongitude;

    @Column(name = "pulang_lokasi_status", length = 20)
    private String pulangLokasiStatus;

    @Column(name = "pulang_jarak_meter")
    private Double pulangJarakMeter;

    @Column(name = "pulang_gps_accuracy")
    private Double pulangGpsAccuracy;

    // Foto Selfie
    @Column(name = "foto_masuk", columnDefinition = "TEXT")
    private String fotoMasuk;

    @Column(name = "foto_pulang", columnDefinition = "TEXT")
    private String fotoPulang;

    // Metadata & Audit
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.tanggal == null) {
            this.tanggal = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
