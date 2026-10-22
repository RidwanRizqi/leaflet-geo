package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.dto.attendance.*;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Absen Masuk (Check-In)
     * POST /api/attendance/check-in
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> checkIn(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AttendanceCheckInRequest request,
            HttpServletRequest servletRequest) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            String clientIp = getClientIp(servletRequest);
            AttendanceResponseDTO result = attendanceService.checkIn(currentUser, request, clientIp);
            return ResponseEntity.ok(ApiResponse.success("Absen masuk berhasil dicatat!", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Absen Pulang (Check-Out)
     * POST /api/attendance/check-out
     */
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> checkOut(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AttendanceCheckOutRequest request,
            HttpServletRequest servletRequest) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            String clientIp = getClientIp(servletRequest);
            AttendanceResponseDTO result = attendanceService.checkOut(currentUser, request, clientIp);
            return ResponseEntity.ok(ApiResponse.success("Absen pulang berhasil dicatat!", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Status absensi hari ini (User Login)
     * GET /api/attendance/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceStatusTodayDTO>> getTodayStatus(
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            AttendanceStatusTodayDTO result = attendanceService.getTodayStatus(currentUser);
            return ResponseEntity.ok(ApiResponse.success("Status absensi hari ini berhasil diambil", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal mengambil status absensi: " + e.getMessage()));
        }
    }

    /**
     * Riwayat absensi user login
     * GET /api/attendance/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDTO>>> getMyHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            List<AttendanceResponseDTO> result = attendanceService.getMyHistory(currentUser, start, end);
            return ResponseEntity.ok(ApiResponse.success("Riwayat absensi berhasil diambil", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal mengambil riwayat: " + e.getMessage()));
        }
    }

    /**
     * List kehadiran untuk Admin
     * GET /api/attendance/admin/list
     */
    @GetMapping("/admin/list")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDTO>>> getAdminList(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal,
            @RequestParam(required = false) String role) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            // Validasi role admin
            if (!Boolean.TRUE.equals(currentUser.getIsAdmin()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Akses ditolak: Hanya admin yang diizinkan"));
            }

            List<AttendanceResponseDTO> list = attendanceService.getAdminList(tanggal, role);
            return ResponseEntity.ok(ApiResponse.success("Daftar kehadiran berhasil diambil", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal mengambil data admin: " + e.getMessage()));
        }
    }

    /**
     * Summary statistik kehadiran untuk Admin
     * GET /api/attendance/admin/summary
     */
    @GetMapping("/admin/summary")
    public ResponseEntity<ApiResponse<AttendanceSummaryDTO>> getAdminSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            if (!Boolean.TRUE.equals(currentUser.getIsAdmin()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Akses ditolak: Hanya admin yang diizinkan"));
            }

            AttendanceSummaryDTO summary = attendanceService.getAdminSummary(tanggal);
            return ResponseEntity.ok(ApiResponse.success("Summary absensi berhasil diambil", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal mengambil summary: " + e.getMessage()));
        }
    }

    /**
     * Data lokasi absensi untuk peta Leaflet Admin
     * GET /api/attendance/admin/locations
     */
    @GetMapping("/admin/locations")
    public ResponseEntity<ApiResponse<List<AttendanceUserLocationDTO>>> getAdminLocations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggal) {
        try {
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("User belum login"));
            }

            if (!Boolean.TRUE.equals(currentUser.getIsAdmin()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Akses ditolak: Hanya admin yang diizinkan"));
            }

            List<AttendanceUserLocationDTO> locations = attendanceService.getAdminLocations(tanggal);
            return ResponseEntity.ok(ApiResponse.success("Data lokasi absensi berhasil diambil", locations));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Gagal mengambil lokasi: " + e.getMessage()));
        }
    }

    /**
     * Download/View foto selfie absensi
     * GET /api/attendance/photo/{filename}
     */
    @GetMapping("/photo/{filename:.+}")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) {
        try {
            Resource resource = attendanceService.getPhotoResource(filename);
            if (resource == null || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper ekstrak IP client
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
