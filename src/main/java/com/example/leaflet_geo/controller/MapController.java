package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

/**
 * MapController - Operasi peta seperti cari NOP, update geometry
 * 
 * Endpoint:
 * - GET /api/map/carinop?nop= - Cari bidang berdasarkan NOP
 * - GET /api/map/infonop - Detail bidang dari Oracle SISMIOP
 * - POST /api/map/newnop - Buat bidang baru
 * - POST /api/map/updatenop - Update geometry bidang
 * - POST /api/map/updatekecamatan - Update geometry kecamatan
 * - POST /api/map/updatekelurahan - Update geometry kelurahan
 * - POST /api/map/updateblok - Update geometry blok
 */
@RestController
@RequestMapping("/api/map")
@CrossOrigin(origins = "*")
public class MapController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    /**
     * GET /api/map/carinop?nop=35.09.130.017.005.0001.0
     * Mencari blok ID berdasarkan NOP
     * 
     * @param nop NOP dalam format XX.XX.XXX.XXX.XXX.XXXX.X
     * @return Blok ID, nama kecamatan, nama kelurahan
     */
    @GetMapping("/carinop")
    public ResponseEntity<Map<String, Object>> searchByNop(@RequestParam String nop) {
        try {
            // Parse NOP: 35.09.130.017.005.0001.0
            String[] parts = nop.split("\\.");
            if (parts.length != 7) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Format NOP tidak valid",
                        "expected", "XX.XX.XXX.XXX.XXX.XXXX.X"));
            }

            String kdProp = parts[0];
            String kdDati2 = parts[1];
            String kdKec = parts[2];
            String kdKel = parts[3];
            String kdBlok = parts[4];

            // Cari blok dan nama-nama
            Map<String, Object> result = postgresJdbcTemplate.queryForMap(
                    "SELECT b.id as id_blok, " +
                            "k.id as id_kecamatan, k.nama as nama_kecamatan, " +
                            "l.id as id_kelurahan, l.nama as nama_kelurahan, " +
                            "TRIM(b.kd_kec) as kd_kec, TRIM(b.kd_kel) as kd_kel " +
                            "FROM sig.blok b " +
                            "LEFT JOIN system.kecamatan k ON k.kd_kec = b.kd_kec " +
                            "LEFT JOIN system.kelurahan l ON l.kd_kec = b.kd_kec AND l.kd_kel = b.kd_kel " +
                            "WHERE b.kd_prop = ? AND b.kd_dati2 = ? AND b.kd_kec = ? AND b.kd_kel = ? AND b.kd_blok = ?",
                    kdProp, kdDati2, kdKec, kdKel, kdBlok);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "NOP tidak ditemukan",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/map/infonop
     * Mendapatkan detail bidang dari Oracle SISMIOP
     * 
     * @param id        UUID bidang dari PostgreSQL
     * @param kd_prop   Kode provinsi
     * @param kd_dati2  Kode kabupaten
     * @param kd_kec    Kode kecamatan
     * @param kd_kel    Kode kelurahan
     * @param kd_blok   Kode blok
     * @param no_urut   Nomor urut
     * @param kd_jns_op Kode jenis objek pajak
     * @return Detail bidang dari SISMIOP
     */
    @GetMapping("/infonop")
    public ResponseEntity<Map<String, Object>> getInfoNop(
            @RequestParam String id,
            @RequestParam String kd_prop,
            @RequestParam String kd_dati2,
            @RequestParam String kd_kec,
            @RequestParam String kd_kel,
            @RequestParam String kd_blok,
            @RequestParam String no_urut,
            @RequestParam String kd_jns_op) {
        try {
            // Query Oracle SISMIOP untuk detail
            Map<String, Object> oracleData = null;
            try {
                oracleData = oracleJdbcTemplate.queryForMap(
                        "SELECT " +
                                "DAT_OBJEK_PAJAK.JALAN_OP, DAT_OBJEK_PAJAK.BLOK_KAV_NO_OP, " +
                                "DAT_OBJEK_PAJAK.RT_OP, DAT_OBJEK_PAJAK.RW_OP, " +
                                "DAT_OBJEK_PAJAK.TOTAL_LUAS_BUMI, DAT_OBJEK_PAJAK.TOTAL_LUAS_BNG, " +
                                "DAT_OBJEK_PAJAK.NJOP_BUMI, DAT_OBJEK_PAJAK.NJOP_BNG, " +
                                "DAT_SUBJEK_PAJAK.NM_WP, DAT_SUBJEK_PAJAK.NPWP, " +
                                "DAT_SUBJEK_PAJAK.JALAN_WP, DAT_SUBJEK_PAJAK.KELURAHAN_WP, DAT_SUBJEK_PAJAK.KOTA_WP, " +
                                "REF_KECAMATAN.NM_KECAMATAN, REF_KELURAHAN.NM_KELURAHAN, " +
                                "DAT_OP_BUMI.KD_ZNT " +
                                "FROM DAT_OBJEK_PAJAK " +
                                "LEFT JOIN DAT_SUBJEK_PAJAK ON DAT_OBJEK_PAJAK.SUBJEK_PAJAK_ID = DAT_SUBJEK_PAJAK.SUBJEK_PAJAK_ID "
                                +
                                "LEFT JOIN DAT_OP_BUMI ON DAT_OP_BUMI.KD_PROPINSI = DAT_OBJEK_PAJAK.KD_PROPINSI AND " +
                                "    DAT_OP_BUMI.KD_DATI2 = DAT_OBJEK_PAJAK.KD_DATI2 AND " +
                                "    DAT_OP_BUMI.KD_KECAMATAN = DAT_OBJEK_PAJAK.KD_KECAMATAN AND " +
                                "    DAT_OP_BUMI.KD_KELURAHAN = DAT_OBJEK_PAJAK.KD_KELURAHAN AND " +
                                "    DAT_OP_BUMI.KD_BLOK = DAT_OBJEK_PAJAK.KD_BLOK AND " +
                                "    DAT_OP_BUMI.NO_URUT = DAT_OBJEK_PAJAK.NO_URUT AND " +
                                "    DAT_OP_BUMI.KD_JNS_OP = DAT_OBJEK_PAJAK.KD_JNS_OP " +
                                "LEFT JOIN REF_KECAMATAN ON REF_KECAMATAN.KD_PROPINSI = DAT_OBJEK_PAJAK.KD_PROPINSI AND "
                                +
                                "    REF_KECAMATAN.KD_DATI2 = DAT_OBJEK_PAJAK.KD_DATI2 AND " +
                                "    REF_KECAMATAN.KD_KECAMATAN = DAT_OBJEK_PAJAK.KD_KECAMATAN " +
                                "LEFT JOIN REF_KELURAHAN ON REF_KELURAHAN.KD_PROPINSI = DAT_OBJEK_PAJAK.KD_PROPINSI AND "
                                +
                                "    REF_KELURAHAN.KD_DATI2 = DAT_OBJEK_PAJAK.KD_DATI2 AND " +
                                "    REF_KELURAHAN.KD_KECAMATAN = DAT_OBJEK_PAJAK.KD_KECAMATAN AND " +
                                "    REF_KELURAHAN.KD_KELURAHAN = DAT_OBJEK_PAJAK.KD_KELURAHAN " +
                                "WHERE DAT_OBJEK_PAJAK.KD_PROPINSI = ? AND DAT_OBJEK_PAJAK.KD_DATI2 = ? AND " +
                                "DAT_OBJEK_PAJAK.KD_KECAMATAN = ? AND DAT_OBJEK_PAJAK.KD_KELURAHAN = ? AND " +
                                "DAT_OBJEK_PAJAK.KD_BLOK = ? AND DAT_OBJEK_PAJAK.NO_URUT = ? AND DAT_OBJEK_PAJAK.KD_JNS_OP = ?",
                        kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op);
            } catch (Exception oracleEx) {
                // Oracle tidak tersedia, gunakan data minimal
                System.out.println("Oracle SISMIOP tidak tersedia: " + oracleEx.getMessage());
            }

            // Get geometry from PostgreSQL
            Map<String, Object> bidangData = postgresJdbcTemplate.queryForMap(
                    "SELECT id, geom FROM sig.bidang WHERE id = ?::uuid",
                    id);

            // Format NOP
            String nop = String.format("%s.%s.%s.%s.%s.%s.%s",
                    kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("nop", nop);
            response.put("id", bidangData.get("id"));
            response.put("geom", bidangData.get("geom"));

            if (oracleData != null) {
                response.put("detail", true);
                response.put("alamatOP", formatAlamatOP(oracleData));
                response.put("namaWP", oracleData.get("NM_WP"));
                response.put("npwp", oracleData.get("NPWP"));
                response.put("alamatWP", formatAlamatWP(oracleData));
                response.put("luasTanah", formatLuas(oracleData.get("TOTAL_LUAS_BUMI")));
                response.put("luasBangunan", formatLuas(oracleData.get("TOTAL_LUAS_BNG")));
                response.put("njopBumi", formatCurrency(oracleData.get("NJOP_BUMI")));
                response.put("njopBangunan", formatCurrency(oracleData.get("NJOP_BNG")));
                response.put("kodeZNT", oracleData.get("KD_ZNT"));
            } else {
                response.put("detail", false);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal mengambil info NOP",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/map/newnop
     * Membuat bidang baru
     * 
     * @param body { nop: "XX.XX.XXX.XXX.XXX.XXXX.X", geometry: "WKB hex string" }
     * @return Success message
     */
    @PostMapping("/newnop")
    public ResponseEntity<Map<String, Object>> createNewNop(@RequestBody Map<String, Object> body) {
        try {
            String nop = (String) body.get("nop");
            String geometry = (String) body.get("geometry");

            String[] parts = nop.split("\\.");
            if (parts.length != 7) {
                return ResponseEntity.badRequest().body(Map.of("error", "Format NOP tidak valid"));
            }

            // Check if NOP already exists and is active
            Integer count = postgresJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sig.bidang WHERE kd_prop = ? AND kd_dati2 = ? AND kd_kec = ? AND " +
                            "kd_kel = ? AND kd_blok = ? AND no_urut = ? AND kd_jns_op = ? AND is_active = true",
                    Integer.class,
                    parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);

            if (count != null && count > 0) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "NOP " + nop + " sudah terpakai"));
            }

            postgresJdbcTemplate.update(
                    "INSERT INTO sig.bidang (kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, nop, geom, is_active, created_at) "
                            +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, NOW())",
                    parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], nop, geometry);

            return ResponseEntity.ok(Map.of("message", "NOP " + nop + " berhasil disimpan"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal simpan NOP",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/map/updatenop
     * Update geometry bidang (bisa multiple)
     * 
     * @param body { geometry: [{ id: "uuid", geom: "WKB hex" }, ...] }
     * @return Success message
     */
    @PostMapping("/updatenop")
    public ResponseEntity<Map<String, Object>> updateNop(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> geometries = (List<Map<String, Object>>) body.get("geometry");

            for (Map<String, Object> item : geometries) {
                String id = (String) item.get("id");
                String geom = (String) item.get("geom");

                int updated = postgresJdbcTemplate.update(
                        "UPDATE sig.bidang SET geom = ?, updated_at = NOW() WHERE id = ?::uuid",
                        geom, id);

                if (updated == 0) {
                    return ResponseEntity.status(404).body(Map.of(
                            "error", "Bidang dengan ID " + id + " tidak ditemukan"));
                }
            }

            return ResponseEntity.ok(Map.of("message", "Bidang berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal update bidang",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/map/updatekecamatan
     * Update geometry kecamatan
     * 
     * @param body { id: "uuid", geom: "WKB hex" }
     * @return Success message
     */
    @PostMapping("/updatekecamatan")
    public ResponseEntity<Map<String, Object>> updateKecamatan(@RequestBody Map<String, Object> body) {
        try {
            String id = (String) body.get("id");
            String geom = (String) body.get("geom");

            int updated = postgresJdbcTemplate.update(
                    "UPDATE system.kecamatan SET geom = ?, updated_at = NOW() WHERE id = ?",
                    geom, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kecamatan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of("message", "Kecamatan berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal update kecamatan",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/map/updatekelurahan
     * Update geometry kelurahan
     */
    @PostMapping("/updatekelurahan")
    public ResponseEntity<Map<String, Object>> updateKelurahan(@RequestBody Map<String, Object> body) {
        try {
            String id = (String) body.get("id");
            String geom = (String) body.get("geom");

            int updated = postgresJdbcTemplate.update(
                    "UPDATE system.kelurahan SET geom = ?, updated_at = NOW() WHERE id = ?",
                    geom, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Kelurahan tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of("message", "Kelurahan berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal update kelurahan",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/map/updateblok
     * Update geometry blok
     */
    @PostMapping("/updateblok")
    public ResponseEntity<Map<String, Object>> updateBlok(@RequestBody Map<String, Object> body) {
        try {
            String id = (String) body.get("id");
            String geom = (String) body.get("geom");

            int updated = postgresJdbcTemplate.update(
                    "UPDATE sig.blok SET geom = ?, updated_at = NOW() WHERE id = ?::uuid",
                    geom, id);

            if (updated == 0) {
                return ResponseEntity.status(404).body(Map.of("error", "Blok tidak ditemukan"));
            }

            return ResponseEntity.ok(Map.of("message", "Blok berhasil diupdate"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Gagal update blok",
                    "message", e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========

    private String formatAlamatOP(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        if (data.get("JALAN_OP") != null)
            sb.append(data.get("JALAN_OP"));
        if (data.get("BLOK_KAV_NO_OP") != null)
            sb.append(" Blok ").append(data.get("BLOK_KAV_NO_OP"));
        if (data.get("RT_OP") != null && data.get("RW_OP") != null) {
            sb.append(" RT/RW ").append(data.get("RT_OP")).append("/").append(data.get("RW_OP"));
        }
        if (data.get("NM_KELURAHAN") != null)
            sb.append(", ").append(data.get("NM_KELURAHAN"));
        if (data.get("NM_KECAMATAN") != null)
            sb.append(", ").append(data.get("NM_KECAMATAN"));
        return sb.toString();
    }

    private String formatAlamatWP(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        if (data.get("JALAN_WP") != null)
            sb.append(data.get("JALAN_WP"));
        if (data.get("KELURAHAN_WP") != null)
            sb.append(", ").append(data.get("KELURAHAN_WP"));
        if (data.get("KOTA_WP") != null)
            sb.append(", ").append(data.get("KOTA_WP"));
        return sb.toString();
    }

    private String formatLuas(Object luas) {
        if (luas == null)
            return "0 m<sup>2</sup>";
        return String.format("%,.0f m<sup>2</sup>", ((Number) luas).doubleValue());
    }

    private String formatCurrency(Object value) {
        if (value == null)
            return "0";
        return String.format("%,.0f", ((Number) value).doubleValue());
    }
}
