package com.example.leaflet_geo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfig implements CommandLineRunner {

    @Autowired
    @Qualifier("postgresJdbcTemplate")
    private JdbcTemplate postgresJdbcTemplate;

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @Autowired
    @Qualifier("mysqlJdbcTemplate")
    private JdbcTemplate mysqlJdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Testing Multiple Database Connections...\n");

        // Test PostgreSQL Connection
        testPostgreSQLConnection();

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Test Oracle Connection (tolerant - won't crash if unavailable)
        try {
            testOracleConnection();
        } catch (Exception e) {
            System.err.println("WARNING: Oracle connection test failed: " + e.getMessage());
            System.err.println("Application will continue without Oracle.");
        }

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Test MySQL SIMATDA Connection (tolerant - won't crash if unavailable)
        try {
            testSIMATDAQuery();
        } catch (Exception e) {
            System.err.println("WARNING: MySQL SIMATDA connection test failed: " + e.getMessage());
            System.err.println("Application will continue without MySQL SIMATDA.");
        }
    }

    private void testPostgreSQLConnection() {
        System.out.println("🐘 Testing PostgreSQL Connection...");
        try {
            // Test database connection
            String result = postgresJdbcTemplate
                    .queryForObject("SELECT 'PostgreSQL connected successfully!' as message", String.class);
            System.out.println("✅ " + result);

            try {
                String schemaExists = postgresJdbcTemplate.queryForObject(
                        "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'sig') THEN 'Schema sig exists' ELSE 'Schema sig does not exist' END",
                        String.class);
                System.out.println("Schema: " + schemaExists);
            } catch (Exception e) {
                System.out.println("Warning: Could not check schema: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("PostgreSQL connection failed: " + e.getMessage());
            System.err.println("Please check your PostgreSQL configuration!");
        }
    }

    private void testOracleConnection() {
        System.out.println("Testing Oracle Connection...");
        try {
            String result = oracleJdbcTemplate
                    .queryForObject("SELECT 'Oracle connected successfully!' as message FROM dual", String.class);
            System.out.println("OK: " + result);
        } catch (Exception e) {
            System.err.println("❌ Oracle connection failed: " + e.getMessage());
            System.err.println("Please check your Oracle configuration!");
        }
    }

    private void testSIMATDAQuery() {
        System.out.println("🏦 Testing MySQL SIMATDA Connection & PBJT Realization Query...");
        try {
            String result = mysqlJdbcTemplate
                    .queryForObject("SELECT 'MySQL SIMATDA connected successfully!' as message", String.class);
            System.out.println("OK: " + result);
        } catch (Exception e) {
            System.err.println("❌ MySQL SIMATDA query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
