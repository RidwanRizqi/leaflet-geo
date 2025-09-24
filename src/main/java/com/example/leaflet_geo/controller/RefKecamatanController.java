package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.entity.RefKecamatan;
import com.example.leaflet_geo.repository.RefKecamatanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ref-kecamatan")
@CrossOrigin(origins = "*")
public class RefKecamatanController {

    @Autowired
    private RefKecamatanRepository refKecamatanRepository;

    /**
     * Get all kecamatan data
     * GET /api/ref-kecamatan
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllKecamatan() {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findAll();
            long totalCount = refKecamatanRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kecamatan berhasil diambil");
            response.put("totalCount", totalCount);
            response.put("data", kecamatanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kecamatan by kode propinsi
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}
     */
    @GetMapping("/propinsi/{kdPropinsi}")
    public ResponseEntity<Map<String, Object>> getKecamatanByPropinsi(@PathVariable String kdPropinsi) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByKdPropinsi(kdPropinsi);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kecamatan untuk propinsi " + kdPropinsi + " berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("totalCount", kecamatanList.size());
            response.put("data", kecamatanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kecamatan by kode propinsi and kode dati2
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}/dati2/{kdDati2}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}")
    public ResponseEntity<Map<String, Object>> getKecamatanByPropinsiAndDati2(
            @PathVariable String kdPropinsi, 
            @PathVariable String kdDati2) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kecamatan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2 + " berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("kdDati2", kdDati2);
            response.put("totalCount", kecamatanList.size());
            response.put("data", kecamatanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kecamatan by primary key
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}")
    public ResponseEntity<Map<String, Object>> getKecamatanByPrimaryKey(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan) {
        try {
            RefKecamatan kecamatan = refKecamatanRepository.findByPrimaryKey(kdPropinsi, kdDati2, kdKecamatan);
            
            Map<String, Object> response = new HashMap<>();
            if (kecamatan != null) {
                response.put("success", true);
                response.put("message", "Data kecamatan ditemukan");
                response.put("data", kecamatan);
            } else {
                response.put("success", false);
                response.put("message", "Data kecamatan tidak ditemukan");
                response.put("data", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search kecamatan by name
     * GET /api/ref-kecamatan/search?nama={namaKecamatan}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKecamatanByName(@RequestParam String nama) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByNmKecamatanContaining(nama);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pencarian kecamatan dengan nama '" + nama + "' berhasil");
            response.put("searchTerm", nama);
            response.put("totalCount", kecamatanList.size());
            response.put("data", kecamatanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mencari data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kecamatan with pagination
     * GET /api/ref-kecamatan/paginated?page={page}&size={size}
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getKecamatanWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findAllWithPagination(offset, size);
            long totalCount = refKecamatanRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kecamatan dengan pagination berhasil diambil");
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("data", kecamatanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kecamatan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kecamatan count
     * GET /api/ref-kecamatan/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getKecamatanCount() {
        try {
            long count = refKecamatanRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Jumlah data kecamatan berhasil diambil");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil jumlah data kecamatan: " + e.getMessage());
            response.put("count", 0);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
