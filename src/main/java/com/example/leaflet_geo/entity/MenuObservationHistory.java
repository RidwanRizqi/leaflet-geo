package com.example.leaflet_geo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pbjt_menu_observation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuObservationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @JsonIgnore
    private PbjtAssessment assessment;

    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "opening_days_per_month", nullable = false)
    private Integer openingDaysPerMonth;

    @Column(name = "menu_items", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> menuItems;

    @Column(name = "monthly_revenue_menu_based")
    private java.math.BigDecimal monthlyRevenueMenuBased;

    @Column(name = "monthly_pbjt_menu_based")
    private java.math.BigDecimal monthlyPbjtMenuBased;

    @Column(name = "annual_pbjt_menu_based")
    private java.math.BigDecimal annualPbjtMenuBased;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
