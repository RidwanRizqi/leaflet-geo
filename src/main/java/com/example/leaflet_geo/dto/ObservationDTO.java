package com.example.leaflet_geo.dto;


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

    // Inner class for sample transaction with notes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SampleTransactionDTO {
        
        
        private BigDecimal amount;

        private String notes;
    }

    
    private LocalDateTime observationDate;

    
    private String dayType;

    
    
    
    private Integer visitors;

    
    
    
    private BigDecimal durationHours;

    private List<SampleTransactionDTO> sampleTransactions;

    private String assignedUserId;

    private String notes;
}
