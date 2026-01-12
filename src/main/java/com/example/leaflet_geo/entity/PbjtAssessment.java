package com.example.leaflet_geo.entity;

import com.example.leaflet_geo.entity.enums.BusinessType;
import com.example.leaflet_geo.entity.enums.ConfidenceLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "pbjt_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PbjtAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "business_id", nullable = false, unique = true, length = 50)
    private String businessId;
    
    @Column(name = "business_name", nullable = false)
    private String businessName;
    
    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;
    
    // Profile data
    @Column(name = "building_area", precision = 10, scale = 2)
    private BigDecimal buildingArea;
    
    @Column(name = "seating_capacity")
    private Integer seatingCapacity;
    
    @Column(name = "operating_hours_start")
    private LocalTime operatingHoursStart;
    
    @Column(name = "operating_hours_end")
    private LocalTime operatingHoursEnd;
    
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    @Column(name = "payment_methods", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] paymentMethods;
    
    // Observation data stored as JSON
    @Column(name = "observations", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> observations;
    
    // Calculation results
    @Column(name = "daily_revenue_weekday", precision = 15, scale = 2)
    private BigDecimal dailyRevenueWeekday;
    
    @Column(name = "daily_revenue_weekend", precision = 15, scale = 2)
    private BigDecimal dailyRevenueWeekend;
    
    @Column(name = "monthly_revenue_raw", precision = 15, scale = 2)
    private BigDecimal monthlyRevenueRaw;
    
    @Column(name = "monthly_revenue_adjusted", precision = 15, scale = 2)
    private BigDecimal monthlyRevenueAdjusted;
    
    // Adjustment factors
    @Column(name = "business_type_coefficient", precision = 4, scale = 2)
    private BigDecimal businessTypeCoefficient;
    
    @Column(name = "location_score", precision = 4, scale = 2)
    private BigDecimal locationScore;
    
    @Column(name = "operational_rate", precision = 4, scale = 2)
    private BigDecimal operationalRate;
    
    // Tax calculations
    @Column(name = "monthly_pbjt", precision = 15, scale = 2)
    private BigDecimal monthlyPbjt;
    
    @Column(name = "annual_pbjt", precision = 15, scale = 2)
    private BigDecimal annualPbjt;
    
    @Column(name = "tax_rate", precision = 4, scale = 2)
    private BigDecimal taxRate;
    
    @Column(name = "inflation_rate", precision = 4, scale = 2)
    private BigDecimal inflationRate;
    
    // Confidence scoring
    @Column(name = "confidence_score")
    private Integer confidenceScore;
    
    @Column(name = "confidence_level", length = 20)
    private String confidenceLevel;
    
    // Validation data as JSON
    @Column(name = "validation_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> validationData;
    
    // Audit trail
    @Column(name = "surveyor_id", length = 50)
    private String surveyorId;
    
    @Column(name = "verified_by", length = 50)
    private String verifiedBy;
    
    @Column(name = "taxpayer_signed")
    private Boolean taxpayerSigned;
    
    // GIS data
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "address", columnDefinition = "text")
    private String address;
    
    @Column(name = "kelurahan")
    private String kelurahan;
    
    @Column(name = "kecamatan")
    private String kecamatan;
    
    @Column(name = "kabupaten")
    private String kabupaten;
    
    // Tax object identification (from SIMATDA)
    @Column(name = "tax_object_id", length = 50)
    private String taxObjectId;
    
    @Column(name = "tax_object_number", length = 50)
    private String taxObjectNumber; // NOP (Nomor Objek Pajak)
    
    @Column(name = "owner_name")
    private String ownerName;
    
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    @Column(name = "seasonal_adjustment", precision = 4, scale = 2)
    private BigDecimal seasonalAdjustment;
    
    @Column(name = "last_survey_date")
    private LocalDate lastSurveyDate;
    
    @Column(name = "notes", columnDefinition = "text")
    private String notes;
    
    @Column(name = "status", length = 20)
    private String status;
    
    // Supporting documents
    @Column(name = "photo_urls", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] photoUrls;
    
    @Column(name = "supporting_doc_url")
    private String supportingDocUrl;
    
    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // One-to-many relationship with observations
    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObservationHistory> observationHistories;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
