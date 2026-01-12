package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;

/**
 * PemdaController - Mengelola data pemda dari tabel system.pemda
 * 
 * Endpoint:
 * - GET /api/pemda/defaultview - Koordinat default peta
 */
@RestController
@RequestMapping("/api/pemda")
@CrossOrigin(origins = "*")
public class PemdaController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/pemda/defaultview
     * Mengembalikan koordinat tengah peta dan zoom level default
     * 
     * Response:
     * - kd_prop: Kode provinsi
     * - kd_dati2: Kode kabupaten/kota
     * - latitude: Koordinat latitude default
     * - longitude: Koordinat longitude default
     * - zoom: Zoom level default
     * - nm_dati2: Nama kabupaten/kota
     * - nm_prop: Nama provinsi
     * 
     * @return Default view configuration
     */
    @GetMapping("/defaultview")
    public ResponseEntity<Map<String, Object>> getDefaultView() {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "latitude, longitude, zoom, nm_dati2, nm_prop " +
                            "FROM system.pemda WHERE is_active = true LIMIT 1");
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            // Fallback nilai default jika tabel kosong atau error
            // Koordinat default: Kabupaten Lumajang, Jawa Timur
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("kd_prop", "35");
            fallback.put("kd_dati2", "09");
            fallback.put("latitude", -8.1351);
            fallback.put("longitude", 113.2234);
            fallback.put("zoom", 11);
            fallback.put("nm_dati2", "LUMAJANG");
            fallback.put("nm_prop", "JAWA TIMUR");
            return ResponseEntity.ok(fallback);
        }
    }

    /**
     * GET /api/pemda
     * Mengembalikan semua data pemda
     * 
     * @return Pemda data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPemda() {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT * FROM system.pemda WHERE is_active = true LIMIT 1");
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Data pemda tidak ditemukan",
                    "message", e.getMessage()));
        }
    }
}
