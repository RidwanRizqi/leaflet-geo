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
 * CRUD Endpoints:
 * - GET /api/kelurahan - Paginated list with filtering
 * - GET /api/kelurahan/list?kd_kec=&option= - Daftar kelurahan (dropdown)
 * - GET /api/kelurahan/{id} - Detail satu kelurahan
 * - POST /api/kelurahan - Create new kelurahan
 * - PUT /api/kelurahan/{id} - Update kelurahan
 * - DELETE /api/kelurahan/{id} - Soft delete kelurahan
 * - PATCH /api/kelurahan/{id}/recover - Recover deleted kelurahan
 */
@RestController
@RequestMapping("/api/kelurahan")
@CrossOrigin(origins = "*")
public class KelurahanController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/kelurahan?page=0&size=10&kd_kec=&kd_kel=&nama=
     * Paginated list dengan filter
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getKelurahanPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String kd_kec,
            @RequestParam(required = false) String kd_kel,
            @RequestParam(required = false) String nama) {
        try {
            StringBuilder whereClause = new StringBuilder("WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (kd_kec != null && !kd_kec.trim().isEmpty()) {
                whereClause.append(" AND TRIM(kd_kec) = ?");
                params.add(kd_kec.trim());
            }
            if (kd_kel != null && !kd_kel.trim().isEmpty()) {
                whereClause.append(" AND TRIM(kd_kel) = ?");
                params.add(kd_kel.trim());
            }
            if (nama != null && !nama.trim().isEmpty()) {
                whereClause.append(" AND LOWER(nama) LIKE LOWER(?)");
                params.add("%" + nama.trim() + "%");
            }

            String countSql = "SELECT COUNT(*) FROM system.kelurahan " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

            String dataSql = "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                    "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, nama, " +
                    "encode(ST_AsBinary(geom), 'hex') as geom, is_active, created_at, updated_at " +
                    "FROM system.kelurahan " + whereClause +
                    " ORDER BY kd_kec ASC, kd_kel ASC LIMIT ? OFFSET ?";

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
     * GET /api/kelurahan/list?kd_kec=130&option=true|false
     * Untuk dropdown (tidak termasuk yang non-aktif)
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getKelurahanList(
            @RequestParam String kd_kec,
            @RequestParam(defaultValue = "true") String option) {
        try {
            List<Map<String, Object>> data;

            if ("true".equals(option)) {
                data = postgresJdbcTemplate.queryForList(
                        "SELECT TRIM(kd_kel) as value, CONCAT(TRIM(kd_kel), ' - ', nama) as label, " +
                                "TRIM(kd_kec) as kd_kec, id as id_kelurahan " +
                                "FROM system.kelurahan WHERE kd_kec = ? AND is_active = true ORDER BY kd_kel ASC",
                        kd_kec);
                List<Map<String, Object>> result = new ArrayList<>();
                Map<String, Object> placeholder = new HashMap<>();
                placeholder.put("value", "");
                placeholder.put("label", "-- Pilih Kelurahan --");
                result.add(placeholder);
                result.addAll(data);
                return ResponseEntity.ok(result);
            } else {
                data = postgresJdbcTemplate.queryForList(
                        "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                                "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, nama, " +
                                "encode(ST_AsBinary(geom), 'hex') as geom " +
                                "FROM system.kelurahan WHERE kd_kec = ? AND is_active = true ORDER BY kd_kel ASC",
                        kd_kec);
                return ResponseEntity.ok(data);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    /**
     * GET /api/kelurahan/{id}
     * Detail satu kelurahan
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKelurahanById(@PathVariable String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, nama, " +
                            "encode(ST_AsBinary(geom), 'hex') as geom, is_active, created_at, updated_at " +
                            "FROM system.kelurahan WHERE id = ?::uuid",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Kelurahan tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/kelurahan/view?id=xxx (legacy format)
     */
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewKelurahan(@RequestParam String id) {
        return getKelurahanById(id);
    }

    /**
     * POST /api/kelurahan
     * Create new kelurahan
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createKelurahan(@RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String kdKel = (String) body.get("kd_kel");
            String nama = (String) body.get("nama");
            String geom = (String) body.get("geom");

            if (kdKec == null || kdKel == null || nama == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec, kd_kel, dan nama wajib diisi"));
            }

            Map<String, Object> pemda = postgresJdbcTemplate.queryForMap(
                    "SELECT TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2 FROM system.pemda LIMIT 1");

            String sql;
            Object[] params;

            if (geom != null && !geom.trim().isEmpty()) {
                sql = "INSERT INTO system.kelurahan (id, kd_prop, kd_dati2, kd_kec, kd_kel, nama, geom, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ST_GeomFromWKB(decode(?, 'hex'), 4326), true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, kdKel, nama, geom };
            } else {
                sql = "INSERT INTO system.kelurahan (id, kd_prop, kd_dati2, kd_kec, kd_kel, nama, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, kdKel, nama };
            }

            Map<String, Object> result = postgresJdbcTemplate.queryForMap(sql, params);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kelurahan berhasil ditambahkan",
                    "id", result.get("id")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/kelurahan/{id}
     * Update kelurahan
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateKelurahan(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String kdKel = (String) body.get("kd_kel");
            String nama = (String) body.get("nama");
            String geom = (String) body.get("geom");

            if (kdKec == null || kdKel == null || nama == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec, kd_kel, dan nama wajib diisi"));
            }

            String sql;
            int updated;

            if (geom != null && !geom.trim().isEmpty()) {
                sql = "UPDATE system.kelurahan SET kd_kec = ?, kd_kel = ?, nama = ?, " +
                        "geom = ST_GeomFromWKB(decode(?, 'hex'), 4326), updated_at = NOW() WHERE id = ?::uuid";
                updated = postgresJdbcTemplate.update(sql, kdKec, kdKel, nama, geom, id);
            } else {
                sql = "UPDATE system.kelurahan SET kd_kec = ?, kd_kel = ?, nama = ?, updated_at = NOW() WHERE id = ?::uuid";
                updated = postgresJdbcTemplate.update(sql, kdKec, kdKel, nama, id);
            }

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kelurahan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kelurahan berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/kelurahan/{id}
     * Soft delete kelurahan (set is_active = false)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteKelurahan(@PathVariable String id) {
        try {
            String sql = "UPDATE system.kelurahan SET is_active = false, updated_at = NOW() WHERE id = ?::uuid";
            int updated = postgresJdbcTemplate.update(sql, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kelurahan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kelurahan berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/kelurahan/{id}/recover
     * Recover deleted kelurahan (set is_active = true)
     */
    @PatchMapping("/{id}/recover")
    public ResponseEntity<Map<String, Object>> recoverKelurahan(@PathVariable String id) {
        try {
            String sql = "UPDATE system.kelurahan SET is_active = true, updated_at = NOW() WHERE id = ?::uuid";
            int updated = postgresJdbcTemplate.update(sql, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kelurahan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kelurahan berhasil dipulihkan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
