package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database-test")
@CrossOrigin(origins = "*")
public class DatabaseTestController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    /**
     * Test PostgreSQL connection
     */
    @GetMapping("/postgres")
    public ResponseEntity<ApiResponse<String>> testPostgreSQL() {
        try {
            String message = postgresJdbcTemplate.queryForObject("SELECT 'PostgreSQL is working!' as message",
                    String.class);
            return ResponseEntity.ok(
                    ApiResponse.success("PostgreSQL connection successful", message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("PostgreSQL connection failed: " + e.getMessage()));
        }
    }

    /**
     * Test Oracle connection
     */
    @GetMapping("/oracle")
    public ResponseEntity<ApiResponse<String>> testOracle() {
        try {
            String message = oracleJdbcTemplate.queryForObject("SELECT 'Oracle is working!' as message FROM dual",
                    String.class);
            return ResponseEntity.ok(
                    ApiResponse.success("Oracle connection successful", message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Oracle connection failed: " + e.getMessage()));
        }
    }

    /**
     * Test both database connections
     */
    @GetMapping("/both")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testBothDatabases() {
        Map<String, Object> results = new HashMap<>();

        // Test PostgreSQL
        try {
            String pgMessage = postgresJdbcTemplate.queryForObject("SELECT 'PostgreSQL is working!' as message",
                    String.class);
            results.put("postgresql", Map.of("status", "success", "message", pgMessage));
        } catch (Exception e) {
            results.put("postgresql", Map.of("status", "error", "message", e.getMessage()));
        }

        // Test Oracle
        try {
            String oracleMessage = oracleJdbcTemplate.queryForObject("SELECT 'Oracle is working!' as message FROM dual",
                    String.class);
            results.put("oracle", Map.of("status", "success", "message", oracleMessage));
        } catch (Exception e) {
            results.put("oracle", Map.of("status", "error", "message", e.getMessage()));
        }

        return ResponseEntity.ok(
                ApiResponse.success("Database connection tests completed", results));
    }
}
