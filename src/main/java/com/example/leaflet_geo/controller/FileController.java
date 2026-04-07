package com.example.leaflet_geo.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;

/**
 * Serves uploaded files from the uploads/ directory.
 * Uses /api/file/ path so it's covered by existing permitAll() rules.
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    // Absolute path to the uploads directory (parent of leaflet-geo)
    private static final String UPLOAD_BASE = "/media/zpreoz/New Volume/College/POLINEMA/PROJECT/PRODUCTION/gis-tax-refactoring/uploads";

    @GetMapping("/hotel-images/{filename:.+}")
    public ResponseEntity<Resource> serveHotelImage(@PathVariable String filename) {
        try {
            File file = new File(UPLOAD_BASE + "/hotel-images/" + filename);
            if (!file.exists() || !file.isFile()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
