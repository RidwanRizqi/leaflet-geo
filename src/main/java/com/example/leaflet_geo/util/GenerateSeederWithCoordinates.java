package com.example.leaflet_geo.util;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Generate PBJT Assessment Seeder with coordinates calculated from kelurahan polygon centroids
 * 
 * This utility:
 * 1. Fetches top 100 PBJT businesses from SIMATDA
 * 2. Gets kelurahan polygon coordinates from PostgreSQL sig.kelurahan_batas
 * 3. Calculates centroid + random offset for each business based on kelurahan
 * 4. Outputs SQL INSERT statements with valid coordinates
 */
public class GenerateSeederWithCoordinates {
    
    // PostgreSQL connection for kelurahan boundaries
    static String pgUrl = "jdbc:postgresql://localhost:5432/sig";
    static String pgUser = "postgres";
    static String pgPassword = "root";
    
    // MySQL SIMATDA connection
    static String mysqlUrl = "jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC";
    static String mysqlUser = "polinema";
    static String mysqlPassword = "P0l1n3m4@bprd";
    
    // Cache for kelurahan centroids
    static Map<String, double[]> kelurahanCentroids = new HashMap<>();
    static Map<String, double[]> kecamatanCentroids = new HashMap<>();
    
    public static void main(String[] args) {
        try (Connection pgConn = DriverManager.getConnection(pgUrl, pgUser, pgPassword);
             Connection mysqlConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword)) {
            
            System.out.println("-- PBJT Assessment Seeder with Coordinates from Polygon Centroids");
            System.out.println("-- Generated on: " + new java.util.Date());
            System.out.println();
            
            // Load kelurahan centroids from PostgreSQL
            loadKelurahanCentroids(pgConn);
            loadKecamatanCentroids(pgConn);
            
            System.out.println("-- Loaded " + kelurahanCentroids.size() + " kelurahan centroids");
            System.out.println("-- Loaded " + kecamatanCentroids.size() + " kecamatan centroids");
            System.out.println();
            
            // Query SIMATDA for top 100 PBJT businesses
            String simatdaQuery = """
                SELECT 
                    b.t_idobjek as id_objek,
                    b.t_namaobjek as nama_objek,
                    b.t_namawp as nama_wp,
                    b.t_nop as nop,
                    b.t_alamatobjek as alamat,
                    b.t_namakel as kelurahan,
                    b.t_namakec as kecamatan,
                    b.t_lat as lat_simatda,
                    b.t_long as long_simatda,
                    SUM(COALESCE(a.t_jmlhpembayaran, 0)) as total_realisasi
                FROM t_transaksi a
                LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
                LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
                WHERE c.s_jenisobjek = 2
                  AND a.t_tglpembayaran IS NOT NULL
                  AND YEAR(a.t_tglpembayaran) BETWEEN 2021 AND 2025
                GROUP BY b.t_idobjek, b.t_namaobjek, b.t_namawp, b.t_nop, 
                         b.t_alamatobjek, b.t_namakel, b.t_namakec, b.t_lat, b.t_long
                ORDER BY total_realisasi DESC
                LIMIT 100
                """;
            
            PreparedStatement pstmt = mysqlConn.prepareStatement(simatdaQuery);
            ResultSet rs = pstmt.executeQuery();
            
            // Generate SQL
            System.out.println("-- Clear existing SIM- data");
            System.out.println("DELETE FROM pbjt_assessments WHERE business_id LIKE 'SIM-%';");
            System.out.println();
            
            int counter = 0;
            DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.US));
            DecimalFormat coordFormat = new DecimalFormat("0.000000", DecimalFormatSymbols.getInstance(Locale.US));
            Random random = new Random(42); // Fixed seed for reproducibility
            
            while (rs.next()) {
                counter++;
                String businessId = String.format("SIM-%04d", counter);
                String namaObjek = escapeSQL(rs.getString("nama_objek"));
                String namaWp = escapeSQL(rs.getString("nama_wp"));
                String nop = rs.getString("nop");
                String alamat = escapeSQL(rs.getString("alamat"));
                String kelurahan = rs.getString("kelurahan");
                String kecamatan = rs.getString("kecamatan");
                String idObjek = rs.getString("id_objek");
                double totalRealisasi = rs.getDouble("total_realisasi");
                
                // Calculate coordinates from kelurahan polygon centroid
                double[] coords = getCoordinatesForLocation(kelurahan, kecamatan, random);
                double latitude = coords[0];
                double longitude = coords[1];
                
                // Calculate financial metrics based on realisasi
                double annualPbjt = totalRealisasi;
                double monthlyPbjt = annualPbjt / 12;
                double dailyRevenueWeekday = (monthlyPbjt / 0.10) / 30 * 0.8; // reverse calc
                double dailyRevenueWeekend = dailyRevenueWeekday * 1.5;
                double monthlyRevenueRaw = monthlyPbjt / 0.10;
                
                // Generate INSERT statement
                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO pbjt_assessments (business_id, business_name, assessment_date, ");
                sql.append("building_area, seating_capacity, business_type, ");
                sql.append("daily_revenue_weekday, daily_revenue_weekend, ");
                sql.append("monthly_revenue_raw, monthly_revenue_adjusted, monthly_pbjt, annual_pbjt, ");
                sql.append("tax_rate, inflation_rate, confidence_score, confidence_level, ");
                sql.append("latitude, longitude, address, kelurahan, kecamatan, ");
                sql.append("tax_object_id, tax_object_number, owner_name, phone_number, ");
                sql.append("seasonal_adjustment, last_survey_date, notes, status) VALUES\n");
                
                sql.append("('").append(businessId).append("', ");
                sql.append("'").append(namaObjek).append("', CURRENT_DATE, ");
                sql.append(df.format(100 + counter * 5)).append(", "); // building_area
                sql.append((20 + counter)).append(", "); // seating_capacity
                sql.append("'RESTAURANT', ");
                sql.append(df.format(dailyRevenueWeekday)).append(", ");
                sql.append(df.format(dailyRevenueWeekend)).append(", ");
                sql.append(df.format(monthlyRevenueRaw)).append(", ");
                sql.append(df.format(monthlyRevenueRaw)).append(", ");
                sql.append(df.format(monthlyPbjt)).append(", ");
                sql.append(df.format(annualPbjt)).append(", ");
                sql.append("0.10, 0.03, ");
                sql.append((85 + (counter % 15))).append(", "); // confidence_score
                sql.append("'HIGH', ");
                sql.append(coordFormat.format(latitude)).append(", ");
                sql.append(coordFormat.format(longitude)).append(", ");
                sql.append("'").append(alamat).append("', ");
                sql.append("'").append(kelurahan != null ? kelurahan.toUpperCase() : "").append("', ");
                sql.append("'").append(kecamatan != null ? kecamatan.toUpperCase() : "").append("', ");
                sql.append("'").append(idObjek).append("', ");
                sql.append("'").append(nop != null ? nop : "").append("', ");
                sql.append("'").append(namaWp).append("', ");
                sql.append("'', "); // phone_number
                sql.append("1.00, CURRENT_DATE, ");
                sql.append("'Data from SIMATDA - ID: ").append(idObjek).append(", NOP: ").append(nop).append("', ");
                sql.append("'ACTIVE');\n");
                
                System.out.println(sql.toString());
            }
            
            rs.close();
            pstmt.close();
            
            System.out.println("-- Total: " + counter + " records generated");
            
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load kelurahan polygon centroids from PostgreSQL
     */
    static void loadKelurahanCentroids(Connection conn) throws SQLException {
        String query = """
            SELECT 
                UPPER(nama) as nama,
                ST_X(ST_Centroid(ST_GeomFromWKB(geom))) as center_lng,
                ST_Y(ST_Centroid(ST_GeomFromWKB(geom))) as center_lat
            FROM sig.kelurahan_batas
            WHERE is_active = true AND geom IS NOT NULL
            """;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            String nama = rs.getString("nama");
            double lat = rs.getDouble("center_lat");
            double lng = rs.getDouble("center_lng");
            kelurahanCentroids.put(nama, new double[]{lat, lng});
        }
        
        rs.close();
        stmt.close();
    }
    
    /**
     * Load kecamatan polygon centroids from PostgreSQL
     */
    static void loadKecamatanCentroids(Connection conn) throws SQLException {
        String query = """
            SELECT 
                UPPER(nama) as nama,
                ST_X(ST_Centroid(ST_GeomFromWKB(geom))) as center_lng,
                ST_Y(ST_Centroid(ST_GeomFromWKB(geom))) as center_lat
            FROM sig.kecamatan_batas
            WHERE is_active = true AND geom IS NOT NULL
            """;
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            String nama = rs.getString("nama");
            double lat = rs.getDouble("center_lat");
            double lng = rs.getDouble("center_lng");
            kecamatanCentroids.put(nama, new double[]{lat, lng});
        }
        
        rs.close();
        stmt.close();
    }
    
    /**
     * Get coordinates for a location based on kelurahan/kecamatan centroid + random offset
     */
    static double[] getCoordinatesForLocation(String kelurahan, String kecamatan, Random random) {
        // Try kelurahan first
        if (kelurahan != null && !kelurahan.isEmpty()) {
            String kelKey = kelurahan.toUpperCase();
            if (kelurahanCentroids.containsKey(kelKey)) {
                double[] centroid = kelurahanCentroids.get(kelKey);
                // Add small random offset (within ~100-300m)
                double latOffset = (random.nextDouble() - 0.5) * 0.005;
                double lngOffset = (random.nextDouble() - 0.5) * 0.005;
                return new double[]{centroid[0] + latOffset, centroid[1] + lngOffset};
            }
        }
        
        // Fallback to kecamatan
        if (kecamatan != null && !kecamatan.isEmpty()) {
            String kecKey = kecamatan.toUpperCase();
            if (kecamatanCentroids.containsKey(kecKey)) {
                double[] centroid = kecamatanCentroids.get(kecKey);
                // Add larger random offset for kecamatan (within ~500m)
                double latOffset = (random.nextDouble() - 0.5) * 0.01;
                double lngOffset = (random.nextDouble() - 0.5) * 0.01;
                return new double[]{centroid[0] + latOffset, centroid[1] + lngOffset};
            }
        }
        
        // Fallback to Lumajang center
        double lumajangLat = -8.1335;
        double lumajangLng = 113.2246;
        double latOffset = (random.nextDouble() - 0.5) * 0.02;
        double lngOffset = (random.nextDouble() - 0.5) * 0.02;
        return new double[]{lumajangLat + latOffset, lumajangLng + lngOffset};
    }
    
    /**
     * Escape single quotes in SQL string
     */
    static String escapeSQL(String value) {
        if (value == null) return "";
        return value.replace("'", "''").trim();
    }
}
