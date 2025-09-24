package com.example.leaflet_geo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/database-test")
public class DatabaseTestController {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @GetMapping("/postgres")
    public Map<String, Object> testPostgreSQL() {
        Map<String, Object> result = new HashMap<>();
        try {
            String message = postgresJdbcTemplate.queryForObject("SELECT 'PostgreSQL is working!' as message", String.class);
            result.put("status", "success");
            result.put("message", message);
            result.put("database", "PostgreSQL");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("database", "PostgreSQL");
        }
        return result;
    }

    @GetMapping("/oracle")
    public Map<String, Object> testOracle() {
        Map<String, Object> result = new HashMap<>();
        try {
            String message = oracleJdbcTemplate.queryForObject("SELECT 'Oracle is working!' as message FROM dual", String.class);
            result.put("status", "success");
            result.put("message", message);
            result.put("database", "Oracle");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("database", "Oracle");
        }
        return result;
    }

    @GetMapping("/both")
    public Map<String, Object> testBothDatabases() {
        Map<String, Object> result = new HashMap<>();
        
        // Test PostgreSQL
        Map<String, Object> postgresResult = testPostgreSQL();
        result.put("postgresql", postgresResult);
        
        // Test Oracle
        Map<String, Object> oracleResult = testOracle();
        result.put("oracle", oracleResult);
        
        return result;
    }
}
