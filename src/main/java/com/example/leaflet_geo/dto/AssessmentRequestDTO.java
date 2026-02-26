package com.example.leaflet_geo.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRequestDTO {

    private String businessId;

    
    private String businessName;

    
    private LocalDate assessmentDate;

    // Profile data
    
    
    private BigDecimal buildingArea;

    
    
    
    private Integer seatingCapacity;

    
    private LocalTime operatingHoursStart;

    
    private LocalTime operatingHoursEnd;

    private String businessType;

    private List<String> paymentMethods;

    // Location data
    
    
    
    private BigDecimal latitude;

    
    
    
    private BigDecimal longitude;

    
    private String address;

    // Advanced Location Factors
    private String roadType;
    private Boolean nearSchool;
    private Boolean nearOffice;
    private Boolean nearMarket;

    private String kelurahan;
    private String kecamatan;
    private String kabupaten;

    // Observations
    
    private List<ObservationDTO> observations;

    // Optional validation data
    private Map<String, Object> validationData;

    // Audit trail
    
    private String surveyorId;

    private String verifiedBy;
    private Boolean taxpayerSigned;

    // Supporting documents
    private List<String> photoUrls;
    private String supportingDocUrl;

    // Menu Based Method Inputs
    private List<MenuItemDTO> menuItems;
    private Integer openingDaysPerMonth;

    // Tax configuration (optional - defaults will be applied)
    private BigDecimal taxRate;
    private BigDecimal inflationRate;
    private BigDecimal operationalRate;
}
