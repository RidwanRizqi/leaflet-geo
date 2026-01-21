package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * BlokController - Mengelola data blok dari tabel sig.blok
 * 
 * CRUD Endpoints:
 * - GET /api/blok - Paginated list with filtering
 * - GET /api/blok/list?kd_kec=&kd_kel= - Daftar blok (dropdown)
 * - GET /api/blok/{id} - Detail satu blok
 * - POST /api/blok - Create new blok
 * - PUT /api/blok/{id} - Update blok
 * - DELETE /api/blok/{id} - Soft delete blok
 * - PATCH /api/blok/{id}/recover - Recover deleted blok
 */
@RestController
@RequestMapping("/api/blok")
@CrossOrigin(origins = "*")
public class BlokController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/blok?page=0&size=10&kd_kec=&kd_kel=&kd_blok=
     * Paginated list dengan filter
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getBlokPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String kd_kec,
            @RequestParam(required = false) String kd_kel,
            @RequestParam(required = false) String kd_blok) {
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
            if (kd_blok != null && !kd_blok.trim().isEmpty()) {
                whereClause.append(" AND TRIM(kd_blok) = ?");
                params.add(kd_blok.trim());
            }

            String countSql = "SELECT COUNT(*) FROM sig.blok " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

            String dataSql = "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                    "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, TRIM(kd_blok) as kd_blok, " +
                    "encode(ST_AsBinary(geom), 'hex') as geom, is_active, created_at, updated_at " +
                    "FROM sig.blok " + whereClause +
                    " ORDER BY kd_kec ASC, kd_kel ASC, kd_blok ASC LIMIT ? OFFSET ?";

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
     * GET /api/blok/list?kd_kec=130&kd_kel=017
     */
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> getBlokList(
            @RequestParam String kd_kec,
            @RequestParam String kd_kel) {
        try {
            List<Map<String, Object>> data = postgresJdbcTemplate.queryForList(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, TRIM(kd_blok) as kd_blok, " +
                            "encode(ST_AsBinary(geom), 'hex') as geom " +
                            "FROM sig.blok WHERE kd_kec = ? AND kd_kel = ? AND is_active = true " +
                            "ORDER BY kd_blok ASC",
                    kd_kec, kd_kel);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBlokById(@PathVariable String id) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT id, TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2, " +
                            "TRIM(kd_kec) as kd_kec, TRIM(kd_kel) as kd_kel, TRIM(kd_blok) as kd_blok, " +
                            "encode(ST_AsBinary(geom), 'hex') as geom, is_active " +
                            "FROM sig.blok WHERE id = ?::uuid",
                    id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Blok tidak ditemukan"));
        }
    }

    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> viewBlok(@RequestParam String id) {
        return getBlokById(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBlok(@RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String kdKel = (String) body.get("kd_kel");
            String kdBlok = (String) body.get("kd_blok");
            String geom = (String) body.get("geom");

            if (kdKec == null || kdKel == null || kdBlok == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec, kd_kel, dan kd_blok wajib diisi"));
            }

            Map<String, Object> pemda = postgresJdbcTemplate.queryForMap(
                    "SELECT TRIM(kd_prop) as kd_prop, TRIM(kd_dati2) as kd_dati2 FROM system.pemda LIMIT 1");

            String sql;
            Object[] params;

            if (geom != null && !geom.trim().isEmpty()) {
                sql = "INSERT INTO sig.blok (id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, geom, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ST_GeomFromWKB(decode(?, 'hex'), 4326), true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, kdKel, kdBlok, geom };
            } else {
                sql = "INSERT INTO sig.blok (id, kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, is_active, created_at, updated_at) "
                        +
                        "VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, true, NOW(), NOW()) RETURNING id";
                params = new Object[] { pemda.get("kd_prop"), pemda.get("kd_dati2"), kdKec, kdKel, kdBlok };
            }

            Map<String, Object> result = postgresJdbcTemplate.queryForMap(sql, params);
            return ResponseEntity
                    .ok(Map.of("success", true, "message", "Blok berhasil ditambahkan", "id", result.get("id")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBlok(@PathVariable String id,
            @RequestBody Map<String, Object> body) {
        try {
            String kdKec = (String) body.get("kd_kec");
            String kdKel = (String) body.get("kd_kel");
            String kdBlok = (String) body.get("kd_blok");
            String geom = (String) body.get("geom");

            if (kdKec == null || kdKel == null || kdBlok == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "kd_kec, kd_kel, dan kd_blok wajib diisi"));
            }

            int updated;
            if (geom != null && !geom.trim().isEmpty()) {
                updated = postgresJdbcTemplate.update(
                        "UPDATE sig.blok SET kd_kec = ?, kd_kel = ?, kd_blok = ?, geom = ST_GeomFromWKB(decode(?, 'hex'), 4326), updated_at = NOW() WHERE id = ?::uuid",
                        kdKec, kdKel, kdBlok, geom, id);
            } else {
                updated = postgresJdbcTemplate.update(
                        "UPDATE sig.blok SET kd_kec = ?, kd_kel = ?, kd_blok = ?, updated_at = NOW() WHERE id = ?::uuid",
                        kdKec, kdKel, kdBlok, id);
            }

            if (updated == 0)
                return ResponseEntity.status(404).body(Map.of("error", "Blok tidak ditemukan"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Blok berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBlok(@PathVariable String id) {
        try {
            int updated = postgresJdbcTemplate.update(
                    "UPDATE sig.blok SET is_active = false, updated_at = NOW() WHERE id = ?::uuid", id);
            if (updated == 0)
                return ResponseEntity.status(404).body(Map.of("error", "Blok tidak ditemukan"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Blok berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/recover")
    public ResponseEntity<Map<String, Object>> recoverBlok(@PathVariable String id) {
        try {
            int updated = postgresJdbcTemplate.update(
                    "UPDATE sig.blok SET is_active = true, updated_at = NOW() WHERE id = ?::uuid", id);
            if (updated == 0)
                return ResponseEntity.status(404).body(Map.of("error", "Blok tidak ditemukan"));
            return ResponseEntity.ok(Map.of("success", true, "message", "Blok berhasil dipulihkan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
