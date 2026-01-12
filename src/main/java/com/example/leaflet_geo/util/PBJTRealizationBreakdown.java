package com.example.leaflet_geo.util;

import java.sql.*;
import java.util.*;

public class PBJTRealizationBreakdown {
    
    public static void main(String[] args) {
        // PostgreSQL connection for PBJT assessments
        String pgUrl = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String pgUser = "postgres";
        String pgPassword = "root";
        
        // MySQL SIMATDA connection
        String mysqlUrl = "jdbc:mysql://192.178.10.112:3306/simpatda_lumajang?useSSL=false&serverTimezone=UTC";
        String mysqlUser = "polinema";
        String mysqlPassword = "P0l1n3m4@bprd";
        
        try (Connection pgConn = DriverManager.getConnection(pgUrl, pgUser, pgPassword);
             Connection mysqlConn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword)) {
            
            System.out.println("✅ Connected to both databases!");
            System.out.println("\n" + "=".repeat(150));
            System.out.println("BREAKDOWN REALISASI PAJAK PBJT PER TAHUN");
            System.out.println("=".repeat(150) + "\n");
            
            // Get PBJT assessments with NOP from PostgreSQL
            String pgQuery = """
                SELECT business_id, business_name, tax_object_number, owner_name, 
                       address, kelurahan, kecamatan, latitude, longitude, annual_pbjt
                FROM pbjt_assessments 
                WHERE tax_object_number IS NOT NULL 
                  AND tax_object_number != ''
                ORDER BY annual_pbjt DESC
                LIMIT 20
                """;
            
            Statement pgStmt = pgConn.createStatement();
            ResultSet pgRs = pgStmt.executeQuery(pgQuery);
            
            int counter = 0;
            while (pgRs.next()) {
                counter++;
                String businessName = pgRs.getString("business_name");
                String nop = pgRs.getString("tax_object_number");
                String ownerName = pgRs.getString("owner_name");
                String address = pgRs.getString("address");
                String kelurahan = pgRs.getString("kelurahan");
                String kecamatan = pgRs.getString("kecamatan");
                double latitude = pgRs.getDouble("latitude");
                double longitude = pgRs.getDouble("longitude");
                double annualPbjt = pgRs.getDouble("annual_pbjt");
                
                System.out.println(counter + ". " + businessName);
                System.out.println("-".repeat(150));
                System.out.println("   NOP           : " + nop);
                System.out.println("   Wajib Pajak   : " + ownerName);
                System.out.println("   Alamat        : " + address);
                System.out.println("   Kelurahan     : " + kelurahan);
                System.out.println("   Kecamatan     : " + kecamatan);
                
                if (latitude != 0 && longitude != 0) {
                    System.out.println("   Koordinat     : " + latitude + ", " + longitude);
                } else {
                    System.out.println("   Koordinat     : Belum tersedia");
                }
                
                // Query SIMATDA for breakdown by year
                String mysqlQuery = """
                    SELECT 
                        YEAR(a.t_tglpembayaran) as tahun,
                        COUNT(*) as jumlah_transaksi,
                        SUM(COALESCE(a.t_jmlhpembayaran, 0)) as total_bayar
                    FROM t_transaksi a
                    LEFT JOIN view_wpobjek b ON a.t_idwpobjek = b.t_idobjek
                    LEFT JOIN s_rekening c ON a.t_idkorek = c.s_idkorek
                    WHERE b.t_nop = ?
                      AND c.s_jenisobjek = 2
                      AND a.t_tglpembayaran IS NOT NULL
                    GROUP BY YEAR(a.t_tglpembayaran)
                    ORDER BY tahun DESC
                    """;
                
                PreparedStatement mysqlPstmt = mysqlConn.prepareStatement(mysqlQuery);
                mysqlPstmt.setString(1, nop);
                ResultSet mysqlRs = mysqlPstmt.executeQuery();
                
                // Calculate total realisasi
                double totalRealisasi = 0;
                List<YearlyData> yearlyDataList = new ArrayList<>();
                
                while (mysqlRs.next()) {
                    int tahun = mysqlRs.getInt("tahun");
                    int jumlahTransaksi = mysqlRs.getInt("jumlah_transaksi");
                    double totalBayar = mysqlRs.getDouble("total_bayar");
                    totalRealisasi += totalBayar;
                    yearlyDataList.add(new YearlyData(tahun, jumlahTransaksi, totalBayar));
                }
                
                System.out.printf("   Total Realisasi: Rp %,.0f%n", totalRealisasi);
                
                if (!yearlyDataList.isEmpty()) {
                    System.out.println("\n   Breakdown Per Tahun:");
                    for (YearlyData data : yearlyDataList) {
                        System.out.printf("   - %d: %d transaksi = Rp %,.0f%n", 
                            data.tahun, data.jumlahTransaksi, data.totalBayar);
                    }
                } else {
                    System.out.println("\n   ⚠️  Tidak ada data realisasi di SIMATDA untuk NOP ini");
                }
                
                mysqlRs.close();
                mysqlPstmt.close();
                
                System.out.println();
            }
            
            pgRs.close();
            pgStmt.close();
            
            System.out.println("=".repeat(150));
            System.out.println("✅ Selesai! Total " + counter + " objek pajak dianalisis");
            
        } catch (SQLException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class YearlyData {
        int tahun;
        int jumlahTransaksi;
        double totalBayar;
        
        YearlyData(int tahun, int jumlahTransaksi, double totalBayar) {
            this.tahun = tahun;
            this.jumlahTransaksi = jumlahTransaksi;
            this.totalBayar = totalBayar;
        }
    }
}
