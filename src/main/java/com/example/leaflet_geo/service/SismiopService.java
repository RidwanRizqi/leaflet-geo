package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
}
