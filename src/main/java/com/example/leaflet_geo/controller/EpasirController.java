package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.service.EpasirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/epasir")
@CrossOrigin(origins = "*")
public class EpasirController {

    private final EpasirService epasirService;

    public EpasirController(EpasirService epasirService) {
        this.epasirService = epasirService;
    }

    /**
     * Test koneksi ke database E-PASIR
     * GET /api/epasir/test
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testConnection() {
        try {
            String result = epasirService.testConnection();
            return ResponseEntity.ok(
                    ApiResponse.success("Test connection to E-PASIR database berhasil", result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Connection failed: " + e.getMessage()));
        }
    }

    /**
     * Analisa database - get list of all tables
     * GET /api/epasir/analyze
     */
    @GetMapping("/analyze")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> analyzeDatabase() {
        try {
            List<Map<String, Object>> tables = epasirService.getAllTables();
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
     * GET /api/epasir/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> columns = epasirService.getTableStructure(tableName);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Structure for table " + tableName + " retrieved. Column count: " + columns.size(), columns,
                            (long) columns.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get table structure: " + e.getMessage()));
        }
    }

    /**
     * Get sample data from a table
     * GET /api/epasir/tables/{tableName}/data?limit=10
     */
    @GetMapping("/tables/{tableName}/data")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> data = epasirService.getSampleData(tableName, limit);
            return ResponseEntity.ok(
                    ApiResponse.success("Sample data from " + tableName + " retrieved (limit: " + limit + ")", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get sample data: " + e.getMessage()));
        }
    }
}
