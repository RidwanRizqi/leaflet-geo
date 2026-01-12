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
        
        // Test Oracle Connection
        testOracleConnection();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Test MySQL SIMATDA Connection and Query PBJT Realization
        testSIMATDAQuery();
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
    
    private void testSIMATDAQuery() {
        System.out.println("🏦 Testing MySQL SIMATDA Connection & PBJT Realization Query...");
        try {
            // Test database connection
            String result = mysqlJdbcTemplate.queryForObject("SELECT 'MySQL SIMATDA connected successfully!' as message", String.class);
            System.out.println("✅ " + result);
            
            // Execute PBJT Realization Query
            String query = """
                SELECT 
                    b.t_idobjek,
                    b.t_nop,
                    b.t_namaobjek,
                    b.t_namawp,
                    b.t_alamatobjek,
                    b.s_namakel,
                    b.s_namakec,
                    b.t_notelpobjek,
                    b.t_latitudeobjek,
                    b.t_longitudeobjek,
                    SUM(COALESCE(a.t_jmlhpembayaran, 0)) AS total_realisasi
                FROM t_transaksi a
                LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
                LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
                WHERE c.s_jenisobjek = 2
                GROUP BY 
                    b.t_idobjek,
                    b.t_nop,
                    b.t_namaobjek,
                    b.t_namawp,
                    b.t_alamatobjek,
                    b.s_namakel,
                    b.s_namakec,
                    b.t_notelpobjek,
                    b.t_latitudeobjek,
                    b.t_longitudeobjek
                ORDER BY 
                    total_realisasi DESC
                LIMIT 10
                """;
            
            System.out.println("\n📊 Top 10 PBJT Realization Data from SIMATDA:");
            System.out.println("=" + "=".repeat(120));
            System.out.printf("%-15s %-30s %-30s %-25s %-20s %n", 
                "ID Objek", "Nama Objek", "Nama WP", "Kelurahan", "Total Realisasi");
            System.out.println("=" + "=".repeat(120));
            
            mysqlJdbcTemplate.query(query, (rs) -> {
                String idObjek = rs.getString("t_idobjek");
                String namaObjek = rs.getString("t_namaobjek");
                String namaWp = rs.getString("t_namawp");
                String kelurahan = rs.getString("s_namakel");
                double totalRealisasi = rs.getDouble("total_realisasi");
                String latitude = rs.getString("t_latitudeobjek");
                String longitude = rs.getString("t_longitudeobjek");
                
                System.out.printf("%-15s %-30s %-30s %-25s Rp %,.0f%n", 
                    idObjek != null ? idObjek : "-",
                    namaObjek != null ? (namaObjek.length() > 28 ? namaObjek.substring(0, 25) + "..." : namaObjek) : "-",
                    namaWp != null ? (namaWp.length() > 28 ? namaWp.substring(0, 25) + "..." : namaWp) : "-",
                    kelurahan != null ? (kelurahan.length() > 23 ? kelurahan.substring(0, 20) + "..." : kelurahan) : "-",
                    totalRealisasi);
                
                if (latitude != null && longitude != null) {
                    System.out.println("   📍 Coordinates: " + latitude + ", " + longitude);
                }
            });
            
            System.out.println("=" + "=".repeat(120));
            
            // Count total PBJT objects
            Long totalCount = mysqlJdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT b.t_idobjek) FROM t_transaksi a " +
                "LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek " +
                "LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek " +
                "WHERE c.s_jenisobjek = 2", 
                Long.class
            );
            System.out.println("📈 Total PBJT Objects with Transactions: " + totalCount);
            
        } catch (Exception e) {
            System.err.println("❌ MySQL SIMATDA query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

