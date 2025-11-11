package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.service.SimatdaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simatda")
@CrossOrigin(origins = "*")
public class SimatdaController {

    private final SimatdaService simatdaService;

    public SimatdaController(SimatdaService simatdaService) {
        this.simatdaService = simatdaService;
    }

    /**
     * Endpoint untuk test koneksi ke database SIMATDA
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        boolean isConnected = simatdaService.testConnection();
        
        response.put("connected", isConnected);
        response.put("database", "simpatda_lumajang");
        response.put("message", isConnected ? "Koneksi berhasil" : "Koneksi gagal");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint untuk mengambil data dari tabel tertentu
     * Contoh: GET /api/simatda/data?table=nama_tabel
     */
    @GetMapping("/data")
    public ResponseEntity<List<Map<String, Object>>> getData(@RequestParam String table) {
        try {
            List<Map<String, Object>> data = simatdaService.getAllData(table);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint untuk execute query custom
     * POST body: { "query": "SELECT * FROM table WHERE ..." }
     */
    @PostMapping("/query")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Map<String, Object>> data = simatdaService.executeQuery(query);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
