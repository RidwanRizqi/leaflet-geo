package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.model.RefKecamatan;
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
    public ResponseEntity<ApiResponse<List<RefKecamatan>>> getAllKecamatan() {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findAll();
            long totalCount = refKecamatanRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Data kecamatan berhasil diambil", kecamatanList, totalCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Get kecamatan by kode propinsi
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}
     */
    @GetMapping("/propinsi/{kdPropinsi}")
    public ResponseEntity<ApiResponse<List<RefKecamatan>>> getKecamatanByPropinsi(@PathVariable String kdPropinsi) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByKdPropinsi(kdPropinsi);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kecamatan untuk propinsi " + kdPropinsi + " berhasil diambil",
                            kecamatanList,
                            (long) kecamatanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Get kecamatan by kode propinsi and kode dati2
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}/dati2/{kdDati2}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}")
    public ResponseEntity<ApiResponse<List<RefKecamatan>>> getKecamatanByPropinsiAndDati2(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kecamatan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2
                                    + " berhasil diambil",
                            kecamatanList,
                            (long) kecamatanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Get kecamatan by primary key
     * GET /api/ref-kecamatan/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}")
    public ResponseEntity<ApiResponse<RefKecamatan>> getKecamatanByPrimaryKey(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan) {
        try {
            RefKecamatan kecamatan = refKecamatanRepository.findByPrimaryKey(kdPropinsi, kdDati2, kdKecamatan);

            Map<String, Object> response = new HashMap<>();
            if (kecamatan != null) {
                return ResponseEntity.ok(
                        ApiResponse.success("Data kecamatan ditemukan", kecamatan));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.error("Data kecamatan tidak ditemukan"));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Search kecamatan by name
     * GET /api/ref-kecamatan/search?nama={namaKecamatan}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RefKecamatan>>> searchKecamatanByName(@RequestParam String nama) {
        try {
            List<RefKecamatan> kecamatanList = refKecamatanRepository.findByNmKecamatanContaining(nama);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Pencarian kecamatan dengan nama '" + nama + "' berhasil",
                            kecamatanList,
                            (long) kecamatanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mencari data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Get kecamatan count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getKecamatanCount() {
        try {
            long count = refKecamatanRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Jumlah data kecamatan berhasil diambil", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil jumlah data kecamatan: " + e.getMessage()));
        }
    }

    /**
     * Get kecamatan with pagination
     * NOTE: Keep HashMap for this one because it needs extra fields (page, size,
     * totalPages)
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
