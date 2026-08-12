package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/opsen")
@CrossOrigin(origins = "*")
public class OpsenController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    // 1. GET Paginated Data
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOpsenPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer tahun,
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) String jenis_opsen) {
        try {
            StringBuilder whereClause = new StringBuilder("WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (tahun != null) {
                whereClause.append(" AND tahun = ?");
                params.add(tahun);
            }
            if (bulan != null) {
                whereClause.append(" AND bulan = ?");
                params.add(bulan);
            }
            if (jenis_opsen != null && !jenis_opsen.trim().isEmpty()) {
                whereClause.append(" AND LOWER(jenis_opsen) LIKE LOWER(?)");
                params.add("%" + jenis_opsen.trim() + "%");
            }

            String countSql = "SELECT COUNT(*) FROM system.opsen_realisasi " + whereClause;
            Long totalCount = postgresJdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

            String dataSql = "SELECT id, tahun, bulan, jenis_opsen, nilai_realisasi, keterangan, created_at, updated_at "
                    +
                    "FROM system.opsen_realisasi " + whereClause +
                    " ORDER BY tahun DESC, bulan DESC, jenis_opsen ASC LIMIT ? OFFSET ?";

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

    // 2. GET Jenis Opsen (Dinamis / Hardcode untuk dropdown)
    @GetMapping("/jenis-opsen")
    public ResponseEntity<List<Map<String, String>>> getJenisOpsen() {
        // Jika ke depannya ada tambahan jenis opsen, bisa ditambahkan di sini
        List<Map<String, String>> options = List.of(
                Map.of("value", "Opsen PKB", "label", "Opsen PKB"),
                Map.of("value", "Opsen BBNKB", "label", "Opsen BBNKB"));
        return ResponseEntity.ok(options);
    }

    // 3. POST Create Data Baru
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOpsen(@RequestBody Map<String, Object> body) {
        try {
            Integer tahun = (Integer) body.get("tahun");
            Integer bulan = (Integer) body.get("bulan");
            String jenisOpsen = (String) body.get("jenis_opsen");
            Object nilaiObj = body.get("nilai_realisasi");
            String keterangan = (String) body.get("keterangan");

            if (tahun == null || bulan == null || jenisOpsen == null || nilaiObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Tahun, Bulan, Jenis Opsen, dan Nilai Realisasi wajib diisi"));
            }

            BigDecimal nilaiRealisasi = new BigDecimal(nilaiObj.toString());

            // Cek apakah data sudah ada (mencegah duplikat di bulan & tahun yang sama)
            Integer count = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM system.opsen_realisasi WHERE tahun = ? AND bulan = ? AND jenis_opsen = ?",
                    Integer.class, tahun, bulan, jenisOpsen);

            if (count != null && count > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Data realisasi untuk " + jenisOpsen + " pada bulan " + bulan + " tahun " + tahun
                                + " sudah ada. Silakan gunakan fitur edit."));
            }

            String sql = "INSERT INTO system.opsen_realisasi (tahun, bulan, jenis_opsen, nilai_realisasi, keterangan, created_at, updated_at) "
                    +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
            postgresJdbcTemplate.update(sql, tahun, bulan, jenisOpsen, nilaiRealisasi, keterangan);

            return ResponseEntity.ok(Map.of("success", true, "message", "Data realisasi Opsen berhasil ditambahkan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // 4. PUT Update Data
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOpsen(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Object nilaiObj = body.get("nilai_realisasi");
            String keterangan = (String) body.get("keterangan");

            if (nilaiObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nilai Realisasi wajib diisi"));
            }

            BigDecimal nilaiRealisasi = new BigDecimal(nilaiObj.toString());

            String sql = "UPDATE system.opsen_realisasi SET nilai_realisasi = ?, keterangan = ?, updated_at = NOW() WHERE id = ?";
            int updated = postgresJdbcTemplate.update(sql, nilaiRealisasi, keterangan, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Data Opsen tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Data realisasi Opsen berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // 5. DELETE Hapus Data
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOpsen(@PathVariable Long id) {
        try {
            String sql = "DELETE FROM system.opsen_realisasi WHERE id = ?";
            int deleted = postgresJdbcTemplate.update(sql, id);

            if (deleted == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Data Opsen tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Data realisasi Opsen berhasil dihapus"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
