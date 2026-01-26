package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.entity.DatSubjekPajak;
import com.example.leaflet_geo.repository.DatSubjekPajakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dat-subjek-pajak")
@CrossOrigin(origins = "*")
public class DatSubjekPajakController {

    @Autowired
    private DatSubjekPajakRepository datSubjekPajakRepository;

    // ============================================
    // PAGINATION ENDPOINTS (Keep HashMap - has extra fields)
    // ============================================

    /**
     * Get all subjek pajak with pagination
     * GET /api/dat-subjek-pajak?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDatSubjekPajak(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<DatSubjekPajak> data = datSubjekPajakRepository.findPaginated(offset, size);
            int totalCount = datSubjekPajakRepository.count();

            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", totalPages,
                    "hasNext", hasNext,
                    "hasPrev", hasPrev));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    // ============================================
    // SIMPLE ENDPOINTS (Use ApiResponse)
    // ============================================

    /**
     * Get count of subjek pajak
     * GET /api/dat-subjek-pajak/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCount() {
        try {
            int count = datSubjekPajakRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Count berhasil diambil", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by ID
     * GET /api/dat-subjek-pajak/{subjekPajakId}
     */
    @GetMapping("/{subjekPajakId}")
    public ResponseEntity<ApiResponse<DatSubjekPajak>> getDatSubjekPajakById(@PathVariable String subjekPajakId) {
        try {
            Optional<DatSubjekPajak> subjekPajak = datSubjekPajakRepository.findById(subjekPajakId);

            if (subjekPajak.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.success("Data subjek pajak berhasil diambil", subjekPajak.get()));
            } else {
                return ResponseEntity.status(404).body(
                        ApiResponse.error("Data subjek pajak tidak ditemukan"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by NPWP
     * GET /api/dat-subjek-pajak/npwp/{npwp}
     */
    @GetMapping("/npwp/{npwp}")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> getDatSubjekPajakByNpwp(@PathVariable String npwp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNpwp(npwp);
            return ResponseEntity.ok(
                    ApiResponse.success("Data subjek pajak untuk NPWP " + npwp + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by NPWPD
     * GET /api/dat-subjek-pajak/npwpd/{npwpd}
     */
    @GetMapping("/npwpd/{npwpd}")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> getDatSubjekPajakByNpwpd(@PathVariable String npwpd) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNpwpd(npwpd);
            return ResponseEntity.ok(
                    ApiResponse.success("Data subjek pajak untuk NPWPD " + npwpd + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Search subjek pajak by name
     * GET /api/dat-subjek-pajak/search/name?nmWp=...
     */
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> searchDatSubjekPajakByName(@RequestParam String nmWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNmWpContaining(nmWp);
            return ResponseEntity.ok(
                    ApiResponse.success("Pencarian subjek pajak dengan nama '" + nmWp + "' berhasil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by email
     * GET /api/dat-subjek-pajak/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> getDatSubjekPajakByEmail(@PathVariable String email) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByEmail(email);
            return ResponseEntity.ok(
                    ApiResponse.success("Data subjek pajak untuk email " + email + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by kota
     * GET /api/dat-subjek-pajak/kota/{kotaWp}
     */
    @GetMapping("/kota/{kotaWp}")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> getDatSubjekPajakByKota(@PathVariable String kotaWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByKotaWp(kotaWp);
            return ResponseEntity.ok(
                    ApiResponse.success("Data subjek pajak untuk kota " + kotaWp + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get subjek pajak by kelurahan
     * GET /api/dat-subjek-pajak/kelurahan/{kelurahanWp}
     */
    @GetMapping("/kelurahan/{kelurahanWp}")
    public ResponseEntity<ApiResponse<List<DatSubjekPajak>>> getDatSubjekPajakByKelurahan(
            @PathVariable String kelurahanWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByKelurahanWp(kelurahanWp);
            return ResponseEntity.ok(
                    ApiResponse.success("Data subjek pajak untuk kelurahan " + kelurahanWp + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }
}
