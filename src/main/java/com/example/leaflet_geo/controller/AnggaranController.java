package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

/**
 * AnggaranController - Mengelola data anggaran pajak per tahun
 * 
 * CRUD Endpoints:
 * - GET /api/anggaran - Paginated list with filtering
 * - GET /api/anggaran/{tahun}/{jenis} - Get single record
 * - POST /api/anggaran - Create new anggaran
 * - PUT /api/anggaran/{tahun}/{jenis} - Update anggaran
 * - DELETE /api/anggaran/{tahun}/{jenis} - Delete anggaran
 */
@RestController
@RequestMapping("/api/anggaran")
@CrossOrigin(origins = "*")
public class AnggaranController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    /**
     * GET /api/anggaran?page=0&size=10&tahun_anggaran=&jenis_pajak=
     * Paginated list dengan filter
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnggaranPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer tahun_anggaran,
            @RequestParam(required = false) String jenis_pajak) {
        try {
            StringBuilder whereClause = new StringBuilder("WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (tahun_anggaran != null) {
                whereClause.append(" AND tahun_anggaran = ?");
                params.add(tahun_anggaran);
            }
            if (jenis_pajak != null && !jenis_pajak.trim().isEmpty()) {
                whereClause.append(" AND LOWER(jenis_pajak) LIKE LOWER(?)");
                params.add("%" + jenis_pajak.trim() + "%");
            }

            String countSql = "SELECT COUNT(*) FROM system.anggaran " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

            String dataSql = "SELECT tahun_anggaran, jenis_pajak, nilai_anggaran, created_at, updated_at " +
                    "FROM system.anggaran " + whereClause +
                    " ORDER BY tahun_anggaran DESC, jenis_pajak ASC LIMIT ? OFFSET ?";

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
     * GET /api/anggaran/list
     * Get all years for dropdown
     */
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getYears() {
        try {
            List<Integer> years = postgresJdbcTemplate.queryForList(
                    "SELECT DISTINCT tahun_anggaran FROM system.anggaran ORDER BY tahun_anggaran DESC",
                    Integer.class);
            return ResponseEntity.ok(years);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * GET /api/anggaran/jenis-pajak
     * Get jenis pajak options
     */
    @GetMapping("/jenis-pajak")
    public ResponseEntity<List<Map<String, String>>> getJenisPajak() {
        List<Map<String, String>> options = List.of(
                Map.of("value", "REKLAME", "label", "Pajak Reklame"),
                Map.of("value", "AIR_TANAH", "label", "Pajak Air Tanah"),
                Map.of("value", "MBLB", "label", "Pajak MBLB"),
                Map.of("value", "PBB_P2", "label", "PBB-P2"),
                Map.of("value", "BPHTB", "label", "BPHTB"),
                Map.of("value", "PBJT_MAKANAN_MINUMAN", "label", "PBJT Makanan dan/atau Minuman"),
                Map.of("value", "PBJT_LISTRIK", "label", "PBJT Tenaga Listrik"),
                Map.of("value", "PBJT_HOTEL", "label", "PBJT Jasa Perhotelan"),
                Map.of("value", "PBJT_PARKIR", "label", "PBJT Jasa Parkir"),
                Map.of("value", "PBJT_HIBURAN", "label", "PBJT Jasa Kesenian dan Hiburan"),
                Map.of("value", "OPSEN_PKB", "label", "Opsen PKB"),
                Map.of("value", "OPSEN_BBNKB", "label", "Opsen BBNKB"));
        return ResponseEntity.ok(options);
    }

    /**
     * GET /api/anggaran/{tahun}/{jenis}
     * Get single anggaran by composite key
     */
    @GetMapping("/{tahun}/{jenis}")
    public ResponseEntity<Map<String, Object>> getAnggaranById(
            @PathVariable Integer tahun,
            @PathVariable String jenis) {
        try {
            Map<String, Object> data = postgresJdbcTemplate.queryForMap(
                    "SELECT tahun_anggaran, jenis_pajak, nilai_anggaran, created_at, updated_at " +
                            "FROM system.anggaran WHERE tahun_anggaran = ? AND jenis_pajak = ?",
                    tahun, jenis);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Anggaran tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/anggaran
     * Create new anggaran
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAnggaran(@RequestBody Map<String, Object> body) {
        try {
            Integer tahunAnggaran = (Integer) body.get("tahun_anggaran");
            String jenisPajak = (String) body.get("jenis_pajak");
            Object nilaiObj = body.get("nilai_anggaran");

            if (tahunAnggaran == null || jenisPajak == null || nilaiObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "tahun_anggaran, jenis_pajak, dan nilai_anggaran wajib diisi"));
            }

            BigDecimal nilaiAnggaran;
            if (nilaiObj instanceof Number) {
                nilaiAnggaran = new BigDecimal(nilaiObj.toString());
            } else {
                nilaiAnggaran = new BigDecimal((String) nilaiObj);
            }

            // Check if already exists
            Integer count = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM system.anggaran WHERE tahun_anggaran = ? AND jenis_pajak = ?",
                    Integer.class, tahunAnggaran, jenisPajak);

            if (count != null && count > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error",
                        "Data anggaran untuk tahun " + tahunAnggaran + " dan jenis " + jenisPajak + " sudah ada"));
            }

            String sql = "INSERT INTO system.anggaran (tahun_anggaran, jenis_pajak, nilai_anggaran, created_at, updated_at) "
                    +
                    "VALUES (?, ?, ?, NOW(), NOW())";
            postgresJdbcTemplate.update(sql, tahunAnggaran, jenisPajak, nilaiAnggaran);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Anggaran berhasil ditambahkan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/anggaran/{tahun}/{jenis}
     * Update anggaran
     */
    @PutMapping("/{tahun}/{jenis}")
    public ResponseEntity<Map<String, Object>> updateAnggaran(
            @PathVariable Integer tahun,
            @PathVariable String jenis,
            @RequestBody Map<String, Object> body) {
        try {
            Object nilaiObj = body.get("nilai_anggaran");

            if (nilaiObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "nilai_anggaran wajib diisi"));
            }

            BigDecimal nilaiAnggaran;
            if (nilaiObj instanceof Number) {
                nilaiAnggaran = new BigDecimal(nilaiObj.toString());
            } else {
                nilaiAnggaran = new BigDecimal((String) nilaiObj);
            }

            String sql = "UPDATE system.anggaran SET nilai_anggaran = ?, updated_at = NOW() " +
                    "WHERE tahun_anggaran = ? AND jenis_pajak = ?";
            int updated = postgresJdbcTemplate.update(sql, nilaiAnggaran, tahun, jenis);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Anggaran tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Anggaran berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/anggaran/{tahun}/{jenis}
     * Delete anggaran (hard delete)
     */
    @DeleteMapping("/{tahun}/{jenis}")
    public ResponseEntity<Map<String, Object>> deleteAnggaran(
            @PathVariable Integer tahun,
            @PathVariable String jenis) {
        try {
            String sql = "DELETE FROM system.anggaran WHERE tahun_anggaran = ? AND jenis_pajak = ?";
            int deleted = postgresJdbcTemplate.update(sql, tahun, jenis);

            if (deleted == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Anggaran tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Anggaran berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
