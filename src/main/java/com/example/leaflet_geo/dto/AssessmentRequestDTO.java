package com.example.leaflet_geo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    
    @NotBlank(message = "Business ID is required")
    @Size(max = 50, message = "Business ID must not exceed 50 characters")
    private String businessId;
    
    @NotBlank(message = "Business name is required")
    private String businessName;
    
    @NotNull(message = "Assessment date is required")
    private LocalDate assessmentDate;
    
    // Profile data
    @DecimalMin(value = "10.0", message = "Building area must be at least 10 m²")
    @DecimalMax(value = "10000.0", message = "Building area seems unrealistic")
    private BigDecimal buildingArea;
    
    @NotNull(message = "Seating capacity is required")
    @Min(value = 5, message = "Minimum seating capacity is 5")
    @Max(value = 500, message = "Maximum seating capacity is 500")
    private Integer seatingCapacity;
    
    @NotNull(message = "Operating start time is required")
    private LocalTime operatingHoursStart;
    
    @NotNull(message = "Operating end time is required")
    private LocalTime operatingHoursEnd;
    
    private String businessType;
    
    private List<String> paymentMethods;
    
    // Location data
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private BigDecimal latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private BigDecimal longitude;
    
    @NotBlank(message = "Address is required")
    private String address;

    // Advanced Location Factors (New)
    private String roadType; // ARTERI, KOLEKTOR, LOKAL, GANG
    private Boolean nearSchool;
    private Boolean nearOffice;
    private Boolean nearMarket;

    private String kelurahan;
    private String kecamatan;
    private String kabupaten;
    
    // Observations (optional if menu items provided)
    // Removed @Size(min = 2) to allow Menu-only based assessment
    @Valid
    private List<ObservationDTO> observations;
    
    // Optional validation data
    private Map<String, Object> validationData;
    
    // Audit trail
    @NotBlank(message = "Surveyor ID is required")
    private String surveyorId;
    
    private String verifiedBy;
    private Boolean taxpayerSigned;
    
    // Supporting documents
    private List<String> photoUrls;
    private String supportingDocUrl;
    
    // New: Menu Based Method Inputs
    private List<MenuItemDTO> menuItems;
    private Integer openingDaysPerMonth;

    // Tax configuration (optional - defaults will be applied)
    private BigDecimal taxRate;
    private BigDecimal inflationRate;
    private BigDecimal operationalRate;
}
