package com.example.leaflet_geo.dto.attendance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceCheckOutRequest {

    @NotNull(message = "Latitude wajib diisi")
    private Double latitude;

    @NotNull(message = "Longitude wajib diisi")
    private Double longitude;

    private Double gpsAccuracy;

    private String fotoBase64;

    private String deviceInfo;
}
