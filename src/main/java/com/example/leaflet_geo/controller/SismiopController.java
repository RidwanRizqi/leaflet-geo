package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.service.SismiopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<String>> testConnection() {
        try {
            String result = sismiopService.testConnection();
            return ResponseEntity.ok(
                    ApiResponse.success("SISMIOP connection test berhasil", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Connection failed: " + e.getMessage()));
        }
    }

    /**
     * Analisa database - get list of all tables
     * GET /api/sismiop/analyze
     */
    @GetMapping("/analyze")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> analyzeDatabase() {
        try {
            List<Map<String, Object>> tables = sismiopService.getAllTables();
            return ResponseEntity.ok(
                    ApiResponse.success("Database analysis completed. Total tables: " + tables.size(), tables,
                            (long) tables.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to analyze database: " + e.getMessage()));
        }
    }

    /**
     * Get structure of a specific table
     * GET /api/sismiop/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> columns = sismiopService.getTableStructure(tableName);
            Long rowCount = sismiopService.getTableRowCount(tableName);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Structure for table " + tableName.toUpperCase() + " retrieved. Columns: " + columns.size()
                                    + ", Rows: " + rowCount,
                            columns,
                            (long) columns.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get table structure: " + e.getMessage()));
        }
    }

    /**
     * Get sample data from a table
     * GET /api/sismiop/tables/{tableName}/data?limit=10
     */
    @GetMapping("/tables/{tableName}/data")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> data = sismiopService.getSampleData(tableName, limit);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Sample data from " + tableName.toUpperCase() + " retrieved (limit: " + limit + ")",
                            data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get sample data: " + e.getMessage()));
        }
    }

    /**
     * Get data objek pajak by NOP
     * GET /api/sismiop/objek-pajak/{nop}
     */
    @GetMapping("/objek-pajak/{nop}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getObjekPajak(@PathVariable String nop) {
        try {
            Map<String, Object> data = sismiopService.getObjekPajakByNop(nop);
            return ResponseEntity.ok(
                    ApiResponse.success("Objek pajak dengan NOP " + nop + " ditemukan", data));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("Objek pajak tidak ditemukan: " + e.getMessage()));
        }
    }

    /**
     * Get data subjek pajak by ID
     * GET /api/sismiop/subjek-pajak/{subjekPajakId}
     */
    @GetMapping("/subjek-pajak/{subjekPajakId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubjekPajak(@PathVariable String subjekPajakId) {
        try {
            Map<String, Object> data = sismiopService.getSubjekPajakById(subjekPajakId);
            return ResponseEntity.ok(
                    ApiResponse.success("Subjek pajak dengan ID " + subjekPajakId + " ditemukan", data));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("Subjek pajak tidak ditemukan: " + e.getMessage()));
        }
    }

    /**
     * Get list kecamatan
     * GET /api/sismiop/kecamatan
     */
    @GetMapping("/kecamatan")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getKecamatanList() {
        try {
            List<Map<String, Object>> data = sismiopService.getKecamatanList();
            return ResponseEntity.ok(
                    ApiResponse.success("List kecamatan berhasil diambil", data, (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get kecamatan list: " + e.getMessage()));
        }
    }

    /**
     * Get list kelurahan by kecamatan
     * GET /api/sismiop/kelurahan?kdKecamatan=010
     */
    @GetMapping("/kelurahan")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getKelurahanByKecamatan(
            @RequestParam String kdKecamatan) {
        try {
            List<Map<String, Object>> data = sismiopService.getKelurahanByKecamatan(kdKecamatan);
            return ResponseEntity.ok(
                    ApiResponse.success("List kelurahan untuk kecamatan " + kdKecamatan + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get kelurahan list: " + e.getMessage()));
        }
    }

    /**
     * Get SPPT by NOP and Tahun
     * GET /api/sismiop/sppt/{nop}?tahun=2024
     */
    @GetMapping("/sppt/{nop}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSppt(
            @PathVariable String nop,
            @RequestParam String tahun) {
        try {
            Map<String, Object> data = sismiopService.getSpptByNopTahun(nop, tahun);
            return ResponseEntity.ok(
                    ApiResponse.success("SPPT untuk NOP " + nop + " tahun " + tahun + " ditemukan", data));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                    ApiResponse.error("SPPT tidak ditemukan: " + e.getMessage()));
        }
    }
}
