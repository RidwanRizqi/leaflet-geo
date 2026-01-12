package com.example.leaflet_geo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sig.bidang")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bidang {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "kd_prop", length = 2, nullable = false)
    private String kdProp;

    @Column(name = "kd_dati2", length = 2, nullable = false)
    private String kdDati2;

    @Column(name = "kd_kec", length = 3, nullable = false)
    private String kdKec;

    @Column(name = "kd_kel", length = 3, nullable = false)
    private String kdKel;

    @Column(name = "kd_blok", length = 3, nullable = false)
    private String kdBlok;

    @Column(name = "no_urut", length = 4, nullable = false)
    private String noUrut;

    @Column(name = "kd_jns_op", length = 1, nullable = false)
    private String kdJnsOp;

    @Column(name = "nop", length = 24, unique = true)
    private String nop;

    @Column(name = "geom", columnDefinition = "text", nullable = false)
    private String geom;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", columnDefinition = "uuid")
    private UUID deletedBy;

    @Column(name = "recover_at")
    private LocalDateTime recoverAt;

    @Column(name = "recover_by", columnDefinition = "uuid")
    private UUID recoverBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Unique constraint for composite key
    @Table(uniqueConstraints = @UniqueConstraint(name = "nop_ukey", columnNames = { "kdProp", "kdDati2", "kdKec",
            "kdKel", "kdBlok", "noUrut", "kdJnsOp" }))
    public static class BidangTable {
    }
}
