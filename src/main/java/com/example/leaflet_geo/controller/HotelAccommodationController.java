package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.service.HotelAccommodationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for hotel/accommodation (PBJT Jasa Perhotelan) data.
 * 
 * Architecture:
 * - CRUD operations work on LOCAL PostgreSQL database
 * - Sync endpoint imports data FROM SIMATDA MySQL (read-only, never modifies SIMATDA)
 */
@RestController
@RequestMapping("/api/hotel-accommodations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotelAccommodationController {

    private final HotelAccommodationService hotelService;

    /**
     * Get all hotel accommodations from LOCAL database
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHotels(
            @RequestParam(required = false) String search) {

        List<Map<String, Object>> hotels = hotelService.getAllHotels();

        // Apply search filter if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            hotels = hotels.stream()
                    .filter(h -> {
                        String name = h.get("property_name") != null ? h.get("property_name").toString().toLowerCase() : "";
                        String address = h.get("address") != null ? h.get("address").toString().toLowerCase() : "";
                        String owner = h.get("owner_name") != null ? h.get("owner_name").toString().toLowerCase() : "";
                        return name.contains(searchLower) || address.contains(searchLower) || owner.contains(searchLower);
                    })
                    .toList();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", hotels);
        response.put("total", hotels.size());
        response.put("message", "Hotel accommodations retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get hotel by ID from LOCAL database
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHotelById(@PathVariable long id) {
        Map<String, Object> hotel = hotelService.getHotelById(id);

        if (hotel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", "Hotel not found with ID: " + id));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", hotel);
        response.put("message", "Hotel retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Create hotel in LOCAL database
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createHotel(@RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> created = hotelService.createHotel(data);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", created);
            response.put("message", "Hotel created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update hotel in LOCAL database
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHotel(
            @PathVariable long id, @RequestBody Map<String, Object> data) {
        try {
            Map<String, Object> updated = hotelService.updateHotel(id, data);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "Hotel updated successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Delete hotel from LOCAL database
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHotel(@PathVariable long id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Hotel deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get dashboard metrics (aggregated stats from LOCAL database)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = hotelService.getDashboardMetrics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", metrics);
        response.put("message", "Dashboard metrics retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get all hotels with their realization data from LOCAL database
     */
    @GetMapping("/with-realization")
    public ResponseEntity<Map<String, Object>> getAllHotelsWithRealization() {
        List<Map<String, Object>> hotels = hotelService.getAllHotelsWithRealization();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", hotels);
        response.put("total", hotels.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get realization data for a specific hotel from LOCAL database
     */
    @GetMapping("/{id}/realization")
    public ResponseEntity<Map<String, Object>> getHotelRealization(@PathVariable long id) {
        List<Map<String, Object>> realization = hotelService.getHotelRealization(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", realization);

        return ResponseEntity.ok(response);
    }

    /**
     * Sync hotel data FROM SIMATDA → LOCAL database.
     * This is READ-ONLY on SIMATDA — it only reads from SIMATDA and writes to local.
     * Call this to import/refresh hotel data from the government system.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncFromSimatda() {
        Map<String, Object> result = hotelService.syncFromSimatda();

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.getOrDefault("success", false));
        response.put("data", result);
        response.put("message", result.getOrDefault("message", "Sync completed"));

        return ResponseEntity.ok(response);
    }

    /**
     * Upload hotel images
     */
    @PostMapping("/upload-images")
    public ResponseEntity<Map<String, Object>> uploadHotelImages(
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<String> uploadedUrls = new ArrayList<>();
            String uploadDir = "uploads/hotel-images/";

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            if (files.length > 4) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "Maximum 4 images allowed"));
            }

            for (MultipartFile file : files) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                uploadedUrls.add("/uploads/hotel-images/" + fileName);
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
}
