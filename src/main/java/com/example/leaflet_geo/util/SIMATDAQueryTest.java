package com.example.leaflet_geo.util;

import java.sql.*;
import java.nio.file.*;
import java.util.Locale;

public class SIMATDAQueryTest {
    
    public static void main(String[] args) {
        String url = "jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC";
        String username = "polinema";
        String password = "P0l1n3m4@bprd";
        
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
            LIMIT 100
            """;
        
        System.out.println("🏦 Connecting to MySQL SIMATDA Database...\n");
        
        StringBuilder sqlSeeder = new StringBuilder();
        sqlSeeder.append("-- PBJT Assessment Seeder from SIMATDA (Top 100)\n");
        sqlSeeder.append("-- Generated from actual realization data\n\n");
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("✅ Connected successfully!\n");
            System.out.println("📊 PBJT Realization Data from SIMATDA:");
            System.out.println("=".repeat(150));
            System.out.printf("%-10s %-25s %-25s %-30s %-20s %-20s %15s%n", 
                "ID Objek", "NOP", "Nama Objek", "Nama WP", "Kelurahan", "Kecamatan", "Total Realisasi");
            System.out.println("=".repeat(150));
            
            int count = 0;
            double grandTotal = 0;
            
            while (rs.next()) {
                count++;
                String idObjek = rs.getString("t_idobjek");
                String nop = rs.getString("t_nop");
                String namaObjek = rs.getString("t_namaobjek");
                String namaWp = rs.getString("t_namawp");
                String alamat = rs.getString("t_alamatobjek");
                String kelurahan = rs.getString("s_namakel");
                String kecamatan = rs.getString("s_namakec");
                String telp = rs.getString("t_notelpobjek");
                String latitude = rs.getString("t_latitudeobjek");
                String longitude = rs.getString("t_longitudeobjek");
                double totalRealisasi = rs.getDouble("total_realisasi");
                
                grandTotal += totalRealisasi;
                
                // Generate SQL INSERT
                String businessId = "SIM-" + String.format("%04d", count);
                String businessName = namaObjek != null ? namaObjek.replace("'", "''") : "Unknown Business";
                String ownerName = namaWp != null ? namaWp.replace("'", "''") : "";
                String address = alamat != null ? alamat.replace("'", "''") : "";
                String kel = kelurahan != null ? kelurahan.trim() : "UNKNOWN";
                String kec = kecamatan != null ? kecamatan.trim() : "UNKNOWN";
                String lat = (latitude != null && !latitude.isEmpty()) ? latitude : "0";
                String lon = (longitude != null && !longitude.isEmpty()) ? longitude : "0";
                
                // Calculate assessment values from total realisasi (annual)
                double annualPbjt = totalRealisasi;
                double monthlyPbjt = annualPbjt / 12;
                double monthlyRaw = monthlyPbjt / 0.10; // Reverse calculate from 10% tax
                double dailyRevenue = monthlyRaw / 30;
                
                sqlSeeder.append(String.format(Locale.US,
                    "INSERT INTO pbjt_assessments (business_id, business_name, assessment_date, " +
                    "building_area, seating_capacity, business_type, " +
                    "daily_revenue_weekday, daily_revenue_weekend, monthly_revenue_raw, monthly_revenue_adjusted, " +
                    "monthly_pbjt, annual_pbjt, tax_rate, inflation_rate, " +
                    "confidence_score, confidence_level, " +
                    "latitude, longitude, address, kelurahan, kecamatan, " +
                    "tax_object_id, tax_object_number, owner_name, phone_number, " +
                    "seasonal_adjustment, last_survey_date, notes, status) VALUES\n" +
                    "('%s', '%s', CURRENT_DATE, " +
                    "%.2f, %d, 'RESTAURANT', " +
                    "%.2f, %.2f, %.2f, %.2f, " +
                    "%.2f, %.2f, %.2f, %.2f, " +
                    "%d, 'HIGH', " +
                    "%s, %s, '%s', '%s', '%s', " +
                    "'%s', '%s', '%s', '%s', " +
                    "%.2f, CURRENT_DATE, 'Data from SIMATDA - ID: %s, NOP: %s', 'ACTIVE');\n\n",
                    businessId, businessName,
                    100.0 + (count * 5.0), // building_area
                    20 + (count % 50), // seating_capacity
                    dailyRevenue * 0.8, // daily_revenue_weekday
                    dailyRevenue * 1.2, // daily_revenue_weekend
                    monthlyRaw, // monthly_revenue_raw
                    monthlyRaw, // monthly_revenue_adjusted
                    monthlyPbjt, // monthly_pbjt
                    annualPbjt, // annual_pbjt
                    0.10, // tax_rate
                    0.03, // inflation_rate
                    85 + (count % 15), // confidence_score
                    lat, lon,
                    address, kel, kec,
                    idObjek != null ? idObjek : "", // tax_object_id
                    nop != null ? nop : "", // tax_object_number
                    ownerName,
                    telp != null ? telp : "",
                    1.00, // seasonal_adjustment
                    idObjek != null ? idObjek : "",
                    nop != null ? nop : ""
                ));
                
                // Display to console (first 20 only)
                if (count <= 20) {
                    System.out.printf("%-10s %-25s %-25s %-30s %-20s %-20s Rp %,15.0f%n",
                        idObjek != null ? (idObjek.length() > 9 ? idObjek.substring(0, 9) : idObjek) : "-",
                        nop != null ? (nop.length() > 24 ? nop.substring(0, 21) + "..." : nop) : "-",
                        namaObjek != null ? (namaObjek.length() > 24 ? namaObjek.substring(0, 21) + "..." : namaObjek) : "-",
                        namaWp != null ? (namaWp.length() > 29 ? namaWp.substring(0, 26) + "..." : namaWp) : "-",
                        kelurahan != null ? (kelurahan.length() > 19 ? kelurahan.substring(0, 16) + "..." : kelurahan) : "-",
                        kecamatan != null ? (kecamatan.length() > 19 ? kecamatan.substring(0, 16) + "..." : kecamatan) : "-",
                        totalRealisasi
                    );
                    
                    if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty()) {
                        System.out.println("    📍 Coordinates: " + latitude + ", " + longitude);
                    }
                    if (alamat != null && !alamat.isEmpty()) {
                        System.out.println("    📫 Alamat: " + alamat);
                    }
                    if (telp != null && !telp.isEmpty()) {
                        System.out.println("    📞 Telp: " + telp);
                    }
                    System.out.println();
                }
            }
            
            System.out.println("=".repeat(150));
            System.out.println("📈 Total Records Processed: " + count);
            System.out.println("💰 Grand Total Realisasi (Top " + count + "): Rp " + String.format("%,.0f", grandTotal));
            
            // Write SQL seeder file
            try {
                Files.writeString(
                    Path.of("src/main/resources/sql/pbjt_assessment_seeder_simatda.sql"),
                    sqlSeeder.toString()
                );
                System.out.println("✅ SQL Seeder file generated: src/main/resources/sql/pbjt_assessment_seeder_simatda.sql");
            } catch (Exception e) {
                System.err.println("❌ Failed to write SQL file: " + e.getMessage());
            }
            
            System.out.println("=".repeat(150));
            
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
