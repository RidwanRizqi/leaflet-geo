package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * BlokController - Mengelola data blok dari tabel sig.blok
 * 
 * Endpoint:
 * - GET /api/blok/list?kd_kec=&kd_kel= - Daftar blok berdasarkan kecamatan dan
 * kelurahan
 * - GET /api/blok/view?id= - Detail satu blok dengan geometry
 * - GET /api/blok/{id} - Alternatif: Detail blok by ID
 */
@RestController
@RequestMapping("/api/blok")
@CrossOrigin(origins = "*")
public class BlokController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/blok/list?kd_kec=130&kd_kel=017
     * Menampilkan daftar blok berdasarkan kecamatan dan kelurahan
     * 
     * @param kd_kec Kode kecamatan (3 digit)
     * @param kd_kel Kode kelurahan (3 digit)
     * @return List of blok data with geometry
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getBlokList(
            @RequestParam String kd_kec,
            @RequestParam String kd_kel) {
        try {
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, geom " +
                            "FROM sig.blok WHERE kd_kec = ? AND kd_kel = ? AND is_active = true " +
                            "ORDER BY kd_blok ASC",
                    kd_kec, kd_kel);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    /**
     * GET /api/blok/view?id=xxx
     * Menampilkan detail satu blok (format legacy)
     * 
     * @param id UUID blok
     * @return Single blok data with geometry
     */
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewBlok(@RequestParam String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, geom " +
                            "FROM sig.blok WHERE id = ?::uuid",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Blok tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/blok/{id}
     * Alternatif endpoint untuk detail blok
     * 
     * @param id UUID blok
     * @return Single blok data with geometry
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBlokById(@PathVariable String id) {
        return viewBlok(id);
    }
}
