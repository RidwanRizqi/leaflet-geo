package com.example.leaflet_geo.dto.attendance;

import com.example.leaflet_geo.model.AttendanceSetting;
import com.example.leaflet_geo.model.OfficeLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceStatusTodayDTO {

    private Boolean hasCheckedIn;
    private Boolean hasCheckedOut;
    private Boolean isMandatory;
    private String defaultLandingUrl;
    private AttendanceResponseDTO attendance;
    private OfficeLocation officeLocation;
    private AttendanceSetting setting;
}
