package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.PbjtAssessmentWithRealizationDTO;
import com.example.leaflet_geo.service.PbjtRealizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pbjt-realization")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PbjtRealizationController {
    
    private final PbjtRealizationService realizationService;
    
    @GetMapping
    public ResponseEntity<List<PbjtAssessmentWithRealizationDTO>> getAllWithRealization() {
        List<PbjtAssessmentWithRealizationDTO> results = realizationService.getAllAssessmentsWithRealization();
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PbjtAssessmentWithRealizationDTO> getByIdWithRealization(@PathVariable Long id) {
        PbjtAssessmentWithRealizationDTO result = realizationService.getAssessmentWithRealizationById(id);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/by-location")
    public ResponseEntity<List<PbjtAssessmentWithRealizationDTO>> getByLocation(
            @RequestParam String kecamatan,
            @RequestParam String kelurahan) {
        List<PbjtAssessmentWithRealizationDTO> results = realizationService.getAssessmentsByLocationWithRealization(kecamatan, kelurahan);
        return ResponseEntity.ok(results);
    }
}
