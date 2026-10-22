package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.MasterMenuDTO;
import com.example.leaflet_geo.service.PbjtMasterMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pbjt-master-menus")
@Slf4j
public class PbjtMasterMenuController {

    private final PbjtMasterMenuService masterMenuService;

    public PbjtMasterMenuController(PbjtMasterMenuService masterMenuService) {
        this.masterMenuService = masterMenuService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMenus(@RequestParam(value = "search", required = false) String search) {
        try {
            List<MasterMenuDTO> list;
            if (search != null && !search.trim().isEmpty()) {
                list = masterMenuService.searchMenus(search);
            } else {
                list = masterMenuService.getAllMenus();
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", list,
                    "total", list.size()
            ));
        } catch (Exception e) {
            log.error("Error fetching master menus", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMenu(@RequestBody MasterMenuDTO dto) {
        try {
            MasterMenuDTO result = masterMenuService.saveMenu(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result,
                    "message", "Menu berhasil disimpan ke master menu"
            ));
        } catch (Exception e) {
            log.error("Error saving master menu", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteAllMenus() {
        try {
            long count = masterMenuService.deleteAll();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Berhasil menghapus " + count + " master menu",
                    "deleted", count
            ));
        } catch (Exception e) {
            log.error("Error deleting all master menus", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
}
