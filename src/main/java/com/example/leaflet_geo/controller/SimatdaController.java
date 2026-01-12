package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.service.SimatdaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Boolean>> testConnection() {
        boolean isConnected = simatdaService.testConnection();
        if (isConnected) {
            return ResponseEntity.ok(
                    ApiResponse.success("Koneksi ke database simpatda_lumajang berhasil", isConnected));
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error("Koneksi ke database simpatda_lumajang gagal"));
        }
    }

    /**
     * Endpoint untuk mengambil data dari tabel tertentu
     */
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getData(@RequestParam String table) {
        try {
            List<Map<String, Object>> data = simatdaService.getAllData(table);
            return ResponseEntity.ok(
                    ApiResponse.success("Data dari tabel " + table + " berhasil diambil", data, (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data: " + e.getMessage()));
        }
    }

    /**
     * Endpoint untuk execute query custom
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> executeQuery(
            @RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Query tidak boleh kosong"));
            }

            List<Map<String, Object>> data = simatdaService.executeQuery(query);
            return ResponseEntity.ok(
                    ApiResponse.success("Query berhasil dieksekusi", data, (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengeksekusi query: " + e.getMessage()));
        }
    }
}
