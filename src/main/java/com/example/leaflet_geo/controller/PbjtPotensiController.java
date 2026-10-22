package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.PbjtPotensiDTO;
import com.example.leaflet_geo.dto.PbjtPotensiObservationDTO;
import com.example.leaflet_geo.service.PbjtPotensiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pbjt-potensi")
@Slf4j
public class PbjtPotensiController {

    private final PbjtPotensiService potensiService;

    public PbjtPotensiController(PbjtPotensiService potensiService) {
        this.potensiService = potensiService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPotensiList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "surveyorId", required = false) String surveyorId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Page<PbjtPotensiDTO> resultPage = potensiService.searchPotensi(search, surveyorId, page, size);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", resultPage.getContent());
            response.put("totalElements", resultPage.getTotalElements());
            response.put("totalPages", resultPage.getTotalPages());
            response.put("currentPage", resultPage.getNumber());
            response.put("pageSize", resultPage.getSize());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching potensi list", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPotensiById(@PathVariable("id") Long id) {
        try {
            PbjtPotensiDTO dto = potensiService.getPotensiById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", dto
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error fetching potensi by id: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/generate-kode")
    public ResponseEntity<Map<String, Object>> getGeneratedKode(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String kode = potensiService.generateKodePotensi(date != null ? date : LocalDate.now());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "kodePotensi", kode
            ));
        } catch (Exception e) {
            log.error("Error generating kode potensi", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> getPotensiForExport(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "surveyorId", required = false) String surveyorId) {
        try {
            List<PbjtPotensiDTO> list = potensiService.getPotensiForExport(startDate, endDate, surveyorId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", list,
                    "total", list.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching export data", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPotensi(
            @RequestBody PbjtPotensiDTO dto,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        try {
            String effectiveUser = (username != null && !username.trim().isEmpty()) ? username : dto.getUpdatedBy();
            PbjtPotensiDTO created = potensiService.createPotensi(dto, effectiveUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", created,
                    "message", "Data Potensi PBJT berhasil disimpan"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating potensi", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePotensi(
            @PathVariable("id") Long id,
            @RequestBody PbjtPotensiDTO dto,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        try {
            String effectiveUser = (username != null && !username.trim().isEmpty()) ? username : dto.getUpdatedBy();
            PbjtPotensiDTO updated = potensiService.updatePotensi(id, dto, effectiveUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", updated,
                    "message", "Data Potensi PBJT berhasil diperbarui"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating potensi: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePotensi(@PathVariable("id") Long id) {
        try {
            potensiService.deletePotensi(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Data Potensi PBJT berhasil dihapus"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting potensi: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/menus-by-name")
    public ResponseEntity<Map<String, Object>> getMenusByName(@RequestParam("name") String name) {
        try {
            Map<String, Object> result = potensiService.getMenusByBusinessName(name);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching menus by name: {}", name, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/business-names")
    public ResponseEntity<Map<String, Object>> getBusinessNames() {
        try {
            List<String> names = potensiService.getAllBusinessNames();
            return ResponseEntity.ok(Map.of("success", true, "data", names));
        } catch (Exception e) {
            log.error("Error fetching business names", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ================= ENDPOINTS OBSERVASI POTENSI =================

    @GetMapping("/{id}/observations")
    public ResponseEntity<Map<String, Object>> getObservations(@PathVariable("id") Long id) {
        try {
            List<PbjtPotensiObservationDTO> list = potensiService.getObservations(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", list,
                    "total", list.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching observations for potensi id: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/{id}/observations")
    public ResponseEntity<Map<String, Object>> addObservation(
            @PathVariable("id") Long id,
            @RequestBody PbjtPotensiObservationDTO dto,
            @RequestHeader(value = "X-User-Name", required = false) String username) {
        try {
            String effectiveUser = (username != null && !username.trim().isEmpty()) ? username : dto.getPetugas();
            PbjtPotensiObservationDTO saved = potensiService.addObservation(id, dto, effectiveUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", saved,
                    "message", "Observasi potensi berhasil disimpan dan potensi pajak telah diperbarui."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error adding observation for potensi id: {}", id, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/observations/{obsId}")
    public ResponseEntity<Map<String, Object>> deleteObservation(@PathVariable("obsId") Long obsId) {
        try {
            potensiService.deleteObservation(obsId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Observasi potensi berhasil dihapus dan potensi pajak telah dihitung ulang."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error deleting observation id: {}", obsId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
}
