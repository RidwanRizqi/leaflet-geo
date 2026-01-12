package com.example.leaflet_geo.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EpasirService {

    private final JdbcTemplate epasirJdbcTemplate;

    public EpasirService(@Qualifier("epasirJdbcTemplate") JdbcTemplate epasirJdbcTemplate) {
        this.epasirJdbcTemplate = epasirJdbcTemplate;
    }

    /**
     * Test koneksi ke database E-PASIR
     */
    public String testConnection() {
        try {
            String result = epasirJdbcTemplate.queryForObject("SELECT version()", String.class);
            return "E-PASIR Connection Success! PostgreSQL Version: " + result;
        } catch (Exception e) {
            return "E-PASIR Connection Failed: " + e.getMessage();
        }
    }

    /**
     * Get list of all tables in E-PASIR database
     */
    public List<Map<String, Object>> getAllTables() {
        String sql = """
            SELECT 
                table_name,
                (SELECT COUNT(*) FROM information_schema.columns 
                 WHERE table_schema = t.table_schema AND table_name = t.table_name) as column_count
            FROM information_schema.tables t
            WHERE table_schema = 'public' 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;
        return epasirJdbcTemplate.queryForList(sql);
    }

    /**
     * Get realisasi Pajak Mineral per tahun dari t_skab
     */
    public Long getRealisasiMineralTahunan(Integer tahun) {
        try {
            String sql = """
                SELECT COALESCE(SUM(nominal), 0) AS total_realisasi
                FROM t_skab
                WHERE EXTRACT(YEAR FROM tanggal) = ?
                AND is_deleted = false
                """;
            Map<String, Object> result = epasirJdbcTemplate.queryForMap(sql, tahun);
            return ((Number) result.get("total_realisasi")).longValue();
        } catch (Exception e) {
            System.err.println("Warning: Error fetching Mineral realisasi for year " + tahun + ": " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Get realisasi Pajak Sarang Burung Walet per tahun
     * Note: Untuk saat ini return 0 karena tidak ada transaksi walet di t_skab
     */
    public Long getRealisasiWaletTahunan(Integer tahun) {
        try {
            // Jika ada tabel khusus walet, bisa dipisah
            // Untuk saat ini semua transaksi di t_skab adalah mineral
            return 0L;
        } catch (Exception e) {
            System.err.println("Warning: Error fetching Walet realisasi for year " + tahun + ": " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Get total realisasi E-PASIR (hanya Mineral untuk saat ini)
     */
    public Long getRealisasiTotalTahunan(Integer tahun) {
        return getRealisasiMineralTahunan(tahun);
    }

    /**
     * Get realisasi per bulan
     */
    public List<Map<String, Object>> getRealisasiBulanan(Integer tahun) {
        try {
            String sql = """
                SELECT 
                    EXTRACT(MONTH FROM tanggal) AS bulan,
                    COALESCE(SUM(nominal), 0) AS realisasi
                FROM t_skab
                WHERE EXTRACT(YEAR FROM tanggal) = ?
                AND is_deleted = false
                GROUP BY EXTRACT(MONTH FROM tanggal)
                ORDER BY bulan
                """;
            return epasirJdbcTemplate.queryForList(sql, tahun);
        } catch (Exception e) {
            System.err.println("Warning: Error fetching E-PASIR monthly data: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get struktur tabel tertentu
     */
    public List<Map<String, Object>> getTableStructure(String tableName) {
        String sql = """
            SELECT 
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = ?
            ORDER BY ordinal_position
            """;
        return epasirJdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * Get sample data from a table
     */
    public List<Map<String, Object>> getSampleData(String tableName, int limit) {
        String sql = String.format("SELECT * FROM %s LIMIT %d", tableName, limit);
        return epasirJdbcTemplate.queryForList(sql);
    }
}
