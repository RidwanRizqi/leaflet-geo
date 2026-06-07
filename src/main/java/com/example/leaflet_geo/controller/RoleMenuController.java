package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import com.example.leaflet_geo.dto.RoleMenuDTO;
import com.example.leaflet_geo.dto.RoleMenuUpdateRequest;
import com.example.leaflet_geo.model.User;
import com.example.leaflet_geo.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-menu")
@CrossOrigin(origins = "*")
public class RoleMenuController {

    @Autowired
    private RoleMenuService roleMenuService;

    /**
     * Get all role-menu mappings (for admin management page)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RoleMenuDTO>>> getAllRoleMenus() {
        try {
            List<RoleMenuDTO> data = roleMenuService.getAllRoleMenus();
            return ResponseEntity.ok(ApiResponse.success("Data role-menu berhasil diambil", data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal mengambil data: " + e.getMessage()));
        }
    }

    /**
     * Get menu IDs for a specific role
     */
    @GetMapping("/role/{roleName}")
    public ResponseEntity<ApiResponse<List<Integer>>> getMenuByRole(@PathVariable String roleName) {
        try {
            List<Integer> menuIds = roleMenuService.getMenuIdsByRole(roleName.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Menu IDs for role " + roleName, menuIds));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal mengambil data: " + e.getMessage()));
        }
    }

    /**
     * Get permitted menu IDs for the current logged-in user
     * Used by frontend to filter sidebar
     */
    @GetMapping("/my-permissions")
    public ResponseEntity<ApiResponse<List<Integer>>> getMyPermissions(
            @AuthenticationPrincipal User currentUser) {
        try {
            if (currentUser == null) {
                return ResponseEntity.ok(ApiResponse.success("Not logged in", List.of()));
            }

            // Admin gets all menus (null = all)
            if (Boolean.TRUE.equals(currentUser.getIsAdmin())) {
                return ResponseEntity.ok(ApiResponse.success("Admin has all permissions", null));
            }

            List<Integer> menuIds = roleMenuService.getMenuIdsForCurrentUser(currentUser.getRole());
            return ResponseEntity.ok(ApiResponse.success("Menu permissions berhasil diambil", menuIds));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal mengambil permissions: " + e.getMessage()));
        }
    }

    /**
     * Get distinct role names from existing mappings
     */
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<String>>> getAllRoles() {
        try {
            List<String> roles = roleMenuService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success("Roles berhasil diambil", roles));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal mengambil roles: " + e.getMessage()));
        }
    }

    /**
     * Update role-menu mappings (assign which menus a role can access)
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateRoleMenu(@RequestBody RoleMenuUpdateRequest request) {
        try {
            roleMenuService.updateRoleMenu(request);
            return ResponseEntity.ok(ApiResponse.success("Mapping role-menu berhasil diupdate", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal update mapping: " + e.getMessage()));
        }
    }

    /**
     * Delete all mappings for a role
     */
    @DeleteMapping("/role/{roleName}")
    public ResponseEntity<ApiResponse<Void>> deleteRoleMenu(@PathVariable String roleName) {
        try {
            roleMenuService.deleteRoleMenu(roleName);
            return ResponseEntity.ok(ApiResponse.success("Mapping role-menu berhasil dihapus", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.error("Gagal hapus mapping: " + e.getMessage()));
        }
    }
}
