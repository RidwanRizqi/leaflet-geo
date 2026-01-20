package com.example.leaflet_geo.util;

import java.sql.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Generate PBJT Realisasi Seeder dari SIMATDA
 * 
 * Script ini akan:
 * 1. Fetch data realisasi per tahun (2021-2025) dari SIMATDA
 * 2. Match dengan pbjt_assessments yang sudah ada
 * 3. Generate SQL INSERT untuk tabel pbjt_realisasi
 * 
 * Setelah seeder dijalankan, aplikasi tidak perlu connect ke SIMATDA lagi
 */
public class GenerateRealisasiSeeder {
    
    // MySQL SIMATDA connection
    static String mysqlUrl = "jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC";
    static String mysqlUser = "polinema";
    static String mysqlPassword = "P0l1n3m4@bprd";
    
    // PostgreSQL PBJT connection
    static String pgUrl = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
    static String pgUser = "postgres";
    static String pgPassword = "root";
    
    public static void main(String[] args) {
        try (Connection mysqlConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
             Connection pgConn = DriverManager.getConnection(pgUrl, pgUser, pgPassword)) {
            
            System.out.println("-- ================================================================");
            System.out.println("-- PBJT REALISASI SEEDER (2021-2025)");
            System.out.println("-- Generated from SIMATDA on: " + new java.util.Date());
            System.out.println("-- ================================================================");
            System.out.println();
            
            // Get existing assessments with tax_object_id
            Map<String, Long> taxObjectToAssessmentId = loadAssessmentMapping(pgConn);
            System.out.println("-- Loaded " + taxObjectToAssessmentId.size() + " assessments with tax_object_id");
            System.out.println();
            
            // Fetch realisasi data from SIMATDA per tahun
            String realisasiQuery = """
                SELECT 
                    b.t_idobjek as tax_object_id,
                    b.t_nop as nop,
                    b.t_namaobjek as business_name,
                    YEAR(a.t_tglpembayaran) as tahun,
                    SUM(COALESCE(a.t_jmlhpembayaran, 0)) as realisasi_amount,
                    COUNT(a.t_idtransaksi) as jumlah_transaksi
                FROM t_transaksi a
                LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
                LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
                WHERE c.s_jenisobjek = 2
                  AND a.t_tglpembayaran IS NOT NULL
                  AND YEAR(a.t_tglpembayaran) BETWEEN 2021 AND 2025
                  AND b.t_idobjek IS NOT NULL
                GROUP BY b.t_idobjek, b.t_nop, b.t_namaobjek, YEAR(a.t_tglpembayaran)
                ORDER BY b.t_idobjek, YEAR(a.t_tglpembayaran)
                """;
            
            PreparedStatement pstmt = mysqlConn.prepareStatement(realisasiQuery);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("-- Clear existing realisasi data");
            System.out.println("TRUNCATE TABLE pbjt_realisasi CASCADE;");
            System.out.println();
            System.out.println("-- Insert realisasi data");
            
            DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
            int counter = 0;
            int matched = 0;
            int skipped = 0;
            
            List<String> insertStatements = new ArrayList<>();
            
            while (rs.next()) {
                counter++;
                String taxObjectId = rs.getString("tax_object_id");
                String nop = rs.getString("nop");
                String businessName = escapeSQL(rs.getString("business_name"));
                int tahun = rs.getInt("tahun");
                double realisasiAmount = rs.getDouble("realisasi_amount");
                int jumlahTransaksi = rs.getInt("jumlah_transaksi");
                
                // Check if this tax_object_id exists in assessments
                Long assessmentId = taxObjectToAssessmentId.get(taxObjectId);
                
                if (assessmentId != null) {
                    matched++;
                    
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO pbjt_realisasi ");
                    sql.append("(assessment_id, tax_object_id, nop, business_name, tahun, realisasi_amount, jumlah_transaksi) ");
                    sql.append("VALUES (");
                    sql.append(assessmentId).append(", ");
                    sql.append("'").append(taxObjectId).append("', ");
                    sql.append("'").append(nop != null ? nop : "").append("', ");
                    sql.append("'").append(businessName).append("', ");
                    sql.append(tahun).append(", ");
                    sql.append(df.format(realisasiAmount)).append(", ");
                    sql.append(jumlahTransaksi);
                    sql.append(");");
                    
                    insertStatements.add(sql.toString());
                } else {
                    skipped++;
                    if (skipped <= 5) {
                        System.out.println("-- ⚠️ Skipped: tax_object_id=" + taxObjectId + 
                                         " (not found in assessments) - " + businessName);
                    }
                }
            }
            
            // Print all insert statements
            for (String sql : insertStatements) {
                System.out.println(sql);
            }
            
            System.out.println();
            System.out.println("-- ================================================================");
            System.out.println("-- SUMMARY");
            System.out.println("-- ================================================================");
            System.out.println("-- Total records from SIMATDA: " + counter);
            System.out.println("-- Matched with assessments: " + matched);
            System.out.println("-- Skipped (no assessment): " + skipped);
            System.out.println("-- ================================================================");
            
            // Generate summary by year
            generateYearlySummary(pgConn);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Map<String, Long> loadAssessmentMapping(Connection pgConn) throws SQLException {
        Map<String, Long> mapping = new HashMap<>();
        
        String query = """
            SELECT id, tax_object_id 
            FROM pbjt_assessments 
            WHERE tax_object_id IS NOT NULL 
              AND tax_object_id != ''
            """;
        
        Statement stmt = pgConn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            Long id = rs.getLong("id");
            String taxObjectId = rs.getString("tax_object_id");
            mapping.put(taxObjectId, id);
        }
        
        rs.close();
        stmt.close();
        
        return mapping;
    }
    
    private static void generateYearlySummary(Connection pgConn) {
        System.out.println();
        System.out.println("-- ================================================================");
        System.out.println("-- QUERY TO VERIFY REALISASI DATA");
        System.out.println("-- ================================================================");
        System.out.println("/*");
        System.out.println("SELECT ");
        System.out.println("    tahun,");
        System.out.println("    COUNT(*) as jumlah_objek,");
        System.out.println("    SUM(realisasi_amount) as total_realisasi,");
        System.out.println("    SUM(jumlah_transaksi) as total_transaksi,");
        System.out.println("    AVG(realisasi_amount) as avg_realisasi");
        System.out.println("FROM pbjt_realisasi");
        System.out.println("GROUP BY tahun");
        System.out.println("ORDER BY tahun;");
        System.out.println("*/");
        System.out.println();
        System.out.println("/*");
        System.out.println("-- View realisasi per assessment");
        System.out.println("SELECT ");
        System.out.println("    a.business_id,");
        System.out.println("    a.business_name,");
        System.out.println("    r.tahun,");
        System.out.println("    r.realisasi_amount,");
        System.out.println("    r.jumlah_transaksi");
        System.out.println("FROM pbjt_assessments a");
        System.out.println("JOIN pbjt_realisasi r ON a.id = r.assessment_id");
        System.out.println("WHERE a.business_id = 'SIM-0001'");
        System.out.println("ORDER BY r.tahun;");
        System.out.println("*/");
    }
    
    private static String escapeSQL(String value) {
        if (value == null) return "";
        return value.replace("'", "''");
    }
}
