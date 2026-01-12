package com.example.leaflet_geo.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationDTO {
    
    @NotNull(message = "Observation date is required")
    private LocalDateTime observationDate;
    
    @NotBlank(message = "Day type is required")
    private String dayType;
    
    @NotNull(message = "Number of visitors is required")
    @Min(value = 1, message = "At least 1 visitor must be recorded")
    @Max(value = 1000, message = "Visitor count seems unrealistic")
    private Integer visitors;
    
    @NotNull(message = "Duration hours is required")
    @DecimalMin(value = "0.5", message = "Minimum observation duration is 0.5 hours")
    @DecimalMax(value = "24.0", message = "Maximum observation duration is 24 hours")
    private BigDecimal durationHours;
    
    @NotNull(message = "Sample transactions are required")
    @Size(min = 5, max = 30, message = "Please provide 5-30 sample transaction values")
    private List<BigDecimal> sampleTransactions;
    
    private String notes;
}
