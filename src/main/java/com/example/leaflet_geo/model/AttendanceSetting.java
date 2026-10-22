package com.example.leaflet_geo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attendance_setting", schema = "system")
public class AttendanceSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "role_name", length = 25, nullable = false, unique = true)
    private String roleName;

    @Builder.Default
    @Column(name = "is_mandatory", nullable = false)
    private Boolean isMandatory = false;

    @Builder.Default
    @Column(name = "default_landing_url", length = 100)
    private String defaultLandingUrl = "/dashboard-pajak";

    @Column(name = "jam_masuk_start")
    private LocalTime jamMasukStart;

    @Column(name = "jam_masuk_end")
    private LocalTime jamMasukEnd;

    @Column(name = "jam_pulang_start")
    private LocalTime jamPulangStart;

    @Column(name = "jam_pulang_end")
    private LocalTime jamPulangEnd;

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
        if (this.isMandatory == null) {
            this.isMandatory = false;
        }
        if (this.defaultLandingUrl == null) {
            this.defaultLandingUrl = "/dashboard-pajak";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
