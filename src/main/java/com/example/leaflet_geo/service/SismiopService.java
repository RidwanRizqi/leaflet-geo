package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SismiopService {

    private final JdbcTemplate sismiopJdbcTemplate;

    public SismiopService(@Qualifier("sismiopJdbcTemplate") JdbcTemplate sismiopJdbcTemplate) {
        this.sismiopJdbcTemplate = sismiopJdbcTemplate;
    }

    /**
     * Test koneksi ke database SISMIOP Oracle
     */
    public String testConnection() {
        try {
            // Query simple untuk test koneksi
            Integer result = sismiopJdbcTemplate.queryForObject(
                "SELECT 1 FROM DUAL", 
                Integer.class
            );
            // Get Oracle version
            String version = sismiopJdbcTemplate.queryForObject(
                "SELECT BANNER FROM v$version WHERE ROWNUM = 1",
                String.class
            );
            return "SISMIOP Connection Success! Oracle Version: " + version;
        } catch (Exception e) {
            return "SISMIOP Connection Failed: " + e.getMessage();
        }
    }

    /**
     * Get list of all tables in SISMIOP schema
     */
    public List<Map<String, Object>> getAllTables() {
        String sql = """
            SELECT 
                table_name,
                (SELECT COUNT(*) FROM user_tab_columns 
                 WHERE table_name = t.table_name) as column_count
            FROM user_tables t
            ORDER BY table_name
            """;
        return sismiopJdbcTemplate.queryForList(sql);
    }

    /**
     * Get table structure (columns) for a specific table
     */
    public List<Map<String, Object>> getTableStructure(String tableName) {
        String sql = """
            SELECT 
                column_name,
                data_type,
                data_length,
                nullable,
                data_default
            FROM user_tab_columns
            WHERE table_name = ?
            ORDER BY column_id
            """;
        return sismiopJdbcTemplate.queryForList(sql, tableName.toUpperCase());
    }

    /**
     * Get row count for a specific table
     */
    public Long getTableRowCount(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName.toUpperCase());
        return sismiopJdbcTemplate.queryForObject(sql, Long.class);
    }

    /**
     * Get sample data from a table
     */
    public List<Map<String, Object>> getSampleData(String tableName, int limit) {
        String sql = String.format(
            "SELECT * FROM %s WHERE ROWNUM <= %d", 
            tableName.toUpperCase(), 
            limit
        );
        return sismiopJdbcTemplate.queryForList(sql);
    }

    /**
     * Get data objek pajak by NOP
     */
    public Map<String, Object> getObjekPajakByNop(String nop) {
        if (nop == null || nop.length() != 18) {
            throw new IllegalArgumentException("NOP harus 18 digit");
        }
        
        String kdPropinsi = nop.substring(0, 2);
        String kdDati2 = nop.substring(2, 4);
        String kdKecamatan = nop.substring(4, 7);
        String kdKelurahan = nop.substring(7, 10);
        String kdBlok = nop.substring(10, 13);
        String noUrut = nop.substring(13, 17);
        String kdJnsOp = nop.substring(17, 18);
        
        String sql = """
            SELECT *
            FROM DAT_OBJEK_PAJAK
            WHERE KD_PROPINSI = ?
              AND KD_DATI2 = ?
              AND KD_KECAMATAN = ?
              AND KD_KELURAHAN = ?
              AND KD_BLOK = ?
              AND NO_URUT = ?
              AND KD_JNS_OP = ?
            """;
        
        try {
            return sismiopJdbcTemplate.queryForMap(
                sql, 
                kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, 
                kdBlok, noUrut, kdJnsOp
            );
        } catch (Exception e) {
            throw new RuntimeException("Data NOP tidak ditemukan: " + nop);
        }
    }

    /**
     * Get data subjek pajak by ID
     */
    public Map<String, Object> getSubjekPajakById(String subjekPajakId) {
        String sql = "SELECT * FROM DAT_SUBJEK_PAJAK WHERE SUBJEK_PAJAK_ID = ?";
        try {
            return sismiopJdbcTemplate.queryForMap(sql, subjekPajakId);
        } catch (Exception e) {
            throw new RuntimeException("Data Subjek Pajak tidak ditemukan: " + subjekPajakId);
        }
    }

    /**
     * Get list kecamatan
     */
    public List<Map<String, Object>> getKecamatanList() {
        String sql = """
            SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, NM_KECAMATAN
            FROM REF_KECAMATAN
            ORDER BY KD_KECAMATAN
            """;
        return sismiopJdbcTemplate.queryForList(sql);
    }

    /**
     * Get list kelurahan by kecamatan
     */
    public List<Map<String, Object>> getKelurahanByKecamatan(String kdKecamatan) {
        String sql = """
            SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, NM_KELURAHAN
            FROM REF_KELURAHAN
            WHERE KD_KECAMATAN = ?
            ORDER BY KD_KELURAHAN
            """;
        return sismiopJdbcTemplate.queryForList(sql, kdKecamatan);
    }

    /**
     * Get SPPT (Surat Pemberitahuan Pajak Terutang) by NOP and Tahun
     */
    public Map<String, Object> getSpptByNopTahun(String nop, String tahun) {
        if (nop == null || nop.length() != 18) {
            throw new IllegalArgumentException("NOP harus 18 digit");
        }
        
        String kdPropinsi = nop.substring(0, 2);
        String kdDati2 = nop.substring(2, 4);
        String kdKecamatan = nop.substring(4, 7);
        String kdKelurahan = nop.substring(7, 10);
        String kdBlok = nop.substring(10, 13);
        String noUrut = nop.substring(13, 17);
        String kdJnsOp = nop.substring(17, 18);
        
        String sql = """
            SELECT *
            FROM SPPT
            WHERE KD_PROPINSI = ?
              AND KD_DATI2 = ?
              AND KD_KECAMATAN = ?
              AND KD_KELURAHAN = ?
              AND KD_BLOK = ?
              AND NO_URUT = ?
              AND KD_JNS_OP = ?
              AND THN_PAJAK_SPPT = ?
            """;
        
        try {
            return sismiopJdbcTemplate.queryForMap(
                sql, 
                kdPropinsi, kdDati2, kdKecamatan, kdKelurahan, 
                kdBlok, noUrut, kdJnsOp, tahun
            );
        } catch (Exception e) {
            throw new RuntimeException("Data SPPT tidak ditemukan untuk NOP: " + nop + " Tahun: " + tahun);
        }
    }

    /**
     * Debug: Get SPPT by individual kode components (for testing format matching)
     */
    public Map<String, Object> getSpptByKodes(String kdProp, String kdDati2, String kdKec, 
            String kdKel, String kdBlok, String noUrut, String kdJnsOp, String tahun) {
        
        // First try with original format (with leading zeros)
        System.out.println("🔍 Trying SPPT lookup with original format:");
        System.out.println("   kd_prop=" + kdProp + ", kd_dati2=" + kdDati2 + ", kd_kec=" + kdKec);
        System.out.println("   kd_kel=" + kdKel + ", kd_blok=" + kdBlok + ", no_urut=" + noUrut);
        System.out.println("   kd_jns_op=" + kdJnsOp + ", tahun=" + tahun);
        
        String sql = """
            SELECT s.*, 
                   COALESCE(p.JML_SPPT_YG_DIBAYAR, 0) AS BAYAR,
                   CASE 
                       WHEN p.JML_SPPT_YG_DIBAYAR >= s.PBB_YG_HARUS_DIBAYAR_SPPT THEN 'LUNAS'
                       ELSE 'TERHUTANG'
                   END AS STATUS_BAYAR
            FROM SPPT s
            LEFT JOIN PEMBAYARAN_SPPT p ON 
                s.KD_PROPINSI = p.KD_PROPINSI AND
                s.KD_DATI2 = p.KD_DATI2 AND
                s.KD_KECAMATAN = p.KD_KECAMATAN AND
                s.KD_KELURAHAN = p.KD_KELURAHAN AND
                s.KD_BLOK = p.KD_BLOK AND
                s.NO_URUT = p.NO_URUT AND
                s.KD_JNS_OP = p.KD_JNS_OP AND
                s.THN_PAJAK_SPPT = p.THN_PAJAK_SPPT
            WHERE s.KD_PROPINSI = ?
              AND s.KD_DATI2 = ?
              AND s.KD_KECAMATAN = ?
              AND s.KD_KELURAHAN = ?
              AND s.KD_BLOK = ?
              AND s.NO_URUT = ?
              AND s.KD_JNS_OP = ?
              AND s.THN_PAJAK_SPPT = ?
            """;
        
        try {
            // Try original format first
            Map<String, Object> result = sismiopJdbcTemplate.queryForMap(
                sql, kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJnsOp, tahun);
            System.out.println("✅ Found with original format!");
            return result;
        } catch (Exception e1) {
            System.out.println("❌ Not found with original format, trying stripped format...");
            
            // Try with stripped leading zeros
            String strippedKdKec = stripLeadingZeros(kdKec);
            String strippedKdKel = stripLeadingZeros(kdKel);
            String strippedKdBlok = stripLeadingZeros(kdBlok);
            String strippedNoUrut = stripLeadingZeros(noUrut);
            
            System.out.println("🔍 Stripped format: kd_kec=" + strippedKdKec + ", kd_kel=" + strippedKdKel + 
                             ", kd_blok=" + strippedKdBlok + ", no_urut=" + strippedNoUrut);
            
            try {
                Map<String, Object> result = sismiopJdbcTemplate.queryForMap(
                    sql, kdProp, kdDati2, strippedKdKec, strippedKdKel, 
                    strippedKdBlok, strippedNoUrut, kdJnsOp, tahun);
                System.out.println("✅ Found with stripped format!");
                return result;
            } catch (Exception e2) {
                // Try to find any data in SPPT for this area to see actual format
                System.out.println("❌ Not found with stripped format either. Checking actual DB format...");
                
                try {
                    String sampleSql = """
                        SELECT KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP 
                        FROM SPPT 
                        WHERE KD_PROPINSI = ? AND KD_DATI2 = ? AND THN_PAJAK_SPPT = ?
                        AND ROWNUM <= 5
                        """;
                    List<Map<String, Object>> samples = sismiopJdbcTemplate.queryForList(sampleSql, kdProp, kdDati2, tahun);
                    if (!samples.isEmpty()) {
                        System.out.println("📋 Sample data from DB:");
                        for (Map<String, Object> sample : samples) {
                            System.out.println("   KD_KEC=" + sample.get("KD_KECAMATAN") + 
                                             ", KD_KEL=" + sample.get("KD_KELURAHAN") +
                                             ", KD_BLOK=" + sample.get("KD_BLOK") +
                                             ", NO_URUT=" + sample.get("NO_URUT"));
                        }
                    }
                } catch (Exception e3) {
                    System.out.println("❌ Could not get sample data: " + e3.getMessage());
                }
                
                throw new RuntimeException("Data SPPT tidak ditemukan. Checked both original and stripped formats.");
            }
        }
    }

    /**
     * Get realisasi PBB P2 per tahun dari PEMBAYARAN_SPPT
     */
    public Long getRealisasiPbbTahunan(String tahun) {
        try {
            String sql = """
                SELECT COALESCE(SUM(JML_SPPT_YG_DIBAYAR), 0) AS TOTAL_REALISASI
                FROM PEMBAYARAN_SPPT
                WHERE THN_PAJAK_SPPT = ?
                """;
            Map<String, Object> result = sismiopJdbcTemplate.queryForMap(sql, tahun);
            Object value = result.get("TOTAL_REALISASI");
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return 0L;
        } catch (Exception e) {
            System.err.println("Warning: Error fetching PBB realisasi for year " + tahun + ": " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Get target PBB P2 per tahun dari SPPT
     */
    public Long getTargetPbbTahunan(String tahun) {
        try {
            String sql = """
                SELECT COALESCE(SUM(PBB_YG_HARUS_DIBAYAR_SPPT), 0) AS TOTAL_TARGET
                FROM SPPT
                WHERE THN_PAJAK_SPPT = ?
                """;
            Map<String, Object> result = sismiopJdbcTemplate.queryForMap(sql, tahun);
            Object value = result.get("TOTAL_TARGET");
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            // Fallback jika tidak ada data
            return 50_000_000_000L; // Default 50 Miliar
        } catch (Exception e) {
            System.err.println("Warning: No PBB target found for year " + tahun + ", using default: " + e.getMessage());
            return 50_000_000_000L; // Default 50 Miliar
        }
    }

    /**
     * Get realisasi PBB per bulan
     */
    public List<Map<String, Object>> getRealisasiPbbBulanan(String tahun) {
        try {
            String sql = """
                SELECT 
                    EXTRACT(MONTH FROM TGL_PEMBAYARAN_SPPT) AS BULAN,
                    COALESCE(SUM(JML_SPPT_YG_DIBAYAR), 0) AS REALISASI
                FROM PEMBAYARAN_SPPT
                WHERE THN_PAJAK_SPPT = ?
                GROUP BY EXTRACT(MONTH FROM TGL_PEMBAYARAN_SPPT)
                ORDER BY BULAN
                """;
            return sismiopJdbcTemplate.queryForList(sql, tahun);
        } catch (Exception e) {
            System.err.println("Warning: Error fetching PBB monthly data: " + e.getMessage());
            return List.of();
        }
    }
    /**
     * Get payment status for multiple NOPs for a specific year
     * Returns a map of NOP -> Status (LUNAS/TERHUTANG)
     */
    public Map<String, Map<String, Object>> getPaymentStatusByNops(List<String> nops, String tahun) {
        if (nops == null || nops.isEmpty()) {
            return Map.of();
        }

        // Create placeholders for IN clause
        String inSql = String.join(",", java.util.Collections.nCopies(nops.size(), "?"));

        System.out.println("🔍 Checking payment status for " + nops.size() + " NOPs in year " + tahun);
        if (nops.size() > 0) {
             System.out.println("📝 Sample NOPs to check: " + nops.subList(0, Math.min(nops.size(), 5)));
        }

        // Query to check payment status
        // We check PEMBAYARAN_SPPT to see if paid
        // Use LPAD to ensure consistent formatting with leading zeros
        String sql = String.format("""
            SELECT 
                LPAD(s.KD_PROPINSI, 2, '0') || LPAD(s.KD_DATI2, 2, '0') || LPAD(s.KD_KECAMATAN, 3, '0') || 
                LPAD(s.KD_KELURAHAN, 3, '0') || LPAD(s.KD_BLOK, 3, '0') || LPAD(s.NO_URUT, 4, '0') || 
                LPAD(s.KD_JNS_OP, 1, '0') AS NOP,
                s.PBB_YG_HARUS_DIBAYAR_SPPT AS TAGIHAN,
                COALESCE(p.JML_SPPT_YG_DIBAYAR, 0) AS BAYAR,
                CASE 
                    WHEN p.JML_SPPT_YG_DIBAYAR >= s.PBB_YG_HARUS_DIBAYAR_SPPT THEN 'LUNAS'
                    ELSE 'TERHUTANG'
                END AS STATUS
            FROM SPPT s
            LEFT JOIN PEMBAYARAN_SPPT p ON 
                s.KD_PROPINSI = p.KD_PROPINSI AND
                s.KD_DATI2 = p.KD_DATI2 AND
                s.KD_KECAMATAN = p.KD_KECAMATAN AND
                s.KD_KELURAHAN = p.KD_KELURAHAN AND
                s.KD_BLOK = p.KD_BLOK AND
                s.NO_URUT = p.NO_URUT AND
                s.KD_JNS_OP = p.KD_JNS_OP AND
                s.THN_PAJAK_SPPT = p.THN_PAJAK_SPPT
            WHERE s.THN_PAJAK_SPPT = ?
            AND (LPAD(s.KD_PROPINSI, 2, '0') || LPAD(s.KD_DATI2, 2, '0') || LPAD(s.KD_KECAMATAN, 3, '0') || 
                 LPAD(s.KD_KELURAHAN, 3, '0') || LPAD(s.KD_BLOK, 3, '0') || LPAD(s.NO_URUT, 4, '0') || 
                 LPAD(s.KD_JNS_OP, 1, '0')) IN (%s)
            """, inSql);

        // Prepare arguments: tahun first, then all nops
        List<Object> args = new java.util.ArrayList<>();
        args.add(tahun);
        args.addAll(nops);

        try {
            List<Map<String, Object>> results = sismiopJdbcTemplate.queryForList(sql, args.toArray());
            System.out.println("✅ Query executed. Found " + results.size() + " matching records.");
            
            // Convert to Map<NOP, Data>
            Map<String, Map<String, Object>> statusMap = new java.util.HashMap<>();
            for (Map<String, Object> row : results) {
                String nop = (String) row.get("NOP");
                statusMap.put(nop, row);
            }
            return statusMap;
        } catch (Exception e) {
            System.err.println("❌ Error fetching payment status: " + e.getMessage());
            e.printStackTrace();
            return Map.of();
        }
    }

    /**
     * Get payment status by individual kode components (more reliable than NOP string matching)
     * Input: List of Maps containing kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op
     * Output: Map with composite key -> payment status data
     */
    public Map<String, Map<String, Object>> getPaymentStatusByKodes(
            List<Map<String, String>> kodeList, String tahun) {
        
        if (kodeList == null || kodeList.isEmpty()) {
            return Map.of();
        }

        System.out.println("🔍 Checking payment status for " + kodeList.size() + " bidang records using individual kodes in year " + tahun);

        // First, let's check what format is in the database by doing a sample query
        if (!kodeList.isEmpty()) {
            Map<String, String> sampleKode = kodeList.get(0);
            try {
                String debugSql = """
                    SELECT KD_PROPINSI, KD_DATI2, KD_KECAMATAN, KD_KELURAHAN, KD_BLOK, NO_URUT, KD_JNS_OP 
                    FROM SPPT 
                    WHERE THN_PAJAK_SPPT = ? 
                    AND ROWNUM <= 3
                    """;
                List<Map<String, Object>> sampleData = sismiopJdbcTemplate.queryForList(debugSql, tahun);
                if (!sampleData.isEmpty()) {
                    Map<String, Object> sample = sampleData.get(0);
                    System.out.println("📋 DB FORMAT SAMPLE: KD_KEC='" + sample.get("KD_KECAMATAN") + 
                                     "', KD_KEL='" + sample.get("KD_KELURAHAN") + 
                                     "', KD_BLOK='" + sample.get("KD_BLOK") + 
                                     "', NO_URUT='" + sample.get("NO_URUT") + "'");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Could not fetch sample data: " + e.getMessage());
            }
        }

        // Build dynamic WHERE clause with OR conditions for each kode set
        // Use TRIM and TO_NUMBER for flexible matching (handles both '060' and '60')
        StringBuilder whereClause = new StringBuilder("(");
        List<Object> args = new ArrayList<>();
        args.add(tahun); // First parameter is tahun

        for (int i = 0; i < kodeList.size(); i++) {
            if (i > 0) whereClause.append(" OR ");
            
            // Use TO_NUMBER for numeric comparison to handle leading zeros mismatch
            whereClause.append("(TO_NUMBER(s.KD_PROPINSI) = TO_NUMBER(?) AND TO_NUMBER(s.KD_DATI2) = TO_NUMBER(?) AND ")
                      .append("TO_NUMBER(s.KD_KECAMATAN) = TO_NUMBER(?) AND TO_NUMBER(s.KD_KELURAHAN) = TO_NUMBER(?) AND ")
                      .append("TO_NUMBER(s.KD_BLOK) = TO_NUMBER(?) AND TO_NUMBER(s.NO_URUT) = TO_NUMBER(?) AND ")
                      .append("TO_NUMBER(s.KD_JNS_OP) = TO_NUMBER(?))");
            
            Map<String, String> kodes = kodeList.get(i);
            
            // Keep original values - TO_NUMBER will handle the conversion
            String kdProp = kodes.get("kd_prop");
            String kdDati2 = kodes.get("kd_dati2");
            String kdKec = kodes.get("kd_kec");
            String kdKel = kodes.get("kd_kel");
            String kdBlok = kodes.get("kd_blok");
            String noUrut = kodes.get("no_urut");
            String kdJnsOp = kodes.get("kd_jns_op");
            
            if (i == 0) {
                System.out.println("📝 Sample query params (original): kd_kec=" + kdKec + ", kd_kel=" + kdKel + 
                                 ", kd_blok=" + kdBlok + ", no_urut=" + noUrut);
            }
            
            args.add(kdProp);
            args.add(kdDati2);
            args.add(kdKec);
            args.add(kdKel);
            args.add(kdBlok);
            args.add(noUrut);
            args.add(kdJnsOp);
        }
        whereClause.append(")");

        String sql = """
            SELECT 
                s.KD_PROPINSI,
                s.KD_DATI2,
                s.KD_KECAMATAN,
                s.KD_KELURAHAN,
                s.KD_BLOK,
                s.NO_URUT,
                s.KD_JNS_OP,
                s.PBB_YG_HARUS_DIBAYAR_SPPT AS TAGIHAN,
                COALESCE(p.JML_SPPT_YG_DIBAYAR, 0) AS BAYAR,
                CASE 
                    WHEN p.JML_SPPT_YG_DIBAYAR >= s.PBB_YG_HARUS_DIBAYAR_SPPT THEN 'LUNAS'
                    ELSE 'TERHUTANG'
                END AS STATUS
            FROM SPPT s
            LEFT JOIN PEMBAYARAN_SPPT p ON 
                s.KD_PROPINSI = p.KD_PROPINSI AND
                s.KD_DATI2 = p.KD_DATI2 AND
                s.KD_KECAMATAN = p.KD_KECAMATAN AND
                s.KD_KELURAHAN = p.KD_KELURAHAN AND
                s.KD_BLOK = p.KD_BLOK AND
                s.NO_URUT = p.NO_URUT AND
                s.KD_JNS_OP = p.KD_JNS_OP AND
                s.THN_PAJAK_SPPT = p.THN_PAJAK_SPPT
            WHERE s.THN_PAJAK_SPPT = ?
            AND """ + whereClause.toString();

        try {
            List<Map<String, Object>> results = sismiopJdbcTemplate.queryForList(sql, args.toArray());
            System.out.println("✅ Query executed. Found " + results.size() + " matching records out of " + kodeList.size() + " requested.");
            
            // Log first result for debugging
            if (!results.isEmpty()) {
                Map<String, Object> first = results.get(0);
                System.out.println("🔍 Sample DB result: KD_KEC=" + first.get("KD_KECAMATAN") + 
                                 ", KD_KEL=" + first.get("KD_KELURAHAN") + 
                                 ", KD_BLOK=" + first.get("KD_BLOK") + 
                                 ", NO_URUT=" + first.get("NO_URUT") +
                                 ", STATUS=" + first.get("STATUS"));
            }
            
            // Convert to Map with composite key using ORIGINAL format from kodeList
            // This ensures matching works with the input format
            Map<String, Map<String, Object>> statusMap = new HashMap<>();
            
            for (Map<String, Object> row : results) {
                // Convert DB values to numeric for comparison (handles leading zeros)
                long dbKdKec = toLong(row.get("KD_KECAMATAN"));
                long dbKdKel = toLong(row.get("KD_KELURAHAN"));
                long dbKdBlok = toLong(row.get("KD_BLOK"));
                long dbNoUrut = toLong(row.get("NO_URUT"));
                long dbKdJnsOp = toLong(row.get("KD_JNS_OP"));
                
                // Find matching original kode from input list using numeric comparison
                for (Map<String, String> originalKode : kodeList) {
                    if (toLong(originalKode.get("kd_kec")) == dbKdKec &&
                        toLong(originalKode.get("kd_kel")) == dbKdKel &&
                        toLong(originalKode.get("kd_blok")) == dbKdBlok &&
                        toLong(originalKode.get("no_urut")) == dbNoUrut &&
                        toLong(originalKode.get("kd_jns_op")) == dbKdJnsOp) {
                        
                        // Build key using ORIGINAL format with leading zeros
                        String key = originalKode.get("kd_prop") + "-" + 
                                   originalKode.get("kd_dati2") + "-" + 
                                   originalKode.get("kd_kec") + "-" + 
                                   originalKode.get("kd_kel") + "-" + 
                                   originalKode.get("kd_blok") + "-" + 
                                   originalKode.get("no_urut") + "-" + 
                                   originalKode.get("kd_jns_op");
                        statusMap.put(key, row);
                        break;
                    }
                }
            }
            
            if (!statusMap.isEmpty()) {
                Map.Entry<String, Map<String, Object>> first = statusMap.entrySet().iterator().next();
                System.out.println("🔍 Sample result key: " + first.getKey() + ", Status: " + first.getValue().get("STATUS"));
            }
            
            return statusMap;
        } catch (Exception e) {
            System.err.println("❌ Error fetching payment status by kodes: " + e.getMessage());
            e.printStackTrace();
            return Map.of();
        }
    }
    
    /**
     * Helper method to convert Object to long safely (handles leading zeros)
     */
    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        String str = String.valueOf(value).trim();
        if (str.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * Helper method to strip leading zeros from kode
     */
    private String stripLeadingZeros(String kode) {
        if (kode == null || kode.isEmpty()) {
            return kode;
        }
        // Don't strip if it would result in empty string
        String stripped = kode.replaceFirst("^0+(?!$)", "");
        return stripped.isEmpty() ? "0" : stripped;
    }
}
