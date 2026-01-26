package com.example.leaflet_geo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user", schema = "system")
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "username", length = 32, unique = true)
    private String username;

    @Column(name = "password", length = 64)
    private String password; // BCrypt hash

    @Column(name = "nama", length = 100)
    private String nama;

    @Column(name = "id_unit", length = 36)
    private String idUnit;

    @Column(name = "token", length = 64)
    private String token;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "role", length = 25)
    private String role;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    @Column(name = "recover_at")
    private LocalDateTime recoverAt;

    @Column(name = "recover_by", length = 36)
    private String recoverBy;
}