package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.model.DatObjekPajak;
import com.example.leaflet_geo.model.DatSubjekPajak;
import com.example.leaflet_geo.repository.DatObjekPajakRepository;
import com.example.leaflet_geo.repository.DatSubjekPajakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dat-objek-pajak")
@CrossOrigin(origins = "*")
public class DatObjekPajakController {

    @Autowired
    private DatObjekPajakRepository datObjekPajakRepository;

    @Autowired
    private DatSubjekPajakRepository datSubjekPajakRepository;

    // ============================================
    // PAGINATION ENDPOINTS (Keep HashMap - has extra fields)
    // ============================================

    /**
     * Get all objek pajak with pagination
     * GET /api/dat-objek-pajak?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDatObjekPajak(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<DatObjekPajak> data = datObjekPajakRepository.findPaginated(offset, size);
            int totalCount = datObjekPajakRepository.count();

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
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get count of objek pajak
     * GET /api/dat-objek-pajak/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCount() {
        try {
            int count = datObjekPajakRepository.count();
            return ResponseEntity.ok(
                    ApiResponse.success("Count berhasil diambil", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by ID
     * GET
     * /api/dat-objek-pajak/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{kdBlok}/{noUrut}/{kdJnsOp}
     */
    @GetMapping("/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{kdBlok}/{noUrut}/{kdJnsOp}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatObjekPajakById(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan,
            @PathVariable String kdBlok,
            @PathVariable String noUrut,
            @PathVariable String kdJnsOp) {
        try {
            Optional<DatObjekPajak> objekPajak = datObjekPajakRepository.findByIdWithReferences(
                    kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, kdBlok, noUrut, kdJnsOp);

            if (objekPajak.isPresent()) {
                DatSubjekPajak subjekPajak = null;
                if (objekPajak.get().getSubjekPajakId() != null) {
                    Optional<DatSubjekPajak> subjekPajakOpt = datSubjekPajakRepository
                            .findByIdWithReferences(objekPajak.get().getSubjekPajakId());
                    if (subjekPajakOpt.isPresent()) {
                        subjekPajak = subjekPajakOpt.get();
                    }
                }

                Map<String, Object> data = new HashMap<>();
                data.put("objekPajak", objekPajak.get());
                data.put("subjekPajak", subjekPajak);

                return ResponseEntity.ok(
                        ApiResponse.success("Data objek pajak berhasil diambil", data));
            } else {
                return ResponseEntity.status(404).body(
                        ApiResponse.error("Data objek pajak tidak ditemukan"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by kecamatan with pagination
     * GET
     * /api/dat-objek-pajak/kecamatan/{kdPropinsi}/{kdDati2}/{kdKecamatan}?page=0&size=10
     */
    @GetMapping("/kecamatan/{kdPropinsi}/{kdDati2}/{kdKecamatan}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakByKecamatan(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<DatObjekPajak> data = datObjekPajakRepository.findPaginatedByKecamatan(kdPropinsi, kdDati2,
                    kdKecamatan, offset, size);
            int totalCount = datObjekPajakRepository.countByKecamatan(kdPropinsi, kdDati2, kdKecamatan);

            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of(
                    "kdPropinsi", kdPropinsi,
                    "kdDati2", kdDati2,
                    "kdKecamatan", kdKecamatan));
            response.put("pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", totalPages,
                    "hasNext", hasNext,
                    "hasPrev", hasPrev));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by kelurahan with pagination
     * GET
     * /api/dat-objek-pajak/kelurahan/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}?page=0&size=10
     */
    @GetMapping("/kelurahan/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakByKelurahan(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<DatObjekPajak> data = datObjekPajakRepository.findPaginatedByKelurahan(kdPropinsi, kdDati2,
                    kdKecamatan, kdKelurahan, offset, size);
            int totalCount = datObjekPajakRepository.countByKelurahan(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan);

            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of(
                    "kdPropinsi", kdPropinsi,
                    "kdDati2", kdDati2,
                    "kdKecamatan", kdKecamatan,
                    "kdKelurahan", kdKelurahan));
            response.put("pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", totalCount,
                    "totalPages", totalPages,
                    "hasNext", hasNext,
                    "hasPrev", hasPrev));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak detail by kecamatan, kelurahan, and noUrut
     * GET
     * /api/dat-objek-pajak/detail/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{noUrut}
     */
    @GetMapping("/detail/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{noUrut}")
    public ResponseEntity<ApiResponse<List<DatObjekPajak>>> getDatObjekPajakDetail(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan,
            @PathVariable String noUrut) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByKecamatanAndKelurahanAndNoUrut(
                    kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, noUrut);
            return ResponseEntity.ok(
                    ApiResponse.success("Data objek pajak berhasil diambil", data, (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by subjek pajak ID
     * GET /api/dat-objek-pajak/subjek-pajak/{subjekPajakId}
     */
    @GetMapping("/subjek-pajak/{subjekPajakId}")
    public ResponseEntity<ApiResponse<List<DatObjekPajak>>> getDatObjekPajakBySubjekPajakId(
            @PathVariable String subjekPajakId) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findBySubjekPajakId(subjekPajakId);
            return ResponseEntity.ok(
                    ApiResponse.success("Data objek pajak untuk subjek " + subjekPajakId + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by NPWP
     * GET /api/dat-objek-pajak/npwp/{npwp}
     */
    @GetMapping("/npwp/{npwp}")
    public ResponseEntity<ApiResponse<List<DatObjekPajak>>> getDatObjekPajakByNpwp(@PathVariable String npwp) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByNpwp(npwp);
            return ResponseEntity.ok(
                    ApiResponse.success("Data objek pajak untuk NPWP " + npwp + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * Get objek pajak by No Formulir SPOP
     * GET /api/dat-objek-pajak/no-formulir-spop/{noFormulirSpop}
     */
    @GetMapping("/no-formulir-spop/{noFormulirSpop}")
    public ResponseEntity<ApiResponse<List<DatObjekPajak>>> getDatObjekPajakByNoFormulirSpop(
            @PathVariable String noFormulirSpop) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByNoFormulirSpop(noFormulirSpop);
            return ResponseEntity.ok(
                    ApiResponse.success("Data objek pajak untuk formulir " + noFormulirSpop + " berhasil diambil", data,
                            (long) data.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Error: " + e.getMessage()));
        }
    }
}
