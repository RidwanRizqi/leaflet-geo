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
@Table(name = "role_menu_mapping", schema = "system")
public class RoleMenuMapping {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "role_name", length = 25, nullable = false)
    private String roleName;

    @Column(name = "menu_id", nullable = false)
    private Integer menuId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
