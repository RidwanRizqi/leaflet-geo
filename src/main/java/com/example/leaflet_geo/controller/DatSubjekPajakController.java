package com.example.leaflet_geo.controller;

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
                "hasPrev", hasPrev
            ));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");

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
            int count = datSubjekPajakRepository.count();
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

    @GetMapping("/{subjekPajakId}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakById(@PathVariable String subjekPajakId) {
        try {
            Optional<DatSubjekPajak> subjekPajak = datSubjekPajakRepository.findById(subjekPajakId);

            if (subjekPajak.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("data", subjekPajak.get());
                response.put("success", true);
                response.put("message", "Data subjek pajak berhasil diambil");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Data subjek pajak tidak ditemukan"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/npwp/{npwp}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakByNpwp(@PathVariable String npwp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNpwp(npwp);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("npwp", npwp));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/npwpd/{npwpd}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakByNpwpd(@PathVariable String npwpd) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNpwpd(npwpd);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("npwpd", npwpd));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<Map<String, Object>> searchDatSubjekPajakByName(@RequestParam String nmWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByNmWpContaining(nmWp);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("nmWp", nmWp));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakByEmail(@PathVariable String email) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("email", email));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/kota/{kotaWp}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakByKota(@PathVariable String kotaWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByKotaWp(kotaWp);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("kotaWp", kotaWp));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
            response.put("totalCount", data.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/kelurahan/{kelurahanWp}")
    public ResponseEntity<Map<String, Object>> getDatSubjekPajakByKelurahan(@PathVariable String kelurahanWp) {
        try {
            List<DatSubjekPajak> data = datSubjekPajakRepository.findByKelurahanWp(kelurahanWp);

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);
            response.put("filters", Map.of("kelurahanWp", kelurahanWp));
            response.put("success", true);
            response.put("message", "Data subjek pajak berhasil diambil");
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
