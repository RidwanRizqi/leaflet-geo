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
 * CRUD Endpoints:
 * - GET /api/kecamatan - Paginated list with filtering
 * - GET /api/kecamatan/list?option=true|false - Daftar kecamatan (dropdown)
 * - GET /api/kecamatan/{id} - Detail satu kecamatan
 * - POST /api/kecamatan - Create new kecamatan
 * - PUT /api/kecamatan/{id} - Update kecamatan
 * - DELETE /api/kecamatan/{id} - Soft delete kecamatan
 * - PATCH /api/kecamatan/{id}/recover - Recover deleted kecamatan
 */
@RestController
@RequestMapping("/api/kecamatan")
@CrossOrigin(origins = "*")
public class KecamatanController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/kecamatan?page=0&size=10&kd_kec=&nama=
     * Paginated list dengan filter
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getKecamatanPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String kd_kec,
            @RequestParam(required = false) String nama) {
        try {
            // Build WHERE clause with filters
            StringBuilder whereClause = new StringBuilder("WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (kd_kec != null && !kd_kec.trim().isEmpty()) {
                whereClause.append(" AND TRIM(kd_kec) = ?");
                params.add(kd_kec.trim());
            }
            if (nama != null && !nama.trim().isEmpty()) {
                whereClause.append(" AND LOWER(nama) LIKE LOWER(?)");
                params.add("%" + nama.trim() + "%");
            }

            // Count total
            String countSql = "SELECT COUNT(*) FROM system.kecamatan " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

            // Get paginated data
            String dataSql = "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                    "TRIM(kd_kec) as kd_kec, nama, color, " +
                    "encode(ST_AsBinary(geom), 'hex') as geom, is_active, created_at, updated_at " +
                    "FROM system.kecamatan " + whereClause +
                    " ORDER BY kd_kec ASC LIMIT ? OFFSET ?";

            params.add(size);
            params.add(page * size);

            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(dataSql, params.toArray());

            Map<String, Object> response = new HashMap<>();
            response.put("items", data);
            response.put("totalCount", totalCount);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/kecamatan/list?option=true|false
     * Untuk dropdown (tidak termasuk yang non-aktif)
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getKecamatanList(
            @RequestParam(defaultValue = "true") String option) {
        try {
            List<Map<String, Object>> data;

            if ("true".equals(option)) {
                data = postgresJdbcTemplate.queryForList(
                        "SELECT TRIM(kd_kec) as value, CONCAT(TRIM(kd_kec), ' - ', nama) as label " +
                                "FROM system.kecamatan WHERE is_active = true ORDER BY kd_kec ASC");
                List<Map<String, Object>> result = new ArrayList<>();
                Map<String, Object> placeholder = new HashMap<>();
                placeholder.put("value", "");
                placeholder.put("label", "-- Pilih Kecamatan --");
                result.add(placeholder);
                result.addAll(data);
                return ResponseEntity.ok(result);
            } else {
                data = postgresJdbcTemplate.queryForList(
                        "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                                "TRIM(kd_kec) as kd_kec, nama, color, " +
                                "encode(ST_AsBinary(geom), 'hex') as geom " +
                                "FROM system.kecamatan WHERE is_active = true ORDER BY kd_kec ASC");
                return ResponseEntity.ok(data);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    /**
     * GET /api/kecamatan/{id}
     * Detail satu kecamatan
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKecamatanById(@PathVariable String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, nama, color, " +
                            "encode(ST_AsBinary(geom), 'hex') as geom, is_active, created_at, updated_at " +
                            "FROM system.kecamatan WHERE id = ?::uuid",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Kecamatan tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/kecamatan/view?id=xxx (legacy format)
     */
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewKecamatan(@RequestParam String id) {
        return getKecamatanById(id);
    }

    /**
     * POST /api/kecamatan
     * Create new kecamatan
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createKecamatan(@RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String nama = (String) body.get("nama");
            String color = (String) body.get("color");
            String geom = (String) body.get("geom"); // WKB hex string

            if (kdKec == null || nama == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec dan nama wajib diisi"));
            }

            // Get pemda default values
            Map<String, Object> pemda = postgresJdbcTemplate.queryForMap(
                    "SELECT TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2 FROM system.pemda LIMIT 1");

            String sql;
            Object[] params;

            if (geom != null && !geom.trim().isEmpty()) {
                sql = "INSERT INTO system.kecamatan (id, kd_prop, kd_dati2, kd_kec, nama, color, geom, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ST_GeomFromWKB(decode(?, 'hex'), 4326), true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, nama, color, geom };
            } else {
                sql = "INSERT INTO system.kecamatan (id, kd_prop, kd_dati2, kd_kec, nama, color, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, nama, color };
            }

            Map<String, Object> result = postgresJdbcTemplate.queryForMap(sql, params);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kecamatan berhasil ditambahkan",
                    "id", result.get("id")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/kecamatan/{id}
     * Update kecamatan
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateKecamatan(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String nama = (String) body.get("nama");
            String color = (String) body.get("color");
            String geom = (String) body.get("geom");

            if (kdKec == null || nama == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec dan nama wajib diisi"));
            }

            String sql;
            int updated;

            if (geom != null && !geom.trim().isEmpty()) {
                sql = "UPDATE system.kecamatan SET kd_kec = ?, nama = ?, color = ?, " +
                        "geom = ST_GeomFromWKB(decode(?, 'hex'), 4326), updated_at = NOW() WHERE id = ?::uuid";
                updated = postgresJdbcTemplate.update(sql, kdKec, nama, color, geom, id);
            } else {
                sql = "UPDATE system.kecamatan SET kd_kec = ?, nama = ?, color = ?, updated_at = NOW() WHERE id = ?::uuid";
                updated = postgresJdbcTemplate.update(sql, kdKec, nama, color, id);
            }

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kecamatan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kecamatan berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/kecamatan/{id}
     * Soft delete kecamatan (set is_active = false)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteKecamatan(@PathVariable String id) {
        try {
            String sql = "UPDATE system.kecamatan SET is_active = false, updated_at = NOW() WHERE id = ?::uuid";
            int updated = postgresJdbcTemplate.update(sql, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kecamatan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kecamatan berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/kecamatan/{id}/recover
     * Recover deleted kecamatan (set is_active = true)
     */
    @PatchMapping("/{id}/recover")
    public ResponseEntity<Map<String, Object>> recoverKecamatan(@PathVariable String id) {
        try {
            String sql = "UPDATE system.kecamatan SET is_active = true, updated_at = NOW() WHERE id = ?::uuid";
            int updated = postgresJdbcTemplate.update(sql, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kecamatan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kecamatan berhasil dipulihkan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
