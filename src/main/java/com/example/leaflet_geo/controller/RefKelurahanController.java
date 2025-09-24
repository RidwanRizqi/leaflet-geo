package com.example.leaflet_geo.controller;

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
     * GET /api/ref-kelurahan
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllKelurahan() {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findAll();
            long totalCount = refKelurahanRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan berhasil diambil");
            response.put("totalCount", totalCount);
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
     * Get kelurahan by kode propinsi
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}
     */
    @GetMapping("/propinsi/{kdPropinsi}")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsi(@PathVariable String kdPropinsi) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsi(kdPropinsi);
            long totalCount = refKelurahanRepository.countByKdPropinsi(kdPropinsi);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + " berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("totalCount", totalCount);
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
     * Get kelurahan by kode propinsi and kode dati2
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/dati2/{kdDati2}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiAndDati2(
            @PathVariable String kdPropinsi, 
            @PathVariable String kdDati2) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2 + " berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("kdDati2", kdDati2);
            response.put("totalCount", totalCount);
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
     * Get kelurahan by kode propinsi, dati2, and kecamatan
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiDati2AndKecamatan(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2, kdKecamatan);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2, kdKecamatan);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + ", dati2 " + kdDati2 + ", dan kecamatan " + kdKecamatan + " berhasil diambil");
            response.put("kdPropinsi", kdPropinsi);
            response.put("kdDati2", kdDati2);
            response.put("kdKecamatan", kdKecamatan);
            response.put("totalCount", totalCount);
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
     * Get kelurahan by primary key
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}/kelurahan/{kdKelurahan}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}/kelurahan/{kdKelurahan}")
    public ResponseEntity<Map<String, Object>> getKelurahanByPrimaryKey(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan) {
        try {
            RefKelurahan kelurahan = refKelurahanRepository.findByPrimaryKey(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan);
            
            Map<String, Object> response = new HashMap<>();
            if (kelurahan != null) {
                response.put("success", true);
                response.put("message", "Data kelurahan ditemukan");
                response.put("data", kelurahan);
            } else {
                response.put("success", false);
                response.put("message", "Data kelurahan tidak ditemukan");
                response.put("data", null);
            }
            
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
     * Search kelurahan by name
     * GET /api/ref-kelurahan/search?nama={namaKelurahan}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchKelurahanByName(@RequestParam String nama) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByNmKelurahanContaining(nama);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pencarian kelurahan dengan nama '" + nama + "' berhasil");
            response.put("searchTerm", nama);
            response.put("totalCount", kelurahanList.size());
            response.put("data", kelurahanList);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mencari data kelurahan: " + e.getMessage());
            response.put("data", null);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get kelurahan by kode sektor
     * GET /api/ref-kelurahan/sektor/{kdSektor}
     */
    @GetMapping("/sektor/{kdSektor}")
    public ResponseEntity<Map<String, Object>> getKelurahanBySektor(@PathVariable String kdSektor) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdSektor(kdSektor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk sektor " + kdSektor + " berhasil diambil");
            response.put("kdSektor", kdSektor);
            response.put("totalCount", kelurahanList.size());
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
     * Get kelurahan by kode pos
     * GET /api/ref-kelurahan/kodepos/{kdPosKelurahan}
     */
    @GetMapping("/kodepos/{kdPosKelurahan}")
    public ResponseEntity<Map<String, Object>> getKelurahanByKodePos(@PathVariable String kdPosKelurahan) {
        try {
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPosKelurahan(kdPosKelurahan);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk kode pos " + kdPosKelurahan + " berhasil diambil");
            response.put("kdPosKelurahan", kdPosKelurahan);
            response.put("totalCount", kelurahanList.size());
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
     * Get kelurahan with pagination
     * GET /api/ref-kelurahan/paginated?page={page}&size={size}
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
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/paginated?page={page}&size={size}
     */
    @GetMapping("/propinsi/{kdPropinsi}/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiWithPagination(
            @PathVariable String kdPropinsi,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiWithPagination(kdPropinsi, offset, size);
            long totalCount = refKelurahanRepository.countByKdPropinsi(kdPropinsi);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + " dengan pagination berhasil diambil");
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
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/dati2/{kdDati2}/paginated?page={page}&size={size}
     */
    @GetMapping("/propinsi/{kdPropinsi}/dati2/{kdDati2}/paginated")
    public ResponseEntity<Map<String, Object>> getKelurahanByPropinsiAndDati2WithPagination(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiAndKdDati2WithPagination(kdPropinsi, kdDati2, offset, size);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2(kdPropinsi, kdDati2);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + " dan dati2 " + kdDati2 + " dengan pagination berhasil diambil");
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
     * GET /api/ref-kelurahan/propinsi/{kdPropinsi}/dati2/{kdDati2}/kecamatan/{kdKecamatan}/paginated?page={page}&size={size}
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
            List<RefKelurahan> kelurahanList = refKelurahanRepository.findByKdPropinsiAndKdDati2AndKdKecamatanWithPagination(kdPropinsi, kdDati2, kdKecamatan, offset, size);
            long totalCount = refKelurahanRepository.countByKdPropinsiAndKdDati2AndKdKecamatan(kdPropinsi, kdDati2, kdKecamatan);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Data kelurahan untuk propinsi " + kdPropinsi + ", dati2 " + kdDati2 + ", dan kecamatan " + kdKecamatan + " dengan pagination berhasil diambil");
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

    /**
     * Get kelurahan count
     * GET /api/ref-kelurahan/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getKelurahanCount() {
        try {
            long count = refKelurahanRepository.count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Jumlah data kelurahan berhasil diambil");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Gagal mengambil jumlah data kelurahan: " + e.getMessage());
            response.put("count", 0);
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
