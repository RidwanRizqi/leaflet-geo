package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.service.SismiopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sismiop")
@CrossOrigin(origins = "*")
public class SismiopController {

    private final SismiopService sismiopService;

    public SismiopController(SismiopService sismiopService) {
        this.sismiopService = sismiopService;
    }

    /**
     * Test koneksi ke database SISMIOP
     * GET /api/sismiop/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = sismiopService.testConnection();
            response.put("status", "success");
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Analisa database - get list of all tables
     * GET /api/sismiop/analyze
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> tables = sismiopService.getAllTables();
            response.put("status", "success");
            response.put("totalTables", tables.size());
            response.put("tables", tables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get structure of a specific table
     * GET /api/sismiop/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<Map<String, Object>> getTableStructure(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> columns = sismiopService.getTableStructure(tableName);
            Long rowCount = sismiopService.getTableRowCount(tableName);
            
            response.put("status", "success");
            response.put("tableName", tableName.toUpperCase());
            response.put("columnCount", columns.size());
            response.put("rowCount", rowCount);
            response.put("columns", columns);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get sample data from a table
     * GET /api/sismiop/tables/{tableName}/data?limit=10
     */
    @GetMapping("/tables/{tableName}/data")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> data = sismiopService.getSampleData(tableName, limit);
            response.put("status", "success");
            response.put("tableName", tableName.toUpperCase());
            response.put("limit", limit);
            response.put("resultCount", data.size());
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get data objek pajak by NOP
     * GET /api/sismiop/objek-pajak/{nop}
     */
    @GetMapping("/objek-pajak/{nop}")
    public ResponseEntity<Map<String, Object>> getObjekPajak(@PathVariable String nop) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = sismiopService.getObjekPajakByNop(nop);
            response.put("status", "success");
            response.put("nop", nop);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * Get data subjek pajak by ID
     * GET /api/sismiop/subjek-pajak/{subjekPajakId}
     */
    @GetMapping("/subjek-pajak/{subjekPajakId}")
    public ResponseEntity<Map<String, Object>> getSubjekPajak(@PathVariable String subjekPajakId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = sismiopService.getSubjekPajakById(subjekPajakId);
            response.put("status", "success");
            response.put("subjekPajakId", subjekPajakId);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * Get list kecamatan
     * GET /api/sismiop/kecamatan
     */
    @GetMapping("/kecamatan")
    public ResponseEntity<Map<String, Object>> getKecamatanList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> data = sismiopService.getKecamatanList();
            response.put("status", "success");
            response.put("count", data.size());
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get list kelurahan by kecamatan
     * GET /api/sismiop/kelurahan?kdKecamatan=010
     */
    @GetMapping("/kelurahan")
    public ResponseEntity<Map<String, Object>> getKelurahanByKecamatan(
            @RequestParam String kdKecamatan) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> data = sismiopService.getKelurahanByKecamatan(kdKecamatan);
            response.put("status", "success");
            response.put("kdKecamatan", kdKecamatan);
            response.put("count", data.size());
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get SPPT by NOP and Tahun
     * GET /api/sismiop/sppt/{nop}?tahun=2024
     */
    @GetMapping("/sppt/{nop}")
    public ResponseEntity<Map<String, Object>> getSppt(
            @PathVariable String nop,
            @RequestParam String tahun) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = sismiopService.getSpptByNopTahun(nop, tahun);
            response.put("status", "success");
            response.put("nop", nop);
            response.put("tahun", tahun);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    /**
     * Debug: Get SPPT by individual kode components
     * GET /api/sismiop/sppt-by-kode?kd_prop=35&kd_dati2=08&kd_kec=060&kd_kel=014&kd_blok=007&no_urut=0028&kd_jns_op=0&tahun=2025
     */
    @GetMapping("/sppt-by-kode")
    public ResponseEntity<Map<String, Object>> getSpptByKode(
            @RequestParam String kd_prop,
            @RequestParam String kd_dati2,
            @RequestParam String kd_kec,
            @RequestParam String kd_kel,
            @RequestParam String kd_blok,
            @RequestParam String no_urut,
            @RequestParam String kd_jns_op,
            @RequestParam String tahun) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = sismiopService.getSpptByKodes(
                kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op, tahun);
            response.put("status", "success");
            response.put("params", Map.of(
                "kd_prop", kd_prop,
                "kd_dati2", kd_dati2,
                "kd_kec", kd_kec,
                "kd_kel", kd_kel,
                "kd_blok", kd_blok,
                "no_urut", no_urut,
                "kd_jns_op", kd_jns_op,
                "tahun", tahun
            ));
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("params", Map.of(
                "kd_prop", kd_prop,
                "kd_dati2", kd_dati2,
                "kd_kec", kd_kec,
                "kd_kel", kd_kel,
                "kd_blok", kd_blok,
                "no_urut", no_urut,
                "kd_jns_op", kd_jns_op,
                "tahun", tahun
            ));
            return ResponseEntity.status(404).body(response);
        }
    }
}
