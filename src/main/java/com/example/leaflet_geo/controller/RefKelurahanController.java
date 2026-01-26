package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.entity.RefKelurahan;
import com.example.leaflet_geo.repository.RefKelurahanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ref-kelurahan")
@CrossOrigin(origins = "*")
public class RefKelurahanController {

    @Autowired
    private RefKelurahanRepository refKelurahanRepository;

    /**
     * Get all kelurahan data
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getAllKelurahan() {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findAll();
            long totalCount = refKelurahanRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Data kelurahan berhasil diambil", kelurahanList, totalCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by kode propinsi
     */
    @GetMapping("/propinsi/{kdPropinsi}")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getKelurahanByPropinsi(@PathVariable String kdPropinsi) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsi(kdPropinsi);
            long totalCount = refKelurahanRepository.countByKdPropinsi(kdPropinsi);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kelurahan untuk propinsi " + kdPropinsi + " berhasil diambil",
                            kelurahanList,
                            totalCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by kode propinsi and kode dati2
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getKelurahanByPropinsiAndDati2(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kelurahan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2
                                    + " berhasil diambil",
                            kelurahanList,
                            totalCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by kode propinsi, dati2, and kecamatan
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getKelurahanByPropinsiDati2AndKecamatan(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository
                    .findByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2, kdKecamatan);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2,
                    kdKecamatan);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kelurahan untuk propinsi " + kdPropinsi + ", dati2 " + kdDati2 + ", dan kecamatan "
                                    + kdKecamatan + " berhasil diambil",
                            kelurahanList,
                            totalCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by primary key
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}/kelurahan/{kdKelurahan}")
    public ResponseEntity<ApiResponse<RefKelurahan>> getKelurahanByPrimaryKey(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan) {
        try {
            RefKelurahan kelurahan = refKelurahanRepository.findByPrimaryKey(kdPropinsi, kdDati2, kdKecamatan,
                    kdKelurahan);
            if (kelurahan != null) {
                return ResponseEntity.ok(
                        ApiResponse.success("Data kelurahan ditemukan", kelurahan));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.error("Data kelurahan tidak ditemukan"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Search kelurahan by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> searchKelurahanByName(@RequestParam String nama) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByNmKelurahanContaining(nama);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Pencarian kelurahan dengan nama '" + nama + "' berhasil",
                            kelurahanList,
                            (long) kelurahanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mencari data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by kode sektor
     */
    @GetMapping("/sektor/{kdSektor}")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getKelurahanBySektor(@PathVariable String kdSektor) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdSektor(kdSektor);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kelurahan untuk sektor " + kdSektor + " berhasil diambil",
                            kelurahanList,
                            (long) kelurahanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan by kode pos
     */
    @GetMapping("/kodepos/{kdPosKelurahan}")
    public ResponseEntity<ApiResponse<List<RefKelurahan>>> getKelurahanByKodePos(@PathVariable String kdPosKelurahan) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPosKelurahan(kdPosKelurahan);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Data kelurahan untuk kode pos " + kdPosKelurahan + " berhasil diambil",
                            kelurahanList,
                            (long) kelurahanList.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil data kelurahan: " + e.getMessage()));
        }
    }

    /**
     * Get kelurahan count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getKelurahanCount() {
        try {
            long count = refKelurahanRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Jumlah data kelurahan berhasil diambil", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Gagal mengambil jumlah data kelurahan: " + e.getMessage()));
        }
    }

    // ============================================
    // PAGINATION ENDPOINTS (Keep HashMap - has extra fields)
    // ============================================

    /**
     * Get kelurahan with pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findAllWithPagination(offset, size);
            long totalCount = refKelurahanRepository.count();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan dengan pagination berhasil diambil");
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("data", kelurahanList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kelurahan: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kelurahan by propinsi with pagination
     */
    @GetMapping("/propinsi/{kdPropinsi}/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiWithPagination(
            @PathVariable String kdPropinsi,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiWithPagination(kdPropinsi, offset,
                    size);
            long totalCount = refKelurahanRepository.countByKdPropinsi(kdPropinsi);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message",
                    "Data kelurahan untuk propinsi " + kdPropinsi + " dengan pagination berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("data", kelurahanList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kelurahan: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kelurahan by propinsi and dati2 with pagination
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiAndDati2WithPagination(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository
                    .findByKdPropinsiAndKdDati2WithPagination(kdPropinsi, kdDati2, offset, size);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2
                    + " dengan pagination berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("kdDati2", kdDati2);
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("data", kelurahanList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kelurahan: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kelurahan by propinsi, dati2, and kecamatan with pagination
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiDati2AndKecamatanWithPagination(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository
                    .findByKdPropinsiAndKdDati2AndKdKecamatanWithPagination(kdPropinsi, kdDati2, kdKecamatan, offset,
                            size);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2,
                    kdKecamatan);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + ", dati2 " + kdDati2
                    + ", dan kecamatan " + kdKecamatan + " dengan pagination berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("kdDati2", kdDati2);
            response.put("kdKecamatan", kdKecamatan);
            response.put("page", page);
            response.put("size", size);
            response.put("totalCount", totalCount);
            response.put("totalPages", (int) Math.ceil((double) totalCount / size));
            response.put("data", kelurahanList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil data kelurahan: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.internalServerError().body(response);
        }
    }
}
