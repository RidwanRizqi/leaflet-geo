package com.example.leaflet_geo.controller;

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
    public ResponseEntity<List<Map<String, Object>>> getAllTables() {
        try {
            List<Map<String, Object>> tables = bphtbService.getAllTables();
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get table structure
     * GET /api/bphtb/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<List<Map<String, Object>>> getTableStructure(@PathVariable String tableName) {
        try {
            List<Map<String, Object>> structure = bphtbService.getTableStructure(tableName);
            return ResponseEntity.ok(structure);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get table row count
     * GET /api/bphtb/tables/{tableName}/count
     */
    @GetMapping("/tables/{tableName}/count")
    public ResponseEntity<Map<String, Object>> getTableRowCount(@PathVariable String tableName) {
        try {
            Long count = bphtbService.getTableRowCount(tableName);
            Map<String, Object> response = new HashMap<>();
            response.put("table_name", tableName);
            response.put("row_count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get sample data from table
     * GET /api/bphtb/tables/{tableName}/sample?limit=10
     */
    @GetMapping("/tables/{tableName}/sample")
    public ResponseEntity<List<Map<String, Object>>> getSampleData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> data = bphtbService.getSampleData(tableName, limit);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
