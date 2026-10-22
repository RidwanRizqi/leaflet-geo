package com.example.leaflet_geo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pbjt_menu_observation_history")
@Getter
@Setter
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
    private BigDecimal monthlyRevenueMenuBased;

    @Column(name = "monthly_pbjt_menu_based")
    private BigDecimal monthlyPbjtMenuBased;

    @Column(name = "annual_pbjt_menu_based")
    private BigDecimal annualPbjtMenuBased;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MenuObservationHistory() {
    }

    public MenuObservationHistory(Long id, PbjtAssessment assessment, LocalDate observationDate,
                                  Integer openingDaysPerMonth, List<Map<String, Object>> menuItems,
                                  BigDecimal monthlyRevenueMenuBased, BigDecimal monthlyPbjtMenuBased,
                                  BigDecimal annualPbjtMenuBased, LocalDateTime createdAt) {
        this.id = id;
        this.assessment = assessment;
        this.observationDate = observationDate;
        this.openingDaysPerMonth = openingDaysPerMonth;
        this.menuItems = menuItems;
        this.monthlyRevenueMenuBased = monthlyRevenueMenuBased;
        this.monthlyPbjtMenuBased = monthlyPbjtMenuBased;
        this.annualPbjtMenuBased = annualPbjtMenuBased;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static MenuObservationHistoryBuilder builder() {
        return new MenuObservationHistoryBuilder();
    }

    public static class MenuObservationHistoryBuilder {
        private Long id;
        private PbjtAssessment assessment;
        private LocalDate observationDate;
        private Integer openingDaysPerMonth;
        private List<Map<String, Object>> menuItems;
        private BigDecimal monthlyRevenueMenuBased;
        private BigDecimal monthlyPbjtMenuBased;
        private BigDecimal annualPbjtMenuBased;
        private LocalDateTime createdAt;

        MenuObservationHistoryBuilder() {
        }

        public MenuObservationHistoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MenuObservationHistoryBuilder assessment(PbjtAssessment assessment) {
            this.assessment = assessment;
            return this;
        }

        public MenuObservationHistoryBuilder observationDate(LocalDate observationDate) {
            this.observationDate = observationDate;
            return this;
        }

        public MenuObservationHistoryBuilder openingDaysPerMonth(Integer openingDaysPerMonth) {
            this.openingDaysPerMonth = openingDaysPerMonth;
            return this;
        }

        public MenuObservationHistoryBuilder menuItems(List<Map<String, Object>> menuItems) {
            this.menuItems = menuItems;
            return this;
        }

        public MenuObservationHistoryBuilder monthlyRevenueMenuBased(BigDecimal monthlyRevenueMenuBased) {
            this.monthlyRevenueMenuBased = monthlyRevenueMenuBased;
            return this;
        }

        public MenuObservationHistoryBuilder monthlyPbjtMenuBased(BigDecimal monthlyPbjtMenuBased) {
            this.monthlyPbjtMenuBased = monthlyPbjtMenuBased;
            return this;
        }

        public MenuObservationHistoryBuilder annualPbjtMenuBased(BigDecimal annualPbjtMenuBased) {
            this.annualPbjtMenuBased = annualPbjtMenuBased;
            return this;
        }

        public MenuObservationHistoryBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MenuObservationHistory build() {
            return new MenuObservationHistory(id, assessment, observationDate, openingDaysPerMonth,
                    menuItems, monthlyRevenueMenuBased, monthlyPbjtMenuBased, annualPbjtMenuBased, createdAt);
        }
    }
}
