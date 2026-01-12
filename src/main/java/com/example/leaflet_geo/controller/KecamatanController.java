package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * KecamatanController - Mengelola data kecamatan dari tabel system.kecamatan
 * 
 * Endpoint:
 * - GET /api/kecamatan/list?option=true|false - Daftar kecamatan
 * - GET /api/kecamatan/view?id= - Detail satu kecamatan dengan geometry
 * - GET /api/kecamatan/{id} - Alternatif: Detail kecamatan by ID
 */
@RestController
@RequestMapping("/api/kecamatan")
@CrossOrigin(origins = "*")
public class KecamatanController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/kecamatan/list?option=true|false
     * 
     * option=true: Untuk dropdown (value, label) - default
     * option=false: Data lengkap dengan geometry
     * 
     * @param option Format output ('true' atau 'false')
     * @return List of kecamatan data
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getKecamatanList(
            @RequestParam(defaultValue = "true") String option) {
        try {
            List<Map<String, Object>> data;

            if ("true".equals(option)) {
                // Format dropdown - untuk select/combobox
                data = postgresJdbcTemplate.queryForList(
                        "SELECT TRIM(kd_kec) as value, CONCAT(TRIM(kd_kec), ' - ', nama) as label " +
                                "FROM system.kecamatan WHERE is_active = true ORDER BY kd_kec ASC");
                // Tambah opsi kosong di awal untuk placeholder
                List<Map<String, Object>> result = new ArrayList<>();
                Map<String, Object> placeholder = new HashMap<>();
                placeholder.put("value", "");
                placeholder.put("label", "-- Pilih Kecamatan --");
                result.add(placeholder);
                result.addAll(data);
                return ResponseEntity.ok(result);
            } else {
                // Data lengkap dengan geometry - untuk render peta
                data = postgresJdbcTemplate.queryForList(
                        "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                                "TRIM(kd_kec) as kd_kec, nama, color, geom " +
                                "FROM system.kecamatan WHERE is_active = true ORDER BY kd_kec ASC");
                return ResponseEntity.ok(data);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    /**
     * GET /api/kecamatan/view?id=xxx
     * Menampilkan detail satu kecamatan (format legacy)
     * 
     * @param id UUID kecamatan
     * @return Single kecamatan data with geometry
     */
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewKecamatan(@RequestParam String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, nama, color, geom " +
                            "FROM system.kecamatan WHERE id = ?",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Kecamatan tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/kecamatan/{id}
     * Alternatif endpoint untuk detail kecamatan
     * 
     * @param id UUID kecamatan
     * @return Single kecamatan data with geometry
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKecamatanById(@PathVariable String id) {
        return viewKecamatan(id);
    }
}
