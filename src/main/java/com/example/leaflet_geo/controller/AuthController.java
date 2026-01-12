package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.dto.auth.ChangePasswordRequest;
import com.example.leaflet_geo.dto.auth.LoginRequest;
import com.example.leaflet_geo.dto.auth.LoginResponse;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 * 
 * Handles user authentication endpoints.
 * Matches legacy Yii 2 UserController endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login user
     * POST /api/auth/login
     * 
     * Matches legacy: POST /user/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse result = authService.login(request);
            return ResponseEntity.ok(
                    ApiResponse.success("Selamat Datang " + result.getNama(), result));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Gagal login: " + e.getMessage()));
        }
    }

    /**
     * Logout user
     * POST /api/auth/logout
     * 
     * Matches legacy: POST /user/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User currentUser) {
        try {
            authService.logout(currentUser);
            return ResponseEntity.ok(
                    ApiResponse.success("Logout berhasil", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Gagal logout: " + e.getMessage()));
        }
    }

    /**
     * Change password
     * POST /api/auth/changepass
     * 
     * Matches legacy: POST /user/changepass
     */
    @PostMapping("/changepass")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(currentUser, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Sandi berhasil diubah", null));
        } catch (Exception e) {
            return ResponseEntity.status(406).body(
                    ApiResponse.error("Gagal ubah sandi: " + e.getMessage()));
        }
    }

    /**
     * Create admin user
     * POST /api/auth/createadmin
     * 
     * Matches legacy: POST /user/createadmin
     */
    @PostMapping("/createadmin")
    public ResponseEntity<ApiResponse<Map<String, String>>> createAdmin(@RequestBody LoginRequest request) {
        try {
            User admin = authService.createAdmin(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("User admin created",
                            Map.of("id", admin.getId(), "username", admin.getUsername())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Gagal buat admin: " + e.getMessage()));
        }
    }

    /**
     * Get current user info
     * GET /api/auth/me
     * 
     * NEW endpoint (not in legacy)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        try {
            LoginResponse response = LoginResponse.builder()
                    .nama(currentUser.getNama())
                    .idUnit(currentUser.getIdUnit())
                    .role(currentUser.getRole())
                    .token(currentUser.getToken())
                    .build();
            return ResponseEntity.ok(
                    ApiResponse.success("User info retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Gagal ambil user info: " + e.getMessage()));
        }
    }
}
