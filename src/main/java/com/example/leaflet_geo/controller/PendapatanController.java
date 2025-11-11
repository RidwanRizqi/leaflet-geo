package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.DashboardSummaryDTO;
import com.example.leaflet_geo.dto.TargetRealisasiDTO;
import com.example.leaflet_geo.dto.TopKontributorDTO;
import com.example.leaflet_geo.dto.TrendBulananDTO;
import com.example.leaflet_geo.service.PendapatanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pendapatan")
@CrossOrigin(origins = "*")
public class PendapatanController {

    private final PendapatanService pendapatanService;

    public PendapatanController(PendapatanService pendapatanService) {
        this.pendapatanService = pendapatanService;
    }

    /**
     * Get Dashboard Summary
     * GET /api/pendapatan/summary?tahun=2025
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
            @RequestParam(defaultValue = "2025") Integer tahun) {
        try {
            DashboardSummaryDTO summary = pendapatanService.getDashboardSummary(tahun);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Target vs Realisasi per Jenis Pajak
     * GET /api/pendapatan/target-realisasi?tahun=2025
     */
    @GetMapping("/target-realisasi")
    public ResponseEntity<List<TargetRealisasiDTO>> getTargetRealisasi(
            @RequestParam(defaultValue = "2025") Integer tahun) {
        try {
            List<TargetRealisasiDTO> data = pendapatanService.getTargetRealisasiPerJenis(tahun);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Trend Bulanan (Kumulatif)
     * GET /api/pendapatan/trend-bulanan?tahun=2025
     */
    @GetMapping("/trend-bulanan")
    public ResponseEntity<List<TrendBulananDTO>> getTrendBulanan(
            @RequestParam(defaultValue = "2025") Integer tahun) {
        try {
            List<TrendBulananDTO> data = pendapatanService.getTrendBulanan(tahun);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Top Kontributor
     * GET /api/pendapatan/top-kontributor?tahun=2025&limit=10
     */
    @GetMapping("/top-kontributor")
    public ResponseEntity<List<TopKontributorDTO>> getTopKontributor(
            @RequestParam(defaultValue = "2025") Integer tahun,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<TopKontributorDTO> data = pendapatanService.getTopKontributor(tahun, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Realisasi by Jenis Pajak (Detail)
     * GET /api/pendapatan/realisasi-by-jenis?tahun=2025&jenisPajakId=1
     */
    @GetMapping("/realisasi-by-jenis")
    public ResponseEntity<List<Map<String, Object>>> getRealisasiByJenis(
            @RequestParam(defaultValue = "2025") Integer tahun,
            @RequestParam(required = false) String jenisPajakId) {
        try {
            List<Map<String, Object>> data = pendapatanService.getRealisasiByJenisPajak(tahun, jenisPajakId);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
