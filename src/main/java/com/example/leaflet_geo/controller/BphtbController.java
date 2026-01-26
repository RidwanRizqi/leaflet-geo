package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.service.BphtbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bphtb")
@CrossOrigin(origins = "*")
public class BphtbController {

    private final BphtbService bphtbService;

    public BphtbController(BphtbService bphtbService) {
        this.bphtbService = bphtbService;
    }

    /**
     * Test koneksi database BPHTB
     * GET /api/bphtb/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testConnection() {
        try {
            String result = bphtbService.testConnection();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get list of all tables
     * GET /api/bphtb/tables
     */
    @GetMapping("/tables")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTables() {
        try {
            List<Map<String, Object>> tables = bphtbService.getAllTables();
            return ResponseEntity.ok(
                    ApiResponse.success("Tables retrieved successfully", tables, (long) tables.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get tables: " + e.getMessage()));
        }
    }

    /**
     * Get table structure
     * GET /api/bphtb/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> structure = bphtbService.getTableStructure(tableName);
            return ResponseEntity.ok(
                    ApiResponse.success("Structure for table " + tableName + " retrieved", structure,
                            (long) structure.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get table structure: " + e.getMessage()));
        }
    }

    /**
     * Get table row count
     */
    @GetMapping("/tables/{tableName}/count")
    public ResponseEntity<ApiResponse<Long>> getTableRowCount(@PathVariable String tableName) {
        try {
            Long count = bphtbService.getTableRowCount(tableName);
            return ResponseEntity.ok(
                    ApiResponse.success("Row count for table " + tableName + " retrieved", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get row count: " + e.getMessage()));
        }
    }

    /**
     * Get sample data from table
     */
    @GetMapping("/tables/{tableName}/sample")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSampleData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> data = bphtbService.getSampleData(tableName, limit);
            return ResponseEntity.ok(
                    ApiResponse.success("Sample data from " + tableName + " retrieved", data, (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Failed to get sample data: " + e.getMessage()));
        }
    }
}
