package com.example.leaflet_geo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pbjt_observation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private PbjtAssessment assessment;
    
    @Column(name = "observation_date", nullable = false)
    private LocalDateTime observationDate;
    
    @Column(name = "day_type", nullable = false, length = 30)
    private String dayType;
    
    @Column(name = "visitors", nullable = false)
    private Integer visitors;
    
    @Column(name = "duration_hours", precision = 3, scale = 1, nullable = false)
    private BigDecimal durationHours;
    
    @Column(name = "avg_transaction", precision = 10, scale = 2, nullable = false)
    private BigDecimal avgTransaction;
    
    @Column(name = "sample_transactions", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<BigDecimal> sampleTransactions;
    
    @Column(name = "visitors_per_hour", precision = 10, scale = 2)
    private BigDecimal visitorsPerHour;
    
    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
