package com.example.leaflet_geo.dto.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummaryDTO {

    private LocalDate tanggal;
    private Long totalUser;
    private Long totalHadir;
    private Long totalBelumHadir;
    private Long totalDiKantor;
    private Long totalDiLuarKantor;
}
