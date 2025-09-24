package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.entity.DatObjekPajak;
import com.example.leaflet_geo.entity.DatSubjekPajak;
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
                "hasPrev", hasPrev
            ));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            int count = datObjekPajakRepository.count();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Count berhasil diambil",
                "totalCount", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{kdBlok}/{noUrut}/{kdJnsOp}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakById(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan,
            @PathVariable String kdBlok,
            @PathVariable String noUrut,
            @PathVariable String kdJnsOp) {
        try {
            // Get objek pajak with reference data
            Optional<DatObjekPajak> objekPajak = datObjekPajakRepository.findByIdWithReferences(
                kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, kdBlok, noUrut, kdJnsOp
            );

            if (objekPajak.isPresent()) {
                // Get subjek pajak data with reference data if available
                DatSubjekPajak subjekPajak = null;
                if (objekPajak.get().getSubjekPajakId() != null) {
                    Optional<DatSubjekPajak> subjekPajakOpt = datSubjekPajakRepository.findByIdWithReferences(objekPajak.get().getSubjekPajakId());
                    if (subjekPajakOpt.isPresent()) {
                        subjekPajak = subjekPajakOpt.get();
                    }
                }

                Map<String, Object> response = new HashMap<>();
                response.put("objekPajak", objekPajak.get());
                response.put("subjekPajak", subjekPajak);
                response.put("success", true);
                response.put("message", "Data objek pajak berhasil diambil");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Data objek pajak tidak ditemukan"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/kecamatan/{kdPropinsi}/{kdDati2}/{kdKecamatan}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakByKecamatan(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int offset = page * size;
            List<DatObjekPajak> data = datObjekPajakRepository.findPaginatedByKecamatan(kdPropinsi, kdDati2, kdKecamatan, offset, size);
            int totalCount = datObjekPajakRepository.countByKecamatan(kdPropinsi, kdDati2, kdKecamatan);

            int totalPages = (int) Math.ceil((double) totalCount / size);
            boolean hasNext = page < totalPages - 1;
            boolean hasPrev = page > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of(
                "kdPropinsi", kdPropinsi,
                "kdDati2", kdDati2,
                "kdKecamatan", kdKecamatan
            ));
            response.put("pagination", Map.of(
                "page", page,
                "size", size,
                "totalElements", totalCount,
                "totalPages", totalPages,
                "hasNext", hasNext,
                "hasPrev", hasPrev
            ));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

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
            List<DatObjekPajak> data = datObjekPajakRepository.findPaginatedByKelurahan(kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, offset, size);
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
                "kdKelurahan", kdKelurahan
            ));
            response.put("pagination", Map.of(
                "page", page,
                "size", size,
                "totalElements", totalCount,
                "totalPages", totalPages,
                "hasNext", hasNext,
                "hasPrev", hasPrev
            ));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/detail/{kdPropinsi}/{kdDati2}/{kdKecamatan}/{kdKelurahan}/{noUrut}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakDetail(
            @PathVariable String kdPropinsi,
            @PathVariable String kdDati2,
            @PathVariable String kdKecamatan,
            @PathVariable String kdKelurahan,
            @PathVariable String noUrut) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByKecamatanAndKelurahanAndNoUrut(
                kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, noUrut
            );

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of(
                "kdPropinsi", kdPropinsi,
                "kdDati2", kdDati2,
                "kdKecamatan", kdKecamatan,
                "kdKelurahan", kdKelurahan,
                "noUrut", noUrut
            ));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/subjek-pajak/{subjekPajakId}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakBySubjekPajakId(@PathVariable String subjekPajakId) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findBySubjekPajakId(subjekPajakId);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("subjekPajakId", subjekPajakId));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/npwp/{npwp}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakByNpwp(@PathVariable String npwp) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByNpwp(npwp);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("npwp", npwp));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/no-formulir-spop/{noFormulirSpop}")
    public ResponseEntity<Map<String, Object>> getDatObjekPajakByNoFormulirSpop(@PathVariable String noFormulirSpop) {
        try {
            List<DatObjekPajak> data = datObjekPajakRepository.findByNoFormulirSpop(noFormulirSpop);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("noFormulirSpop", noFormulirSpop));
            response.put("success", true);
            response.put("message", "Data objek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
}
