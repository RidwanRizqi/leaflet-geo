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
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Testing Multiple Database Connections...\n");
        
        // Test PostgreSQL Connection
        testPostgreSQLConnection();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test Oracle Connection
        testOracleConnection();
    }
    
    private void testPostgreSQLConnection() {
        System.out.println("🐘 Testing PostgreSQL Connection...");
        try {
            // Test database connection
            String result = postgresJdbcTemplate.queryForObject("SELECT 'PostgreSQL connected successfully!' as message", String.class);
            System.out.println("✅ " + result);
            
            // Check if schema exists
            String schemaExists = postgresJdbcTemplate.queryForObject(
                "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name = 'sig') THEN 'Schema sig exists' ELSE 'Schema sig does not exist' END",
                String.class
            );
            System.out.println("📁 " + schemaExists);
            
            // Check if table exists
            String tableExists = postgresJdbcTemplate.queryForObject(
                "SELECT CASE WHEN EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = 'sig' AND table_name = 'bidang') THEN 'Table sig.bidang exists' ELSE 'Table sig.bidang does not exist' END",
                String.class
            );
            System.out.println("🗃️ " + tableExists);
            
            // Count records if table exists
            try {
                Long count = postgresJdbcTemplate.queryForObject("SELECT COUNT(*) FROM sig.bidang", Long.class);
                System.out.println("📊 Total records in sig.bidang: " + count);
            } catch (Exception e) {
                System.out.println("⚠️ Could not count records: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ PostgreSQL connection failed: " + e.getMessage());
            System.err.println("Please check your PostgreSQL configuration!");
        }
    }
    
    private void testOracleConnection() {
        System.out.println("🔶 Testing Oracle Connection...");
        try {
            // Test database connection
            String result = oracleJdbcTemplate.queryForObject("SELECT 'Oracle connected successfully!' as message FROM dual", String.class);
            System.out.println("✅ " + result);
            
            // Get Oracle version
            String version = oracleJdbcTemplate.queryForObject("SELECT banner FROM v$version WHERE rownum = 1", String.class);
            System.out.println("🔧 Oracle Version: " + version);
            
            // Check current user
            String currentUser = oracleJdbcTemplate.queryForObject("SELECT USER FROM dual", String.class);
            System.out.println("👤 Current User: " + currentUser);
            
            // Check if we can access system tables
            try {
                Long tableCount = oracleJdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables", Long.class);
                System.out.println("📊 Tables accessible by current user: " + tableCount);
            } catch (Exception e) {
                System.out.println("⚠️ Could not count tables: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Oracle connection failed: " + e.getMessage());
            System.err.println("Please check your Oracle configuration!");
        }
    }
}

