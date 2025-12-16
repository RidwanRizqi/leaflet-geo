package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.service.EpasirService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = epasirService.testConnection();
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
     * GET /api/epasir/analyze
     */
    @GetMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> tables = epasirService.getAllTables();
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
     * GET /api/epasir/tables/{tableName}/structure
     */
    @GetMapping("/tables/{tableName}/structure")
    public ResponseEntity<Map<String, Object>> getTableStructure(@PathVariable String tableName) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> columns = epasirService.getTableStructure(tableName);
            
            response.put("status", "success");
            response.put("tableName", tableName);
            response.put("columnCount", columns.size());
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
     * GET /api/epasir/tables/{tableName}/data?limit=10
     */
    @GetMapping("/tables/{tableName}/data")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "10") Integer limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> data = epasirService.getSampleData(tableName, limit);
            response.put("status", "success");
            response.put("tableName", tableName);
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
}
