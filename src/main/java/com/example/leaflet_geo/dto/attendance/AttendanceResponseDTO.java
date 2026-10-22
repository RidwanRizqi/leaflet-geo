package com.example.leaflet_geo.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponseDTO {

    private Long id;
    private String userId;
    private String userName;
    private String userRole;
    private LocalDate tanggal;
    private LocalDateTime jamMasuk;
    private LocalDateTime jamPulang;

    // Lokasi Masuk
    private Double latitude;
    private Double longitude;
    private String lokasiStatus; // 'DI_KANTOR' | 'DI_LUAR_KANTOR'
    private Double jarakMeter;
    private Double gpsAccuracy;
    private String fotoMasuk;

    // Lokasi Pulang
    private Double pulangLatitude;
    private Double pulangLongitude;
    private String pulangLokasiStatus;
    private Double pulangJarakMeter;
    private Double pulangGpsAccuracy;
    private String fotoPulang;

    private String deviceInfo;
    private Boolean isCompleted; // true jika sudah absen masuk dan pulang
}
