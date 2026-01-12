package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * KelurahanController - Mengelola data kelurahan dari tabel system.kelurahan
 * 
 * Endpoint:
 * - GET /api/kelurahan/list?kd_kec=&option= - Daftar kelurahan berdasarkan
 * kecamatan
 * - GET /api/kelurahan/view?id= - Detail satu kelurahan dengan geometry
 * - GET /api/kelurahan/{id} - Alternatif: Detail kelurahan by ID
 */
@RestController
@RequestMapping("/api/kelurahan")
@CrossOrigin(origins = "*")
public class KelurahanController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/kelurahan/list?kd_kec=130&option=true|false
     * 
     * option=true: Untuk dropdown (value, label, kd_kec) - default
     * option=false: Data lengkap dengan geometry
     * 
     * @param kd_kec Kode kecamatan (3 digit)
     * @param option Format output ('true' atau 'false')
     * @return List of kelurahan data
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getKelurahanList(
            @RequestParam String kd_kec,
            @RequestParam(defaultValue = "true") String option) {
        try {
            List<Map<String, Object>> data;

            if ("true".equals(option)) {
                // Format dropdown
                data = postgresJdbcTemplate.queryForList(
                        "SELECT TRIM(kd_kel) as value, CONCAT(TRIM(kd_kel), ' - ', nama) as label, " +
                                "TRIM(kd_kec) as kd_kec, id as id_kelurahan " +
                                "FROM system.kelurahan WHERE kd_kec = ? AND is_active = true ORDER BY kd_kel ASC",
                        kd_kec);
                // Tambah opsi kosong di awal
                List<Map<String, Object>> result = new ArrayList<>();
                Map<String, Object> placeholder = new HashMap<>();
                placeholder.put("value", "");
                placeholder.put("label", "-- Pilih Kelurahan --");
                result.add(placeholder);
                result.addAll(data);
                return ResponseEntity.ok(result);
            } else {
                // Data lengkap dengan geometry
                data = postgresJdbcTemplate.queryForList(
                        "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                                "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, nama, geom " +
                                "FROM system.kelurahan WHERE kd_kec = ? AND is_active = true ORDER BY kd_kel ASC",
                        kd_kec);
                return ResponseEntity.ok(data);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    /**
     * GET /api/kelurahan/view?id=xxx
     * Menampilkan detail satu kelurahan (format legacy)
     * 
     * @param id UUID kelurahan
     * @return Single kelurahan data with geometry
     */
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewKelurahan(@RequestParam String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, nama, geom " +
                            "FROM system.kelurahan WHERE id = ?",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Kelurahan tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/kelurahan/{id}
     * Alternatif endpoint untuk detail kelurahan
     * 
     * @param id UUID kelurahan
     * @return Single kelurahan data with geometry
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKelurahanById(@PathVariable String id) {
        return viewKelurahan(id);
    }
}
