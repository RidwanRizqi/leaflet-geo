package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.dto.AssessmentResponseDTO;
import com.example.leaflet_geo.dto.CalculationResultDTO;
import com.example.leaflet_geo.dto.PbjtLocationStatsDTO;
import com.example.leaflet_geo.dto.RealisasiDTO;
import com.example.leaflet_geo.model.PbjtAssessment;
import com.example.leaflet_geo.service.PbjtAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pbjt-assessments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PbjtAssessmentController {

    private final PbjtAssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {

        Page<PbjtAssessment> assessmentPage = assessmentService.getAllAssessments(page, size, category);
        List<PbjtAssessment> assessments = assessmentPage.getContent();

        // Batch fetch realization data
        List<String> taxObjectIds = assessments.stream()
                .map(PbjtAssessment::getTaxObjectId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<RealisasiDTO>> realisasiMap = assessmentService.getRealisasiHistoryMap(taxObjectIds);

        List<AssessmentResponseDTO> data = assessments.stream()
                .map(a -> {
                    String taxObjId = a.getTaxObjectId();
                    List<RealisasiDTO> history = realisasiMap.getOrDefault(taxObjId, Collections.emptyList());
                    return assessmentService.convertToResponseDTO(a, history);
                })
                .collect(Collectors.toList());

        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            data = data.stream()
                    .filter(a -> {
                        String name = a.getBusinessName() != null ? a.getBusinessName().toLowerCase() : "";
                        String addr = a.getAddress() != null ? a.getAddress().toLowerCase() : "";
                        String bId = a.getBusinessId() != null ? a.getBusinessId().toLowerCase() : "";
                        return name.contains(searchLower) || addr.contains(searchLower) || bId.contains(searchLower);
                    })
                    .collect(Collectors.toList());
        }

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", assessmentPage.getNumber());
        pagination.put("size", assessmentPage.getSize());
        pagination.put("totalElements", assessmentPage.getTotalElements());
        pagination.put("totalPages", assessmentPage.getTotalPages());
        pagination.put("hasNext", assessmentPage.hasNext());
        pagination.put("hasPrev", assessmentPage.hasPrevious());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("pagination", pagination);
        response.put("message", "Assessments retrieved successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAssessmentById(@PathVariable Long id) {
        Optional<PbjtAssessment> assessmentOpt = assessmentService.getAssessmentById(id);

        if (assessmentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Assessment not found with ID: " + id));
        }

        AssessmentResponseDTO dto = assessmentService.convertToResponseDTO(assessmentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dto);
        response.put("message", "Assessment retrieved successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<Map<String, Object>> getAssessmentByBusinessId(@PathVariable String businessId) {
        Optional<PbjtAssessment> assessmentOpt = assessmentService.getAssessmentByBusinessId(businessId);

        if (assessmentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Assessment not found for business: " + businessId));
        }

        AssessmentResponseDTO dto = assessmentService.convertToResponseDTO(assessmentOpt.get());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", dto);
        response.put("message", "Assessment retrieved successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/kabupaten/{kabupaten}")
    public ResponseEntity<Map<String, Object>> getAssessmentsByKabupaten(@PathVariable String kabupaten) {
        List<PbjtAssessment> assessments = assessmentService.getAssessmentsByKabupaten(kabupaten);
        List<AssessmentResponseDTO> data = assessments.stream()
                .map(assessmentService::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "message", "Assessments for kabupaten: " + kabupaten));
    }

    @GetMapping("/kecamatan/{kecamatan}")
    public ResponseEntity<Map<String, Object>> getAssessmentsByKecamatan(@PathVariable String kecamatan) {
        List<PbjtAssessment> assessments = assessmentService.getAssessmentsByKecamatan(kecamatan);
        List<AssessmentResponseDTO> data = assessments.stream()
                .map(assessmentService::convertToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "message", "Assessments for kecamatan: " + kecamatan));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssessment(
            @RequestBody AssessmentRequestDTO request) {
        try {
            PbjtAssessment assessment = assessmentService.createAssessment(request);
            AssessmentResponseDTO dto = assessmentService.convertToResponseDTO(assessment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            response.put("message", "Assessment created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAssessment(
            @PathVariable Long id,
            @RequestBody AssessmentRequestDTO request) {
        try {
            PbjtAssessment assessment = assessmentService.updateAssessment(id, request);
            AssessmentResponseDTO dto = assessmentService.convertToResponseDTO(assessment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", dto);
            response.put("message", "Assessment updated successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssessment(@PathVariable Long id) {
        try {
            assessmentService.deleteAssessment(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Assessment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Failed to delete assessment: " + e.getMessage()));
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateAssessment(
            @RequestBody AssessmentRequestDTO request) {
        CalculationResultDTO result = assessmentService.calculateAssessment(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", result);
        response.put("message", "Calculation completed");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            long count = assessmentService.getAllAssessments(0, 1).getTotalElements();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Count berhasil diambil",
                    "totalCount", count));
        } catch (Exception e) {
            log.error("Error getting count", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PBJT Assessment Service",
                "timestamp", java.time.LocalDateTime.now().toString()));
    }

    @PostMapping("/upload-images")
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> uploadedUrls = new ArrayList<>();
            String uploadDir = "uploads/pbjt-images/";

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (files.length > 4) {
                    return ResponseEntity.badRequest().body(
                            Map.of("success", false, "message", "Maximum 4 images allowed"));
                }

                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                uploadedUrls.add("/uploads/pbjt-images/" + fileName);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "urls", uploadedUrls,
                    "message", "Images uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("success", false, "message", "Upload failed: " + e.getMessage()));
        }
    }

    // Map statistics endpoints
    @GetMapping("/stats/by-kecamatan")
    public ResponseEntity<List<PbjtLocationStatsDTO>> getStatsByKecamatan(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(assessmentService.getStatsByKecamatan(category));
    }

    @GetMapping("/stats/by-kelurahan/{kecamatan}")
    public ResponseEntity<List<PbjtLocationStatsDTO>> getStatsByKelurahan(
            @PathVariable String kecamatan,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(assessmentService.getStatsByKelurahan(kecamatan, category));
    }

    @GetMapping("/by-location")
    public ResponseEntity<List<AssessmentResponseDTO>> getByLocation(
            @RequestParam String kecamatan,
            @RequestParam String kelurahan,
            @RequestParam(required = false) String category) {
        List<PbjtAssessment> assessments = assessmentService.getAssessmentsByLocation(kecamatan, kelurahan, category);
        List<AssessmentResponseDTO> data = assessments.stream()
                .map(assessmentService::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }
}
