package com.example.leaflet_geo.test;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

//@Component // Uncomment to run
public class TestRealisasiQuery implements CommandLineRunner {
    
private final JdbcTemplate pbjtJdbcTemplate;

    public TestRealisasiQuery(@Qualifier("pbjtJdbcTemplate") JdbcTemplate pbjtJdbcTemplate) {
        this.pbjtJdbcTemplate = pbjtJdbcTemplate;
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== TEST REALISASI QUERY ===");
        
        String query = """
            SELECT 
                assessment_id,
                tahun,
                realisasi_amount
            FROM pbjt_realisasi
            WHERE assessment_id = 81
            ORDER BY tahun
            """;
        
        List<Map<String, Object>> rows = pbjtJdbcTemplate.queryForList(query);
        
        System.out.println("Found " + rows.size() + " rows for assessment_id=81:");
        for (Map<String, Object> row : rows) {
            Long assessmentId = ((Number) row.get("assessment_id")).longValue();
            Integer tahun = ((Number) row.get("tahun")).intValue();
            BigDecimal amount = (BigDecimal) row.get("realisasi_amount");
            System.out.println("  - Year: " + tahun + ", Amount: " + amount);
        }
        
        System.out.println("=== TEST COMPLETE ===\n");
    }
}
