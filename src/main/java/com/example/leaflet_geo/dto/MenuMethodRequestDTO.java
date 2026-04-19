package com.example.leaflet_geo.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class MenuMethodRequestDTO {
    private LocalDate observationDate;
    private Integer openingDaysPerMonth;
    private List<Map<String, Object>> menuItems;
}
