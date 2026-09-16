package com.example.leaflet_geo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunMigration {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String user = "postgres";
        String password = "1234"; // From application.properties
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Executing V5__create_pbjt_realisasi_table.sql...");
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/db/migration/V5__create_pbjt_realisasi_table.sql")));
            stmt.execute(sql);
            System.out.println("Table created.");
            
            System.out.println("Executing pbjt_realisasi_clean.sql...");
            String cleanSql = new String(Files.readAllBytes(Paths.get("pbjt_realisasi_clean.sql")));
            stmt.execute(cleanSql);
            System.out.println("Clean data executed.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
