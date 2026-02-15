package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.dto.AssessmentResponseDTO;
import com.example.leaflet_geo.entity.PbjtAssessment;
import com.example.leaflet_geo.service.PbjtAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pbjt-assessments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PbjtAssessmentController {
    
    private final PbjtAssessmentService assessmentService;
    
    @Value("${file.upload.dir:uploads/pbjt-images}")
    private String uploadDir;
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateAssessment(@Valid @RequestBody AssessmentRequestDTO request) {
        try {
            com.example.leaflet_geo.dto.CalculationResultDTO result = assessmentService.calculateAssessment(request);
            return ResponseEntity.ok(Map.of(
                "data", result,
                "success", true,
                "message", "Kalkulasi berhasil"
            ));
        } catch (Exception e) {
            log.error("Error calculating assessment", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAssessments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<PbjtAssessment> assessmentPage = assessmentService.getAllAssessments(page, size);
            
            // Extract Tax Object IDs for batch fetching
            List<String> taxObjectIds = assessmentPage.getContent().stream()
                .map(PbjtAssessment::getTaxObjectId)
                .collect(Collectors.toList());
            
            // Fetch realization history in one batch query (FAST)
            Map<String, List<com.example.leaflet_geo.dto.RealisasiDTO>> realizationMap = 
                assessmentService.getRealisasiHistoryMap(taxObjectIds);
            
            // Convert to DTO with pre-fetched history
            List<AssessmentResponseDTO> data = assessmentPage.getContent().stream()
                .map(a -> assessmentService.convertToResponseDTO(
                    a, 
                    realizationMap.getOrDefault(a.getTaxObjectId(), java.util.Collections.emptyList())
                ))
                .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                "data", data,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "totalElements", assessmentPage.getTotalElements(),
                    "totalPages", assessmentPage.getTotalPages(),
                    "hasNext", assessmentPage.hasNext(),
                    "hasPrev", assessmentPage.hasPrevious()
                ),
                "success", true,
                "message", "Data PBJT assessments berhasil diambil"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting assessments", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAssessmentById(@PathVariable Long id) {
        try {
            PbjtAssessment assessment = assessmentService.getAssessmentById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found with ID: " + id));
            
            AssessmentResponseDTO data = assessmentService.convertToResponseDTO(assessment);
            
            return ResponseEntity.ok(Map.of(
                "data", data,
                "success", true,
                "message", "Assessment berhasil diambil"
            ));
        } catch (Exception e) {
            log.error("Error getting assessment by ID: {}", id, e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/business/{businessId}")
    public ResponseEntity<Map<String, Object>> getAssessmentByBusinessId(@PathVariable String businessId) {
        try {
            PbjtAssessment assessment = assessmentService.getAssessmentByBusinessId(businessId)
                .orElseThrow(() -> new RuntimeException("Assessment not found for business ID: " + businessId));
            
            AssessmentResponseDTO data = assessmentService.convertToResponseDTO(assessment);
            
            return ResponseEntity.ok(Map.of(
                "data", data,
                "success", true,
                "message", "Assessment berhasil diambil"
            ));
        } catch (Exception e) {
            log.error("Error getting assessment by business ID: {}", businessId, e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/kabupaten/{kabupaten}")
    public ResponseEntity<Map<String, Object>> getAssessmentsByKabupaten(@PathVariable String kabupaten) {
        try {
            List<PbjtAssessment> assessments = assessmentService.getAssessmentsByKabupaten(kabupaten);
            
            List<AssessmentResponseDTO> data = assessments.stream()
                .map(assessmentService::convertToResponseDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "data", data,
                "success", true,
                "message", "Data assessments untuk kabupaten " + kabupaten + " berhasil diambil"
            ));
        } catch (Exception e) {
            log.error("Error getting assessments by kabupaten: {}", kabupaten, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/kecamatan/{kecamatan}")
    public ResponseEntity<Map<String, Object>> getAssessmentsByKecamatan(@PathVariable String kecamatan) {
        try {
            List<PbjtAssessment> assessments = assessmentService.getAssessmentsByKecamatan(kecamatan);
            
            List<AssessmentResponseDTO> data = assessments.stream()
                .map(assessmentService::convertToResponseDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "data", data,
                "success", true,
                "message", "Data assessments untuk kecamatan " + kecamatan + " berhasil diambil"
            ));
        } catch (Exception e) {
            log.error("Error getting assessments by kecamatan: {}", kecamatan, e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAssessment(@Valid @RequestBody AssessmentRequestDTO request) {
        try {
            PbjtAssessment assessment = assessmentService.createAssessment(request);
            AssessmentResponseDTO data = assessmentService.convertToResponseDTO(assessment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", data,
                "success", true,
                "message", "Assessment berhasil dibuat"
            ));
        } catch (Exception e) {
            log.error("Error creating assessment", e);
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AssessmentRequestDTO request) {
        try {
            PbjtAssessment assessment = assessmentService.updateAssessment(id, request);
            AssessmentResponseDTO data = assessmentService.convertToResponseDTO(assessment);
            
            return ResponseEntity.ok(Map.of(
                "data", data,
                "success", true,
                "message", "Assessment berhasil diupdate"
            ));
        } catch (Exception e) {
            log.error("Error updating assessment: {}", id, e);
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAssessment(@PathVariable Long id) {
        try {
            assessmentService.deleteAssessment(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Assessment berhasil dihapus"
            ));
        } catch (Exception e) {
            log.error("Error deleting assessment: {}", id, e);
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            long count = assessmentService.getAllAssessments(0, Integer.MAX_VALUE).getTotalElements();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Count berhasil diambil",
                "totalCount", count
            ));
        } catch (Exception e) {
            log.error("Error getting count", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            long count = assessmentService.getAllAssessments(0, 1).getTotalElements();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "PBJT Assessment API is running",
                "totalRecords", count
            ));
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Health check failed: " + e.getMessage()
            ));
        }
    }
    
    // Map statistics endpoints
    @GetMapping("/stats/by-kecamatan")
    public ResponseEntity<List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO>> getStatsByKecamatan() {
        try {
            List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO> stats = assessmentService.getStatsByKecamatan();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting stats by kecamatan", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/stats/by-kelurahan/{kecamatan}")
    public ResponseEntity<List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO>> getStatsByKelurahan(@PathVariable String kecamatan) {
        try {
            List<com.example.leaflet_geo.dto.PbjtLocationStatsDTO> stats = assessmentService.getStatsByKelurahan(kecamatan);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting stats by kelurahan for kecamatan: {}", kecamatan, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/by-location")
    public ResponseEntity<List<PbjtAssessment>> getAssessmentsByLocation(
            @RequestParam String kecamatan,
            @RequestParam String kelurahan) {
        try {
            List<PbjtAssessment> assessments = assessmentService.getAssessmentsByLocation(kecamatan, kelurahan);
            return ResponseEntity.ok(assessments);
        } catch (Exception e) {
            log.error("Error getting assessments by location: {}, {}", kecamatan, kelurahan, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Upload business images
     * Max 10 images, 5MB per file
     */
    @PostMapping("/upload-images")
    public ResponseEntity<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files) {
        try {
            // Validate file count
            if (files.length > 10) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Maksimal 10 gambar"
                ));
            }
            
            // Create upload directory if not exists
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
                log.info("Created upload directory: {}", uploadDir);
            }
            
            List<String> uploadedUrls = new ArrayList<>();
            
            for (MultipartFile file : files) {
                // Validate file
                if (file.isEmpty()) {
                    continue;
                }
                
                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "File " + file.getOriginalFilename() + " bukan gambar"
                    ));
                }
                
                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Save file
                Path filePath = Paths.get(uploadDir, uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Store relative URL
                String fileUrl = "/uploads/pbjt-images/" + uniqueFilename;
                uploadedUrls.add(fileUrl);
                
                log.info("Uploaded image: {} -> {}", originalFilename, fileUrl);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Gambar berhasil diupload",
                "urls", uploadedUrls
            ));
            
        } catch (IOException e) {
            log.error("Error uploading images", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error uploading images: " + e.getMessage()
            ));
        }
    }
}
